package com.example.backend.vo;

import java.util.List;

/**
 * 分页结果封装类
 * 用于统一返回分页查询结果
 *
 * @param <T> 数据类型
 */
public class PageResult<T> {
    /** 当前页数据列表 */
    private List<T> list;
    /** 总记录数 */
    private int total;
    /** 当前页码 (1-based) */
    private int page;
    /** 每页大小 */
    private int size;
    /** 总页数 */
    private int totalPages;
    
    public PageResult() {}
    
    /**
     * 构造函数
     * 自动计算总页数
     *
     * @param list  当前页数据
     * @param total 总记录数
     * @param page  当前页码
     * @param size  每页大小
     */
    public PageResult(List<T> list, int total, int page, int size) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.size = size;
        this.totalPages = (int) Math.ceil((double) total / size);
    }
    
    // Getters and Setters
    public List<T> getList() {
        return list;
    }
    
    public void setList(List<T> list) {
        this.list = list;
    }
    
    public int getTotal() {
        return total;
    }
    
    public void setTotal(int total) {
        this.total = total;
    }
    
    public int getPage() {
        return page;
    }
    
    public void setPage(int page) {
        this.page = page;
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
