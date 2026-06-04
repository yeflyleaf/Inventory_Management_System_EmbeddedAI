package com.example.backend.controller;

import com.example.backend.annotation.Log;
import com.example.backend.common.Result;
import com.example.backend.entity.User;
import com.example.backend.dao.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * 用户个人资料控制器
 * 用于用户更新自己的资料
 */
@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserProfileController {

    @Autowired
    private UserMapper userMapper;

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    /**
     * 删除旧头像文件
     */
    private void deleteOldAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return;
        }
        
        try {
            // 从URL中提取文件名，如 "/uploads/avatars/xxx.jpg" -> "avatars/xxx.jpg"
            if (avatarUrl.startsWith("/uploads/")) {
                String relativePath = avatarUrl.substring("/uploads/".length());
                Path filePath = Paths.get(uploadPath, relativePath);
                
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    System.out.println("已删除旧头像文件: " + filePath);
                }
            }
        } catch (IOException e) {
            System.err.println("删除旧头像文件失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户头像
     */
    @PutMapping("/avatar")
    @Log(module = "个人中心", action = "更新头像", description = "用户更新头像")
    public Result<Void> updateAvatar(@RequestBody Map<String, String> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("用户未登录");
        }

        String avatarUrl = body.get("avatar");
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return Result.error("头像URL不能为空");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 删除旧头像文件
        deleteOldAvatar(user.getAvatar());

        user.setAvatar(avatarUrl);
        userMapper.update(user);

        return Result.success(null, "头像更新成功");
    }

    /**
     * 更新用户个人资料
     */
    @PutMapping("/profile")
    @Log(module = "个人中心", action = "更新资料", description = "用户更新个人资料")
    public Result<User> updateProfile(@RequestBody Map<String, String> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("用户未登录");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 只允许更新部分字段
        if (body.containsKey("email")) {
            user.setEmail(body.get("email"));
        }
        if (body.containsKey("phone")) {
            user.setPhone(body.get("phone"));
        }
        if (body.containsKey("nickname")) {
            user.setNickname(body.get("nickname"));
        }
        if (body.containsKey("avatar")) {
            String newAvatar = body.get("avatar");
            String oldAvatar = user.getAvatar();
            
            // 如果头像有变化，删除旧头像文件
            if (newAvatar != null && !newAvatar.equals(oldAvatar)) {
                deleteOldAvatar(oldAvatar);
            }
            
            user.setAvatar(newAvatar);
        }

        userMapper.update(user);

        // 返回更新后的用户信息（隐藏密码）
        user.setPassword(null);
        return Result.success(user, "资料更新成功");
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    @Log(module = "个人中心", action = "修改密码", description = "用户修改登录密码")
    public Result<Void> changePassword(@RequestBody Map<String, String> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("用户未登录");
        }

        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        if (oldPassword == null || oldPassword.isEmpty()) {
            return Result.error("当前密码不能为空");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            return Result.error("新密码不能为空");
        }
        if (newPassword.length() < 6) {
            return Result.error("新密码长度不能少于6位");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 验证当前密码是否正确
        if (!user.getPassword().equals(oldPassword)) {
            return Result.error("当前密码不正确");
        }

        // 更新密码
        userMapper.updatePassword(userId, newPassword);

        return Result.success(null, "密码修改成功");
    }
}
