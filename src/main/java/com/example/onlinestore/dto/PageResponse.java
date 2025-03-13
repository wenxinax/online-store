package com.example.onlinestore.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分页响应类
 */
public class PageResponse<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private List<T> items;
    private long total;
    private int pageNum;
    private int pageSize;
    
    public PageResponse() {
    }
    
    public PageResponse(List<T> items, long total, int pageNum, int pageSize) {
        this.items = items;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }
    
    public static <T> PageResponse<T> of(List<T> items, long total, int pageNum, int pageSize) {
        return new PageResponse<>(items, total, pageNum, pageSize);
    }
    
    public List<T> getItems() {
        return items;
    }
    
    public void setItems(List<T> items) {
        this.items = items;
    }
    
    public long getTotal() {
        return total;
    }
    
    public void setTotal(long total) {
        this.total = total;
    }
    
    public int getPageNum() {
        return pageNum;
    }
    
    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
} 