import os
import uvicorn
from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

from router import router  # noqa: E402
from tools import set_java_backend_url  # noqa: E402

# Configure backend connection url from env
java_backend_url = os.getenv("JAVA_BACKEND_URL", "http://localhost:8080")
set_java_backend_url(java_backend_url)

app = FastAPI(title="Warehouse AI Microservice", version="1.0.0")

# Setup CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origin_regex=".*",
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

from starlette.exceptions import HTTPException as StarletteHTTPException  # noqa: E402

@app.exception_handler(StarletteHTTPException)
async def http_exception_handler(request: Request, exc: StarletteHTTPException):
    return JSONResponse(
        status_code=exc.status_code,
        content={"success": False, "message": str(exc.detail)}
    )

# Global Exception handler
@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    import traceback
    traceback.print_exc()
    return JSONResponse(
        status_code=500,
        content={"success": False, "message": f"AI服务内部错误: {str(exc)}"}
    )

# Register routes under '/ai' prefix (matched by API Gateway routing /api/ai/)
app.include_router(router, prefix="/ai")

@app.on_event("startup")
def startup_event():
    try:
        from agent import get_embedding_model
        print("Preloading sentence transformer model at startup...")
        get_embedding_model()
        print("Sentence transformer model preloaded successfully.")
    except Exception as e:
        print(f"Error preloading embedding model: {e}")

if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
