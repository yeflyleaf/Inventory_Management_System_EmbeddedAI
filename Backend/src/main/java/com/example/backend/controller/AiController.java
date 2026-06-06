package com.example.backend.controller;

import com.example.backend.annotation.LoginRequired;
import com.example.backend.common.Result;
import com.example.backend.service.WarehouseAiAssistant;
import com.example.backend.service.ProductEmbeddingService;
import dev.langchain4j.service.TokenStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.example.backend.dto.ChatHistoryDTO;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import java.util.List;
import java.util.ArrayList;

import com.example.backend.service.SystemSettingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

/**
 * AI 智能助手控制器
 * 提供基于 SSE 协议的流式对话和管理向量库同步的端点
 */
@RestController
@RequestMapping("/ai")
@CrossOrigin
public class AiController {

    private static final Logger logger = LoggerFactory.getLogger(AiController.class);

    @Autowired
    private WarehouseAiAssistant aiAssistant;

    @Autowired
    private ProductEmbeddingService productEmbeddingService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private SystemSettingService systemSettingService;

    /**
     * 获取当前登录用户的 AI 对话历史记录
     */
    @GetMapping("/history")
    @LoginRequired
    public Result<List<ChatHistoryDTO>> getHistory(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.success(new ArrayList<>());
        }
        String key = "chat:memory:" + userId;
        String json = redisTemplate.opsForValue().get(key);
        if (json == null || json.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        List<ChatMessage> messages = ChatMessageDeserializer.messagesFromJson(json);
        List<ChatHistoryDTO> dtoList = new ArrayList<>();
        for (ChatMessage message : messages) {
            if (message instanceof UserMessage userMessage) {
                dtoList.add(new ChatHistoryDTO("user", userMessage.text(), ""));
            } else if (message instanceof AiMessage aiMessage) {
                // 仅提取文本内容，且排除工具调用等空消息
                if (aiMessage.text() != null && !aiMessage.text().isEmpty()) {
                    dtoList.add(new ChatHistoryDTO("assistant", aiMessage.text(), ""));
                }
            }
        }
        return Result.success(dtoList);
    }

