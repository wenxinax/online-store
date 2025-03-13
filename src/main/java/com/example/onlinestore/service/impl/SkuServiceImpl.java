package com.example.onlinestore.service.impl;

import com.example.onlinestore.bean.Sku;
import com.example.onlinestore.entity.SkuEntity;
import com.example.onlinestore.mapper.SkuMapper;
import com.example.onlinestore.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private SkuMapper skuMapper;

    @Override
    public void addSku(Sku sku) {
        SkuEntity skuEntity = convertToSkuEntity(sku);
        skuEntity.setCreatedAt(LocalDateTime.now());
        skuEntity.setUpdatedAt(LocalDateTime.now());
        skuMapper.insertSku(skuEntity);
    }

    @Override
    public Sku getSkuById(Long id) {
        SkuEntity skuEntity = skuMapper.findById(id);
        return convertToSku(skuEntity);
    }

    @Override
    public List<Sku> getSkusByItemId(Long itemId) {
        List<SkuEntity> skuEntities = skuMapper.findByItemId(itemId);
        return skuEntities.stream()
                .map(this::convertToSku)
                .collect(Collectors.toList());
    }

    @Override
    public void updateSku(Sku sku) {
        SkuEntity skuEntity = convertToSkuEntity(sku);
        skuEntity.setUpdatedAt(LocalDateTime.now());
        skuMapper.updateSku(skuEntity);
    }

    @Override
    public void deleteSku(Long id) {
        skuMapper.deleteSku(id);
    }

    private Sku convertToSku(SkuEntity skuEntity) {
        if (skuEntity == null) {
            return null;
        }
        
        Sku sku = new Sku();
        sku.setId(skuEntity.getId());
        sku.setItemId(skuEntity.getItemId());
        sku.setName(skuEntity.getTitle());
        // 可以根据需要设置其他属性
        
        return sku;
    }

    private SkuEntity convertToSkuEntity(Sku sku) {
        if (sku == null) {
            return null;
        }
        
        SkuEntity skuEntity = new SkuEntity();
        skuEntity.setId(sku.getId());
        skuEntity.setItemId(sku.getItemId());
        skuEntity.setTitle(sku.getName());
        // 可以根据需要设置其他属性
        
        return skuEntity;
    }
} 