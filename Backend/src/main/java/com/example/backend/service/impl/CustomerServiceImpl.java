package com.example.backend.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.example.backend.dao.CustomerMapper;
import com.example.backend.dto.CustomerDTO;
import com.example.backend.entity.Customer;
import com.example.backend.service.CustomerService;
import com.example.backend.vo.CustomerVO;

/**
 * 客户服务实现类
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerMapper customerMapper;

    /**
     * 添加新客户
     * 会检查客户名称是否已存在，如果存在则抛出异常
     * 同时会清空 'customers' 缓存
     *
     * @param customerDTO 包含新客户信息的DTO对象
     * @throws RuntimeException 如果客户名称已存在
     */
    @Override
    @CacheEvict(value = "customers", allEntries = true)
    public void addCustomer(CustomerDTO customerDTO) {
        if (customerMapper.countByName(customerDTO.getName(), null) > 0) {
            throw new RuntimeException("客户名称已存在");
        }
        Customer customer = new Customer();
        customer.setName(customerDTO.getName());
        customer.setContactPerson(customerDTO.getContactPerson());
        customer.setPhone(customerDTO.getPhone());
        customer.setPhone2(customerDTO.getPhone2());
        customer.setAddress(customerDTO.getAddress());
        customer.setCreatedAt(LocalDateTime.now());
        customerMapper.insert(customer);
    }

    /**
     * 更新现有客户信息
     * 会检查更新后的客户名称是否与其他客户冲突
     * 同时会清空 'customers' 缓存
     *
     * @param customerDTO 包含更新后客户信息的DTO对象，必须包含ID
     * @throws RuntimeException 如果客户名称与其他客户冲突
     */
    @Override
    @CacheEvict(value = "customers", allEntries = true)
    public void updateCustomer(CustomerDTO customerDTO) {
        if (customerMapper.countByName(customerDTO.getName(), customerDTO.getId()) > 0) {
            throw new RuntimeException("客户名称已存在");
        }
        Customer customer = customerMapper.selectById(customerDTO.getId());
        if (customer != null) {
            customer.setName(customerDTO.getName());
            customer.setContactPerson(customerDTO.getContactPerson());
            customer.setPhone(customerDTO.getPhone());
            customer.setPhone2(customerDTO.getPhone2());
            customer.setAddress(customerDTO.getAddress());
            customerMapper.update(customer);
        }
    }

    /**
     * 根据ID删除客户
     * 同时会清空 'customers' 缓存
     *
     * @param id 要删除的客户ID
     */
    @Override
    @CacheEvict(value = "customers", allEntries = true)
    public void deleteCustomer(Long id) {
        customerMapper.deleteById(id);
    }

    /**
     * 根据ID查询客户详情
     * 优先从缓存 'customers' 中获取，key为 'customer:' + id
     *
     * @param id 客户ID
     * @return 客户VO对象，如果未找到则返回null
     */
    @Override
    @Cacheable(value = "customers", key = "'customer:' + #id")
    public CustomerVO getCustomerById(Long id) {
        Customer customer = customerMapper.selectById(id);
        return convertToVO(customer);
    }

    /**
     * 获取所有客户列表
     * 优先从缓存 'customers' 中获取，key为 'all'
     *
     * @return 包含所有客户的VO列表
     */
    @Override
    @Cacheable(value = "customers", key = "'all'")
    public List<CustomerVO> getAllCustomers() {
        return customerMapper.selectAll().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 将 Customer 实体转换为 CustomerVO 对象
     *
     * @param customer Customer实体对象
     * @return CustomerVO对象，如果输入为null则返回null
     */
    private CustomerVO convertToVO(Customer customer) {
        if (customer == null)
            return null;
        CustomerVO vo = new CustomerVO();
        vo.setId(customer.getId());
        vo.setName(customer.getName());
        vo.setContactPerson(customer.getContactPerson());
        vo.setPhone(customer.getPhone());
        vo.setPhone2(customer.getPhone2());
        vo.setAddress(customer.getAddress());
        vo.setCreatedAt(customer.getCreatedAt());
        return vo;
    }
}
