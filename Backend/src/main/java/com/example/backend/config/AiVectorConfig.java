package com.example.backend.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.redis.RedisEmbeddingStore;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import com.example.backend.service.WarehouseAiAssistant;
import com.example.backend.service.WarehouseTools;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.time.Duration;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@EnableAsync
public class AiVectorConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${redis.vector.store.index-name:item_index}")
    private String indexName;

    @Value("${redis.vector.store.dimension:384}")
    private int dimension;

    @Value("${langchain4j.open-ai.chat-model.base-url:https://api.openai.com/v1}")
    private String chatBaseUrl;

    @Value("${langchain4j.open-ai.chat-model.api-key:demo}")
    private String chatApiKey;

    @Value("${langchain4j.open-ai.chat-model.model-name:gpt-4o}")
    private String chatModelName;

    @Autowired
    private ObjectProvider<com.example.backend.service.SystemSettingService> systemSettingServiceProvider;

    @Bean
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        try {
            RedisEmbeddingStore.Builder builder = RedisEmbeddingStore.builder()
                    .host(redisHost)
                    .port(redisPort)
                    .indexName(indexName)
                    .dimension(dimension);
            
            if (redisPassword != null && !redisPassword.trim().isEmpty()) {
                builder.password(redisPassword);
            }
            
            return builder.build();
        } catch (Exception e) {
            System.err.println("WARNING: Failed to initialize RedisEmbeddingStore (e.g. Redis lacks RediSearch module or is offline). Falling back to InMemoryEmbeddingStore. Error: " + e.getMessage());
            return new dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore<>();
        }
    }

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return new DynamicChatLanguageModel(chatBaseUrl, chatApiKey, chatModelName, systemSettingServiceProvider);
    }

    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        return new DynamicStreamingChatLanguageModel(chatBaseUrl, chatApiKey, chatModelName, systemSettingServiceProvider);
    }

    @Bean
    public WarehouseAiAssistant warehouseAiAssistant(
            StreamingChatLanguageModel streamingChatLanguageModel,
            ChatLanguageModel chatLanguageModel,
            WarehouseTools warehouseTools,
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel,
            StringRedisTemplate redisTemplate) {
        
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(5)
                .minScore(0.6)
                .build();
        
        ChatMemoryStore chatMemoryStore = new RedisChatMemoryStore(redisTemplate);
        
        return AiServices.builder(WarehouseAiAssistant.class)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                .chatLanguageModel(chatLanguageModel)
                .tools(warehouseTools)
                .contentRetriever(contentRetriever)
                .chatMemoryProvider(userId -> MessageWindowChatMemory.builder()
                        .id(userId)
                        .maxMessages(10)
                        .chatMemoryStore(chatMemoryStore)
                        .build())
                .build();
    }
}
