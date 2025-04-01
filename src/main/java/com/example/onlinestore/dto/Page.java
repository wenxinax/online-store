package com.example.onlinestore.dto;

import jakarta.validation.Valid;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分页响应类
 */
@Setter
@Getter
@EqualsAndHashCode
@ToString
public class Page<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = -7246762671705038818L;

    private List<T> items;
    private long total;
    private int pageNum;
    private int pageSize;
    
    public Page() {
    }
    
    public Page(List<T> items, long total, int pageNum, int pageSize) {
        this.items = items;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }
    
    public static <T> Page<T> of(List<T> items, long total, int pageNum, int pageSize) {
        if (items == null) {
            items = List.of();
        }
        return new Page<>(items, total, pageNum, pageSize);
    }

}