package com.example.onlinestore.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 商品查询DTO，用于封装查询参数
 */
@Setter
@Getter
public class ItemQueryRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -4983112718912894206L;

    private Long categoryId;
    private String name;

    @Min(value = 1, message = "page must be greater than or equal to 1")
    private int page = 1;

    @Min(value = 1, message = "size must be greater than or equal to 1")
    @Max(value = 100, message = "size must be less than or equal to 100")
    private int size = 10;

}