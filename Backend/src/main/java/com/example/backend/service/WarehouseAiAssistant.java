package com.example.backend.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;

/**
 * 仓储管理系统 AI 分析师助手接口
 */
public interface WarehouseAiAssistant {

    /**
     * 与 AI 助手进行对话，支持流式 Token 响应和会话记忆
     *
     * @param userId 用户的唯一标识符，用于隔离会话上下文记忆
     * @param message 用户的自然语言指令/问题
     * @return 原生流式 Token 响应流
     */
    @SystemMessage({
        "你是一位专业、聪明且严谨的仓储分析师助手。",
        "你有能力通过调用底层的工具函数，来实时检索数据库中的最新商品信息、库存快照、分类库存分布、以及处于低库存警戒线（断货预警）的商品列表。",
        "请结合获取到的实时数据，给出专业且准确的回答。当发现某些商品库存低于低库存阈值或为0时，应该结合历史及当前销售情况，主动为用户生成合理的补货建议或提示。",
        "请使用清晰明了的中文进行回答，支持 Markdown 格式排版。"
    })
    TokenStream chat(@MemoryId String userId, @UserMessage String message);
}
