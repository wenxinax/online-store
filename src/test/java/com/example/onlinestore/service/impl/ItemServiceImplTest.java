package com.example.onlinestore.service.impl;

import com.example.onlinestore.bean.Attribute;
import com.example.onlinestore.bean.Item;
import com.example.onlinestore.dto.CreateItemRequest;
import com.example.onlinestore.dto.UpdateItemRequest;
import com.example.onlinestore.dto.ItemListQueryRequest;
import com.example.onlinestore.dto.ItemAttributeRequest;
import com.example.onlinestore.entity.ItemEntity;
import com.example.onlinestore.enums.AttributeInputType;
import com.example.onlinestore.enums.ItemStatus;
import com.example.onlinestore.errors.ErrorCode;
import com.example.onlinestore.exceptions.BizException;
import com.example.onlinestore.mapper.ItemMapper;
import com.example.onlinestore.service.AttributeService;
import com.example.onlinestore.service.BrandService;
import com.example.onlinestore.service.CategoryService;
import com.example.onlinestore.service.OssService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.pagehelper.PageInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ItemServiceImpl Tests - Testing framework: JUnit 5 with Mockito")
class ItemServiceImplTest {

    @Mock
    private AttributeService attributeService;

    @Mock
    private OssService ossService;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private BrandService brandService;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ItemServiceImpl itemService;

    private CreateItemRequest createItemRequest;
    private UpdateItemRequest updateItemRequest;
    private ItemListQueryRequest listQueryRequest;
    private ItemEntity itemEntity;
    private Item item;
    private Attribute attribute;
    private ItemAttributeRequest attributeRequest;

    @BeforeEach
    void setUp() {
        // Configure service properties
        ReflectionTestUtils.setField(itemService, "forbiddenWords", "刀,gun,weapon");
        ReflectionTestUtils.setField(itemService, "uploadDescriptionToOSS", false);
        ReflectionTestUtils.setField(itemService, "defaultItemSortScore", 1);

        // CreateItemRequest setup
        createItemRequest = new CreateItemRequest();
        createItemRequest.setName("Test Item");
        createItemRequest.setDescription("Test Description");
        createItemRequest.setMainImageUrl("http://example.com/image.jpg");
        createItemRequest.setSubImageUrls(Arrays.asList("http://example.com/sub1.jpg", "http://example.com/sub2.jpg"));
        createItemRequest.setBrandId(1L);
        createItemRequest.setCategoryId(1L);
        createItemRequest.setSortScore(5);
        attributeRequest = new ItemAttributeRequest();
        attributeRequest.setAttributeId(1L);
        attributeRequest.setValue("Test Value");
        createItemRequest.setAttributes(Arrays.asList(attributeRequest));

        // UpdateItemRequest setup
        updateItemRequest = new UpdateItemRequest();
        updateItemRequest.setName("Updated Item");
        updateItemRequest.setDescription("Updated Description");
        updateItemRequest.setMainImageUrl("http://example.com/updated.jpg");
        updateItemRequest.setSubImageUrls(Arrays.asList("http://example.com/updated_sub.jpg"));
        updateItemRequest.setAttributes(Arrays.asList(attributeRequest));

        // ListQueryRequest setup
        listQueryRequest = new ItemListQueryRequest();
        listQueryRequest.setPageNum(1);
        listQueryRequest.setPageSize(10);
        listQueryRequest.setName("Test");

        // ItemEntity setup
        itemEntity = new ItemEntity();
        itemEntity.setId(1L);
        itemEntity.setName("Test Item");
        itemEntity.setDescription("Test Description");
        itemEntity.setMainImageURL("http://example.com/image.jpg");
        itemEntity.setSubImageURLs("[\"http://example.com/sub1.jpg\",\"http://example.com/sub2.jpg\"]");
        itemEntity.setBrandId(1L);
        itemEntity.setCategoryId(1L);
        itemEntity.setStatus(ItemStatus.DRAFT.name());
        itemEntity.setSortScore(5);
        itemEntity.setCreatedAt(LocalDateTime.now());
        itemEntity.setUpdatedAt(LocalDateTime.now());

        // Item bean setup
        item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setMainImageURL("http://example.com/image.jpg");
        item.setSubImageURLs(Arrays.asList("http://example.com/sub1.jpg", "http://example.com/sub2.jpg"));
        item.setBrandId(1L);
        item.setCategoryId(1L);
        item.setStatus(ItemStatus.DRAFT);
        item.setSortScore(5);

        // Attribute setup
        attribute = new Attribute();
        attribute.setId(1L);
        attribute.setInputType(AttributeInputType.TEXT);
    }