    /**
     * 流式 AI 仓储问答接口 (Server-Sent Events)
     *
     * @param message 用户的问答输入
     * @param request 请求上下文，用于提取 JWT 解析的 userId
     * @param response 响应上下文，用于关闭 Nginx 反向代理缓存
     * @return SseEmitter 实例
     */
    @GetMapping(value = "/chat", produces = "text/event-stream")
    @LoginRequired
    public SseEmitter chat(@RequestParam("message") String message, 
                           HttpServletRequest request, 
                           HttpServletResponse response) {
        
        // 显式添加响应头，关闭 Nginx 缓存，确保打字机效果流畅推送
        response.setHeader("X-Accel-Buffering", "no");
        
        Long userId = (Long) request.getAttribute("userId");
        String memoryId = userId != null ? userId.toString() : "anonymous";

        // 速率限制检查 (RPM & 输入 TPM & RPD)
        String rawRpm = systemSettingService.getValue("ai_max_rpm");
        String rawTpm = systemSettingService.getValue("ai_max_tpm");
        String rawRpd = systemSettingService.getValue("ai_max_rpd");

        long currentMinute = System.currentTimeMillis() / 60000;
        String currentDay = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));

        String rpmKey = "ai:ratelimit:rpm:" + currentMinute;
        String tpmKey = "ai:ratelimit:tpm:" + currentMinute;
        String rpdKey = "ai:ratelimit:rpd:" + currentDay;

        int estimatedPromptTokens = estimateTokens(message);

        boolean rpmIncremented = false;
        boolean tpmIncremented = false;

        try {
            // 1. 检查并递增 RPM
            if (rawRpm != null && !rawRpm.trim().isEmpty()) {
                int maxRpm = Integer.parseInt(rawRpm.trim());
                Long currentRpm = redisTemplate.opsForValue().increment(rpmKey, 1);
                if (currentRpm != null) {
                    rpmIncremented = true;
                    if (currentRpm == 1) {
                        redisTemplate.expire(rpmKey, java.time.Duration.ofMinutes(2));
                    }
                    if (currentRpm > maxRpm) {
                        redisTemplate.opsForValue().decrement(rpmKey, 1);
                        rpmIncremented = false;
                        response.setStatus(429);
                        response.setCharacterEncoding("UTF-8");
                        response.setContentType("application/json");
                        response.getWriter().write("{\"success\":false,\"message\":\"AI 服务调用过于频繁，已达到系统最高 RPM 限制（" + maxRpm + "次/分钟），请稍后再试。\"}");
                        response.getWriter().flush();
                        return null;
                    }
                }
            }

            // 2. 检查并递增 TPM (只对输入限制)
            if (rawTpm != null && !rawTpm.trim().isEmpty()) {
                int maxTpm = Integer.parseInt(rawTpm.trim());
                String currentTpmStr = redisTemplate.opsForValue().get(tpmKey);
                long currentTpm = currentTpmStr != null ? Long.parseLong(currentTpmStr) : 0;
                if (currentTpm + estimatedPromptTokens > maxTpm) {
                    if (rpmIncremented) {
                        redisTemplate.opsForValue().decrement(rpmKey, 1);
                    }
                    response.setStatus(429);
                    response.setCharacterEncoding("UTF-8");
                    response.setContentType("application/json");
                    response.getWriter().write("{\"success\":false,\"message\":\"AI 服务 Token 使用率过高，已达到系统最高 TPM 限制（输入：" + maxTpm + " tokens/分钟），请稍后再试。\"}");
                    response.getWriter().flush();
                    return null;
                }
                redisTemplate.opsForValue().increment(tpmKey, estimatedPromptTokens);
                tpmIncremented = true;
                redisTemplate.expire(tpmKey, java.time.Duration.ofMinutes(2));
            }

            // 3. 检查并递增 RPD
            if (rawRpd != null && !rawRpd.trim().isEmpty()) {
                int maxRpd = Integer.parseInt(rawRpd.trim());
                Long currentRpd = redisTemplate.opsForValue().increment(rpdKey, 1);
                if (currentRpd != null) {
                    if (currentRpd == 1) {
                        redisTemplate.expire(rpdKey, java.time.Duration.ofDays(2));
                    }
                    if (currentRpd > maxRpd) {
                        redisTemplate.opsForValue().decrement(rpdKey, 1);
                        if (rpmIncremented) {
                            redisTemplate.opsForValue().decrement(rpmKey, 1);
                        }
                        if (tpmIncremented) {
                            redisTemplate.opsForValue().decrement(tpmKey, estimatedPromptTokens);
                        }
                        response.setStatus(429);
                        response.setCharacterEncoding("UTF-8");
                        response.setContentType("application/json");
                        response.getWriter().write("{\"success\":false,\"message\":\"AI 服务调用已达每日上限，已达到系统最高 RPD 限制（" + maxRpd + "次/天），请明天再试。\"}");
                        response.getWriter().flush();
                        return null;
                    }
                }
            }

        } catch (Exception e) {
            // Redis 异常时记录日志并降级放行，保证服务可用性
            logger.warn("AI 速率限制检查时遇到异常，已绕过限制: {}", e.getMessage());
        }

        // 设置2分钟超时时间
        SseEmitter emitter = new SseEmitter(120000L);

        TokenStream tokenStream = aiAssistant.chat(memoryId, message);

        tokenStream
                .onNext(token -> {
                    try {
                        // 推送普通文本 token 到前端
                        emitter.send(SseEmitter.event().data(token));
                    } catch (IOException e) {
                        // 客户端断开连接等异常处理
                    }
                })
                .onComplete(resp -> {
                    try {
                        // 显式通知流结束，推送 [DONE] 标志
                        emitter.send(SseEmitter.event().name("complete").data("[DONE]"));
                        emitter.complete();
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                })
                .onError(error -> {
                    try {
                        // 推送错误消息
                        emitter.send(SseEmitter.event().name("error").data(error.getMessage()));
                        emitter.completeWithError(error);
                    } catch (IOException e) {
                        emitter.complete();
                    }
                })
                .start();

        return emitter;
    }

    private int estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        // 估算 Token：字符长度 * 2
        return (int) Math.ceil(text.length() * 2.0);
    }

    /**
     * 手动触发重建全部商品向量索引的接口
     */
    @PostMapping("/reindex")
    @LoginRequired
    public Result<Void> reindex() {
        productEmbeddingService.reindexAllProducts();
        return Result.success(null, "已触发全量商品向量索引重建");
    }
}
