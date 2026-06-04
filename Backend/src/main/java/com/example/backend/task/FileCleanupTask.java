package com.example.backend.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

/**
 * 文件清理定时任务
 * 用于定期清理系统中的临时文件，维护磁盘空间
 */
@Component
public class FileCleanupTask {

    private static final Logger logger = LoggerFactory.getLogger(FileCleanupTask.class);

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    /**
     * 定时清理临时文件任务
     * 执行频率：每天凌晨 3 点 (Cron: 0 0 3 * * ?)
     * 清理规则：扫描 'temp' 目录，删除最后修改时间超过 24 小时的文件
     * 目的：防止临时文件无限堆积占用服务器存储空间
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupTempFiles() {
        logger.info("开始执行临时文件清理任务...");
        
        Path tempDir = Paths.get(uploadPath, "temp");
        
        if (!Files.exists(tempDir)) {
            logger.info("临时目录不存在，无需清理");
            return;
        }

        try (Stream<Path> files = Files.list(tempDir)) {
            files.forEach(path -> {
                try {
                    if (Files.isRegularFile(path)) {
                        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
                        Instant lastModifiedTime = attrs.lastModifiedTime().toInstant();
                        Instant twentyFourHoursAgo = Instant.now().minus(24, ChronoUnit.HOURS);

                        if (lastModifiedTime.isBefore(twentyFourHoursAgo)) {
                            Files.delete(path);
                            logger.info("已删除过期临时文件: {}", path.getFileName());
                        }
                    }
                } catch (IOException e) {
                    logger.error("处理文件失败: {}", path, e);
                }
            });
        } catch (IOException e) {
            logger.error("遍历临时目录失败", e);
        }
        
        logger.info("临时文件清理任务完成");
    }
}
