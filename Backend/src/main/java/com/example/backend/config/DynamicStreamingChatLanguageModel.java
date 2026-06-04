package com.example.backend.config;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.StreamingResponseHandler;
import com.example.backend.service.SystemSettingService;
import com.example.backend.utils.EncryptionUtils;
import org.springframework.beans.factory.ObjectProvider;

import java.time.Duration;
import java.util.List;

/**
 * 动态代理流式聊天模型，支持在运行期动态切换和解密 AI API 密钥
 */
public class DynamicStreamingChatLanguageModel implements StreamingChatLanguageModel {
    private final String chatBaseUrl;
    private final String defaultApiKey;
    private final String chatModelName;
    private final Double chatTemperature;
    private final ObjectProvider<SystemSettingService> systemSettingServiceProvider;

    private volatile String cachedKey = null;
    private volatile StreamingChatLanguageModel cachedModel = null;

    public DynamicStreamingChatLanguageModel(String chatBaseUrl, String defaultApiKey, String chatModelName, 
                                             Double chatTemperature, ObjectProvider<SystemSettingService> systemSettingServiceProvider) {
        this.chatBaseUrl = chatBaseUrl;
        this.defaultApiKey = defaultApiKey;
        this.chatModelName = chatModelName;
        this.chatTemperature = chatTemperature;
        this.systemSettingServiceProvider = systemSettingServiceProvider;
    }

    private synchronized StreamingChatLanguageModel getDelegate() {
        String apiKey = defaultApiKey;
        try {
            SystemSettingService systemSettingService = systemSettingServiceProvider.getIfAvailable();
            if (systemSettingService != null) {
                String encryptedKey = systemSettingService.getValue("ai_api_key");
                if (encryptedKey != null && !encryptedKey.trim().isEmpty()) {
                    apiKey = EncryptionUtils.decrypt(encryptedKey);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load or decrypt dynamic AI API key for streaming, using default: " + e.getMessage());
        }

        // 缓存判断，如果密钥没变，则复用已创建的底层流式模型
        if (cachedModel != null && apiKey.equals(cachedKey)) {
            return cachedModel;
        }

        cachedKey = apiKey;
        cachedModel = OpenAiStreamingChatModel.builder()
                .baseUrl(chatBaseUrl)
                .apiKey(apiKey)
                .modelName(chatModelName)
                .temperature(chatTemperature)
                .timeout(Duration.ofSeconds(60))
                .build();
        return cachedModel;
    }

    @Override
    public void generate(List<ChatMessage> messages, StreamingResponseHandler<AiMessage> handler) {
        getDelegate().generate(messages, handler);
    }

    @Override
    public void generate(List<ChatMessage> messages, List<ToolSpecification> toolSpecifications, StreamingResponseHandler<AiMessage> handler) {
        getDelegate().generate(messages, toolSpecifications, handler);
    }

    @Override
    public void generate(List<ChatMessage> messages, ToolSpecification toolSpecification, StreamingResponseHandler<AiMessage> handler) {
        getDelegate().generate(messages, toolSpecification, handler);
    }
}
