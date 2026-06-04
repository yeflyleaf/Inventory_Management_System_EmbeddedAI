package com.example.backend.controller;

import com.example.backend.annotation.Log;
import com.example.backend.common.Result;
import com.example.backend.dto.LoginDTO;
import com.example.backend.dto.UserAddDTO;
import com.example.backend.service.UserService;
import com.example.backend.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 提供用户登录、注册及退出功能
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     * @param loginDTO 登录信息
     * @return 登录结果(包含Token)
     */
    @PostMapping("/login")
    @Log(module = "认证", action = "登录", description = "用户登录")
    public Result<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = userService.login(loginDTO);
        return Result.success(loginVO, "登录成功");
    }

    /**
     * 用户退出
     * @param token 认证Token
     * @return 成功信息
     */
    @PostMapping("/logout")
    @Log(module = "认证", action = "退出", description = "用户退出")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        // Remove "Bearer " prefix if present
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        userService.logout(token);
        return Result.success(null, "退出成功");
    }

    /**
     * 用户注册
     * @param userAddDTO 注册信息
     * @return 成功信息
     */
    @PostMapping("/register")
    @Log(module = "认证", action = "注册", description = "用户注册")
    public Result<Void> register(@RequestBody UserAddDTO userAddDTO) {
        userService.addUser(userAddDTO);
        return Result.success(null, "注册成功");
    }
}
