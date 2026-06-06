package com.example.backend.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.example.backend.dao.SupplierMapper;
import com.example.backend.dto.SupplierDTO;
import com.example.backend.entity.Supplier;
import com.example.backend.service.SupplierService;
import com.example.backend.vo.SupplierVO;

/**
 * 供应商服务实现类
 */
@Service
public class SupplierServiceImpl implements SupplierService {

    @Autowired
    private SupplierMapper supplierMapper;

    /**
     * 添加新供应商
     * 检查供应商名称是否已存在
     * 清空 'suppliers' 缓存
     *
     * @param supplierDTO 供应商信息DTO
     * @throws RuntimeException 如果供应商名称已存在
     */
    @Override
    @CacheEvict(value = "suppliers", allEntries = true)
    public void addSupplier(SupplierDTO supplierDTO) {
        if (supplierMapper.countByName(supplierDTO.getName(), null) > 0) {
            throw new RuntimeException("供应商名称已存在");
        }
        Supplier supplier = new Supplier();
        supplier.setName(supplierDTO.getName());
        supplier.setContactPerson(supplierDTO.getContactPerson());
        supplier.setPhone(supplierDTO.getPhone());
        supplier.setPhone2(supplierDTO.getPhone2());
        supplier.setAddress(supplierDTO.getAddress());
        supplier.setCreatedAt(LocalDateTime.now());
        supplierMapper.insert(supplier);
    }

    /**
     * 更新供应商信息
     * 检查名称是否与其他供应商冲突
     * 清空 'suppliers' 缓存
     *
     * @param supplierDTO 供应商信息DTO，必须包含ID
     * @throws RuntimeException 如果供应商名称冲突
     */
    @Override
    @CacheEvict(value = "suppliers", allEntries = true)
    public void updateSupplier(SupplierDTO supplierDTO) {
        if (supplierMapper.countByName(supplierDTO.getName(), supplierDTO.getId()) > 0) {
            throw new RuntimeException("供应商名称已存在");
        }
        Supplier supplier = supplierMapper.selectById(supplierDTO.getId());
        if (supplier != null) {
            supplier.setName(supplierDTO.getName());
            supplier.setContactPerson(supplierDTO.getContactPerson());
            supplier.setPhone(supplierDTO.getPhone());
            supplier.setPhone2(supplierDTO.getPhone2());
            supplier.setAddress(supplierDTO.getAddress());
            supplierMapper.update(supplier);
        }
    }

    /**
     * 根据ID删除供应商
     * 清空 'suppliers' 缓存
     *
     * @param id 供应商ID
     */
    @Override
    @CacheEvict(value = "suppliers", allEntries = true)
    public void deleteSupplier(Long id) {
        supplierMapper.deleteById(id);
    }

    /**
     * 根据ID获取供应商详情
     * 优先从缓存 'suppliers' 中获取，key为 'supplier:' + id
     *
     * @param id 供应商ID
     * @return 供应商VO对象
     */
    @Override
    @Cacheable(value = "suppliers", key = "'supplier:' + #id")
    public SupplierVO getSupplierById(Long id) {
        Supplier supplier = supplierMapper.selectById(id);
        return convertToVO(supplier);
    }

    /**
     * 获取所有供应商列表
     * 优先从缓存 'suppliers' 中获取，key为 'all'
     *
     * @return 所有供应商VO列表
     */
    @Override
    @Cacheable(value = "suppliers", key = "'all'")
    public List<SupplierVO> getAllSuppliers() {
        return supplierMapper.selectAll().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 将 Supplier 实体转换为 SupplierVO 对象
     *
     * @param supplier Supplier实体对象
     * @return SupplierVO对象
     */
    private SupplierVO convertToVO(Supplier supplier) {
        if (supplier == null)
            return null;
        SupplierVO vo = new SupplierVO();
        vo.setId(supplier.getId());
        vo.setName(supplier.getName());
        vo.setContactPerson(supplier.getContactPerson());
        vo.setPhone(supplier.getPhone());
        vo.setPhone2(supplier.getPhone2());
        vo.setAddress(supplier.getAddress());
        vo.setCreatedAt(supplier.getCreatedAt());
        return vo;
    }
}
