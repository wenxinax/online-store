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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SkuServiceImpl Unit Tests")
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
    private Attribute validAttribute;
    private AttributeValue validAttributeValue;
    private ItemAttributeRequest validItemAttributeRequest;

    @BeforeEach
    void setUp() {
        // Setup valid attribute
        validAttribute = new Attribute();
        validAttribute.setId(1L);
        validAttribute.setAttributeType(AttributeType.SKU);
        validAttribute.setInputType(AttributeInputType.SINGLE_SELECT);

        // Setup valid attribute value
        validAttributeValue = new AttributeValue();
        validAttributeValue.setId(1L);
        validAttributeValue.setValue("Red");

        // Setup valid item attribute request
        validItemAttributeRequest = new ItemAttributeRequest();
        validItemAttributeRequest.setAttributeId(1L);
        validItemAttributeRequest.setAttributeValueId(1L);

        // Setup valid create SKU request
        validCreateSkuRequest = new CreateSkuRequest();
        validCreateSkuRequest.setItemId(1L);
        validCreateSkuRequest.setSkuCode("TEST-SKU-001");
        validCreateSkuRequest.setName("Test SKU");
        validCreateSkuRequest.setDescription("Test Description");
        validCreateSkuRequest.setPrice(new BigDecimal("99.99"));
        validCreateSkuRequest.setImage("https://example.com/image.jpg");
        validCreateSkuRequest.setStockQuantity(100);
        validCreateSkuRequest.setWarningQuantity(10);
        validCreateSkuRequest.setDefaultSku(1);
        validCreateSkuRequest.setAttributes(List.of(validItemAttributeRequest));

        // Setup valid SKU entity
        validSkuEntity = new SkuEntity();
        validSkuEntity.setId(1L);
        validSkuEntity.setItemId(1L);
        validSkuEntity.setSkuCode("TEST-SKU-001");
        validSkuEntity.setName("Test SKU");
        validSkuEntity.setDescription("Test Description");
        validSkuEntity.setPrice(new BigDecimal("99.99"));
        validSkuEntity.setImage("https://example.com/image.jpg");
        validSkuEntity.setStockQuantity(100);
        validSkuEntity.setWarningQuantity(10);
        validSkuEntity.setDefaultSku(1);
        validSkuEntity.setSoldQuantity(0);
        validSkuEntity.setCreatedAt(LocalDateTime.now());
        validSkuEntity.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should create SKU successfully with valid request")
    void createSku_WithValidRequest_ShouldReturnSku() {
        when(skuMapper.findBySkuCode(validCreateSkuRequest.getSkuCode())).thenReturn(null);
        when(attributeService.getAttributeById(1L)).thenReturn(validAttribute);
        when(attributeService.getAttributeValueById(1L)).thenReturn(validAttributeValue);
        when(skuMapper.insert(any(SkuEntity.class))).thenReturn(1);
        when(itemAttributeRelationMapper.findByItemIdAndSkuId(anyLong(), anyLong()))
            .thenReturn(Collections.emptyList());
        when(itemAttributeRelationMapper.batchInsert(anyList())).thenReturn(1);

        Sku result = skuService.createSku(validCreateSkuRequest);

        assertNotNull(result);
        assertEquals(validCreateSkuRequest.getSkuCode(), result.getSkuCode());
        assertEquals(validCreateSkuRequest.getName(), result.getName());
        assertEquals(validCreateSkuRequest.getPrice(), result.getPrice());

        verify(itemService).getItemById(validCreateSkuRequest.getItemId());
        verify(skuMapper).findBySkuCode(validCreateSkuRequest.getSkuCode());
        verify(skuMapper).insert(any(SkuEntity.class));
        verify(attributeService).getAttributeById(1L);
        verify(attributeService).getAttributeValueById(1L);
    }

    @Test
    @DisplayName("Should create SKU with multiple attributes successfully")
    void createSku_WithMultipleAttributes_ShouldReturnSku() {
        ItemAttributeRequest secondAttribute = new ItemAttributeRequest();
        secondAttribute.setAttributeId(2L);
        secondAttribute.setAttributeValueId(2L);
        validCreateSkuRequest.setAttributes(List.of(validItemAttributeRequest, secondAttribute));

        Attribute secondValidAttribute = new Attribute();
        secondValidAttribute.setId(2L);
        secondValidAttribute.setAttributeType(AttributeType.SKU);
        secondValidAttribute.setInputType(AttributeInputType.MULTI_SELECT);

        AttributeValue secondValidAttributeValue = new AttributeValue();
        secondValidAttributeValue.setId(2L);
        secondValidAttributeValue.setValue("Large");

        when(skuMapper.findBySkuCode(validCreateSkuRequest.getSkuCode())).thenReturn(null);
        when(attributeService.getAttributeById(1L)).thenReturn(validAttribute);
        when(attributeService.getAttributeById(2L)).thenReturn(secondValidAttribute);
        when(attributeService.getAttributeValueById(1L)).thenReturn(validAttributeValue);
        when(attributeService.getAttributeValueById(2L)).thenReturn(secondValidAttributeValue);
        when(skuMapper.insert(any(SkuEntity.class))).thenReturn(1);
        when(itemAttributeRelationMapper.findByItemIdAndSkuId(anyLong(), anyLong()))
            .thenReturn(Collections.emptyList());
        when(itemAttributeRelationMapper.batchInsert(anyList())).thenReturn(2);

        Sku result = skuService.createSku(validCreateSkuRequest);

        assertNotNull(result);
        verify(attributeService, times(2)).getAttributeById(anyLong());
        verify(attributeService, times(2)).getAttributeValueById(anyLong());
        ArgumentCaptor<List<ItemAttributeRelationEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(itemAttributeRelationMapper).batchInsert(captor.capture());
        assertEquals(2, captor.getValue().size());
    }

    @Test
    @DisplayName("Should throw exception when SKU code already exists")
    void createSku_WithExistingSkuCode_ShouldThrowBizException() {
        when(skuMapper.findBySkuCode(validCreateSkuRequest.getSkuCode())).thenReturn(validSkuEntity);

        BizException exception = assertThrows(BizException.class,
            () -> skuService.createSku(validCreateSkuRequest));

        assertEquals(ErrorCode.SKU_CODE_EXISTS, exception.getErrorCode());
        verify(skuMapper).findBySkuCode(validCreateSkuRequest.getSkuCode());
        verify(skuMapper, never()).insert(any());
    }

    @Test
    @DisplayName("Should throw exception when warning quantity exceeds stock quantity")
    void createSku_WithWarningQuantityExceedsStock_ShouldThrowBizException() {
        validCreateSkuRequest.setWarningQuantity(200);
        validCreateSkuRequest.setStockQuantity(100);
        when(skuMapper.findBySkuCode(validCreateSkuRequest.getSkuCode())).thenReturn(null);

        BizException exception = assertThrows(BizException.class,
            () -> skuService.createSku(validCreateSkuRequest));

        assertEquals(ErrorCode.SKU_WARNING_QUANTITY_EXCEEDS_STOCK_QUANTITY, exception.getErrorCode());
        verify(skuMapper, never()).insert(any());
    }

    @Test
    @DisplayName("Should throw exception when attribute is not SKU type")
    void createSku_WithNonSkuAttribute_ShouldThrowBizException() {
        validAttribute.setAttributeType(AttributeType.ITEM);
        when(skuMapper.findBySkuCode(validCreateSkuRequest.getSkuCode())).thenReturn(null);
        when(attributeService.getAttributeById(1L)).thenReturn(validAttribute);

        BizException exception = assertThrows(BizException.class,
            () -> skuService.createSku(validCreateSkuRequest));

        assertEquals(ErrorCode.ATTRIBUTE_TYPE_NOT_SKU, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw exception when attribute input type is invalid")
    void createSku_WithInvalidInputType_ShouldThrowBizException() {
        validAttribute.setInputType(AttributeInputType.INPUT);
        when(skuMapper.findBySkuCode(validCreateSkuRequest.getSkuCode())).thenReturn(null);
        when(attributeService.getAttributeById(1L)).thenReturn(validAttribute);

        BizException exception = assertThrows(BizException.class,
            () -> skuService.createSku(validCreateSkuRequest));

        assertEquals(ErrorCode.SKU_ATTRIBUTE_INPUT_TYPE_INVALID, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw exception when attribute value ID is null")
    void createSku_WithNullAttributeValueId_ShouldThrowBizException() {
        validItemAttributeRequest.setAttributeValueId(null);
        when(skuMapper.findBySkuCode(validCreateSkuRequest.getSkuCode())).thenReturn(null);
        when(attributeService.getAttributeById(1L)).thenReturn(validAttribute);

        BizException exception = assertThrows(BizException.class,
            () -> skuService.createSku(validCreateSkuRequest));

        assertEquals(ErrorCode.SKU_ATTRIBUTE_VALUE_EMPTY, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw exception when SKU insert fails")
    void createSku_WithInsertFailure_ShouldThrowBizException() {
        when(skuMapper.findBySkuCode(validCreateSkuRequest.getSkuCode())).thenReturn(null);
        when(attributeService.getAttributeById(1L)).thenReturn(validAttribute);
        when(attributeService.getAttributeValueById(1L)).thenReturn(validAttributeValue);
        when(skuMapper.insert(any(SkuEntity.class))).thenReturn(0);

        BizException exception = assertThrows(BizException.class,
            () -> skuService.createSku(validCreateSkuRequest));

        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should return list of SKUs for valid item ID")
    void getSkusByItemId_WithValidItemId_ShouldReturnSkuList() {
        Long itemId = 1L;
        List<SkuEntity> skuEntities = List.of(validSkuEntity);

        when(skuMapper.findByItemId(itemId)).thenReturn(skuEntities);
        when(itemAttributeRelationMapper.findByItemIdAndSkuId(anyLong(), anyLong()))
            .thenReturn(Collections.emptyList());

        List<Sku> result = skuService.getSkusByItemId(itemId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(validSkuEntity.getSkuCode(), result.get(0).getSkuCode());
        verify(skuMapper).findByItemId(itemId);
    }

    @Test
    @DisplayName("Should return empty list when no SKUs found for item ID")
    void getSkusByItemId_WithNoSkusFound_ShouldReturnEmptyList() {
        Long itemId = 999L;
        when(skuMapper.findByItemId(itemId)).thenReturn(Collections.emptyList());

        List<Sku> result = skuService.getSkusByItemId(itemId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(skuMapper).findByItemId(itemId);
    }

    @Test
    @DisplayName("Should return empty list when SKU entities are null")
    void getSkusByItemId_WithNullSkuEntities_ShouldReturnEmptyList() {
        Long itemId = 1L;
        when(skuMapper.findByItemId(itemId)).thenReturn(null);

        List<Sku> result = skuService.getSkusByItemId(itemId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(skuMapper).findByItemId(itemId);
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 2L, 100L, 999L})
    @DisplayName("Should handle various item IDs correctly")
    void getSkusByItemId_WithVariousItemIds_ShouldCallMapperCorrectly(Long itemId) {
        when(skuMapper.findByItemId(itemId)).thenReturn(Collections.emptyList());
        skuService.getSkusByItemId(itemId);
        verify(skuMapper).findByItemId(itemId);
    }

    @Test
    @DisplayName("Should update stock quantity successfully")
    void updateStockQuantity_WithValidParameters_ShouldUpdateSuccessfully() {
        Long skuId = 1L;
        Integer newQuantity = 50;

        when(skuMapper.findById(skuId)).thenReturn(validSkuEntity);
        when(skuMapper.updateStockQuantity(skuId, newQuantity)).thenReturn(1);

        skuService.updateStockQuantity(skuId, newQuantity);

        verify(skuMapper).findById(skuId);
        verify(skuMapper).updateStockQuantity(skuId, newQuantity);
    }

    @Test
    @DisplayName("Should throw exception when warning quantity exceeds new stock quantity")
    void updateStockQuantity_WithWarningQuantityExceedsNewStock_ShouldThrowBizException() {
        Long skuId = 1L;
        Integer newQuantity = 5;
        validSkuEntity.setWarningQuantity(10);

        when(skuMapper.findById(skuId)).thenReturn(validSkuEntity);

        BizException exception = assertThrows(BizException.class,
            () -> skuService.updateStockQuantity(skuId, newQuantity));

        assertEquals(ErrorCode.SKU_WARNING_QUANTITY_EXCEEDS_STOCK_QUANTITY, exception.getErrorCode());
        verify(skuMapper, never()).updateStockQuantity(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Should throw exception when update fails")
    void updateStockQuantity_WithUpdateFailure_ShouldThrowBizException() {
        Long skuId = 1L;
        Integer newQuantity = 50;

        when(skuMapper.findById(skuId)).thenReturn(validSkuEntity);
        when(skuMapper.updateStockQuantity(skuId, newQuantity)).thenReturn(0);

        BizException exception = assertThrows(BizException.class,
            () -> skuService.updateStockQuantity(skuId, newQuantity));

        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, exception.getErrorCode());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 50, 100, 1000})
    @DisplayName("Should update with various valid quantities")
    void updateStockQuantity_WithVariousValidQuantities_ShouldUpdateSuccessfully(Integer quantity) {
        Long skuId = 1L;
        validSkuEntity.setWarningQuantity(0);

        when(skuMapper.findById(skuId)).thenReturn(validSkuEntity);
        when(skuMapper.updateStockQuantity(skuId, quantity)).thenReturn(1);

        skuService.updateStockQuantity(skuId, quantity);

        verify(skuMapper).updateStockQuantity(skuId, quantity);
    }

    @Test
    @DisplayName("Should return SKU when found by valid ID")
    void getSkuById_WithValidId_ShouldReturnSku() {
        Long skuId = 1L;

        when(skuMapper.findById(skuId)).thenReturn(validSkuEntity);
        when(itemAttributeRelationMapper.findByItemIdAndSkuId(anyLong(), anyLong()))
            .thenReturn(Collections.emptyList());

        Sku result = skuService.getSkuById(skuId);

        assertNotNull(result);
        assertEquals(validSkuEntity.getId(), result.getId());
        assertEquals(validSkuEntity.getSkuCode(), result.getSkuCode());
        assertEquals(validSkuEntity.getName(), result.getName());
        verify(skuMapper).findById(skuId);
    }

    @Test
    @DisplayName("Should throw exception when SKU not found")
    void getSkuById_WithNonExistentId_ShouldThrowBizException() {
        Long skuId = 999L;
        when(skuMapper.findById(skuId)).thenReturn(null);

        BizException exception = assertThrows(BizException.class,
            () -> skuService.getSkuById(skuId));

        assertEquals(ErrorCode.SKU_NOT_FOUND, exception.getErrorCode());
        verify(skuMapper).findById(skuId);
    }

    @Test
    @DisplayName("Should return SKU with attributes when relations exist")
    void getSkuById_WithExistingRelations_ShouldReturnSkuWithAttributes() {
        Long skuId = 1L;
        ItemAttributeRelationEntity relationEntity = new ItemAttributeRelationEntity();
        relationEntity.setAttributeId(1L);
        relationEntity.setValueId(1L);

        when(skuMapper.findById(skuId)).thenReturn(validSkuEntity);
        when(itemAttributeRelationMapper.findByItemIdAndSkuId(anyLong(), anyLong()))
            .thenReturn(List.of(relationEntity));
        when(attributeService.getAttributeByIdWithValues(1L)).thenReturn(validAttribute);
        when(attributeService.getAttributeValueById(1L)).thenReturn(validAttributeValue);

        Sku result = skuService.getSkuById(skuId);

        assertNotNull(result);
        assertNotNull(result.getAttributes());
        assertEquals(1, result.getAttributes().size());
        verify(attributeService).getAttributeByIdWithValues(1L);
        verify(attributeService).getAttributeValueById(1L);
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("Should throw exception for null SKU ID")
    void getSkuById_WithNullId_ShouldThrowException(Long skuId) {
        assertThrows(Exception.class, () -> skuService.getSkuById(skuId));
    }

    @Test
    @DisplayName("Should handle convertSkuEntity with null entity")
    void convertSkuEntity_WithNullEntity_ShouldReturnNull() {
        Long skuId = 1L;
        when(skuMapper.findById(skuId)).thenReturn(null);
        assertThrows(BizException.class, () -> skuService.getSkuById(skuId));
    }

    @Test
    @DisplayName("Should process SKU attributes with existing relations")
    void createSku_WithExistingRelations_ShouldDeleteAndCreateNew() {
        ItemAttributeRelationEntity existingRelation = new ItemAttributeRelationEntity();
        existingRelation.setAttributeId(2L);

        when(skuMapper.findBySkuCode(validCreateSkuRequest.getSkuCode())).thenReturn(null);
        when(attributeService.getAttributeById(1L)).thenReturn(validAttribute);
        when(attributeService.getAttributeValueById(1L)).thenReturn(validAttributeValue);
        when(skuMapper.insert(any(SkuEntity.class))).thenReturn(1);
        when(itemAttributeRelationMapper.findByItemIdAndSkuId(anyLong(), anyLong()))
            .thenReturn(List.of(existingRelation));
        when(itemAttributeRelationMapper.deleteByItemIdAndAttributeIds(anyLong(), anyList())).thenReturn(1);
        when(itemAttributeRelationMapper.batchInsert(anyList())).thenReturn(1);

        skuService.createSku(validCreateSkuRequest);

        verify(itemAttributeRelationMapper).deleteByItemIdAndAttributeIds(anyLong(), anyList());
        verify(itemAttributeRelationMapper).batchInsert(anyList());
    }

    @Test
    @DisplayName("Should handle batch insert failure in processSkuAttributes")
    void createSku_WithBatchInsertFailure_ShouldThrowBizException() {
        when(skuMapper.findBySkuCode(validCreateSkuRequest.getSkuCode())).thenReturn(null);
        when(attributeService.getAttributeById(1L)).thenReturn(validAttribute);
        when(attributeService.getAttributeValueById(1L)).thenReturn(validAttributeValue);
        when(skuMapper.insert(any(SkuEntity.class))).thenReturn(1);
        when(itemAttributeRelationMapper.findByItemIdAndSkuId(anyLong(), anyLong()))
            .thenReturn(Collections.emptyList());
        when(itemAttributeRelationMapper.batchInsert(anyList())).thenReturn(0);

        BizException exception = assertThrows(BizException.class,
            () -> skuService.createSku(validCreateSkuRequest));

        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should handle delete failure in processSkuAttributes")
    void createSku_WithDeleteFailure_ShouldThrowBizException() {
        ItemAttributeRelationEntity existingRelation = new ItemAttributeRelationEntity();
        existingRelation.setAttributeId(2L);

        when(skuMapper.findBySkuCode(validCreateSkuRequest.getSkuCode())).thenReturn(null);
        when(attributeService.getAttributeById(1L)).thenReturn(validAttribute);
        when(attributeService.getAttributeValueById(1L)).thenReturn(validAttributeValue);
        when(skuMapper.insert(any(SkuEntity.class))).thenReturn(1);
        when(itemAttributeRelationMapper.findByItemIdAndSkuId(anyLong(), anyLong()))
            .thenReturn(List.of(existingRelation));
        when(itemAttributeRelationMapper.deleteByItemIdAndAttributeIds(anyLong(), anyList())).thenReturn(0);

        BizException exception = assertThrows(BizException.class,
            () -> skuService.createSku(validCreateSkuRequest));

        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should handle attribute with INPUT type correctly")
    void getSkuById_WithInputTypeAttribute_ShouldSetInputValue() {
        Long skuId = 1L;
        validAttribute.setInputType(AttributeInputType.INPUT);

        ItemAttributeRelationEntity relationEntity = new ItemAttributeRelationEntity();
        relationEntity.setAttributeId(1L);
        relationEntity.setInputValue("Custom Input Value");

        when(skuMapper.findById(skuId)).thenReturn(validSkuEntity);
        when(itemAttributeRelationMapper.findByItemIdAndSkuId(anyLong(), anyLong()))
            .thenReturn(List.of(relationEntity));
        when(attributeService.getAttributeByIdWithValues(1L)).thenReturn(validAttribute);

        Sku result = skuService.getSkuById(skuId);

        assertNotNull(result);
        assertNotNull(result.getAttributes());
        assertEquals(1, result.getAttributes().size());
        ItemAttributeAndValue attributeAndValue = result.getAttributes().get(0);
        assertEquals("Custom Input Value", attributeAndValue.getInputValue());
        verify(attributeService, never()).getAttributeValueById(anyLong());
    }

    @Test
    @DisplayName("Should verify all interactions in complex scenario")
    void createSku_ComplexScenario_ShouldVerifyAllInteractions() {
        when(skuMapper.findBySkuCode(validCreateSkuRequest.getSkuCode())).thenReturn(null);
        when(attributeService.getAttributeById(1L)).thenReturn(validAttribute);
        when(attributeService.getAttributeValueById(1L)).thenReturn(validAttributeValue);
        when(skuMapper.insert(any(SkuEntity.class))).thenReturn(1);
        when(itemAttributeRelationMapper.findByItemIdAndSkuId(anyLong(), anyLong()))
            .thenReturn(Collections.emptyList());
        when(itemAttributeRelationMapper.batchInsert(anyList())).thenReturn(1);

        Sku result = skuService.createSku(validCreateSkuRequest);

        assertNotNull(result);
        verify(itemService).getItemById(validCreateSkuRequest.getItemId());
        verify(skuMapper).findBySkuCode(validCreateSkuRequest.getSkuCode());
        verify(attributeService).getAttributeById(1L);
        verify(attributeService).getAttributeValueById(1L);

        ArgumentCaptor<SkuEntity> skuEntityCaptor = ArgumentCaptor.forClass(SkuEntity.class);
        verify(skuMapper).insert(skuEntityCaptor.capture());
        SkuEntity capturedEntity = skuEntityCaptor.getValue();
        assertEquals(validCreateSkuRequest.getSkuCode(), capturedEntity.getSkuCode());
        assertEquals(0, capturedEntity.getSoldQuantity());
        assertNotNull(capturedEntity.getCreatedAt());
        assertNotNull(capturedEntity.getUpdatedAt());

        verify(itemAttributeRelationMapper).findByItemIdAndSkuId(anyLong(), anyLong());
        verify(itemAttributeRelationMapper).batchInsert(anyList());

        verifyNoMoreInteractions(skuMapper, attributeService, itemAttributeRelationMapper);
    }
}