package com.example.backend.service;

import com.example.backend.dto.LoginDTO;
import com.example.backend.dto.UserAddDTO;
import com.example.backend.vo.LoginVO;
import com.example.backend.entity.User;
import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {
    /**
     * 用户登录
     * @param loginDTO 登录信息
     * @return 登录结果
     */
    LoginVO login(LoginDTO loginDTO);
    
    /**
     * 用户登出
     * @param token 用户token
     */
    void logout(String token);

    /**
     * 新增用户
     * @param userAddDTO 用户信息
     */
    void addUser(UserAddDTO userAddDTO);

    /**
     * 更新用户
     * @param userAddDTO 用户信息
     */
    void updateUser(UserAddDTO userAddDTO);

    /**
     * 更新用户状态
     * @param id 用户ID
     * @param status 状态(1:启用, 0:禁用)
     */
    void updateUserStatus(Long id, Integer status);

    /**
     * 根据ID获取用户
     * @param id 用户ID
     * @return 用户信息
     */
    User getUserById(Long id);

    /**
     * 获取所有用户
     * @return 用户列表
     */
    List<User> getAllUsers();

    /**
     * 根据条件查询用户
     * @param name 用户名/昵称
     * @param role 角色
     * @return 用户列表
     */
    List<User> getUsers(String name, String role);

    /**
     * 删除用户
     * @param id 用户ID
     */
    void deleteUser(Long id);
}
