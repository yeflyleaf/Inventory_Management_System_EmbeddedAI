package com.example.backend.controller;

import com.example.backend.annotation.Log;
import com.example.backend.common.Result;
import com.example.backend.dto.CustomerDTO;
import com.example.backend.service.CustomerService;
import com.example.backend.vo.CustomerVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 客户管理控制器
 * 提供客户的增删改查功能
 */
@RestController
@RequestMapping("/customers")
@CrossOrigin
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    /**
     * 获取所有客户列表
     * @return 客户列表
     */
    @GetMapping
    public Result<List<CustomerVO>> getAll() {
        List<CustomerVO> customers = customerService.getAllCustomers();
        return Result.success(customers, "获取客户列表成功");
    }

    /**
     * 新增客户
     * @param customerDTO 客户信息
     * @return 成功信息
     */
    @PostMapping
    @Log(module = "客户管理", action = "新增客户", description = "新增客户信息")
    public Result<Void> add(@RequestBody CustomerDTO customerDTO) {
        customerService.addCustomer(customerDTO);
        return Result.success(null, "添加客户成功");
    }

    /**
     * 更新客户信息
     * @param customerDTO 客户信息
     * @return 成功信息
     */
    @PutMapping
    @Log(module = "客户管理", action = "更新客户", description = "更新客户信息")
    public Result<Void> update(@RequestBody CustomerDTO customerDTO) {
        customerService.updateCustomer(customerDTO);
        return Result.success(null, "更新客户成功");
    }

    /**
     * 删除客户
     * @param id 客户ID
     * @return 成功信息
     */
    @DeleteMapping("/{id}")
    @Log(module = "客户管理", action = "删除客户", description = "删除客户信息")
    public Result<Void> delete(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return Result.success(null, "删除客户成功");
    }
}
