package com.example.backend.service;

import com.example.backend.dto.CustomerDTO;
import com.example.backend.vo.CustomerVO;
import java.util.List;

/**
 * 客户服务接口
 */
public interface CustomerService {
    /**
     * 添加客户
     * @param customerDTO 客户信息DTO
     */
    void addCustomer(CustomerDTO customerDTO);

    /**
     * 更新客户信息
     * @param customerDTO 客户信息DTO
     */
    void updateCustomer(CustomerDTO customerDTO);

    /**
     * 删除客户
     * @param id 客户ID
     */
    void deleteCustomer(Long id);

    /**
     * 根据ID获取客户信息
     * @param id 客户ID
     * @return 客户VO
     */
    CustomerVO getCustomerById(Long id);

    /**
     * 获取所有客户列表
     * @return 客户VO列表
     */
    List<CustomerVO> getAllCustomers();
}
