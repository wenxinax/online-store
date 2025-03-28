package com.example.onlinestore.dto;

import com.example.onlinestore.bean.Category;
import com.example.onlinestore.bean.Item;
import com.example.onlinestore.bean.Sku;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 商品详情DTO，包含商品基本信息、类目信息和SKU列表
 */
@Setter
@Getter
public class ItemDetailResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 6878279571174539057L;

    private Item item;
    private Category category;
    private List<Sku> skus;

}