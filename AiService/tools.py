import os
import httpx
from typing import Optional
from langchain_core.tools import tool

JAVA_BACKEND_URL = os.getenv("JAVA_BACKEND_URL", "http://localhost:8080")

def set_java_backend_url(url: str):
    global JAVA_BACKEND_URL
    JAVA_BACKEND_URL = url

@tool
def get_stock_snapshot(warehouseId: Optional[int] = None) -> list:
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

@tool
def get_all_customers() -> list:
    """获取系统中所有客户的列表（包含客户ID、名称、联系人、联系电话、地址）"""
    try:
        resp = httpx.get(f"{JAVA_BACKEND_URL}/internal/ai/tools/customers", timeout=10.0)
        return resp.json() if resp.status_code == 200 else []
    except Exception as e:
        return [f"Error fetching all customers: {e}"]

@tool
def get_customer_detail(customerId: int) -> dict:
    """根据客户ID获取特定客户的详细联系信息、备用电话及联系地址"""
    try:
        resp = httpx.get(f"{JAVA_BACKEND_URL}/internal/ai/tools/customer-detail", params={"customerId": customerId}, timeout=10.0)
        return resp.json() if resp.status_code == 200 else {}
    except Exception as e:
        return {"error": f"Error fetching customer detail: {e}"}

@tool
def get_all_suppliers() -> list:
    """获取系统中所有供应商的列表（包含供应商ID、名称、联系人、联系电话、地址）"""
    try:
        resp = httpx.get(f"{JAVA_BACKEND_URL}/internal/ai/tools/suppliers", timeout=10.0)
        return resp.json() if resp.status_code == 200 else []
    except Exception as e:
        return [f"Error fetching all suppliers: {e}"]

@tool
def get_supplier_detail(supplierId: int) -> dict:
    """根据供应商ID获取特定供应商的详细联系信息、备用电话及联系地址"""
    try:
        resp = httpx.get(f"{JAVA_BACKEND_URL}/internal/ai/tools/supplier-detail", params={"supplierId": supplierId}, timeout=10.0)
        return resp.json() if resp.status_code == 200 else {}
    except Exception as e:
        return {"error": f"Error fetching supplier detail: {e}"}

@tool
def get_purchase_orders() -> list:
    """获取系统中所有采购订单的列表（包含订单ID、单号、供应商ID、总金额、状态、创建时间）"""
    try:
        resp = httpx.get(f"{JAVA_BACKEND_URL}/internal/ai/tools/purchase-orders", timeout=10.0)
        return resp.json() if resp.status_code == 200 else []
    except Exception as e:
        return [f"Error fetching purchase orders: {e}"]

@tool
def get_purchase_order_detail(orderId: int) -> dict:
    """根据采购订单ID获取采购订单的详细商品明细（包含商品ID、名称、采购数量、采购单价、小计）"""
    try:
        resp = httpx.get(f"{JAVA_BACKEND_URL}/internal/ai/tools/purchase-order-detail", params={"orderId": orderId}, timeout=10.0)
        return resp.json() if resp.status_code == 200 else {}
    except Exception as e:
        return {"error": f"Error fetching purchase order detail: {e}"}

@tool
def get_sales_orders() -> list:
    """获取系统中所有销售订单的列表（包含订单ID、单号、客户ID、总金额、状态、创建时间）"""
    try:
        resp = httpx.get(f"{JAVA_BACKEND_URL}/internal/ai/tools/sales-orders", timeout=10.0)
        return resp.json() if resp.status_code == 200 else []
    except Exception as e:
        return [f"Error fetching sales orders: {e}"]

@tool
def get_sales_order_detail(orderId: int) -> dict:
    """根据销售订单ID获取销售订单的详细商品明细（包含商品ID、名称、销售数量、销售单价、小计）"""
    try:
        resp = httpx.get(f"{JAVA_BACKEND_URL}/internal/ai/tools/sales-order-detail", params={"orderId": orderId}, timeout=10.0)
        return resp.json() if resp.status_code == 200 else {}
    except Exception as e:
        return {"error": f"Error fetching sales order detail: {e}"}

@tool
def get_all_warehouses() -> list:
    """获取系统中的所有仓库列表（包含仓库ID、名称、地址）"""
    try:
        resp = httpx.get(f"{JAVA_BACKEND_URL}/internal/ai/tools/warehouses", timeout=10.0)
        return resp.json() if resp.status_code == 200 else []
    except Exception as e:
        return [f"Error fetching all warehouses: {e}"]

@tool
def get_recent_operation_logs(limit: int = 50) -> list:
    """获取系统最近的操作日志列表（包含操作人、模块、动作、操作对象、操作描述、时间），最大限制条数默认为50"""
    try:
        resp = httpx.get(f"{JAVA_BACKEND_URL}/internal/ai/tools/recent-logs", params={"limit": limit}, timeout=10.0)
        return resp.json() if resp.status_code == 200 else []
    except Exception as e:
        return [f"Error fetching recent logs: {e}"]

@tool
def get_all_users() -> list:
    """获取系统里所有已注册的用户账号列表（包含用户ID、用户名、昵称、角色、状态、电话、邮箱、最后登录时间，不含密码）"""
    try:
        resp = httpx.get(f"{JAVA_BACKEND_URL}/internal/ai/tools/users", timeout=10.0)
        return resp.json() if resp.status_code == 200 else []
    except Exception as e:
        return [f"Error fetching all users: {e}"]
