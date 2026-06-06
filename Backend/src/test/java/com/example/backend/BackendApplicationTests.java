package com.example.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.example.backend.service.WarehouseAiAssistant;
import dev.langchain4j.service.TokenStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class BackendApplicationTests {

    @Autowired
    private WarehouseAiAssistant aiAssistant;

    @Test
    void contextLoads() {
        System.out.println("=== Starting AI Assistant test ===");
        TokenStream tokenStream = aiAssistant.chat("test-user-id", "哪些商品处于低库存状态？");
        StringBuilder sb = new StringBuilder();
        CompletableFuture<String> future = new CompletableFuture<>();
        
        tokenStream
            .onNext(token -> {
                System.out.print(token);
                sb.append(token);
            })
            .onComplete(response -> future.complete(sb.toString()))
            .onError(error -> future.completeExceptionally(error))
            .start();
            
        try {
            String result = future.get(30, TimeUnit.SECONDS);
            System.out.println("\n=== AI TEST SUCCESS ===");
            System.out.println(result);
        } catch (Exception e) {
            System.out.println("\n=== AI TEST FAILURE ===");
            System.out.println("Exception: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

}
