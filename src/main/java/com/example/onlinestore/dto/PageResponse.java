package com.example.onlinestore.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分页响应类
 */
@Setter
@Getter
public class PageResponse<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = -7246762671705038818L;

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

}