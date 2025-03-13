package com.example.onlinestore.service;

import com.example.onlinestore.bean.Sku;

import java.util.List;

public interface SkuService {
    /**
     * 添加SKU
     */
    void addSku(Sku sku);
    
    /**
     * 根据ID获取SKU
     */
    Sku getSkuById(Long id);
    
    /**
     * 根据商品ID获取所有SKU
     */
    List<Sku> getSkusByItemId(Long itemId);
    
    /**
     * 更新SKU
     */
    void updateSku(Sku sku);
    
    /**
     * 删除SKU
     */
    void deleteSku(Long id);
} 