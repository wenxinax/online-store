package com.example.onlinestore.dto.converter;

import com.example.onlinestore.bean.Sku;
import com.example.onlinestore.dto.SkuResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class SkuConverter {
    public SkuResponse convert(Sku sku) {
        if (sku == null) {
            return null;
        }
        SkuResponse skuResponse = new SkuResponse();
        BeanUtils.copyProperties(sku, skuResponse);
        return skuResponse;
    }
}
