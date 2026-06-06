package com.example.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import java.lang.reflect.Method;
import dev.langchain4j.store.embedding.redis.RedisEmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStore;

@SpringBootTest
class BackendApplicationTests {

    @Test
    void contextLoads() {
        System.out.println("=== RedisEmbeddingStore Methods ===");
        for (Method method : RedisEmbeddingStore.class.getDeclaredMethods()) {
            System.out.println(method.toString());
        }
        System.out.println("=== EmbeddingStore Methods ===");
        for (Method method : EmbeddingStore.class.getDeclaredMethods()) {
            System.out.println(method.toString());
        }
    }

}
