package com.example.onlinestore.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

// 商品表
@Setter
@Getter
public class ItemEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 2418723127713742396L;

    private Long id;
    private Long categoryId;
    private String name;
    private String secondaryName;
    private String description;
    private String image;
    private Long skuId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
