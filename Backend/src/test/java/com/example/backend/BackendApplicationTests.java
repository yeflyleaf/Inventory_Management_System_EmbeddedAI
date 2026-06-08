package com.example.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.example.backend.service.SystemSettingService;
import com.example.backend.entity.SystemSetting;
import java.util.List;

@SpringBootTest
class BackendApplicationTests {

    @Autowired
    private SystemSettingService systemSettingService;

    @Test
    void contextLoads() {
        System.out.println("================ SYSTEM SETTINGS DEVIATION ================");
        List<SystemSetting> settings = systemSettingService.findAll();
        for (SystemSetting s : settings) {
            System.out.println(s.getSettingKey() + ": " + s.getSettingValue());
        }
        System.out.println("==========================================================");
    }

}
