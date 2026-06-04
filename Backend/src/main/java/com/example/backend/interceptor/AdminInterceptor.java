package com.example.backend.interceptor;

import com.example.backend.annotation.AdminRequired;
import com.example.backend.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员权限拦截器
 * 检查被@AdminRequired注解标记的方法或类，验证用户是否具有管理员权限
 */
@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行OPTIONS请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 如果不是HandlerMethod，放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        // 检查方法或类上是否有@AdminRequired注解
        boolean isAdminRequired = handlerMethod.hasMethodAnnotation(AdminRequired.class) ||
                                   handlerMethod.getBeanType().isAnnotationPresent(AdminRequired.class);
        
        if (!isAdminRequired) {
            return true; // 没有管理员权限要求，放行
        }

        // 获取Token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 验证管理员权限
        if (token != null && jwtUtils.isAdmin(token)) {
            // 存储角色信息到request属性
            request.setAttribute("userRole", "ADMIN");
            return true;
        }

        // 权限不足，返回403
        sendForbiddenResponse(response, "权限不足，需要管理员权限");
        return false;
    }
    
    /**
     * 发送403响应
     */
    private void sendForbiddenResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 403);
        result.put("message", message);
        result.put("data", null);
        
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
