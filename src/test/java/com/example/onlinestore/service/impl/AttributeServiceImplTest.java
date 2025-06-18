package com.example.onlinestore.service.impl;

import com.example.onlinestore.bean.Attribute;
import com.example.onlinestore.bean.AttributeValue;
import com.example.onlinestore.dto.CreateAttributeRequest;
import com.example.onlinestore.dto.ItemAttributeRequest;
import com.example.onlinestore.dto.UpdateAttributeRequest;
import com.example.onlinestore.entity.AttributeEntity;
import com.example.onlinestore.entity.AttributeValueEntity;
import com.example.onlinestore.entity.ItemAttributeRelationEntity;
import com.example.onlinestore.enums.AttributeInputType;
import com.example.onlinestore.enums.AttributeType;
import com.example.onlinestore.errors.ErrorCode;
import com.example.onlinestore.exceptions.BizException;
import com.example.onlinestore.mapper.AttributeMapper;
import com.example.onlinestore.mapper.AttributeValueMapper;
import com.example.onlinestore.mapper.ItemAttributeRelationMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AttributeServiceImpl Tests")
class AttributeServiceImplTest {

    @Mock
    private AttributeMapper attributeMapper;

    @Mock
    private AttributeValueMapper attributeValueMapper;

    @Mock
    private ItemAttributeRelationMapper itemAttributeRelationMapper;

    @InjectMocks
    private AttributeServiceImpl attributeService;

    private AttributeEntity testAttributeEntity;
    private Attribute testAttribute;
    private AttributeValueEntity testAttributeValueEntity;
    private AttributeValue testAttributeValue;
    private CreateAttributeRequest createRequest;
    private UpdateAttributeRequest updateRequest;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        // Setup AttributeEntity
        testAttributeEntity = new AttributeEntity();
        testAttributeEntity.setId(1L);
        testAttributeEntity.setName("Color");
        testAttributeEntity.setAttributeType("PRODUCT");
        testAttributeEntity.setInputType("SINGLE_SELECT");
        testAttributeEntity.setRequired(true);
        testAttributeEntity.setSearchable(true);
        testAttributeEntity.setSortScore(1);
        testAttributeEntity.setVisible(true);
        testAttributeEntity.setCreatedAt(now);
        testAttributeEntity.setUpdatedAt(now);

        // Setup Attribute bean
        testAttribute = new Attribute();
        testAttribute.setId(1L);
        testAttribute.setName("Color");
        testAttribute.setAttributeType(AttributeType.PRODUCT);
        testAttribute.setInputType(AttributeInputType.SINGLE_SELECT);
        testAttribute.setRequired(true);
        testAttribute.setSearchable(true);
        testAttribute.setSortScore(1);
        testAttribute.setVisible(true);

        // Setup AttributeValueEntity
        testAttributeValueEntity = new AttributeValueEntity();
        testAttributeValueEntity.setId(1L);
        testAttributeValueEntity.setAttributeId(1L);
        testAttributeValueEntity.setValue("Red");
        testAttributeValueEntity.setSortScore(1);

        // Setup AttributeValue bean
        testAttributeValue = new AttributeValue();
        testAttributeValue.setId(1L);
        testAttributeValue.setAttributeId(1L);
        testAttributeValue.setValue("Red");
        testAttributeValue.setSortScore(1);

        // Setup CreateAttributeRequest
        createRequest = new CreateAttributeRequest();
        createRequest.setName("Size");
        createRequest.setAttributeType("PRODUCT");
        createRequest.setInputType("SINGLE_SELECT");
        createRequest.setRequired(false);
        createRequest.setSearchable(true);
        createRequest.setSortScore(2);
        createRequest.setVisible(true);

