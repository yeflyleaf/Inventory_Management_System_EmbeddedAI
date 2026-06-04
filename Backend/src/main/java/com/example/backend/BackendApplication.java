package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 库存管理系统后端启动类
 * 1. @SpringBootApplication: 标记为Spring Boot应用，开启自动配置、组件扫描
 * 2. @EnableScheduling: 开启定时任务支持 (用于文件清理等任务)
 */
@SpringBootApplication
@EnableScheduling
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}
