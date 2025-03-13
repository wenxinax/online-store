package com.example.onlinestore.dto;

import com.example.onlinestore.bean.Category;
import com.example.onlinestore.bean.Item;
import com.example.onlinestore.bean.Sku;

import java.io.Serializable;
import java.util.List;

/**
 * 商品详情DTO，包含商品基本信息、类目信息和SKU列表
 */
public class ItemDetailDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Item item;
    private Category category;
    private List<Sku> skus;
    
    public Item getItem() {
        return item;
    }
    
    public void setItem(Item item) {
        this.item = item;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public void setCategory(Category category) {
        this.category = category;
    }
    
    public List<Sku> getSkus() {
        return skus;
    }
    
    public void setSkus(List<Sku> skus) {
        this.skus = skus;
    }
} 