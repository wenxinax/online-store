package com.example.onlinestore.service.impl;

import com.example.onlinestore.bean.Attribute;
import com.example.onlinestore.bean.AttributeValue;
import com.example.onlinestore.bean.ItemAttributeAndValue;
import com.example.onlinestore.bean.Sku;
import com.example.onlinestore.dto.CreateSkuRequest;
import com.example.onlinestore.entity.ItemAttributeRelationEntity;
import com.example.onlinestore.entity.SkuEntity;
import com.example.onlinestore.enums.AttributeInputType;
import com.example.onlinestore.enums.AttributeType;
import com.example.onlinestore.errors.ErrorCode;
import com.example.onlinestore.exceptions.BizException;
import com.example.onlinestore.mapper.ItemAttributeRelationMapper;
import com.example.onlinestore.mapper.SkuMapper;
import com.example.onlinestore.service.AttributeService;
import com.example.onlinestore.service.ItemService;
import com.example.onlinestore.service.SkuService;
import jakarta.validation.Valid;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SkuServiceImpl implements SkuService {
    private static final Logger logger = LoggerFactory.getLogger(SkuServiceImpl.class);

    @Autowired
    private ItemService itemService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private AttributeService attributeService;

    @Autowired
    private ItemAttributeRelationMapper itemAttributeRelationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Sku createSku(@Valid CreateSkuRequest createSkuRequest) {
        //判断商品是否存在
        itemService.getItemById(createSkuRequest.getItemId());
        // 需要判断skuCode是否存在
        if (skuMapper.findBySkuCode(createSkuRequest.getSkuCode()) != null) {
            throw new BizException(ErrorCode.SKU_CODE_EXISTS, createSkuRequest.getSkuCode());
        }

        if (createSkuRequest.getWarningQuantity() > createSkuRequest.getStockQuantity()) {
            logger.error("SKU预警数量 {} 超过库存数量 {}", createSkuRequest.getWarningQuantity(), createSkuRequest.getStockQuantity());
            throw new BizException(ErrorCode.SKU_WARNING_QUANTITY_EXCEEDS_STOCK_QUANTITY);
        }

        // 校验属性
        createSkuRequest.getAttributes().forEach(attributeRequest -> {
            Attribute attribute = attributeService.getAttributeById(attributeRequest.getAttributeId());
            if (attribute.getAttributeType() != AttributeType.SKU) {
                throw new BizException(ErrorCode.ATTRIBUTE_TYPE_NOT_SKU, attributeRequest.getAttributeId());
            }

            if (attribute.getInputType() != AttributeInputType.SINGLE_SELECT && attribute.getInputType() == AttributeInputType.MULTI_SELECT){
                throw new BizException(ErrorCode.SKU_ATTRIBUTE_INPUT_TYPE_INVALID, attributeRequest.getAttributeId());
            }

            if (attributeRequest.getAttributeValueId() == null) {
                throw new BizException(ErrorCode.SKU_ATTRIBUTE_VALUE_EMPTY, attributeRequest.getAttributeId());
            }

            // 校验属性值是否存在
            attributeService.getAttributeValueById(attributeRequest.getAttributeValueId());

        });

        LocalDateTime now = LocalDateTime.now();

        SkuEntity skuEntity = new SkuEntity();
        skuEntity.setItemId(createSkuRequest.getItemId());
        skuEntity.setSkuCode(createSkuRequest.getSkuCode());
        skuEntity.setName(createSkuRequest.getName());
        skuEntity.setDescription(createSkuRequest.getDescription());
        skuEntity.setPrice(createSkuRequest.getPrice());
        skuEntity.setImage(createSkuRequest.getImage());
        skuEntity.setStockQuantity(createSkuRequest.getStockQuantity());
        skuEntity.setWarningQuantity(createSkuRequest.getWarningQuantity());
        skuEntity.setIsDefault(createSkuRequest.getIsDefault());
        skuEntity.setSoldQuantity(0);
        skuEntity.setCreatedAt(now);
        skuEntity.setUpdatedAt(now);
        int effectRows = skuMapper.insert(skuEntity);
        if (effectRows != 1) {
            logger.error("insert sku failed. because effect rows is 0. skuCode:{}", createSkuRequest.getSkuCode());
            throw new BizException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        //记录属性
        List<ItemAttributeRelationEntity> relationEntities = createSkuRequest.getAttributes().stream().map(attributeRequest -> {
            ItemAttributeRelationEntity relationEntity = new ItemAttributeRelationEntity();
            relationEntity.setItemId(createSkuRequest.getItemId());
            relationEntity.setAttributeId(attributeRequest.getAttributeId());
            relationEntity.setValueId(attributeRequest.getAttributeValueId());
            relationEntity.setInputValue(attributeRequest.getValue());
            relationEntity.setCreatedAt(now);
            return relationEntity;
        }).toList();

        if (itemAttributeRelationMapper.batchInsert(relationEntities) != relationEntities.size()) {
            logger.error("insert sku attribute relations failed. because effect rows is {}", relationEntities.size());
            throw new BizException(ErrorCode.INTERNAL_SERVER_ERROR);
        }


        return convertSkuEntity(skuEntity, relationEntities);
    }

    @Override
    public List<Sku> getSkusByItemId(Long itemId) {
        List<SkuEntity> skuEntities = skuMapper.findByItemId(itemId);
        if (CollectionUtils.isEmpty(skuEntities)) {
            return Collections.emptyList();
        }
        return skuEntities.stream().map(skuEntity -> convertSkuEntity(skuEntity, null)).collect(Collectors.toList());
    }

    @Override
    public void updateStockQuantity(Long skuId, Integer quantity) {
        Sku sku = getSkuById(skuId);
        if (sku.getWarningQuantity() > sku.getStockQuantity()) {
            throw new BizException(ErrorCode.SKU_WARNING_QUANTITY_EXCEEDS_STOCK_QUANTITY);
        }

        if (skuMapper.updateStockQuantity(skuId, quantity) != 1) {
            logger.error("update sku stock quantity failed. because effect rows is 0. skuId:{}", skuId);
            throw new BizException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    public Sku getSkuById(Long skuId) {
        SkuEntity skuEntity = skuMapper.findById(skuId);
        if (skuEntity == null) {
            logger.error("sku not found, id: {}", skuId);
            throw new BizException(ErrorCode.SKU_NOT_FOUND);
        }
        return convertSkuEntity(skuEntity, null);
    }

    private Sku convertSkuEntity(SkuEntity skuEntity, List<ItemAttributeRelationEntity> relationEntities) {
        if (skuEntity == null) {
            return null;
        }

        Sku sku = new Sku();
        sku.setId(skuEntity.getId());
        sku.setItemId(skuEntity.getItemId());
        sku.setSkuCode(skuEntity.getSkuCode());
        sku.setName(skuEntity.getName());
        sku.setDescription(skuEntity.getDescription());
        sku.setPrice(skuEntity.getPrice());
        sku.setImage(skuEntity.getImage());
        sku.setIsDefault(skuEntity.getIsDefault());

        if (relationEntities == null) {
            relationEntities = itemAttributeRelationMapper.findByItemIdAndSkuId(skuEntity.getItemId(), skuEntity.getId());
        }
        if (CollectionUtils.isEmpty(relationEntities)) {
            sku.setAttributes(Collections.emptyList());
        } else {
            sku.setAttributes(relationEntities.stream().map(relationEntity -> {
                ItemAttributeAndValue itemAttributeAndValue = new ItemAttributeAndValue();
                Attribute attribute = attributeService.getAttributeByIdWithValues(relationEntity.getAttributeId());
                itemAttributeAndValue.setAttribute(attribute);
                if (attribute.getInputType() == AttributeInputType.INPUT){
                    itemAttributeAndValue.setInputValue(relationEntity.getInputValue());
                }else{
                    AttributeValue attributeValue = attributeService.getAttributeValueById(relationEntity.getValueId());
                    itemAttributeAndValue.setAttributeValue(attributeValue);
                }

                return itemAttributeAndValue;

            }).collect(Collectors.toList());
        }
        return sku;
    }
}
