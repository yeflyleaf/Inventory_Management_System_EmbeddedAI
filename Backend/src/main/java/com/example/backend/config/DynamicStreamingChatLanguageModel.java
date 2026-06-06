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
        String baseUrl = chatBaseUrl;
        String modelName = chatModelName;
        Double temperature = chatTemperature;

        try {
            SystemSettingService systemSettingService = systemSettingServiceProvider.getIfAvailable();
            if (systemSettingService != null) {
                // 1. API Key
                String encryptedKey = systemSettingService.getValue("ai_api_key");
                if (encryptedKey != null && !encryptedKey.trim().isEmpty()) {
                    apiKey = EncryptionUtils.decrypt(encryptedKey);
                }

                // 2. Base URL
                String dbBaseUrl = systemSettingService.getValue("ai_base_url");
                if (dbBaseUrl != null && !dbBaseUrl.trim().isEmpty()) {
                    baseUrl = dbBaseUrl.trim();
                }

                // 3. Model Name
                String dbModelName = systemSettingService.getValue("ai_model_name");
                if (dbModelName != null && !dbModelName.trim().isEmpty()) {
                    modelName = dbModelName.trim();
                }

                // 4. Temperature
                String dbTemp = systemSettingService.getValue("ai_temperature");
                if (dbTemp != null && !dbTemp.trim().isEmpty()) {
                    try {
                        temperature = Double.parseDouble(dbTemp.trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Failed to parse dynamic AI temperature for streaming: " + dbTemp);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load dynamic AI settings for streaming, using defaults: " + e.getMessage());
        }

        // Cache check: rebuild only if any of the dynamic properties have changed.
        String currentCacheKey = String.format("%s|%s|%s|%s", apiKey, baseUrl, modelName, temperature);
        if (cachedModel != null && currentCacheKey.equals(cachedKey)) {
            return cachedModel;
        }

        cachedKey = currentCacheKey;
        cachedModel = OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
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
