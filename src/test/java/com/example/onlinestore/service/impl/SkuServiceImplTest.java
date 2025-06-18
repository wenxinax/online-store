package com.example.onlinestore.service.impl;

import com.example.onlinestore.bean.Attribute;
import com.example.onlinestore.bean.AttributeValue;
import com.example.onlinestore.bean.ItemAttributeAndValue;
import com.example.onlinestore.bean.Sku;
import com.example.onlinestore.dto.CreateSkuRequest;
import com.example.onlinestore.dto.ItemAttributeRequest;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkuServiceImplTest {

    @Mock
    private ItemService itemService;

    @Mock
    private SkuMapper skuMapper;

    @Mock
    private AttributeService attributeService;

    @Mock
    private ItemAttributeRelationMapper itemAttributeRelationMapper;

    @InjectMocks
    private SkuServiceImpl skuService;

    private CreateSkuRequest validCreateSkuRequest;
    private SkuEntity validSkuEntity;
    private Sku validSku;
    private Attribute validAttribute;
    private AttributeValue validAttributeValue;
    private ItemAttributeRequest validItemAttributeRequest;