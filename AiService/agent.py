import os
import json
from typing import Any
import redis
import numpy as np
from sentence_transformers import SentenceTransformer
from langchain_openai import ChatOpenAI
from langchain.agents import create_agent
from langchain_core.messages import HumanMessage, AIMessage, BaseMessage

# System Prompt from original Java SystemMessage (separated into admin and user modes)
SYSTEM_MESSAGE_TEMPLATE_ADMIN = """你是一位专业、聪明且严谨的仓储分析师助手（管理员模式）。
你拥有两种获取数据并服务用户的方式，需根据问题特征选择合适的协同策略：
1. 当用户提问包含模糊、概念性或语义搜索倾向时（例如“找一下那些听起来像数码产品的货物”、“推荐适合送礼的商品”、“查看是否有应季的水果”等），请优先利用内置的 RAG 语义检索知识库，它会自动关联和召回与用户提问最相关的商品上下文信息，无需调用工具。
2. 当用户需要精确、全局的统计、列表或明细数据时，请精准且优先激活对应的专用 `@Tool` 工具函数：
   - 当需要统计低库存商品、断货预警、获取低于警戒线的商品列表时，请调用 `get_low_stock_products`；
   - 当需要统计或盘点各商品分类的库存分布和占比时，请调用 `get_category_stock_stats`；
   - 当需要获取某个特定商品的详细规格和价格等具体信息时，请调用 `get_product_detail`；
   - 当需要获取特定仓库或全部仓库的实时库存快照（如库存数量、变动时间）时，请调用 `get_stock_snapshot`；
   - 当需要查询所有商品的基础列表信息时，请调用 `get_all_products`；
   - 当需要查询系统中的客户列表、分析客户时，请调用 `get_all_customers`；
   - 当需要获取特定客户的详细联系信息与地址时，请调用 `get_customer_detail`；
   - 当需要查询合作的供应商列表时，请调用 `get_all_suppliers`；
   - 当需要获取特定供应商的详细联系信息与地址时，请调用 `get_supplier_detail`；
   - 当需要盘点或查询系统里的采购订单时，请调用 `get_purchase_orders`；
   - 当需要查看特定采购订单的具体商品及金额明细时，请调用 `get_purchase_order_detail`；
   - 当需要盘点或查询系统里的销售订单时，请调用 `get_sales_orders`；
   - 当需要查看特定销售订单的具体商品及金额明细时，请调用 `get_sales_order_detail`；
   - 当需要查询所有的仓库列表时，请调用 `get_all_warehouses`；
   - 当需要追踪系统的近期操作记录、变更历史或用户操作轨迹时，请调用 `get_recent_operation_logs`；
   - 当需要查询系统中的用户账号列表或分析用户、角色分布时，请调用 `get_all_users`。
请结合检索到的 RAG 知识库上下文或工具返回的实时结构化数据，给出专业、严谨且准确的回答。当发现某些商品库存低于警戒线或为0时，应该结合当前情况，主动为用户生成合理的补货建议或提示。
请使用清晰明了的中文进行回答，支持 Markdown 格式排版。"""

SYSTEM_MESSAGE_TEMPLATE_USER = """你是一位专业、聪明且严谨的仓储分析师助手（普通用户模式）。
你拥有两种获取数据并服务用户的方式，需根据问题特征选择合适的协同策略：
1. 当用户提问包含模糊、概念性或语义搜索倾向时（例如“找一下那些听起来像数码产品的货物”、“推荐适合送礼的商品”、“查看是否有应季的水果”等），请优先利用内置的 RAG 语义检索知识库，它会自动关联和召回与用户提问最相关的商品上下文信息，无需调用工具。
2. 当用户需要精确、全局的统计、列表或明细数据时，请精准且优先激活对应的专用 `@Tool` 工具函数：
   - 当需要统计低库存商品、断货预警、获取低于警戒线的商品列表时，请调用 `get_low_stock_products`；
   - 当需要统计或盘点各商品分类的库存分布和占比时，请调用 `get_category_stock_stats`；
   - 当需要获取某个特定商品的详细规格和价格等具体信息时，请调用 `get_product_detail`；
   - 当需要获取特定仓库或全部仓库的实时库存快照（如库存数量、变动时间）时，请调用 `get_stock_snapshot`；
   - 当需要查询所有商品的基础列表信息时，请调用 `get_all_products`；
   - 当需要查询系统中的客户列表、分析客户时，请调用 `get_all_customers`；
   - 当需要获取特定客户的详细联系信息与地址时，请调用 `get_customer_detail`；
   - 当需要查询合作的供应商列表时，请调用 `get_all_suppliers`；
   - 当需要获取特定供应商的详细联系信息与地址时，请调用 `get_supplier_detail`；
   - 当需要盘点或查询系统里的采购订单时，请调用 `get_purchase_orders`；
   - 当需要查看特定采购订单的具体商品及金额明细时，请调用 `get_purchase_order_detail`；
   - 当需要盘点或查询系统里的销售订单时，请调用 `get_sales_orders`；
   - 当需要查看特定销售订单的具体商品及金额明细时，请调用 `get_sales_order_detail`；
   - 当需要查询所有的仓库列表时，请调用 `get_all_warehouses`。
请结合检索到的 RAG 知识库上下文或工具返回的实时结构化数据，给出专业、严谨且准确的回答。当发现某些商品库存低于警戒线或为0时，应该结合当前情况，主动为用户生成合理的补货建议或提示。
对于你无法访问的信息或任何你不具备对应查询工具的请求，请绝对不要在回答中说明、提及、罗列或解释任何你无法访问或无权访问的具体信息（例如：绝对不要在回复中提及“系统用户列表”、“系统操作日志”、“用户登录记录”等词汇，也不要向用户解释由于权限不足无法访问此类管理端信息）。你应当礼貌而直接地仅给出你可以访问并提供的业务信息（包括商品、库存、仓库、销售订单、采购订单、客户及供应商等仓储业务相关数据）。请使用清晰明了的中文进行回答，支持 Markdown 格式排版。"""

