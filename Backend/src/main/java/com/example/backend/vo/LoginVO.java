package com.example.backend.vo;

import lombok.Data;

/**
 * 登录响应VO
 * 包含用户基本信息和JWT Token
 */
@Data
public class LoginVO {
    /** 用户ID */
    private Long id;
    /** 用户名 */
    private String username;
    /** 昵称 */
    private String nickname;
    /** 角色 (ADMIN/USER) */
    private String role;
    /** JWT Token */
    private String token;
    /** 头像URL */
    private String avatar;
    /** 电子邮箱 */
    private String email;
    /** 电话号码 */
    private String phone;
}
