import os
import json
from typing import Optional
import redis
import httpx
import numpy as np
from fastapi import APIRouter, Request, Body, HTTPException, Query
from fastapi.responses import StreamingResponse
from langchain_core.messages import HumanMessage, AIMessage

from rate_limiter import check_rate_limit, get_settings
from agent import (
    load_chat_history,
    save_chat_history,
    search_similar_products,
    get_agent_executor,
    load_long_term_history,
    save_long_term_history
)
from tools import (
    get_stock_snapshot,
    get_low_stock_products,
    get_category_stock_stats,
    get_product_detail,
    get_all_products
)

router = APIRouter()

# Redis and Java backend configurations from env
REDIS_HOST = os.getenv("REDIS_HOST", "localhost")
REDIS_PORT = int(os.getenv("REDIS_PORT", 6379))
REDIS_PASSWORD = os.getenv("REDIS_PASSWORD", "")
REDIS_DB = int(os.getenv("REDIS_DB", 0))

# Initialize Redis client
redis_client = redis.Redis(
    host=REDIS_HOST,
    port=REDIS_PORT,
    password=REDIS_PASSWORD,
    db=REDIS_DB,
    decode_responses=True
)

# Binary-safe Redis client for vector operations (cannot decode binary vector to string)
redis_client_binary = redis.Redis(
    host=REDIS_HOST,
    port=REDIS_PORT,
    password=REDIS_PASSWORD,
    db=REDIS_DB,
    decode_responses=False
)

JAVA_BACKEND_URL = os.getenv("JAVA_BACKEND_URL", "http://localhost:8080")

def get_user_id_from_auth_header(authorization: Optional[str] = None) -> Optional[int]:
    """
    通过 Authorization header 里的 Bearer Token 校验用户，从 Redis 缓存加载用户信息。
    对标原 Java 后端拦截器。
    """
    if not authorization or not authorization.startswith("Bearer "):
        return None
    token = authorization[7:].strip()
    if not token:
        return None
    
    # Redis key: token:<token>
    val = redis_client.get(f"token:{token}")
    if not val:
        return None
    
    try:
        data = json.loads(val)
        # 对标 Jackson NON_FINAL 的 ["java.util.HashMap", { ... }] 序列化格式
        if isinstance(data, list) and len(data) == 2:
            user_info = data[1]
            return user_info.get("userId")
        elif isinstance(data, dict):
            return data.get("userId")
    except Exception as e:
        print(f"WARNING: Error parsing token user info JSON: {e}")
    return None

@router.get("/history")
async def get_history(request: Request):
    """
    获取对话历史，对应 Java 后端 GET /api/ai/history
    """
    auth_header = request.headers.get("Authorization")
    user_id = get_user_id_from_auth_header(auth_header)
    if not user_id:
        raise HTTPException(status_code=401, detail="Unauthorized")
        
    chat_history = load_long_term_history(redis_client, str(user_id))
    
    dto_list = []
    for msg in chat_history:
        role = "user" if msg.get("type") == "USER" else "assistant"
        dto_list.append({
            "role": role,
            "content": msg.get("text"),
            "time": msg.get("time", "")
        })
            
    return {"success": True, "message": "Success", "data": dto_list}

@router.post("/clear")
async def clear_chat_memory(request: Request):
    """
    清空当前对话 session 内存，对应 POST /api/ai/clear
    """
    auth_header = request.headers.get("Authorization")
    user_id = get_user_id_from_auth_header(auth_header)
    if not user_id:
        raise HTTPException(status_code=401, detail="Unauthorized")
        
    try:
        redis_client.delete(f"chat:memory:{user_id}")
        return {"success": True, "message": "当前会话已重置，开启新对话"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"重置会话失败: {str(e)}")

async def sse_chat_generator(message: str, user_id: str, redis_client_text, redis_client_bin):
    """
    SSE 生成器，实时返回 LLM Token，执行完后保存历史
    """
    # 1. 获取动态系统设置 (API Key, Base URL, Model Name)
    settings = await get_settings(JAVA_BACKEND_URL)
    api_key = settings.get("ai_api_key")
    base_url = settings.get("ai_base_url")
    model_name = settings.get("ai_model_name")
    
    # 2. 向量检索 (RAG 语义知识库检索)
    index_name = os.getenv("REDIS_VECTOR_INDEX", "item_index")
    # 模糊检索匹配度阈值为 0.6
    rag_docs = search_similar_products(redis_client_bin, message, index_name, min_similarity=0.6)
    
    # 3. 组装输入
    input_text = message
    if rag_docs:
        context_str = "\n".join([f"- {doc}" for doc in rag_docs])
        input_text = f"【RAG 检索知识库商品上下文信息】：\n{context_str}\n\n【用户问题】：\n{message}"
        
    # 4. 加载历史对话
    chat_history = load_chat_history(redis_client_text, user_id)
    
    # 5. 定义可用工具
    tools_list = [
        get_stock_snapshot,
        get_low_stock_products,
        get_category_stock_stats,
        get_product_detail,
        get_all_products
    ]
    
    # 6. 获取智能体执行器
    agent_executor = get_agent_executor(api_key, base_url, model_name, tools_list)
    
    # 7. 开始流式事件输出
    full_response = ""
    try:
        async for event in agent_executor.astream_events(
            {"input": input_text, "chat_history": chat_history},
            version="v1"
        ):
            kind = event["event"]
            if kind == "on_chat_model_stream":
                # 只有最终生成的文本块有 content，工具调用的 content 为空
                content = event["data"]["chunk"].content
                if content:
                    full_response += content
                    yield f"data: {content}\n\n"
                    
        # 对话结束，保存新的对话记录至 Redis
        new_history = chat_history + [HumanMessage(content=message), AIMessage(content=full_response)]
        save_chat_history(redis_client_text, user_id, new_history)
        save_long_term_history(redis_client_text, user_id, message, full_response)
        
        # 写入 [DONE] 结束符，对标 Java SseEmitter complete
        yield "event: complete\n"
        yield "data: [DONE]\n\n"
        
    except Exception as e:
        print(f"Error executing agent stream: {e}")
        yield "event: error\n"
        yield f"data: {str(e)}\n\n"

