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

import java.io.IOException;

/**
 * AI 智能助手控制器
 * 提供基于 SSE 协议的流式对话和管理向量库同步的端点
 */
@RestController
@RequestMapping("/ai")
@CrossOrigin
public class AiController {

    @Autowired
    private WarehouseAiAssistant aiAssistant;

    @Autowired
    private ProductEmbeddingService productEmbeddingService;

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