        // Setup UpdateAttributeRequest
        updateRequest = new UpdateAttributeRequest();
        updateRequest.setName("Updated Color");
        updateRequest.setAttributeType("PRODUCT");
        updateRequest.setInputType("MULTI_SELECT");
        updateRequest.setRequired(false);
        updateRequest.setSearchable(false);
        updateRequest.setSortScore(3);
        updateRequest.setVisible(false);
    }

    @Nested
    @DisplayName("CreateAttribute Tests")
    class CreateAttributeTests {

        @Test
        @DisplayName("Should create attribute successfully when valid request provided")
        void createAttribute_WhenValidRequestProvided_ShouldCreateSuccessfully() {
            when(attributeMapper.findByName(createRequest.getName())).thenReturn(null);
            when(attributeMapper.insert(any(AttributeEntity.class))).thenReturn(1);

            Attribute result = attributeService.createAttribute(createRequest);

            assertNotNull(result);
            assertEquals(createRequest.getName(), result.getName());
            assertEquals(AttributeType.valueOf(createRequest.getAttributeType()), result.getAttributeType());
            assertEquals(AttributeInputType.valueOf(createRequest.getInputType()), result.getInputType());
            assertEquals(createRequest.getRequired(), result.getRequired());
            assertEquals(createRequest.getSearchable(), result.getSearchable());
            assertEquals(createRequest.getSortScore(), result.getSortScore());
            assertEquals(createRequest.getVisible(), result.getVisible());

            verify(attributeMapper, times(1)).findByName(createRequest.getName());
            verify(attributeMapper, times(1)).insert(any(AttributeEntity.class));
        }

        @Test
        @DisplayName("Should throw BizException when attribute name already exists")
        void createAttribute_WhenNameAlreadyExists_ShouldThrowBizException() {
            when(attributeMapper.findByName(createRequest.getName())).thenReturn(testAttributeEntity);

            BizException exception = assertThrows(BizException.class,
                () -> attributeService.createAttribute(createRequest));

            assertEquals(ErrorCode.ATTRIBUTE_NAME_DUPLICATED, exception.getErrorCode());
            verify(attributeMapper, times(1)).findByName(createRequest.getName());
            verify(attributeMapper, never()).insert(any());
        }

        @Test
        @DisplayName("Should throw BizException when database insert fails")
        void createAttribute_WhenInsertFails_ShouldThrowBizException() {
            when(attributeMapper.findByName(createRequest.getName())).thenReturn(null);
            when(attributeMapper.insert(any(AttributeEntity.class))).thenReturn(0);

            BizException exception = assertThrows(BizException.class,
                () -> attributeService.createAttribute(createRequest));

            assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, exception.getErrorCode());
            verify(attributeMapper, times(1)).findByName(createRequest.getName());
            verify(attributeMapper, times(1)).insert(any(AttributeEntity.class));
        }

        @Test
        @DisplayName("Should throw exception when null request provided")
        void createAttribute_WhenNullRequestProvided_ShouldThrowException() {
            assertThrows(Exception.class, () -> attributeService.createAttribute(null));
            verify(attributeMapper, never()).findByName(any());
            verify(attributeMapper, never()).insert(any());
        }
    }

    @Nested
    @DisplayName("UpdateAttribute Tests")
    class UpdateAttributeTests {

        @Test
        @DisplayName("Should update attribute successfully when changes are made")
        void updateAttribute_WhenChangesAreMade_ShouldUpdateSuccessfully() {
            Long attributeId = 1L;
            when(attributeMapper.findById(attributeId)).thenReturn(testAttributeEntity);
            when(attributeMapper.update(any(AttributeEntity.class))).thenReturn(1);

            assertDoesNotThrow(() -> attributeService.updateAttribute(attributeId, updateRequest));

            verify(attributeMapper, times(1)).findById(attributeId);
            verify(attributeMapper, times(1)).update(any(AttributeEntity.class));
        }

        @Test
        @DisplayName("Should not update when no changes are detected")
        void updateAttribute_WhenNoChangesDetected_ShouldNotUpdate() {
            Long attributeId = 1L;
            UpdateAttributeRequest noChangeRequest = new UpdateAttributeRequest();
            noChangeRequest.setName(testAttributeEntity.getName());
            noChangeRequest.setAttributeType(testAttributeEntity.getAttributeType());
            noChangeRequest.setInputType(testAttributeEntity.getInputType());
            noChangeRequest.setRequired(testAttributeEntity.getRequired());
            noChangeRequest.setSearchable(testAttributeEntity.getSearchable());
            noChangeRequest.setSortScore(testAttributeEntity.getSortScore());
            noChangeRequest.setVisible(testAttributeEntity.getVisible());

            when(attributeMapper.findById(attributeId)).thenReturn(testAttributeEntity);

            assertDoesNotThrow(() -> attributeService.updateAttribute(attributeId, noChangeRequest));

            verify(attributeMapper, times(1)).findById(attributeId);
            verify(attributeMapper, never()).update(any());
        }

        @Test
        @DisplayName("Should throw BizException when attribute not found")
        void updateAttribute_WhenAttributeNotFound_ShouldThrowBizException() {
            Long nonExistentId = 999L;
            when(attributeMapper.findById(nonExistentId)).thenReturn(null);

            BizException exception = assertThrows(BizException.class,
                () -> attributeService.updateAttribute(nonExistentId, updateRequest));

            assertEquals(ErrorCode.ATTRIBUTE_NOT_FOUND, exception.getErrorCode());
            verify(attributeMapper, times(1)).findById(nonExistentId);
            verify(attributeMapper, never()).update(any());
        }

        @Test
        @DisplayName("Should throw BizException when database update fails")
        void updateAttribute_WhenUpdateFails_ShouldThrowBizException() {
            Long attributeId = 1L;
            when(attributeMapper.findById(attributeId)).thenReturn(testAttributeEntity);
            when(attributeMapper.update(any(AttributeEntity.class))).thenReturn(0);

            BizException exception = assertThrows(BizException.class,
                () -> attributeService.updateAttribute(attributeId, updateRequest));

            assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, exception.getErrorCode());
            verify(attributeMapper, times(1)).update(any(AttributeEntity.class));
        }
    }

    @Nested
    @DisplayName("DeleteAttribute Tests")
    class DeleteAttributeTests {

        @Test
        @DisplayName("Should delete attribute successfully when not referenced by items")
        void deleteAttribute_WhenNotReferencedByItems_ShouldDeleteSuccessfully() {
            Long attributeId = 1L;
            when(attributeMapper.findById(attributeId)).thenReturn(testAttributeEntity);
            when(itemAttributeRelationMapper.findByAttributeId(attributeId, 0, 1))
                .thenReturn(Collections.emptyList());
            when(attributeMapper.deleteById(attributeId)).thenReturn(1);
            when(attributeValueMapper.countValuesByAttributeId(attributeId)).thenReturn(2);
            when(attributeValueMapper.deleteByAttributeId(attributeId)).thenReturn(2);

            assertDoesNotThrow(() -> attributeService.deleteAttribute(attributeId));

            verify(attributeMapper, times(1)).findById(attributeId);
            verify(itemAttributeRelationMapper, times(1)).findByAttributeId(attributeId, 0, 1);
            verify(attributeMapper, times(1)).deleteById(attributeId);
            verify(attributeValueMapper, times(1)).countValuesByAttributeId(attributeId);
            verify(attributeValueMapper, times(1)).deleteByAttributeId(attributeId);
        }

        @Test
        @DisplayName("Should delete attribute successfully when no attribute values exist")
        void deleteAttribute_WhenNoAttributeValues_ShouldDeleteSuccessfully() {
            Long attributeId = 1L;
            when(attributeMapper.findById(attributeId)).thenReturn(testAttributeEntity);
            when(itemAttributeRelationMapper.findByAttributeId(attributeId, 0, 1))
                .thenReturn(Collections.emptyList());
            when(attributeMapper.deleteById(attributeId)).thenReturn(1);
            when(attributeValueMapper.countValuesByAttributeId(attributeId)).thenReturn(0);

            assertDoesNotThrow(() -> attributeService.deleteAttribute(attributeId));

            verify(attributeMapper, times(1)).deleteById(attributeId);
            verify(attributeValueMapper, times(1)).countValuesByAttributeId(attributeId);
            verify(attributeValueMapper, never()).deleteByAttributeId(any());
        }

        @Test
        @DisplayName("Should throw BizException when attribute not found")
        void deleteAttribute_WhenAttributeNotFound_ShouldThrowBizException() {
            Long nonExistentId = 999L;
            when(attributeMapper.findById(nonExistentId)).thenReturn(null);

            BizException exception = assertThrows(BizException.class,
                () -> attributeService.deleteAttribute(nonExistentId));

            assertEquals(ErrorCode.ATTRIBUTE_NOT_FOUND, exception.getErrorCode());
            verify(attributeMapper, times(1)).findById(nonExistentId);
            verify(attributeMapper, never()).deleteById(any());
        }

        @Test
        @DisplayName("Should throw BizException when attribute is referenced by items")
        void deleteAttribute_WhenReferencedByItems_ShouldThrowBizException() {
            Long attributeId = 1L;
            ItemAttributeRelationEntity relationEntity = new ItemAttributeRelationEntity();
            relationEntity.setItemId(100L);
            relationEntity.setAttributeId(attributeId);

            when(attributeMapper.findById(attributeId)).thenReturn(testAttributeEntity);
            when(itemAttributeRelationMapper.findByAttributeId(attributeId, 0, 1))
                .thenReturn(Arrays.asList(relationEntity));

            BizException exception = assertThrows(BizException.class,
                () -> attributeService.deleteAttribute(attributeId));

            assertEquals(ErrorCode.ATTRIBUTE_IS_REFERENCE_BY_ITEM, exception.getErrorCode());
            verify(attributeMapper, never()).deleteById(any());
        }

        @Test
        @DisplayName("Should throw BizException when attribute deletion fails")
        void deleteAttribute_WhenDeletionFails_ShouldThrowBizException() {
            Long attributeId = 1L;
            when(attributeMapper.findById(attributeId)).thenReturn(testAttributeEntity);
            when(itemAttributeRelationMapper.findByAttributeId(attributeId, 0, 1))
                .thenReturn(Collections.emptyList());
            when(attributeMapper.deleteById(attributeId)).thenReturn(0);

            BizException exception = assertThrows(BizException.class,
                () -> attributeService.deleteAttribute(attributeId));

            assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, exception.getErrorCode());
        }

        @Test
        @DisplayName("Should throw BizException when attribute value deletion fails")
        void deleteAttribute_WhenValueDeletionFails_ShouldThrowBizException() {
            Long attributeId = 1L;
            when(attributeMapper.findById(attributeId)).thenReturn(testAttributeEntity);
            when(itemAttributeRelationMapper.findByAttributeId(attributeId, 0, 1))
                .thenReturn(Collections.emptyList());
            when(attributeMapper.deleteById(attributeId)).thenReturn(1);
            when(attributeValueMapper.countValuesByAttributeId(attributeId)).thenReturn(2);
            when(attributeValueMapper.deleteByAttributeId(attributeId)).thenReturn(1);

            BizException exception = assertThrows(BizException.class,
                () -> attributeService.deleteAttribute(attributeId));

            assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("GetAttributeById Tests")
    class GetAttributeByIdTests {

        @Test
        @DisplayName("Should return attribute when valid ID provided")
        void getAttributeById_WhenValidIdProvided_ShouldReturnAttribute() {
            Long attributeId = 1L;
            when(attributeMapper.findById(attributeId)).thenReturn(testAttributeEntity);

            Attribute result = attributeService.getAttributeById(attributeId);

            assertNotNull(result);
            assertEquals(testAttributeEntity.getId(), result.getId());
            assertEquals(testAttributeEntity.getName(), result.getName());
            verify(attributeMapper, times(1)).findById(attributeId);
        }

        @Test
        @DisplayName("Should throw BizException when attribute not found")
        void getAttributeById_WhenAttributeNotFound_ShouldThrowBizException() {
            Long nonExistentId = 999L;
            when(attributeMapper.findById(nonExistentId)).thenReturn(null);

            BizException exception = assertThrows(BizException.class,
                () -> attributeService.getAttributeById(nonExistentId));

            assertEquals(ErrorCode.ATTRIBUTE_NOT_FOUND, exception.getErrorCode());
            verify(attributeMapper, times(1)).findById(nonExistentId);
        }
    }

    @Nested
    @DisplayName("GetAttributeByIdWithValues Tests")
    class GetAttributeByIdWithValuesTests {

        @Test
        @DisplayName("Should return attribute with values for SINGLE_SELECT input type")
        void getAttributeByIdWithValues_WhenSingleSelect_ShouldReturnAttributeWithValues() {
            Long attributeId = 1L;
            testAttributeEntity.setInputType("SINGLE_SELECT");
            List<AttributeValueEntity> valueEntities = Arrays.asList(testAttributeValueEntity);

            when(attributeMapper.findById(attributeId)).thenReturn(testAttributeEntity);
            when(attributeValueMapper.findAllAttributeValuesByAttributeId(attributeId))
                .thenReturn(valueEntities);

            Attribute result = attributeService.getAttributeByIdWithValues(attributeId);

            assertNotNull(result);
            assertNotNull(result.getValues());
            assertEquals(1, result.getValues().size());
            assertEquals(testAttributeValueEntity.getValue(), result.getValues().get(0).getValue());
            verify(attributeValueMapper, times(1)).findAllAttributeValuesByAttributeId(attributeId);
        }

        @Test
        @DisplayName("Should return attribute with values for MULTI_SELECT input type")
        void getAttributeByIdWithValues_WhenMultiSelect_ShouldReturnAttributeWithValues() {
            Long attributeId = 1L;
            testAttributeEntity.setInputType("MULTI_SELECT");
            List<AttributeValueEntity> valueEntities = Arrays.asList(testAttributeValueEntity);

            when(attributeMapper.findById(attributeId)).thenReturn(testAttributeEntity);
            when(attributeValueMapper.findAllAttributeValuesByAttributeId(attributeId))
                .thenReturn(valueEntities);

            Attribute result = attributeService.getAttributeByIdWithValues(attributeId);

            assertNotNull(result);
            assertNotNull(result.getValues());
            assertEquals(1, result.getValues().size());
            verify(attributeValueMapper, times(1)).findAllAttributeValuesByAttributeId(attributeId);
        }

        @Test
        @DisplayName("Should return attribute without values for TEXT input type")
        void getAttributeByIdWithValues_WhenTextInput_ShouldReturnAttributeWithoutValues() {
            Long attributeId = 1L;
            testAttributeEntity.setInputType("TEXT");

            when(attributeMapper.findById(attributeId)).thenReturn(testAttributeEntity);

            Attribute result = attributeService.getAttributeByIdWithValues(attributeId);

            assertNotNull(result);
            assertNull(result.getValues());
            verify(attributeValueMapper, never()).findAllAttributeValuesByAttributeId(any());
        }
    }

    @Nested
    @DisplayName("AttributeValue Tests")
    class AttributeValueTests {

        @Test
        @DisplayName("Should find all attribute values for select input types")
        void findAllAttributeValuesByAttributeId_WhenSelectInputType_ShouldReturnValues() {
            Long attributeId = 1L;
            testAttributeEntity.setInputType("SINGLE_SELECT");
            List<AttributeValueEntity> valueEntities = Arrays.asList(testAttributeValueEntity);

            when(attributeMapper.findById(attributeId)).thenReturn(testAttributeEntity);
            when(attributeValueMapper.findAllAttributeValuesByAttributeId(attributeId))
                .thenReturn(valueEntities);

            List<AttributeValue> result = attributeService.findAllAttributeValuesByAttributeId(attributeId);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testAttributeValueEntity.getValue(), result.get(0).getValue());
            verify(attributeValueMapper, times(1)).findAllAttributeValuesByAttributeId(attributeId);
        }

        @Test
        @DisplayName("Should return empty list for non-select input types")
        void findAllAttributeValuesByAttributeId_WhenNonSelectInputType_ShouldReturnEmptyList() {
            Long attributeId = 1L;
            testAttributeEntity.setInputType("TEXT");

            when(attributeMapper.findById(attributeId)).thenReturn(testAttributeEntity);

            List<AttributeValue> result = attributeService.findAllAttributeValuesByAttributeId(attributeId);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(attributeValueMapper, never()).findAllAttributeValuesByAttributeId(any());
        }

        @Test
        @DisplayName("Should get attribute value by ID successfully")
        void getAttributeValueById_WhenValidIdProvided_ShouldReturnAttributeValue() {
            Long valueId = 1L;
            when(attributeValueMapper.findById(valueId)).thenReturn(testAttributeValueEntity);

            AttributeValue result = attributeService.getAttributeValueById(valueId);

            assertNotNull(result);
            assertEquals(testAttributeValueEntity.getId(), result.getId());
            assertEquals(testAttributeValueEntity.getValue(), result.getValue());
            verify(attributeValueMapper, times(1)).findById(valueId);
        }

        @Test
        @DisplayName("Should throw BizException when attribute value not found")
        void getAttributeValueById_WhenValueNotFound_ShouldThrowBizException() {
            Long nonExistentId = 999L;
            when(attributeValueMapper.findById(nonExistentId)).thenReturn(null);

            BizException exception = assertThrows(BizException.class,
                () -> attributeService.getAttributeValueById(nonExistentId));

            assertEquals(ErrorCode.ATTRIBUTE_VALUE_NOT_FOUND, exception.getErrorCode());
            verify(attributeValueMapper, times(1)).findById(nonExistentId);
        }
    }

    @Nested
    @DisplayName("EnsureItemAttributes Tests")
    class EnsureItemAttributesTests {

        @Test
        @DisplayName("Should create new item attributes when no existing relations")
        void ensureItemAttributes_WhenNoExistingRelations_ShouldCreateNewAttributes() {
            Long itemId = 1L;
            Long skuId = 1L;
            ItemAttributeRequest attributeRequest = new ItemAttributeRequest();
            attributeRequest.setAttributeId(1L);
            attributeRequest.setAttributeValueId(1L);
            attributeRequest.setValue("Red");
            List<ItemAttributeRequest> attributes = Arrays.asList(attributeRequest);

            when(itemAttributeRelationMapper.findByItemIdAndSkuId(itemId, skuId))
                .thenReturn(Collections.emptyList());
            when(itemAttributeRelationMapper.batchInsert(anyList())).thenReturn(1);

            assertDoesNotThrow(() -> attributeService.ensureItemAttributes(itemId, skuId, attributes));

            verify(itemAttributeRelationMapper, times(1)).findByItemIdAndSkuId(itemId, skuId);
            verify(itemAttributeRelationMapper, times(1)).batchInsert(anyList());
            verify(itemAttributeRelationMapper, never()).deleteByItemIdAndAttributeIds(any(), any());
        }

        @Test
        @DisplayName("Should update existing item attributes when relations exist")
        void ensureItemAttributes_WhenExistingRelations_ShouldUpdateAttributes() {
            Long itemId = 1L;
            Long skuId = 1L;

            ItemAttributeRelationEntity existingRelation = new ItemAttributeRelationEntity();
            existingRelation.setItemId(itemId);
            existingRelation.setSkuId(skuId);
            existingRelation.setAttributeId(2L);

            ItemAttributeRequest newAttributeRequest = new ItemAttributeRequest();
            newAttributeRequest.setAttributeId(1L);
            newAttributeRequest.setAttributeValueId(1L);
            newAttributeRequest.setValue("Red");
            List<ItemAttributeRequest> attributes = Arrays.asList(newAttributeRequest);

            when(itemAttributeRelationMapper.findByItemIdAndSkuId(itemId, skuId))
                .thenReturn(Arrays.asList(existingRelation));
            when(itemAttributeRelationMapper.deleteByItemIdAndAttributeIds(eq(itemId), anyList()))
                .thenReturn(1);
            when(itemAttributeRelationMapper.batchInsert(anyList())).thenReturn(1);

            assertDoesNotThrow(() -> attributeService.ensureItemAttributes(itemId, skuId, attributes));

            verify(itemAttributeRelationMapper, times(1)).findByItemIdAndSkuId(itemId, skuId);
            verify(itemAttributeRelationMapper, times(1)).deleteByItemIdAndAttributeIds(eq(itemId), anyList());
            verify(itemAttributeRelationMapper, times(1)).batchInsert(anyList());
        }

        @Test
        @DisplayName("Should not insert when no new relations to create")
        void ensureItemAttributes_WhenNoNewRelations_ShouldNotInsert() {
            Long itemId = 1L;
            Long skuId = 1L;

            ItemAttributeRelationEntity existingRelation = new ItemAttributeRelationEntity();
            existingRelation.setItemId(itemId);
            existingRelation.setSkuId(skuId);
            existingRelation.setAttributeId(1L);

            ItemAttributeRequest sameAttributeRequest = new ItemAttributeRequest();
            sameAttributeRequest.setAttributeId(1L);
            sameAttributeRequest.setAttributeValueId(1L);
            sameAttributeRequest.setValue("Red");
            List<ItemAttributeRequest> attributes = Arrays.asList(sameAttributeRequest);

            when(itemAttributeRelationMapper.findByItemIdAndSkuId(itemId, skuId))
                .thenReturn(Arrays.asList(existingRelation));
            when(itemAttributeRelationMapper.deleteByItemIdAndAttributeIds(eq(itemId), anyList()))
                .thenReturn(1);

            assertDoesNotThrow(() -> attributeService.ensureItemAttributes(itemId, skuId, attributes));

            verify(itemAttributeRelationMapper, times(1)).findByItemIdAndSkuId(itemId, skuId);
            verify(itemAttributeRelationMapper, times(1)).deleteByItemIdAndAttributeIds(eq(itemId), anyList());
            verify(itemAttributeRelationMapper, never()).batchInsert(anyList());
        }

        @Test
        @DisplayName("Should throw BizException when deletion fails")
        void ensureItemAttributes_WhenDeletionFails_ShouldThrowBizException() {
            Long itemId = 1L;
            Long skuId = 1L;

            ItemAttributeRelationEntity existingRelation = new ItemAttributeRelationEntity();
            existingRelation.setItemId(itemId);
            existingRelation.setSkuId(skuId);
            existingRelation.setAttributeId(2L);

            ItemAttributeRequest attributeRequest = new ItemAttributeRequest();
            attributeRequest.setAttributeId(1L);
            List<ItemAttributeRequest> attributes = Arrays.asList(attributeRequest);

            when(itemAttributeRelationMapper.findByItemIdAndSkuId(itemId, skuId))
                .thenReturn(Arrays.asList(existingRelation));
            when(itemAttributeRelationMapper.deleteByItemIdAndAttributeIds(eq(itemId), anyList()))
                .thenReturn(0);

            BizException exception = assertThrows(BizException.class,
                () -> attributeService.ensureItemAttributes(itemId, skuId, attributes));

            assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, exception.getErrorCode());
        }

        @Test
        @DisplayName("Should throw BizException when batch insert fails")
        void ensureItemAttributes_WhenBatchInsertFails_ShouldThrowBizException() {
            Long itemId = 1L;
            Long skuId = 1L;
            ItemAttributeRequest attributeRequest = new ItemAttributeRequest();
            attributeRequest.setAttributeId(1L);
            List<ItemAttributeRequest> attributes = Arrays.asList(attributeRequest);

            when(itemAttributeRelationMapper.findByItemIdAndSkuId(itemId, skuId))
                .thenReturn(Collections.emptyList());
            when(itemAttributeRelationMapper.batchInsert(anyList())).thenReturn(0);

            BizException exception = assertThrows(BizException.class,
                () -> attributeService.ensureItemAttributes(itemId, skuId, attributes));

            assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle null parameter validation correctly")
        void methods_WhenNullParametersProvided_ShouldHandleGracefully() {
            assertThrows(Exception.class, () -> attributeService.getAttributeById(null));
            assertThrows(Exception.class, () -> attributeService.deleteAttribute(null));
            assertThrows(Exception.class, () -> attributeService.updateAttribute(null, updateRequest));
            assertThrows(Exception.class, () -> attributeService.getAttributeValueById(null));
        }

        @Test
        @DisplayName("Should handle conversion methods correctly")
        void conversionMethods_ShouldConvertCorrectly() {
            when(attributeMapper.findById(1L)).thenReturn(testAttributeEntity);

            Attribute result = attributeService.getAttributeById(1L);

            assertEquals(testAttributeEntity.getId(), result.getId());
            assertEquals(testAttributeEntity.getName(), result.getName());
            assertEquals(AttributeType.valueOf(testAttributeEntity.getAttributeType()), result.getAttributeType());
            assertEquals(AttributeInputType.valueOf(testAttributeEntity.getInputType()), result.getInputType());
        }

        @Test
        @DisplayName("Should handle empty collections correctly")
        void methods_WhenEmptyCollections_ShouldHandleCorrectly() {
            Long itemId = 1L;
            Long skuId = 1L;
            List<ItemAttributeRequest> emptyAttributes = Collections.emptyList();

            when(itemAttributeRelationMapper.findByItemIdAndSkuId(itemId, skuId))
                .thenReturn(Collections.emptyList());

            assertDoesNotThrow(() -> attributeService.ensureItemAttributes(itemId, skuId, emptyAttributes));
            verify(itemAttributeRelationMapper, never()).batchInsert(anyList());
        }
    }
}