# Lazily load sentence transformer model to save startup memory/time
_embedding_model = None

def get_embedding_model():
    global _embedding_model
    if _embedding_model is None:
        # Load local 384-dimension AllMiniLmL6V2EmbeddingModel equivalent
        model_name = os.getenv("EMBEDDING_MODEL_NAME", "all-MiniLM-L6-v2")
        print(f"Loading sentence transformer model: {model_name}...")
        _embedding_model = SentenceTransformer(model_name)
    return _embedding_model

_index_verified = False
_redis_search_supported = True

def ensure_vector_index(redis_client: redis.Redis, index_name: str):
    """
    Ensure the RediSearch index exists, creating it if it doesn't.
    """
    global _index_verified, _redis_search_supported
    if not _redis_search_supported:
        return
    if _index_verified:
        return
    try:
        redis_client.ft(index_name).info()
        _index_verified = True
    except Exception as e:
        err_msg = str(e).lower()
        if "unknown command" in err_msg:
            print("WARNING: Redis Search (RediSearch) module is not loaded or supported by the Redis server. Vector search (RAG) will be disabled.")
            _redis_search_supported = False
            return
            
        print(f"Index {index_name} not found. Creating RediSearch index...")
        from redis.commands.search.field import TextField, TagField, VectorField  # type: ignore[import-not-found]
        from redis.commands.search.index_definition import IndexDefinition, IndexType  # type: ignore[import-not-found]
        
        schema = [
            TextField("text"),
            TagField("productId"),
            VectorField("vector", "FLAT", {
                "TYPE": "FLOAT32",
                "DIMENSION": 384,
                "DISTANCE_METRIC": "COSINE"
            })
        ]
        try:
            redis_client.ft(index_name).create_index(
                schema,
                definition=IndexDefinition(prefix=["embedding:"], index_type=IndexType.HASH)
            )
            print(f"Index {index_name} created successfully.")
            _index_verified = True
        except Exception as ex:
            ex_msg = str(ex).lower()
            if "unknown command" in ex_msg:
                print("WARNING: Redis Search (RediSearch) module is not loaded or supported by the Redis server. Vector search (RAG) will be disabled.")
                _redis_search_supported = False
            else:
                print(f"Error creating RediSearch index {index_name}: {ex}")

def search_similar_products(redis_client: redis.Redis, query_text: str, index_name: str, min_similarity=0.6) -> list:
    """
    Perform vector search on Redis (RAG retrieval)
    """
    global _redis_search_supported
    if not _redis_search_supported:
        return []
    ensure_vector_index(redis_client, index_name)
    if not _redis_search_supported:
        return []
    try:
        model = get_embedding_model()
        # Compute vector representation (384 dimensions)
        query_vector = model.encode(query_text).astype(np.float32).tobytes()
        
        # In RediSearch with COSINE distance metric:
        # score = 1 - cosine_similarity.
        # Therefore, cosine_similarity >= 0.6 maps to score <= 0.4
        from redis.commands.search.query import Query  # type: ignore[import-not-found]
        q = Query("*=>[KNN 5 @vector $vector_blob AS score]")\
            .sort_by("score")\
            .return_fields("text", "productId", "score")\
            .dialect(2)
            
        results = redis_client.ft(index_name).search(q, query_params={"vector_blob": query_vector})
        
        matched_docs = []
        for doc in results.docs:
            score = float(doc.score)
            similarity = 1.0 - score
            if similarity >= min_similarity:
                text = doc.text
                if isinstance(text, bytes):
                    text = text.decode("utf-8")
                matched_docs.append(text)
                
        return matched_docs
    except Exception as e:
        err_msg = str(e).lower()
        if "unknown command" in err_msg:
            print("WARNING: Redis Search (RediSearch) module is not loaded or supported by the Redis server. Vector search (RAG) will be disabled.")
            _redis_search_supported = False
        else:
            print(f"WARNING: Error searching similar products: {e}")
        return []