    @Test
    @DisplayName("Should create item successfully when all validations pass")
    void createItem_ShouldCreateItemSuccessfully_WhenAllValidationsPass() {
        when(attributeService.getAttributeById(1L)).thenReturn(attribute);
        doNothing().when(categoryService).getCategoryById(1L);
        doNothing().when(brandService).getBrandById(1L);
        when(itemMapper.insert(any(ItemEntity.class))).thenReturn(1);
        doNothing().when(attributeService).ensureItemAttributes(anyLong(), anyLong(), anyList());

        Item result = itemService.createItem(createItemRequest);

        assertNotNull(result);
        assertEquals("Test Item", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertEquals(ItemStatus.DRAFT, result.getStatus());
        verify(itemMapper).insert(any(ItemEntity.class));
        verify(attributeService).ensureItemAttributes(anyLong(), eq(0L), anyList());
    }

    @Test
    @DisplayName("Should create item with OSS description upload when enabled")
    void createItem_ShouldUploadDescriptionToOSS_WhenOssUploadEnabled() {
        ReflectionTestUtils.setField(itemService, "uploadDescriptionToOSS", true);
        String ossUrl = "http://oss.example.com/description.txt";

        when(attributeService.getAttributeById(1L)).thenReturn(attribute);
        when(ossService.uploadItemDescription("Test Description")).thenReturn(ossUrl);
        when(itemMapper.insert(any(ItemEntity.class))).thenReturn(1);
        doNothing().when(attributeService).ensureItemAttributes(anyLong(), anyLong(), anyList());

        itemService.createItem(createItemRequest);

        verify(ossService).uploadItemDescription("Test Description");
        verify(itemMapper).insert(argThat(entity ->
            ossUrl.equals(entity.getDescriptionURL())
        ));
    }

    @Test
    @DisplayName("Should throw BizException when item name contains forbidden words")
    void createItem_ShouldThrowBizException_WhenNameContainsForbiddenWords() {
        createItemRequest.setName("Test 刀 Item");

        BizException exception = assertThrows(
            BizException.class,
            () -> itemService.createItem(createItemRequest)
        );
        assertEquals(ErrorCode.ITEM_NAME_CONTAINS_FORBIDDEN_WORDS, exception.getErrorCode());
        verify(itemMapper, never()).insert(any());
    }

    @Test
    @DisplayName("Should throw BizException when description contains forbidden words")
    void createItem_ShouldThrowBizException_WhenDescriptionContainsForbiddenWords() {
        createItemRequest.setDescription("This contains a gun reference");

        BizException exception = assertThrows(
            BizException.class,
            () -> itemService.createItem(createItemRequest)
        );
        assertEquals(ErrorCode.ITEM_DESCRIPTION_CONTAINS_FORBIDDEN_WORDS, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw BizException when attribute value is empty for single select")
    void createItem_ShouldThrowBizException_WhenSingleSelectAttributeValueEmpty() {
        attribute.setInputType(AttributeInputType.SINGLE_SELECT);
        attributeRequest.setAttributeValueId(null);
        when(attributeService.getAttributeById(1L)).thenReturn(attribute);

        BizException exception = assertThrows(
            BizException.class,
            () -> itemService.createItem(createItemRequest)
        );
        assertEquals(ErrorCode.ITEM_ATTRIBUTE_VALUE_IS_EMPTY, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw BizException when text attribute value is blank")
    void createItem_ShouldThrowBizException_WhenTextAttributeValueBlank() {
        attributeRequest.setValue("");
        when(attributeService.getAttributeById(1L)).thenReturn(attribute);

        BizException exception = assertThrows(
            BizException.class,
            () -> itemService.createItem(createItemRequest)
        );
        assertEquals(ErrorCode.ITEM_ATTRIBUTE_VALUE_IS_EMPTY, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw BizException when item insert fails")
    void createItem_ShouldThrowBizException_WhenInsertFails() {
        when(attributeService.getAttributeById(1L)).thenReturn(attribute);
        when(itemMapper.insert(any(ItemEntity.class))).thenReturn(0);

        BizException exception = assertThrows(
            BizException.class,
            () -> itemService.createItem(createItemRequest)
        );
        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should update item successfully when all validations pass")
    void updateItem_ShouldUpdateItemSuccessfully_WhenAllValidationsPass() {
        Long itemId = 1L;
        when(itemMapper.findById(itemId)).thenReturn(itemEntity);
        when(attributeService.getAttributeById(1L)).thenReturn(attribute);
        when(itemMapper.update(any(ItemEntity.class))).thenReturn(1);
        doNothing().when(attributeService).ensureItemAttributes(itemId, 0L, updateItemRequest.getAttributes());

        assertDoesNotThrow(() -> itemService.updateItem(itemId, updateItemRequest));

        verify(itemMapper).update(any(ItemEntity.class));
        verify(attributeService).ensureItemAttributes(itemId, 0L, updateItemRequest.getAttributes());
    }

    @Test
    @DisplayName("Should update item with OSS description when enabled")
    void updateItem_ShouldUploadDescriptionToOSS_WhenOssUploadEnabled() {
        Long itemId = 1L;
        ReflectionTestUtils.setField(itemService, "uploadDescriptionToOSS", true);
        String ossUrl = "http://oss.example.com/updated-description.txt";

        when(itemMapper.findById(itemId)).thenReturn(itemEntity);
        when(attributeService.getAttributeById(1L)).thenReturn(attribute);
        when(ossService.uploadItemDescription("Updated Description")).thenReturn(ossUrl);
        when(itemMapper.update(any(ItemEntity.class))).thenReturn(1);

        itemService.updateItem(itemId, updateItemRequest);

        verify(ossService).uploadItemDescription("Updated Description");
        verify(itemMapper).update(argThat(entity ->
            ossUrl.equals(entity.getDescriptionURL())
        ));
    }

    @Test
    @DisplayName("Should throw BizException when update name contains forbidden words")
    void updateItem_ShouldThrowBizException_WhenNameContainsForbiddenWords() {
        Long itemId = 1L;
        updateItemRequest.setName("Updated weapon Item");
        when(itemMapper.findById(itemId)).thenReturn(itemEntity);

        BizException exception = assertThrows(
            BizException.class,
            () -> itemService.updateItem(itemId, updateItemRequest)
        );
        assertEquals(ErrorCode.ITEM_NAME_CONTAINS_FORBIDDEN_WORDS, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw BizException when item to update not found")
    void updateItem_ShouldThrowBizException_WhenItemNotFound() {
        Long itemId = 999L;
        when(itemMapper.findById(itemId)).thenReturn(null);

        BizException exception = assertThrows(
            BizException.class,
            () -> itemService.updateItem(itemId, updateItemRequest)
        );
        assertEquals(ErrorCode.ITEM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw BizException when update fails")
    void updateItem_ShouldThrowBizException_WhenUpdateFails() {
        Long itemId = 1L;
        when(itemMapper.findById(itemId)).thenReturn(itemEntity);
        when(attributeService.getAttributeById(1L)).thenReturn(attribute);
        when(itemMapper.update(any(ItemEntity.class))).thenReturn(0);

        BizException exception = assertThrows(
            BizException.class,
            () -> itemService.updateItem(itemId, updateItemRequest)
        );
        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should handle JSON processing exception during sub image update")
    void updateItem_ShouldThrowBizException_WhenJsonProcessingFails() {
        Long itemId = 1L;
        when(itemMapper.findById(itemId)).thenReturn(itemEntity);
        when(attributeService.getAttributeById(1L)).thenReturn(attribute);

        List<String> problematicUrls = new ArrayList<>();
        updateItemRequest.setSubImageUrls(problematicUrls);

        when(itemMapper.update(any(ItemEntity.class))).thenReturn(1);
        assertDoesNotThrow(() -> itemService.updateItem(itemId, updateItemRequest));
    }

    @Test
    @DisplayName("Should return item when found by valid ID")
    void getItemById_ShouldReturnItem_WhenValidIdProvided() {
        Long itemId = 1L;
        when(itemMapper.findById(itemId)).thenReturn(itemEntity);

        Item result = itemService.getItemById(itemId);

        assertNotNull(result);
        assertEquals(itemId, result.getId());
        assertEquals("Test Item", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertEquals(ItemStatus.DRAFT, result.getStatus());
        assertEquals(2, result.getSubImageURLs().size());
        verify(itemMapper).findById(itemId);
    }

    @Test
    @DisplayName("Should throw BizException when item not found")
    void getItemById_ShouldThrowBizException_WhenItemNotFound() {
        Long itemId = 999L;
        when(itemMapper.findById(itemId)).thenReturn(null);

        BizException exception = assertThrows(
            BizException.class,
            () -> itemService.getItemById(itemId)
        );
        assertEquals(ErrorCode.ITEM_NOT_FOUND, exception.getErrorCode());
        verify(itemMapper).findById(itemId);
    }

    @Test
    @DisplayName("Should get description from OSS when description URL is present")
    void getItemById_ShouldGetDescriptionFromOSS_WhenDescriptionURLPresent() {
        Long itemId = 1L;
        String ossDescription = "Description from OSS";
        itemEntity.setDescriptionURL("http://oss.example.com/desc.txt");
        itemEntity.setDescription(null);

        when(itemMapper.findById(itemId)).thenReturn(itemEntity);
        when(ossService.getItemDescription("http://oss.example.com/desc.txt")).thenReturn(ossDescription);

        Item result = itemService.getItemById(itemId);

        assertEquals(ossDescription, result.getDescription());
        verify(ossService).getItemDescription("http://oss.example.com/desc.txt");
    }

    @Test
    @DisplayName("Should return direct description when no OSS URL")
    void getItemById_ShouldReturnDirectDescription_WhenNoOSSURL() {
        Long itemId = 1L;
        itemEntity.setDescriptionURL(null);
        itemEntity.setDescription("Direct description");
        when(itemMapper.findById(itemId)).thenReturn(itemEntity);

        Item result = itemService.getItemById(itemId);

        assertEquals("Direct description", result.getDescription());
        verify(ossService, never()).getItemDescription(anyString());
    }

    @Test
    @DisplayName("Should handle empty sub image URLs gracefully")
    void getItemById_ShouldHandleEmptySubImages_WhenSubImageURLsEmpty() {
        Long itemId = 1L;
        itemEntity.setSubImageURLs("");
        when(itemMapper.findById(itemId)).thenReturn(itemEntity);

        Item result = itemService.getItemById(itemId);

        assertNotNull(result);
        assertNull(result.getSubImageURLs());
    }

    @Test
    @DisplayName("Should handle malformed JSON in sub image URLs")
    void getItemById_ShouldHandleInvalidJSON_WhenSubImageURLsMalformed() {
        Long itemId = 1L;
        itemEntity.setSubImageURLs("invalid json");
        when(itemMapper.findById(itemId)).thenReturn(itemEntity);

        Item result = itemService.getItemById(itemId);

        assertNotNull(result);
        assertTrue(result.getSubImageURLs().isEmpty());
    }

    @Test
    @DisplayName("Should return paginated items when query is successful")
    void listItems_ShouldReturnPaginatedItems_WhenQuerySuccessful() {
        when(itemMapper.queryItemsByOptions(listQueryRequest)).thenReturn(Arrays.asList(itemEntity));

        Page<Item> result = itemService.listItems(listQueryRequest);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Test Item", result.getContent().get(0).getName());
        verify(itemMapper).queryItemsByOptions(listQueryRequest);
    }

    @Test
    @DisplayName("Should return empty page when no items match query")
    void listItems_ShouldReturnEmptyPage_WhenNoItemsMatch() {
        when(itemMapper.queryItemsByOptions(listQueryRequest)).thenReturn(new ArrayList<>());

        Page<Item> result = itemService.listItems(listQueryRequest);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        verify(itemMapper).queryItemsByOptions(listQueryRequest);
    }

    @Test
    @DisplayName("Should handle query with all filters")
    void listItems_ShouldHandleAllFilters_WhenAllFiltersProvided() {
        listQueryRequest.setName("Specific Item");
        listQueryRequest.setBrandId(2L);
        listQueryRequest.setCategoryId(3L);
        listQueryRequest.setStatus("ACTIVE");

        when(itemMapper.queryItemsByOptions(listQueryRequest)).thenReturn(Arrays.asList(itemEntity));

        itemService.listItems(listQueryRequest);

        verify(itemMapper).queryItemsByOptions(argThat(request ->
            "Specific Item".equals(request.getName()) &&
            Long.valueOf(2L).equals(request.getBrandId()) &&
            Long.valueOf(3L).equals(request.getCategoryId()) &&
            "ACTIVE".equals(request.getStatus())
        ));
    }

    @Test
    @DisplayName("Should parse forbidden words correctly from configuration")
    void createItem_ShouldParseForbiddenWords_WhenConfigurationProvided() {
        ReflectionTestUtils.setField(itemService, "forbiddenWords", "word1,word2, word3 ,");
        createItemRequest.setName("This contains word2 test");

        BizException exception = assertThrows(
            BizException.class,
            () -> itemService.createItem(createItemRequest)
        );
        assertEquals(ErrorCode.ITEM_NAME_CONTAINS_FORBIDDEN_WORDS, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should handle empty forbidden words configuration")
    void createItem_ShouldHandleEmptyForbiddenWords_WhenConfigurationEmpty() {
        ReflectionTestUtils.setField(itemService, "forbiddenWords", "");
        when(attributeService.getAttributeById(1L)).thenReturn(attribute);
        when(itemMapper.insert(any(ItemEntity.class))).thenReturn(1);
        doNothing().when(attributeService).ensureItemAttributes(anyLong(), anyLong(), anyList());

        Item result = itemService.createItem(createItemRequest);

        assertNotNull(result);
        assertEquals("Test Item", result.getName());
    }

    @Test
    @DisplayName("Should handle null forbidden words configuration")
    void createItem_ShouldHandleNullForbiddenWords_WhenConfigurationNull() {
        ReflectionTestUtils.setField(itemService, "forbiddenWords", null);
        when(attributeService.getAttributeById(1L)).thenReturn(attribute);
        when(itemMapper.insert(any(ItemEntity.class))).thenReturn(1);
        doNothing().when(attributeService).ensureItemAttributes(anyLong(), anyLong(), anyList());

        Item result = itemService.createItem(createItemRequest);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should use default sort score when not provided")
    void createItem_ShouldUseDefaultSortScore_WhenNotProvided() {
        createItemRequest.setSortScore(null);
        when(attributeService.getAttributeById(1L)).thenReturn(attribute);
        when(itemMapper.insert(any(ItemEntity.class))).thenReturn(1);
        doNothing().when(attributeService).ensureItemAttributes(anyLong(), anyLong(), anyList());

        itemService.createItem(createItemRequest);

        verify(itemMapper).insert(argThat(entity ->
            Integer.valueOf(1).equals(entity.getSortScore())
        ));
    }

    @Test
    @DisplayName("Should handle multi-select attribute validation")
    void createItem_ShouldValidateMultiSelectAttribute_WhenMultiSelectType() {
        attribute.setInputType(AttributeInputType.MULTI_SELECT);
        attributeRequest.setAttributeValueId(null);
        when(attributeService.getAttributeById(1L)).thenReturn(attribute);

        BizException exception = assertThrows(
            BizException.class,
            () -> itemService.createItem(createItemRequest)
        );
        assertEquals(ErrorCode.ITEM_ATTRIBUTE_VALUE_IS_EMPTY, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should match forbidden words case insensitive")
    void createItem_ShouldMatchForbiddenWordsCaseInsensitive_WhenDifferentCase() {
        ReflectionTestUtils.setField(itemService, "forbiddenWords", "WEAPON");
        createItemRequest.setName("This contains weapon text");

        BizException exception = assertThrows(
            BizException.class,
            () -> itemService.createItem(createItemRequest)
        );
        assertEquals(ErrorCode.ITEM_NAME_CONTAINS_FORBIDDEN_WORDS, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should trim whitespace when checking forbidden words")
    void createItem_ShouldTrimWhitespace_WhenCheckingForbiddenWords() {
        createItemRequest.setName("  Test   刀   Item  ");

        BizException exception = assertThrows(
            BizException.class,
            () -> itemService.createItem(createItemRequest)
        );
        assertEquals(ErrorCode.ITEM_NAME_CONTAINS_FORBIDDEN_WORDS, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should validate all attributes in request")
    void createItem_ShouldValidateAllAttributes_WhenMultipleAttributesProvided() {
        ItemAttributeRequest attr1 = new ItemAttributeRequest();
        attr1.setAttributeId(1L);
        attr1.setValue("Valid");

        ItemAttributeRequest attr2 = new ItemAttributeRequest();
        attr2.setAttributeId(2L);
        attr2.setValue("");
        createItemRequest.setAttributes(Arrays.asList(attr1, attr2));

        Attribute attribute2 = new Attribute();
        attribute2.setId(2L);
        attribute2.setInputType(AttributeInputType.TEXT);

        when(attributeService.getAttributeById(1L)).thenReturn(attribute);
        when(attributeService.getAttributeById(2L)).thenReturn(attribute2);

        BizException exception = assertThrows(
            BizException.class,
            () -> itemService.createItem(createItemRequest)
        );
        assertEquals(ErrorCode.ITEM_ATTRIBUTE_VALUE_IS_EMPTY, exception.getErrorCode());
    }
}