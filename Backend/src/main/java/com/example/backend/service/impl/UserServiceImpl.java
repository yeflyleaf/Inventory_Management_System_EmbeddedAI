package com.example.backend.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.backend.dao.UserMapper;
import com.example.backend.dto.LoginDTO;
import com.example.backend.dto.UserAddDTO;
import com.example.backend.entity.User;
import com.example.backend.service.UserService;
import com.example.backend.utils.JwtUtils;
import com.example.backend.vo.LoginVO;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户登录处理
     * 1. 根据用户名查询用户
     * 2. 验证密码是否匹配
     * 3. 检查用户状态是否被禁用
     * 4. 更新最后登录时间
     * 5. 生成JWT Token
     *
     * @param loginDTO 登录请求DTO，包含用户名和密码
     * @return 登录成功VO，包含用户信息和Token
     * @throws RuntimeException 如果用户名密码错误或账号被禁用
     */
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        User user = userMapper.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        // 兼容模式：先尝试 BCrypt 校验，失败后回退到明文比对（用于旧数据迁移过渡）
        boolean passwordMatch;
        if (user.getPassword().startsWith("$2a$") || user.getPassword().startsWith("$2b$")) {
            passwordMatch = passwordEncoder.matches(loginDTO.getPassword(), user.getPassword());
        } else {
            // 旧明文密码：比对后自动升级为 BCrypt 哈希
            passwordMatch = user.getPassword().equals(loginDTO.getPassword());
            if (passwordMatch) {
                user.setPassword(passwordEncoder.encode(loginDTO.getPassword()));
                userMapper.update(user);
            }
        }
        if (!passwordMatch) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("该账号已被禁用，请联系管理员");
        }
        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.update(user);

        LoginVO vo = new LoginVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setRole(user.getRole());
        vo.setAvatar(user.getAvatar());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        // 生成Token时传递角色信息
        vo.setToken(jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole()));
        return vo;
    }

    /**
     * 用户登出
     * 将Token加入黑名单或从Redis中移除
     *
     * @param token 用户Token
     */
    @Override
    public void logout(String token) {
        jwtUtils.removeToken(token);
    }

    /**
     * 新增用户
     * 创建新用户记录，设置默认状态为1 (启用)
     * 清空 'users' 缓存
     *
     * @param userAddDTO 用户添加DTO
     */
    @Override
    @CacheEvict(value = "users", allEntries = true)
    public void addUser(UserAddDTO userAddDTO) {
        User user = new User();
        user.setUsername(userAddDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userAddDTO.getPassword()));
        user.setNickname(userAddDTO.getNickname());
        user.setRole(userAddDTO.getRole());
        user.setPhone(userAddDTO.getPhone());
        user.setEmail(userAddDTO.getEmail());
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        userMapper.insert(user);
    }

    /**
     * 更新用户信息
     * 更新用户的基本信息 (昵称、角色、电话、邮箱)
     * 清空 'users' 缓存
     *
     * @param userAddDTO 用户更新DTO
     */
    @Override
    @CacheEvict(value = "users", allEntries = true)
    public void updateUser(UserAddDTO userAddDTO) {
        User user = userMapper.selectById(userAddDTO.getId());
        if (user != null) {
            user.setNickname(userAddDTO.getNickname());
            user.setRole(userAddDTO.getRole());
            user.setPhone(userAddDTO.getPhone());
            user.setEmail(userAddDTO.getEmail());
            userMapper.update(user);
        }
    }

    /**
     * 更新用户状态
     * 启用或禁用用户账号
     * 清空 'users' 缓存
     *
     * @param id     用户ID
     * @param status 状态值 (1: 启用, 0: 禁用)
     */
    @Override
    @CacheEvict(value = "users", allEntries = true)
    public void updateUserStatus(Long id, Integer status) {
        userMapper.updateStatus(id, status);
    }

    /**
     * 根据ID获取用户详情
     * 优先从缓存 'users' 中获取，key为 'user:' + id
     *
     * @param id 用户ID
     * @return 用户实体对象
     */
    @Override
    @Cacheable(value = "users", key = "'user:' + #id")
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    /**
     * 获取所有用户列表
     * 优先从缓存 'users' 中获取，key为 'all'
     *
     * @return 所有用户列表
     */
    @Override
    @Cacheable(value = "users", key = "'all'")
    public List<User> getAllUsers() {
        return userMapper.selectAll();
    }

    /**
     * 根据条件查询用户
     * 支持按用户名(模糊)和角色筛选
     *
     * @param name 用户名 (可选)
     * @param role 角色 (可选)
     * @return 符合条件的用户列表
     */
    @Override
    public List<User> getUsers(String name, String role) {
        return userMapper.selectByCondition(name, role);
    }

    /**
     * 删除用户
     * 物理删除用户记录
     * 清空 'users' 缓存
     *
     * @param id 用户ID
     */
    @Override
    @CacheEvict(value = "users", allEntries = true)
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }
}
