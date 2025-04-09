package com.example.onlinestore.dto.converter;

import com.example.onlinestore.bean.Sku;
import com.example.onlinestore.dto.SkuResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class SkuConverter {
    public SkuResponse convert(Sku sku) {
        SkuResponse skuResponse = new SkuResponse();
        BeanUtils.copyProperties(sku, skuResponse);
        return skuResponse;
    }
}