@router.get("/chat")
async def chat(request: Request, message: str = Query(...)):
    """
    AI 仓储问答接口 (Server-Sent Events)
    """
    auth_header = request.headers.get("Authorization")
    user_id = get_user_id_from_auth_header(auth_header)
    if not user_id:
        raise HTTPException(status_code=401, detail="Unauthorized")
        
    # 速率限制检查 (RPM, TPM, RPD)
    await check_rate_limit(redis_client, JAVA_BACKEND_URL, message)
    
    return StreamingResponse(
        sse_chat_generator(message, str(user_id), redis_client, redis_client_binary),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "Connection": "keep-alive",
            "X-Accel-Buffering": "no"  # 显式关闭 Nginx 缓存，保障打字机效果
        }
    )

@router.post("/reindex")
async def reindex(request: Request):
    """
    手动触发全量重建商品向量索引的接口
    """
    auth_header = request.headers.get("Authorization")
    user_id = get_user_id_from_auth_header(auth_header)
    if not user_id:
        raise HTTPException(status_code=401, detail="Unauthorized")
        
    try:
        # 1. 从 Java 后端拉取所有商品明细
        async with httpx.AsyncClient(timeout=30.0) as client:
            resp = await client.get(f"{JAVA_BACKEND_URL}/internal/ai/tools/all-products")
            if resp.status_code != 200:
                raise HTTPException(status_code=500, detail="Failed to fetch products from backend")
            products = resp.json()
            
        # 2. 清除旧向量
        keys = redis_client.keys("embedding:*")
        if keys:
            redis_client.delete(*keys)
            
        # 3. 批量生成并计算向量导入 Redis
        from agent import get_embedding_model
        model = get_embedding_model()
        
        for p in products:
            product_id = str(p.get("id"))
            text = f"商品名称: {p.get('name')}, SKU编码: {p.get('sku')}, 商品分类: {p.get('category') or '无'}, 计量单位: {p.get('unit') or '个'}, 条码: {p.get('barcode') or '无'}, 销售价格: {p.get('salePrice') or '0.00'}元"
            
            vector = model.encode(text).tolist()
            vector_bytes = np.array(vector, dtype=np.float32).tobytes()
            
            # 使用 HASH 存储
            redis_client_binary.hset(f"embedding:{product_id}", mapping={
                "text": text.encode("utf-8"),
                "productId": product_id.encode("utf-8"),
                "vector": vector_bytes
            })
            
        return {"success": True, "message": "已成功全量同步商品向量索引"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/embedding/update")
async def embedding_update(product: dict = Body(...)):
    """
    接收 Java 后端同步过来的单个商品实体进行实时向量更新
    """
    product_id = str(product.get("id"))
    if not product_id:
        raise HTTPException(status_code=400, detail="Missing product id")
        
    try:
        from agent import get_embedding_model
        model = get_embedding_model()
        text = f"商品名称: {product.get('name')}, SKU编码: {product.get('sku')}, 商品分类: {product.get('category') or '无'}, 计量单位: {product.get('unit') or '个'}, 条码: {product.get('barcode') or '无'}, 销售价格: {product.get('salePrice') or '0.00'}元"
        
        vector = model.encode(text).tolist()
        vector_bytes = np.array(vector, dtype=np.float32).tobytes()
        
        redis_client_binary.hset(f"embedding:{product_id}", mapping={
            "text": text.encode("utf-8"),
            "productId": product_id.encode("utf-8"),
            "vector": vector_bytes
        })
        return {"success": True, "message": "Embedding updated"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.delete("/embedding/delete")
async def embedding_delete(productId: str):
    """
    接收 Java 后端同步过来的单个商品删除指令，移除对应的向量
    """
    if not productId:
        raise HTTPException(status_code=400, detail="Missing productId")
    try:
        redis_client.delete(f"embedding:{productId}")
        return {"success": True, "message": "Embedding deleted"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
