import os
import httpx
from langchain_core.tools import tool

JAVA_BACKEND_URL = os.getenv("JAVA_BACKEND_URL", "http://localhost:8080")

def set_java_backend_url(url: str):
    global JAVA_BACKEND_URL
    JAVA_BACKEND_URL = url

@tool
def get_stock_snapshot(warehouseId: int = None) -> list:
    """获取指定仓库或所有仓库的当前库存列表快照（包含当前库存数量、商品名称、SKU、分类、最后变动时间）"""
    params = {}
    if warehouseId is not None:
        params["warehouseId"] = warehouseId
    try:
        resp = httpx.get(f"{JAVA_BACKEND_URL}/internal/ai/tools/stock-snapshot", params=params, timeout=10.0)
        return resp.json() if resp.status_code == 200 else []
    except Exception as e:
        return [f"Error fetching stock snapshot: {e}"]

@tool
def get_low_stock_products() -> list:
    """查询库存处于紧张状态（低于低库存预警阈值）的商品列表"""
    try:
        resp = httpx.get(f"{JAVA_BACKEND_URL}/internal/ai/tools/low-stock", timeout=10.0)
        return resp.json() if resp.status_code == 200 else []
    except Exception as e:
        return [f"Error fetching low stock products: {e}"]

@tool
def get_category_stock_stats() -> list:
    """统计各个商品分类的库存总数列表（返回各分类的名称和库存数值）"""
    try:
        resp = httpx.get(f"{JAVA_BACKEND_URL}/internal/ai/tools/category-stock-stats", timeout=10.0)
        return resp.json() if resp.status_code == 200 else []
    except Exception as e:
        return [f"Error fetching category stock stats: {e}"]

@tool
def get_product_detail(productId: int) -> dict:
    """根据商品ID获取商品的详细规格及价格信息"""
    try:
        resp = httpx.get(f"{JAVA_BACKEND_URL}/internal/ai/tools/product-detail", params={"productId": productId}, timeout=10.0)
        return resp.json() if resp.status_code == 200 else {}
    except Exception as e:
        return {"error": f"Error fetching product detail: {e}"}

@tool
def get_all_products() -> list:
    """获取所有商品的基础信息列表（包含商品ID、名称、SKU编码、分类、销售单价）"""
    try:
        resp = httpx.get(f"{JAVA_BACKEND_URL}/internal/ai/tools/all-products", timeout=10.0)
        return resp.json() if resp.status_code == 200 else []
    except Exception as e:
        return [f"Error fetching all products: {e}"]
