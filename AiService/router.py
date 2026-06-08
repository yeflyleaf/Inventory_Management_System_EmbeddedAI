import os
import json
import asyncio
import datetime
from typing import Optional, Any
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
    get_agent_executor
)
from tools import (
    get_stock_snapshot,
    get_low_stock_products,
    get_category_stock_stats,
    get_product_detail,
    get_all_products,
    get_all_customers,
    get_customer_detail,
    get_all_suppliers,
    get_supplier_detail,
    get_purchase_orders,
    get_purchase_order_detail,
    get_sales_orders,
    get_sales_order_detail,
    get_all_warehouses,
    get_recent_operation_logs,
    get_all_users
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

def get_user_info_from_auth_header(authorization: Optional[str] = None) -> Optional[dict]:
    """
    通过 Authorization header 里的 Bearer Token 校验用户，从 Redis 缓存加载用户信息。
    返回包含 userId, role, username 等字段的字典。
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
            return data[1]
        elif isinstance(data, dict):
            return data
    except Exception as e:
        print(f"WARNING: Error parsing token user info JSON: {e}")
    return None

def get_user_id_from_auth_header(authorization: Optional[str] = None) -> Optional[int]:
    """
    通过 Authorization header 获取用户 ID (保持向后兼容)
    """
    info = get_user_info_from_auth_header(authorization)
    return info.get("userId") if info else None

def get_active_session_id(redis_client: redis.Redis, user_id: str) -> Optional[str]:
    val = redis_client.get(f"chat:active_session:{user_id}")
    if val is None:
        return None
    if isinstance(val, bytes):
        return val.decode("utf-8")
    return val

def create_active_session(redis_client: redis.Redis, user_id: str) -> str:
    import uuid
    session_id = f"session_{int(datetime.datetime.now().timestamp())}_{uuid.uuid4().hex[:8]}"
    redis_client.setex(f"chat:active_session:{user_id}", 90 * 86400, session_id)
    redis_client.rpush(f"chat:sessions:{user_id}", session_id)
    redis_client.expire(f"chat:sessions:{user_id}", 90 * 86400)
    return session_id

def migrate_old_flat_history(redis_client: redis.Redis, user_id: str):
    """
    Migrates flat history chat:history:{user_id} into multi-session schema.
    """
    old_key = f"chat:history:{user_id}"
    vals = redis_client.lrange(old_key, 0, -1)
    if not vals:
        return
        
    try:
        parsed_msgs = [json.loads(val) for val in vals]
    except Exception as e:
        print(f"WARNING: Error parsing old flat history for migration: {e}")
        return

    sessions_data = []
    current_session_msgs: list[dict] = []
    
    for msg in parsed_msgs:
        role_type = msg.get("type")
        if role_type == "RESET":
            if current_session_msgs:
                sessions_data.append(current_session_msgs)
                current_session_msgs = []
            continue
            
        start_new_session = False
        if not current_session_msgs:
            start_new_session = True
        else:
            prev_msg = current_session_msgs[-1]
            prev_time_str = prev_msg.get("time", "")
            curr_time_str = msg.get("time", "")
            if prev_time_str and curr_time_str:
                try:
                    prev_time = datetime.datetime.strptime(prev_time_str, "%Y-%m-%d %H:%M:%S")
                    curr_time = datetime.datetime.strptime(curr_time_str, "%Y-%m-%d %H:%M:%S")
                    diff_minutes = (curr_time - prev_time).total_seconds() / 60.0
                    if diff_minutes > 15.0:
                        start_new_session = True
                except Exception:
                    pass
                    
        if start_new_session:
            if current_session_msgs:
                sessions_data.append(current_session_msgs)
            current_session_msgs = [msg]
        else:
            current_session_msgs.append(msg)
            
    if current_session_msgs:
        sessions_data.append(current_session_msgs)
        
    import uuid
    for idx, s_msgs in enumerate(sessions_data):
        base_time = datetime.datetime.now() - datetime.timedelta(days=10) + datetime.timedelta(minutes=idx * 20)
        session_id = f"session_{int(base_time.timestamp())}_{uuid.uuid4().hex[:8]}"
        
        history_key = f"chat:history:{user_id}:{session_id}"
        pipe = redis_client.pipeline()
        for msg in s_msgs:
            pipe.rpush(history_key, json.dumps(msg))
        pipe.expire(history_key, 90 * 86400)
        pipe.rpush(f"chat:sessions:{user_id}", session_id)
        pipe.expire(f"chat:sessions:{user_id}", 90 * 86400)
        pipe.execute()
        
    redis_client.delete(old_key)

@router.get("/history")
async def get_history(request: Request, isAdminChat: bool = Query(False)):
    """
    获取对话历史，对应 Java 后端 GET /api/ai/history (升级为多会话列表返回)
    """
    auth_header = request.headers.get("Authorization")
    user_info = get_user_info_from_auth_header(auth_header)
    if not user_info:
        raise HTTPException(status_code=401, detail="Unauthorized")
        
    user_id = user_info.get("userId")
    user_role = user_info.get("role", "USER")
    
    is_admin_chat_effective = isAdminChat
    if is_admin_chat_effective and user_role != "ADMIN":
        is_admin_chat_effective = False
        
    chat_user_key = f"{user_id}:admin" if is_admin_chat_effective else str(user_id)
        
    sessions_key = f"chat:sessions:{chat_user_key}"
    session_ids = redis_client.lrange(sessions_key, 0, -1)
    
    # 平滑迁移旧数据
    if not is_admin_chat_effective and not session_ids:
        migrate_old_flat_history(redis_client, chat_user_key)
        session_ids = redis_client.lrange(sessions_key, 0, -1)
        
    sessions_list = []
    for raw_session_id in session_ids:
        session_id = raw_session_id.decode("utf-8") if isinstance(raw_session_id, bytes) else str(raw_session_id)
        history_key = f"chat:history:{chat_user_key}:{session_id}"
        vals = redis_client.lrange(history_key, 0, -1)
        if not vals:
            continue
            
        messages = []
        for val in vals:
            try:
                msg = json.loads(val)
                role_type = msg.get("type")
                if role_type == "USER":
                    role = "user"
                elif role_type == "AI":
                    role = "assistant"
                else:
                    continue
                    
                messages.append({
                    "role": role,
                    "content": msg.get("text"),
                    "time": msg.get("time", "")
                })
            except Exception:
                continue
                
        if not messages:
            continue
            
        # 提取第一个 User 提问作为会话标题
        title = "新对话"
        for m in messages:
            if m["role"] == "user":
                title = m["content"]
                break
                
        # 提取第一条消息的时间作为会话时间
        session_time = messages[0]["time"] if messages else ""
        
        sessions_list.append({
            "sessionId": session_id,
            "title": title,
            "time": session_time,
            "messages": messages
        })
        
    return {"success": True, "message": "Success", "data": sessions_list}

@router.get("/history/active")
async def get_active_history(request: Request, isAdminChat: bool = Query(False)):
    """
    获取当前活跃会话的消息列表（平铺格式，由前端智能对话初始渲染调用）
    """
    auth_header = request.headers.get("Authorization")
    user_info = get_user_info_from_auth_header(auth_header)
    if not user_info:
        raise HTTPException(status_code=401, detail="Unauthorized")
        
    user_id = user_info.get("userId")
    user_role = user_info.get("role", "USER")
    
    is_admin_chat_effective = isAdminChat
    if is_admin_chat_effective and user_role != "ADMIN":
        is_admin_chat_effective = False
        
    chat_user_key = f"{user_id}:admin" if is_admin_chat_effective else str(user_id)
        
    active_session_id = get_active_session_id(redis_client, chat_user_key)
    if not active_session_id:
        return {"success": True, "message": "Success", "data": []}
        
    history_key = f"chat:history:{chat_user_key}:{active_session_id}"
    vals = redis_client.lrange(history_key, 0, -1)
    
    dto_list = []
    if vals:
        for val in vals:
            try:
                msg = json.loads(val)
                role_type = msg.get("type")
                if role_type == "USER":
                    role = "user"
                elif role_type == "AI":
                    role = "assistant"
                else:
                    continue
                    
                dto_list.append({
                    "role": role,
                    "content": msg.get("text"),
                    "time": msg.get("time", "")
                })
            except Exception:
                continue
                
    return {"success": True, "message": "Success", "data": dto_list}

@router.post("/clear")
async def clear_chat_memory(request: Request, isAdminChat: bool = Query(False)):
    """
    清空当前对话 session 内存，对应 POST /api/ai/clear
    """
    auth_header = request.headers.get("Authorization")
    user_info = get_user_info_from_auth_header(auth_header)
    if not user_info:
        raise HTTPException(status_code=401, detail="Unauthorized")
        
    user_id = user_info.get("userId")
    user_role = user_info.get("role", "USER")
    
    is_admin_chat_effective = isAdminChat
    if is_admin_chat_effective and user_role != "ADMIN":
        is_admin_chat_effective = False
        
    chat_user_key = f"{user_id}:admin" if is_admin_chat_effective else str(user_id)
        
    try:
        redis_client.delete(f"chat:memory:{chat_user_key}")
        redis_client.delete(f"chat:active_session:{chat_user_key}")
        return {"success": True, "message": "当前会话已重置，开启新对话"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"重置会话失败: {str(e)}")

@router.post("/restore")
async def restore_chat_memory(request: Request, body: Any = Body(...), isAdminChat: bool = Query(False)):
    """
    恢复/重置当前会话的内存上下文，从历史记录加载会话时使用
    """
    auth_header = request.headers.get("Authorization")
    user_info = get_user_info_from_auth_header(auth_header)
    if not user_info:
        raise HTTPException(status_code=401, detail="Unauthorized")
        
    user_id = user_info.get("userId")
    user_role = user_info.get("role", "USER")
    
    is_admin_chat_effective = isAdminChat
    if is_admin_chat_effective and user_role != "ADMIN":
        is_admin_chat_effective = False
        
    chat_user_key = f"{user_id}:admin" if is_admin_chat_effective else str(user_id)
        
    try:
        session_id = None
        messages = []
        if isinstance(body, dict):
            session_id = body.get("sessionId")
            messages = body.get("messages", [])
        elif isinstance(body, list):
            messages = body
            session_id = get_active_session_id(redis_client, chat_user_key)
            if not session_id:
                session_id = create_active_session(redis_client, chat_user_key)
                
        if session_id:
            redis_client.setex(f"chat:active_session:{chat_user_key}", 90 * 86400, session_id)
            
        from langchain_core.messages import BaseMessage
        history_msgs: list[BaseMessage] = []
        for msg in messages:
            role = msg.get("role")
            content = msg.get("content")
            if role == "user":
                history_msgs.append(HumanMessage(content=content))
            elif role == "assistant":
                history_msgs.append(AIMessage(content=content))
                
        save_chat_history(redis_client, chat_user_key, history_msgs)
        return {"success": True, "message": "已成功恢复会话上下文"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"恢复会话上下文失败: {str(e)}")

async def run_agent_background(
    agent_executor, input_text, chat_history, queue,
    redis_client_text, user_id, message, ai_msg_index, now_str, session_id
):
    """
    在后台运行的 Agent 执行器任务。即使客户端连接中断（如刷新页面），此任务仍会完全运行并更新历史。
    """
    from langchain_core.messages import HumanMessage, AIMessage
    from agent import save_chat_history

    full_response = ""
    try:
        async for event in agent_executor.astream_events(
            {"input": input_text, "chat_history": chat_history},
            version="v2"
        ):
            kind = event["event"]
            if kind == "on_chat_model_stream":
                content = event["data"]["chunk"].content
                if content:
                    full_response += content
                    await queue.put({"type": "token", "content": content})

        # 回复生成成功，更新长期历史记录中的占位符
        if ai_msg_index != -1 and session_id:
            key = f"chat:history:{user_id}:{session_id}"
            final_ai_data = {"type": "AI", "text": full_response, "time": now_str}
            redis_client_text.lset(key, ai_msg_index, json.dumps(final_ai_data))

        # 更新短期会话内存
        new_memory = chat_history + [HumanMessage(content=message), AIMessage(content=full_response)]
        save_chat_history(redis_client_text, user_id, new_memory)

        await queue.put({"type": "complete"})

    except Exception as e:
        print(f"Error executing agent stream in background: {e}")
        # 将发生错误的信息或已生成的截断数据更新回历史中，避免对话数据损坏或完全丢失
        if ai_msg_index != -1 and session_id:
            key = f"chat:history:{user_id}:{session_id}"
            error_text = full_response if full_response else f"[生成异常: {str(e)}]"
            if full_response:
                error_text += f"\n\n[发生错误，生成中断: {str(e)}]"
            final_ai_data = {"type": "AI", "text": error_text, "time": now_str}
            try:
                redis_client_text.lset(key, ai_msg_index, json.dumps(final_ai_data))
            except Exception as ex:
                print(f"Failed to update history on error: {ex}")

        # 更新错误至短期会话内存
        try:
            err_memory = chat_history + [HumanMessage(content=message), AIMessage(content=f"[生成中断: {str(e)}]")]
            save_chat_history(redis_client_text, user_id, err_memory)
        except Exception as ex:
            print(f"Failed to update memory on error: {ex}")

        await queue.put({"type": "error", "content": str(e)})

async def sse_chat_generator(message: str, user_id: str, is_admin_chat: bool, redis_client_text, redis_client_bin):
    """
    SSE 生成器，通过读取异步队列实时返回 LLM Token 给前端，在后台任务中安全更新 Redis 历史记录。
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
        get_all_products,
        get_all_customers,
        get_customer_detail,
        get_all_suppliers,
        get_supplier_detail,
        get_purchase_orders,
        get_purchase_order_detail,
        get_sales_orders,
        get_sales_order_detail,
        get_all_warehouses
    ]
    if is_admin_chat:
        tools_list.extend([
            get_recent_operation_logs,
            get_all_users
        ])
    
    # 6. 获取智能体执行器
    agent_executor = get_agent_executor(api_key, base_url, model_name, tools_list, is_admin=is_admin_chat)
    
    # 7. 获取或创建活跃的 Session ID
    session_id = get_active_session_id(redis_client_text, user_id)
    if not session_id:
        session_id = create_active_session(redis_client_text, user_id)
        
    # 立即在 Redis 中保存用户消息和 AI 占位符消息 (以确保在刷新时历史记录不丢失)
    now_str = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    history_key = f"chat:history:{user_id}:{session_id}"
    user_history_data = {"type": "USER", "text": message, "time": now_str}
    ai_history_placeholder = {"type": "AI", "text": "...", "time": now_str}
    
    try:
        new_len = redis_client_text.rpush(
            history_key, 
            json.dumps(user_history_data), 
            json.dumps(ai_history_placeholder)
        )
        ai_msg_index = new_len - 1
        redis_client_text.expire(history_key, 90 * 86400)
    except Exception as e:
        print(f"WARNING: Error saving initial history placeholder: {e}")
        ai_msg_index = -1
        
    # 同步占位符至短期会话内存
    try:
        placeholder_memory = chat_history + [HumanMessage(content=message), AIMessage(content="...")]
        save_chat_history(redis_client_text, user_id, placeholder_memory)
    except Exception as e:
        print(f"WARNING: Error saving initial memory placeholder: {e}")
        
    # 8. 创建 asyncio.Queue 并启动后台执行任务
    queue: asyncio.Queue = asyncio.Queue()
    
    asyncio.create_task(run_agent_background(
        agent_executor, input_text, chat_history, queue,
        redis_client_text, user_id, message, ai_msg_index, now_str, session_id
    ))
    
    # 9. 读取 Queue 并以 SSE 协议返回给前端
    try:
        while True:
            event = await queue.get()
            event_type = event.get("type")
            
            if event_type == "token":
                content = event.get("content")
                yield f"data: {json.dumps({'content': content})}\n\n"
            elif event_type == "complete":
                break
            elif event_type == "error":
                raise Exception(event.get("content"))
                
        # 写入 [DONE] 结束符
        yield "event: complete\n"
        yield "data: [DONE]\n\n"
        
    except Exception as e:
        print(f"Error yielding stream events: {e}")
        yield "event: error\n"
        yield f"data: {str(e)}\n\n"

@router.get("/chat")
async def chat(request: Request, message: str = Query(...), isAdminChat: bool = Query(False)):
    """
    AI 仓储问答接口 (Server-Sent Events)
    """
    auth_header = request.headers.get("Authorization")
    user_info = get_user_info_from_auth_header(auth_header)
    if not user_info:
        raise HTTPException(status_code=401, detail="Unauthorized")
        
    user_id = user_info.get("userId")
    user_role = user_info.get("role", "USER")
    
    is_admin_chat_effective = isAdminChat
    if is_admin_chat_effective and user_role != "ADMIN":
        is_admin_chat_effective = False
        
    chat_user_key = f"{user_id}:admin" if is_admin_chat_effective else str(user_id)
        
    # 速率限制检查 (RPM, TPM, RPD)
    await check_rate_limit(redis_client, JAVA_BACKEND_URL, message)
    
    return StreamingResponse(
        sse_chat_generator(message, chat_user_key, is_admin_chat_effective, redis_client, redis_client_binary),
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
