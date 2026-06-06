package com.example.backend.config;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.agent.tool.ToolSpecification;
import com.example.backend.service.SystemSettingService;
import com.example.backend.utils.EncryptionUtils;
import org.springframework.beans.factory.ObjectProvider;

import java.time.Duration;
import java.util.List;

/**
 * 动态代理聊天模型，支持在运行期动态切换和解密 AI API 密钥
 */
public class DynamicChatLanguageModel implements ChatLanguageModel {
    private final String chatBaseUrl;
    private final String defaultApiKey;
    private final String chatModelName;
    private final ObjectProvider<SystemSettingService> systemSettingServiceProvider;

    private volatile String cachedKey = null;
    private volatile ChatLanguageModel cachedModel = null;

    public DynamicChatLanguageModel(String chatBaseUrl, String defaultApiKey, String chatModelName, 
                                    ObjectProvider<SystemSettingService> systemSettingServiceProvider) {
        this.chatBaseUrl = chatBaseUrl;
        this.defaultApiKey = defaultApiKey;
        this.chatModelName = chatModelName;
        this.systemSettingServiceProvider = systemSettingServiceProvider;
    }

    private synchronized ChatLanguageModel getDelegate() {
        String apiKey = defaultApiKey;
        String baseUrl = chatBaseUrl;
        String modelName = chatModelName;

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
            }
        } catch (Exception e) {
            System.err.println("Failed to load dynamic AI settings, using defaults: " + e.getMessage());
        }

        // Cache check: rebuild only if any of the dynamic properties have changed.
        String currentCacheKey = String.format("%s|%s|%s", apiKey, baseUrl, modelName);
        if (cachedModel != null && currentCacheKey.equals(cachedKey)) {
            return cachedModel;
        }

        cachedKey = currentCacheKey;
        cachedModel = OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .timeout(Duration.ofSeconds(60))
                .build();
        return cachedModel;
    }

    @Override
    public Response<AiMessage> generate(List<ChatMessage> messages) {
        return getDelegate().generate(messages);
    }

    @Override
    public Response<AiMessage> generate(List<ChatMessage> messages, List<ToolSpecification> toolSpecifications) {
        return getDelegate().generate(messages, toolSpecifications);
    }

    @Override
    public Response<AiMessage> generate(List<ChatMessage> messages, ToolSpecification toolSpecification) {
        return getDelegate().generate(messages, toolSpecification);
    }
}
