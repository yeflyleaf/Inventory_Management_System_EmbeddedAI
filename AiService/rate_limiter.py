import time
import datetime
from typing import Any
from fastapi import HTTPException
import redis

# In-memory settings cache to avoid spamming the Java backend on every token check
_settings_cache: dict[str, Any] = {}
_settings_cache_time = 0.0

async def get_settings(java_backend_url: str):
    global _settings_cache, _settings_cache_time
    now = time.time()
    if now - _settings_cache_time < 60.0 and _settings_cache:
        return _settings_cache
    
    import httpx
    try:
        async with httpx.AsyncClient(timeout=3.0) as client:
            resp = await client.get(f"{java_backend_url}/internal/ai/tools/settings")
            if resp.status_code == 200:
                _settings_cache = resp.json()
                _settings_cache_time = now
                return _settings_cache
    except Exception as e:
        print(f"WARNING: Failed to fetch settings from Java backend: {e}")
    
    # Fallback to env defaults or empty values if backend is unreachable
    import os
    return {
        "ai_api_key": os.getenv("OPENAI_API_KEY", "demo"),
        "ai_base_url": os.getenv("OPENAI_API_BASE", "https://api.openai.com/v1"),
        "ai_model_name": os.getenv("OPENAI_MODEL_NAME", "gpt-4o"),
        "ai_max_rpm": "",
        "ai_max_tpm": "",
        "ai_max_rpd": ""
    }

def estimate_tokens(text: str) -> int:
    if not text:
        return 0
    # Simple estimation matching Java logic: char length * 2
    return int(len(text) * 2)

async def check_rate_limit(redis_client: redis.Redis, java_backend_url: str, message: str):
    """
    完全对标原 Java Logic 的 Redis 速率限制器 (RPM & 输入 TPM & RPD)
    如果超出限制，抛出 status_code=429 的 HTTPException。
    """
    settings = await get_settings(java_backend_url)
    
    raw_rpm = settings.get("ai_max_rpm")
    raw_tpm = settings.get("ai_max_tpm")
    raw_rpd = settings.get("ai_max_rpd")
    
    now_ms = int(time.time() * 1000)
    current_minute = now_ms // 60000
    current_day = datetime.datetime.now().strftime("%Y%m%d")
    
    rpm_key = f"ai:ratelimit:rpm:{current_minute}"
    tpm_key = f"ai:ratelimit:tpm:{current_minute}"
    rpd_key = f"ai:ratelimit:rpd:{current_day}"
    
    estimated_prompt_tokens = estimate_tokens(message)
    
    rpm_incremented = False
    tpm_incremented = False
    
    try:
        # 1. 检查并递增 RPM
        if raw_rpm and str(raw_rpm).strip():
            max_rpm = int(str(raw_rpm).strip())
            current_rpm = redis_client.incrby(rpm_key, 1)
            rpm_incremented = True
            if current_rpm == 1:
                redis_client.expire(rpm_key, 120)  # 2 minutes expiry
            if current_rpm > max_rpm:
                redis_client.decrby(rpm_key, 1)
                rpm_incremented = False
                raise HTTPException(
                    status_code=429,
                    detail=f"AI 服务调用过于频繁，已达到系统最高 RPM 限制（{max_rpm}次/分钟），请稍后再试。"
                )
        
        # 2. 检查并递增 TPM (只对输入限制)
        if raw_tpm and str(raw_tpm).strip():
            max_tpm = int(str(raw_tpm).strip())
            current_tpm_val = redis_client.get(tpm_key)
            current_tpm = int(current_tpm_val) if current_tpm_val else 0
            if current_tpm + estimated_prompt_tokens > max_tpm:
                if rpm_incremented:
                    redis_client.decrby(rpm_key, 1)
                raise HTTPException(
                    status_code=429,
                    detail=f"AI 服务 Token 使用率过高，已达到系统最高 TPM 限制（输入：{max_tpm} tokens/分钟），请稍后再试。"
                )
            
            redis_client.incrby(tpm_key, estimated_prompt_tokens)
            tpm_incremented = True
            if not current_tpm_val:
                redis_client.expire(tpm_key, 120)
        
        # 3. 检查并递增 RPD
        if raw_rpd and str(raw_rpd).strip():
            max_rpd = int(str(raw_rpd).strip())
            current_rpd = redis_client.incrby(rpd_key, 1)
            if current_rpd == 1:
                redis_client.expire(rpd_key, 172800)  # 2 days expiry (172800 seconds)
            if current_rpd > max_rpd:
                redis_client.decrby(rpd_key, 1)
                if rpm_incremented:
                    redis_client.decrby(rpm_key, 1)
                if tpm_incremented:
                    redis_client.decrby(tpm_key, estimated_prompt_tokens)
                raise HTTPException(
                    status_code=429,
                    detail=f"AI 服务调用已达每日上限，已达到系统最高 RPD 限制（{max_rpd}次/天），请明天再试。"
                )
                
    except HTTPException:
        raise
    except Exception as e:
        # Redis 异常时记录日志并降级放行，保证服务可用性
        print(f"WARNING: AI 速率限制检查时遇到异常，已绕过限制: {e}")
