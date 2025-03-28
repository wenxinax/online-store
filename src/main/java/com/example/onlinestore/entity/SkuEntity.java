package com.example.onlinestore.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SkuEntity 商品的SKU表
 */
@Setter
@Getter
public class SkuEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = -7603610766473193466L;

    private Long id;
    private Long itemId;
    private String title;
    // SKU图片，JSON格式
    private String images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
