package com.example.backend.aspect;

import com.example.backend.annotation.Log;
import com.example.backend.entity.OperationLog;
import com.example.backend.entity.User;
import com.example.backend.service.OperationLogService;
import com.example.backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志切面
 */
@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private OperationLogService operationLogService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Pointcut("@annotation(com.example.backend.annotation.Log)")
    public void logPointCut() {}

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Exception exception = null;
        
        try {
            result = point.proceed();
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            long time = System.currentTimeMillis() - startTime;
            saveLog(point, result, exception, time);
        }
        
        return result;
    }

    private void saveLog(ProceedingJoinPoint point, Object result, Exception exception, long time) {
        try {
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            Log logAnnotation = method.getAnnotation(Log.class);
            
            OperationLog operationLog = new OperationLog();
            if (logAnnotation != null) {
                operationLog.setModule(logAnnotation.module());
                operationLog.setAction(logAnnotation.action());
                operationLog.setDescription(logAnnotation.description());
            }

            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                // Removed IP address logging as per requirement
                operationLog.setUserAgent(request.getHeader("User-Agent"));
                operationLog.setRequestMethod(request.getMethod());
                operationLog.setRequestUrl(request.getRequestURI());
                
                // 获取用户信息
                Long userId = (Long) request.getAttribute("userId");
                if (userId != null) {
                    operationLog.setUserId(userId);
                    User user = userService.getUserById(userId);
                    if (user != null) {
                        operationLog.setUsername(user.getUsername());
                    }
                }
            }

            // 特殊处理登录接口，从返回结果中获取用户信息
            if (operationLog.getUserId() == null && result != null && result instanceof com.example.backend.common.Result) {
                Object data = ((com.example.backend.common.Result<?>) result).getData();
                if (data instanceof com.example.backend.vo.LoginVO) {
                    com.example.backend.vo.LoginVO loginVO = (com.example.backend.vo.LoginVO) data;
                    operationLog.setUserId(loginVO.getId());
                    operationLog.setUsername(loginVO.getUsername());
                }
            }

            // 请求参数
            Object[] args = point.getArgs();
            try {
                // 对包含敏感 AI API 密钥的参数进行克隆与脱敏处理
                Object[] logArgs = args;
                if (args != null) {
                    logArgs = new Object[args.length];
                    boolean isUpdated = false;
                    for (int i = 0; i < args.length; i++) {
                        Object arg = args[i];
                        if (arg instanceof java.util.Map) {
                            java.util.Map<Object, Object> map = new java.util.HashMap<>((java.util.Map<?, ?>) arg);
                            if (map.containsKey("ai_api_key")) {
                                map.put("ai_api_key", "******");
                                isUpdated = true;
                            }
                            // 如果是 updateSetting(key, body) 形式，且 key 为 ai_api_key
                            boolean hasKey = false;
                            for (Object a : args) {
                                if ("ai_api_key".equals(a)) {
                                    hasKey = true;
                                    break;
                                }
                            }
                            if (hasKey && map.containsKey("value")) {
                                map.put("value", "******");
                                isUpdated = true;
                            }
                            logArgs[i] = map;
                        } else {
                            logArgs[i] = arg;
                        }
                    }
                    if (!isUpdated) {
                        logArgs = args;
                    }
                }

                // 过滤掉不能序列化的参数，如HttpServletRequest等
                String params = objectMapper.writeValueAsString(logArgs);
                
                // 正则兜底替换 (脱敏密码及敏感 API 密钥)
                if (params != null) {
                    if (params.contains("password")) {
                        params = params.replaceAll("\"password\"\\s*:\\s*\"[^\"]+\"", "\"password\":\"******\"");
                    }
                    if (params.contains("oldPassword")) {
                        params = params.replaceAll("\"oldPassword\"\\s*:\\s*\"[^\"]+\"", "\"oldPassword\":\"******\"");
                    }
                    if (params.contains("newPassword")) {
                        params = params.replaceAll("\"newPassword\"\\s*:\\s*\"[^\"]+\"", "\"newPassword\":\"******\"");
                    }
                    if (params.contains("ai_api_key")) {
                        params = params.replaceAll("\"ai_api_key\"\\s*:\\s*\"[^\"]+\"", "\"ai_api_key\":\"******\"");
                    }
                }
                
                // 截断过长的参数
                if (params.length() > 2000) {
                    params = params.substring(0, 2000) + "...";
                }
                operationLog.setRequestParams(params);
            } catch (Exception e) {
                // 忽略序列化错误
            }

            // 响应状态
            if (exception != null) {
                operationLog.setResponseStatus(500);
                String desc = operationLog.getDescription() == null ? "" : operationLog.getDescription();
                operationLog.setDescription(desc + " (Error: " + exception.getMessage() + ")");
            } else {
                operationLog.setResponseStatus(200);
            }
            
            operationLog.setExecutionTime((int) time);
            operationLog.setCreatedAt(LocalDateTime.now());
            
            operationLogService.log(operationLog);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
