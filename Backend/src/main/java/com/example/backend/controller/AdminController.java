package com.example.backend.controller;

import com.example.backend.annotation.AdminRequired;
import com.example.backend.annotation.Log;
import com.example.backend.common.Result;
import com.example.backend.dto.LogQueryDTO;
import com.example.backend.dto.UserAddDTO;
import com.example.backend.entity.OperationLog;
import com.example.backend.entity.SystemSetting;
import com.example.backend.entity.User;
import com.example.backend.service.OperationLogService;
import com.example.backend.service.SystemSettingService;
import com.example.backend.service.UserService;
import com.example.backend.vo.AdminDashboardVO;
import com.example.backend.vo.PageResult;
import com.example.backend.vo.ProductVO;
import com.example.backend.vo.StockVO;
import com.example.backend.service.ProductService;
import com.example.backend.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 管理员控制器
 * 所有接口都需要管理员权限
 */
@RestController
@RequestMapping("/admin")
@CrossOrigin
@AdminRequired  // 类级别注解，该控制器下所有接口都需要管理员权限
public class AdminController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private OperationLogService operationLogService;
    
    @Autowired
    private SystemSettingService systemSettingService;

    @Autowired
    private ProductService productService;

    @Autowired
    private StockService stockService;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== 仪表盘 ====================
    
    /**
     * 获取管理员仪表盘数据
     */
    @GetMapping("/dashboard")
    public Result<AdminDashboardVO> getDashboard() {
        AdminDashboardVO dashboard = new AdminDashboardVO();
        
        // 用户统计
        List<User> allUsers = userService.getAllUsers();
        dashboard.setTotalUsers(allUsers.size());
        dashboard.setActiveUsers((int) allUsers.stream().filter(u -> u.getStatus() == 1).count());
        
        // 今日操作统计
        dashboard.setTotalOperationsToday(operationLogService.countToday());
        
        // 商品统计
        List<ProductVO> allProducts = productService.getAllProducts();
        dashboard.setTotalProducts(allProducts.size());
        
        // 低库存统计 (从系统设置获取阈值，默认为10)
        int lowStockThreshold = systemSettingService.getIntValue("low_stock_threshold", 10);
        
        List<StockVO> stockSnapshot = stockService.getStockSnapshot(null);
        Map<Long, Integer> stockMap = stockSnapshot.stream()
            .collect(Collectors.toMap(StockVO::getItemId, StockVO::getCurrentStock));
            
        long lowStockCount = allProducts.stream()
            .mapToInt(p -> stockMap.getOrDefault(p.getId(), 0))
            .filter(stock -> stock > 0 && stock < lowStockThreshold)
            .count();
            
        long outOfStockCount = allProducts.stream()
            .mapToInt(p -> stockMap.getOrDefault(p.getId(), 0))
            .filter(stock -> stock == 0)
            .count();
            
        dashboard.setLowStockProducts((int) lowStockCount);
        dashboard.setOutOfStockProducts((int) outOfStockCount);
        
        // 系统版本
        dashboard.setSystemVersion("2.0.0");
        
        return Result.success(dashboard);
    }

    // ==================== 用户管理 ====================
    
    /**
     * 获取所有用户列表
     */
    @GetMapping("/users")
    public Result<List<User>> getAllUsers(@RequestParam(required = false) String name, 
                                          @RequestParam(required = false) String role) {
        List<User> users = userService.getUsers(name, role);
        // 隐藏密码
        users.forEach(u -> u.setPassword(null));
        return Result.success(users);
    }
    
    /**
     * 获取单个用户详情
     */
    @GetMapping("/users/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }
    
    /**
     * 创建新用户
     */
    @PostMapping("/users")
    @Log(module = "用户管理", action = "新增用户", description = "创建新用户")
    public Result<Void> createUser(@RequestBody UserAddDTO userAddDTO) {
        userService.addUser(userAddDTO);
        return Result.success(null, "用户创建成功");
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/users/{id}")
    @Log(module = "用户管理", action = "更新用户", description = "更新用户信息")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody UserAddDTO userAddDTO) {
        userAddDTO.setId(id);
        userService.updateUser(userAddDTO);
        return Result.success(null, "用户更新成功");
    }
    
    /**
     * 更新用户状态（启用/禁用）
     */
    @PutMapping("/users/{id}/status")
    @Log(module = "用户管理", action = "更新状态", description = "更新用户状态")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        userService.updateUserStatus(id, status);
        String statusText = (status != null && status == 1) ? "启用" : "禁用";
        return Result.success(null, "用户已" + statusText);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/users/{id}")
    @Log(module = "用户管理", action = "删除用户", description = "删除用户")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success(null, "用户已删除");
    }

    // ==================== 操作日志 ====================
    
    /**
     * 查询操作日志
     */
    @GetMapping("/logs")
    public Result<PageResult<OperationLog>> getLogs(LogQueryDTO queryDTO) {
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (queryDTO.getStartTime() != null && !queryDTO.getStartTime().isEmpty()) {
            startTime = LocalDateTime.parse(queryDTO.getStartTime(), formatter);
        }
        if (queryDTO.getEndTime() != null && !queryDTO.getEndTime().isEmpty()) {
            endTime = LocalDateTime.parse(queryDTO.getEndTime(), formatter);
        }
        
        PageResult<OperationLog> result = operationLogService.findByPage(
            queryDTO.getUserId(),
            queryDTO.getUsername(),
            queryDTO.getModule(),
            queryDTO.getAction(),
            startTime,
            endTime,
            queryDTO.getPage(),
            queryDTO.getSize()
        );
        
        return Result.success(result);
    }
    
    /**
     * 获取最近操作日志
     */
    @GetMapping("/logs/recent")
    public Result<List<OperationLog>> getRecentLogs(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(operationLogService.findRecent(limit));
    }
    
    /**
     * 获取所有日志模块
     */
    @GetMapping("/logs/modules")
    public Result<List<String>> getLogModules() {
        return Result.success(operationLogService.findAllModules());
    }
    
    /**
     * 清理旧日志
     */
    @DeleteMapping("/logs/clean")
    @Log(module = "系统管理", action = "清理日志", description = "清理旧操作日志")
    public Result<Integer> cleanOldLogs(@RequestParam(defaultValue = "90") int keepDays) {
        int count = operationLogService.cleanOldLogs(keepDays);
        return Result.success(count, "已清理 " + count + " 条旧日志");
    }

    // ==================== 系统设置 ====================
    
    /**
     * 获取所有系统设置
     */
    @GetMapping("/settings")
    public Result<List<SystemSetting>> getAllSettings() {
        return Result.success(systemSettingService.findAll());
    }
    
    /**
     * 获取系统设置（Map形式）
     */
    @GetMapping("/settings/map")
    public Result<Map<String, String>> getSettingsAsMap() {
        return Result.success(systemSettingService.getAllAsMap());
    }
    
    /**
     * 更新单个设置
     */
    @PutMapping("/settings/{key}")
    @Log(module = "系统设置", action = "更新设置", description = "更新单个系统设置")
    public Result<Void> updateSetting(@PathVariable String key, @RequestBody Map<String, String> body) {
        String value = body.get("value");
        systemSettingService.updateValue(key, value);
        return Result.success(null, "设置已更新");
    }
    
    /**
     * 批量更新设置
     */
    @PutMapping("/settings")
    public Result<Void> batchUpdateSettings(@RequestBody Map<String, String> settings, HttpServletRequest request) {
        // 获取当前设置用于对比
        Map<String, String> currentSettings = systemSettingService.getAllAsMap();
        
        // 定义日志动作映射
        Map<String, String> actionMap = Map.of(
            "company_name", "修改公司名称",
            "company_phone", "修改联系电话",
            "company_email", "修改公司邮箱",
            "company_address", "修改公司地址",
            "low_stock_threshold", "修改低库存预警阈值",
            "inventory_backlog_days", "修改库存积压天数",
            "allow_negative_stock", "修改允许负库存",
            "order_prefix_purchase", "修改采购单编号前缀",
            "order_prefix_sales", "修改销售单编号前缀"
        );

        Long userId = (Long) request.getAttribute("userId");
        String username = null;
        if (userId != null) {
            User user = userService.getUserById(userId);
            if (user != null) {
                username = user.getUsername();
            }
        }

        for (Map.Entry<String, String> entry : settings.entrySet()) {
            String key = entry.getKey();
            String newValue = entry.getValue();
            String oldValue = currentSettings.get(key);

            // 对比值是否发生变化
            if (!Objects.equals(oldValue, newValue)) {
                String action = actionMap.getOrDefault(key, "修改系统设置");
                String description = String.format("%s: 从 '%s' 修改为 '%s'", action, oldValue == null ? "" : oldValue, newValue);
                
                // 创建日志
                OperationLog log = new OperationLog();
                log.setModule("系统设置");
                log.setAction(action);
                log.setDescription(description);
                log.setUserId(userId);
                log.setUsername(username);
                log.setRequestMethod(request.getMethod());
                log.setRequestUrl(request.getRequestURI());
                log.setResponseStatus(200);
                log.setCreatedAt(LocalDateTime.now());
                log.setExecutionTime(0);
                
                try {
                    // 记录变更的参数
                    Map<String, String> param = Map.of(key, newValue);
                    log.setRequestParams(objectMapper.writeValueAsString(param));
                } catch (Exception e) {
                    log.setRequestParams(key + "=" + newValue);
                }

                operationLogService.log(log);
            }
        }

        systemSettingService.batchUpdate(settings);
        return Result.success(null, "设置已批量更新");
    }
}
