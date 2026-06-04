package com.example.backend.service;

import com.example.backend.dto.SupplierDTO;
import com.example.backend.vo.SupplierVO;
import java.util.List;

/**
 * 供应商服务接口
 */
public interface SupplierService {
    /**
     * 添加供应商
     * @param supplierDTO 供应商信息DTO
     */
    void addSupplier(SupplierDTO supplierDTO);

    /**
     * 更新供应商信息
     * @param supplierDTO 供应商信息DTO
     */
    void updateSupplier(SupplierDTO supplierDTO);

    /**
     * 删除供应商
     * @param id 供应商ID
     */
    void deleteSupplier(Long id);

    /**
     * 根据ID获取供应商信息
     * @param id 供应商ID
     * @return 供应商VO
     */
    SupplierVO getSupplierById(Long id);

    /**
     * 获取所有供应商列表
     * @return 供应商VO列表
     */
    List<SupplierVO> getAllSuppliers();
}
