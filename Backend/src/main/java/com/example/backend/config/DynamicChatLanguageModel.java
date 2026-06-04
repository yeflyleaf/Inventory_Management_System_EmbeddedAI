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
    private final Double chatTemperature;
    private final ObjectProvider<SystemSettingService> systemSettingServiceProvider;

    private volatile String cachedKey = null;
    private volatile ChatLanguageModel cachedModel = null;

    public DynamicChatLanguageModel(String chatBaseUrl, String defaultApiKey, String chatModelName, 
                                    Double chatTemperature, ObjectProvider<SystemSettingService> systemSettingServiceProvider) {
        this.chatBaseUrl = chatBaseUrl;
        this.defaultApiKey = defaultApiKey;
        this.chatModelName = chatModelName;
        this.chatTemperature = chatTemperature;
        this.systemSettingServiceProvider = systemSettingServiceProvider;
    }

    private synchronized ChatLanguageModel getDelegate() {
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
            System.err.println("Failed to load or decrypt dynamic AI API key, using default: " + e.getMessage());
        }

        // 缓存判断，如果密钥没变，则复用已创建的底层模型
        if (cachedModel != null && apiKey.equals(cachedKey)) {
            return cachedModel;
        }

        cachedKey = apiKey;
        cachedModel = OpenAiChatModel.builder()
                .baseUrl(chatBaseUrl)
                .apiKey(apiKey)
                .modelName(chatModelName)
                .temperature(chatTemperature)
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
