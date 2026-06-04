package com.example.backend.config;

import com.example.backend.interceptor.AuthInterceptor;
import com.example.backend.interceptor.AdminInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;
    
    @Autowired
    private AdminInterceptor adminInterceptor;

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 认证拦截器 - 验证用户登录状态
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/**", "/error", "/uploads/**", "/system/**");
        
        // 管理员权限拦截器 - 验证管理员权限（需要在认证拦截器之后）
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**")
                .order(1); // 确保在认证拦截器之后执行
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源映射，使上传的图片可以通过 /uploads/** 访问
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}