def load_chat_history(redis_client: redis.Redis, user_id: str) -> list:
    """
    加载历史会话并序列化为 LangChain 格式的消息
    和 Java RedisChatMemoryStore 格式完全对标
    """
    key = f"chat:memory:{user_id}"
    val = redis_client.get(key)
    if not val:
        return []
    try:
        data = json.loads(val)
        messages: list[BaseMessage] = []
        for msg in data:
            role = msg.get("type")
            text = msg.get("text")
            if role == "USER":
                messages.append(HumanMessage(content=text))
            elif role == "AI" and text:
                messages.append(AIMessage(content=text))
        return messages
    except Exception as e:
        print(f"WARNING: Error loading chat memory for user {user_id}: {e}")
        return []

def save_chat_history(redis_client: redis.Redis, user_id: str, history_messages: list):
    """
    保存历史对话，限制保存最大 10 条消息 (MessageWindowChatMemory 最大消息数限制)
    格式与 Java 保持一致
    """
    key = f"chat:memory:{user_id}"
    data = []
    # Only keep the last 10 messages
    for msg in history_messages[-10:]:
        if isinstance(msg, HumanMessage):
            data.append({"type": "USER", "text": msg.content})
        elif isinstance(msg, AIMessage):
            # Only serialize if the text content is not empty and no tool calls pending
            if msg.content:
                data.append({"type": "AI", "text": msg.content})
    try:
        # Save to redis with 90 days expiration
        redis_client.setex(key, 90 * 86400, json.dumps(data))
    except Exception as e:
        print(f"WARNING: Error saving chat memory for user {user_id}: {e}")

def save_long_term_history(redis_client: redis.Redis, user_id: str, human_msg: str, ai_msg: str):
    """
    保存完整的历史记录到 chat:history:{user_id} 中，保留至少 90 天
    """
    import datetime
    key = f"chat:history:{user_id}"
    now_str = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    
    user_data = {"type": "USER", "text": human_msg, "time": now_str}
    ai_data = {"type": "AI", "text": ai_msg, "time": now_str}
    
    try:
        redis_client.rpush(key, json.dumps(user_data), json.dumps(ai_data))
        redis_client.expire(key, 90 * 86400)
    except Exception as e:
        print(f"WARNING: Error saving long-term history for user {user_id}: {e}")

def load_long_term_history(redis_client: redis.Redis, user_id: str) -> list:
    """
    加载完整的历史记录，保留至少 90 天
    """
    import datetime
    key = f"chat:history:{user_id}"
    try:
        vals = redis_client.lrange(key, 0, -1)
        if not vals:
            # 如果长历史为空，尝试从 chat:memory 转换（平滑升级）
            memory = load_chat_history(redis_client, user_id)
            if memory:
                now_str = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
                history_data = []
                for msg in memory:
                    role_type = "USER" if isinstance(msg, HumanMessage) else "AI"
                    history_data.append({"type": role_type, "text": msg.content, "time": now_str})
                    redis_client.rpush(key, json.dumps({"type": role_type, "text": msg.content, "time": now_str}))
                redis_client.expire(key, 90 * 86400)
                return history_data
            return []
        
        return [json.loads(val) for val in vals]
    except Exception as e:
        print(f"WARNING: Error loading long-term history for user {user_id}: {e}")
        return []

class AgentExecutorWrapper:
    def __init__(self, graph: Any):
        self.graph = graph

    async def astream_events(self, inputs: dict[str, Any], version: str = "v2") -> Any:
        chat_history = inputs.get("chat_history", [])
        input_text = inputs.get("input", "")
        
        messages: list[BaseMessage] = list(chat_history)
        if input_text:
            messages.append(HumanMessage(content=input_text))
            
        graph_input = {"messages": messages}
        
        async for event in self.graph.astream_events(graph_input, version=version):
            yield event

_agent_executor_cache: dict[Any, Any] = {}

def get_agent_executor(api_key: str, base_url: str, model_name: str, tools: list, is_admin: bool = False) -> AgentExecutorWrapper:
    tool_names = tuple(t.name for t in tools)
    cache_key = (api_key, base_url, model_name, tool_names, is_admin)
    
    global _agent_executor_cache
    if cache_key in _agent_executor_cache:
        return _agent_executor_cache[cache_key]
        
    llm = ChatOpenAI(
        openai_api_key=api_key,  # type: ignore[call-arg]
        openai_api_base=base_url,  # type: ignore[call-arg]
        model_name=model_name,  # type: ignore[call-arg]
        temperature=0.7,
        streaming=True
    )
    
    system_prompt = SYSTEM_MESSAGE_TEMPLATE_ADMIN if is_admin else SYSTEM_MESSAGE_TEMPLATE_USER
    
    graph = create_agent(
        model=llm,
        tools=tools,
        system_prompt=system_prompt
    )
    executor = AgentExecutorWrapper(graph)
    _agent_executor_cache[cache_key] = executor
    return executor
