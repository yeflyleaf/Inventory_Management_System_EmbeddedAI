package com.example.backend.dto;

import lombok.Data;

/**
 * 用户添加/更新DTO
 */
@Data
public class UserAddDTO {
    /**
     * 用户ID (更新时必填)
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码 (新增时必填)
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 角色 (admin/user)
     */
    private String role;

    /**
     * 状态 (1:启用, 0:禁用)
     */
    private Integer status;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;
}
