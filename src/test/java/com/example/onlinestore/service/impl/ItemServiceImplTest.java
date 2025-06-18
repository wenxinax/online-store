@BeforeEach
    void setUp() {
        // Set up configuration properties
        ReflectionTestUtils.setField(itemService, "forbiddenWords", "刀,weapon");
        ReflectionTestUtils.setField(itemService, "uploadDescriptionToOSS", false);
        ReflectionTestUtils.setField(itemService, "defaultItemSortScore", 1);
        
        // Set up test data
        ItemAttributeRequest attributeRequest = new ItemAttributeRequest();
        attributeRequest.setAttributeId(1L);
        attributeRequest.setValue("test value");
        
        createItemRequest = new CreateItemRequest();
        createItemRequest.setName("Test Item");
        createItemRequest.setDescription("Test Description");
        createItemRequest.setMainImageUrl("http://example.com/image.jpg");
        createItemRequest.setSubImageUrls(Arrays.asList("http://example.com/sub1.jpg"));
        createItemRequest.setBrandId(1L);
        createItemRequest.setCategoryId(1L);
        createItemRequest.setSortScore(5);
        createItemRequest.setAttributes(Arrays.asList(attributeRequest));
        
        updateItemRequest = new UpdateItemRequest();
        updateItemRequest.setName("Updated Item");
        updateItemRequest.setDescription("Updated Description");
        updateItemRequest.setMainImageUrl("http://example.com/updated.jpg");
        updateItemRequest.setSubImageUrls(Arrays.asList("http://example.com/sub2.jpg"));
        updateItemRequest.setAttributes(Arrays.asList(attributeRequest));
        
        queryRequest = new ItemListQueryRequest();
        queryRequest.setPageNum(1);
        queryRequest.setPageSize(10);
        queryRequest.setCategoryId(1L);
        
        itemEntity = new ItemEntity();
        itemEntity.setId(1L);
        itemEntity.setName("Test Item");
        itemEntity.setDescription("Test Description");
        itemEntity.setMainImageURL("http://example.com/image.jpg");
        itemEntity.setSubImageURLs("[\"http://example.com/sub1.jpg\"]");
        itemEntity.setBrandId(1L);
        itemEntity.setCategoryId(1L);
        itemEntity.setStatus(ItemStatus.DRAFT.name());
        itemEntity.setSortScore(5);
        itemEntity.setCreatedAt(LocalDateTime.now());
        itemEntity.setUpdatedAt(LocalDateTime.now());
        
        attribute = new Attribute();
        attribute.setId(1L);
        attribute.setInputType(AttributeInputType.TEXT);
        
        item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setStatus(ItemStatus.DRAFT);
    }

    @Nested
    @DisplayName("Create Item Tests")
    class CreateItemTests {
        
        @Test
        @DisplayName("Should create item successfully with valid data")
        void shouldCreateItemSuccessfully() {
            // Given
            when(attributeService.getAttributeById(1L)).thenReturn(attribute);
            when(categoryService.getCategoryById(1L)).thenReturn(null); // void method
            when(brandService.getBrandById(1L)).thenReturn(null); // void method
            when(itemMapper.insert(any(ItemEntity.class))).thenAnswer(invocation -> {
                ItemEntity entity = invocation.getArgument(0);
                entity.setId(1L);
                return 1;
            });
            
            // When
            Item result = itemService.createItem(createItemRequest);
            
            // Then
            assertNotNull(result);
            assertEquals("Test Item", result.getName());
            assertEquals("Test Description", result.getDescription());
            verify(itemMapper, times(1)).insert(any(ItemEntity.class));
            verify(attributeService, times(1)).ensureItemAttributes(eq(1L), eq(0L), anyList());
        }
        
        @Test
        @DisplayName("Should throw BizException when item name contains forbidden words")
        void shouldThrowExceptionWhenNameContainsForbiddenWords() {
            // Given
            createItemRequest.setName("Dangerous 刀 item");
            
            // When & Then
            BizException exception = assertThrows(BizException.class, () -> {
                itemService.createItem(createItemRequest);
            });
            assertEquals(ErrorCode.ITEM_NAME_CONTAINS_FORBIDDEN_WORDS, exception.getErrorCode());
            verify(itemMapper, never()).insert(any());
        }
        
        @Test
        @DisplayName("Should throw BizException when description contains forbidden words")
        void shouldThrowExceptionWhenDescriptionContainsForbiddenWords() {
            // Given
            createItemRequest.setDescription("This contains weapon word");
            
            // When & Then
            BizException exception = assertThrows(BizException.class, () -> {
                itemService.createItem(createItemRequest);
            });
            assertEquals(ErrorCode.ITEM_DESCRIPTION_CONTAINS_FORBIDDEN_WORDS, exception.getErrorCode());
            verify(itemMapper, never()).insert(any());
        }
        
        @Test
        @DisplayName("Should throw BizException when attribute value is empty for select type")
        void shouldThrowExceptionWhenAttributeValueEmptyForSelectType() {
            // Given
            attribute.setInputType(AttributeInputType.SINGLE_SELECT);
            ItemAttributeRequest attrReq = createItemRequest.getAttributes().get(0);
            attrReq.setAttributeValueId(null);
            
            when(attributeService.getAttributeById(1L)).thenReturn(attribute);
            
            // When & Then
            BizException exception = assertThrows(BizException.class, () -> {
                itemService.createItem(createItemRequest);
            });
            assertEquals(ErrorCode.ITEM_ATTRIBUTE_VALUE_IS_EMPTY, exception.getErrorCode());
        }
        
        @Test
        @DisplayName("Should throw BizException when attribute value is blank for text type")
        void shouldThrowExceptionWhenAttributeValueBlankForTextType() {
            // Given
            ItemAttributeRequest attrReq = createItemRequest.getAttributes().get(0);
            attrReq.setValue("");
            
            when(attributeService.getAttributeById(1L)).thenReturn(attribute);
            
            // When & Then
            BizException exception = assertThrows(BizException.class, () -> {
                itemService.createItem(createItemRequest);
            });
            assertEquals(ErrorCode.ITEM_ATTRIBUTE_VALUE_IS_EMPTY, exception.getErrorCode());
        }
        
        @Test
        @DisplayName("Should upload description to OSS when configured")
        void shouldUploadDescriptionToOSSWhenConfigured() {
            // Given
            ReflectionTestUtils.setField(itemService, "uploadDescriptionToOSS", true);
            String ossUrl = "http://oss.example.com/description.html";
            
            when(attributeService.getAttributeById(1L)).thenReturn(attribute);
            when(categoryService.getCategoryById(1L)).thenReturn(null);
            when(brandService.getBrandById(1L)).thenReturn(null);
            when(ossService.uploadItemDescription("Test Description")).thenReturn(ossUrl);
            when(itemMapper.insert(any(ItemEntity.class))).thenAnswer(invocation -> {
                ItemEntity entity = invocation.getArgument(0);
                entity.setId(1L);
                return 1;
            });
            
            // When
            Item result = itemService.createItem(createItemRequest);
            
            // Then
            verify(ossService, times(1)).uploadItemDescription("Test Description");
            verify(itemMapper, times(1)).insert(argThat(entity -> 
                ossUrl.equals(entity.getDescriptionURL())
            ));
        }
        
        @Test
        @DisplayName("Should throw BizException when insert fails")
        void shouldThrowExceptionWhenInsertFails() {
            // Given
            when(attributeService.getAttributeById(1L)).thenReturn(attribute);
            when(categoryService.getCategoryById(1L)).thenReturn(null);
            when(brandService.getBrandById(1L)).thenReturn(null);
            when(itemMapper.insert(any(ItemEntity.class))).thenReturn(0); // No rows affected
            
            // When & Then
            BizException exception = assertThrows(BizException.class, () -> {
                itemService.createItem(createItemRequest);
            });
            assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, exception.getErrorCode());
        }
        
        @Test
        @DisplayName("Should use default sort score when not provided")
        void shouldUseDefaultSortScoreWhenNotProvided() {
            // Given
            createItemRequest.setSortScore(null);
            
            when(attributeService.getAttributeById(1L)).thenReturn(attribute);
            when(categoryService.getCategoryById(1L)).thenReturn(null);
            when(brandService.getBrandById(1L)).thenReturn(null);
            when(itemMapper.insert(any(ItemEntity.class))).thenAnswer(invocation -> {
                ItemEntity entity = invocation.getArgument(0);
                entity.setId(1L);
                return 1;
            });
            
            // When
            itemService.createItem(createItemRequest);
            
            // Then
            verify(itemMapper, times(1)).insert(argThat(entity -> 
                entity.getSortScore() == 1 // default value
            ));
        }
    }

    @Nested
    @DisplayName("Update Item Tests")  
    class UpdateItemTests {
        
        @Test
        @DisplayName("Should update item successfully with valid data")
        void shouldUpdateItemSuccessfully() {
            // Given
            when(itemMapper.findById(1L)).thenReturn(itemEntity);
            when(attributeService.getAttributeById(1L)).thenReturn(attribute);
            when(itemMapper.update(any(ItemEntity.class))).thenReturn(1);
            
            // When
            assertDoesNotThrow(() -> {
                itemService.updateItem(1L, updateItemRequest);
            });
            
            // Then
            verify(itemMapper, times(1)).findById(1L);
            verify(itemMapper, times(1)).update(any(ItemEntity.class));
            verify(attributeService, times(1)).ensureItemAttributes(eq(1L), eq(0L), anyList());
        }
        
        @Test
        @DisplayName("Should throw BizException when item not found for update")
        void shouldThrowExceptionWhenItemNotFoundForUpdate() {
            // Given
            when(itemMapper.findById(1L)).thenReturn(null);
            
            // When & Then
            BizException exception = assertThrows(BizException.class, () -> {
                itemService.updateItem(1L, updateItemRequest);
            });
            assertEquals(ErrorCode.ITEM_NOT_FOUND, exception.getErrorCode());
            verify(itemMapper, never()).update(any());
        }
        
        @Test
        @DisplayName("Should throw BizException when update name contains forbidden words")
        void shouldThrowExceptionWhenUpdateNameContainsForbiddenWords() {
            // Given
            updateItemRequest.setName("Bad 刀 name");
            when(itemMapper.findById(1L)).thenReturn(itemEntity);
            
            // When & Then
            BizException exception = assertThrows(BizException.class, () -> {
                itemService.updateItem(1L, updateItemRequest);
            });
            assertEquals(ErrorCode.ITEM_NAME_CONTAINS_FORBIDDEN_WORDS, exception.getErrorCode());
        }
        
        @Test
        @DisplayName("Should throw BizException when update description contains forbidden words")
        void shouldThrowExceptionWhenUpdateDescriptionContainsForbiddenWords() {
            // Given
            updateItemRequest.setDescription("Contains weapon in description");
            when(itemMapper.findById(1L)).thenReturn(itemEntity);
            
            // When & Then
            BizException exception = assertThrows(BizException.class, () -> {
                itemService.updateItem(1L, updateItemRequest);
            });
            assertEquals(ErrorCode.ITEM_DESCRIPTION_CONTAINS_FORBIDDEN_WORDS, exception.getErrorCode());
        }
        
        @Test
        @DisplayName("Should throw BizException when update fails")
        void shouldThrowExceptionWhenUpdateFails() {
            // Given
            when(itemMapper.findById(1L)).thenReturn(itemEntity);
            when(attributeService.getAttributeById(1L)).thenReturn(attribute);
            when(itemMapper.update(any(ItemEntity.class))).thenReturn(0); // No rows affected
            
            // When & Then
            BizException exception = assertThrows(BizException.class, () -> {
                itemService.updateItem(1L, updateItemRequest);
            });
            assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, exception.getErrorCode());
        }
        
        @Test
        @DisplayName("Should handle JSON processing exception for sub images")
        void shouldHandleJsonProcessingExceptionForSubImages() {
            // Given - Create a request with problematic sub image URLs that would cause JSON issues
            updateItemRequest.setSubImageUrls(null); // This should be handled gracefully
            
            when(itemMapper.findById(1L)).thenReturn(itemEntity);
            when(attributeService.getAttributeById(1L)).thenReturn(attribute);
            
            // When & Then - Should throw BizException due to JSON processing error
            assertThrows(BizException.class, () -> {
                itemService.updateItem(1L, updateItemRequest);
            });
        }
        
        @Test
        @DisplayName("Should upload description to OSS during update when configured")
        void shouldUploadDescriptionToOSSDuringUpdate() {
            // Given
            ReflectionTestUtils.setField(itemService, "uploadDescriptionToOSS", true);
            String ossUrl = "http://oss.example.com/updated-description.html";
            
            when(itemMapper.findById(1L)).thenReturn(itemEntity);
            when(attributeService.getAttributeById(1L)).thenReturn(attribute);
            when(ossService.uploadItemDescription("Updated Description")).thenReturn(ossUrl);
            when(itemMapper.update(any(ItemEntity.class))).thenReturn(1);
            
            // When
            itemService.updateItem(1L, updateItemRequest);
            
            // Then
            verify(ossService, times(1)).uploadItemDescription("Updated Description");
            verify(itemMapper, times(1)).update(argThat(entity -> 
                ossUrl.equals(entity.getDescriptionURL())
            ));
        }
    }

    @Nested
    @DisplayName("Get Item By ID Tests")
    class GetItemByIdTests {
        
        @Test
        @DisplayName("Should return item when found by valid ID")
        void shouldReturnItemWhenFoundByValidId() {
            // Given
            when(itemMapper.findById(1L)).thenReturn(itemEntity);
            
            // When
            Item result = itemService.getItemById(1L);
            
            // Then
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("Test Item", result.getName());
            assertEquals("Test Description", result.getDescription());
            assertEquals(ItemStatus.DRAFT, result.getStatus());
            verify(itemMapper, times(1)).findById(1L);
        }
        
        @Test
        @DisplayName("Should throw BizException when item not found")
        void shouldThrowExceptionWhenItemNotFound() {
            // Given
            when(itemMapper.findById(999L)).thenReturn(null);
            
            // When & Then
            BizException exception = assertThrows(BizException.class, () -> {
                itemService.getItemById(999L);
            });
            assertEquals(ErrorCode.ITEM_NOT_FOUND, exception.getErrorCode());
            verify(itemMapper, times(1)).findById(999L);
        }
        
        @Test
        @DisplayName("Should get description from OSS when URL is present")
        void shouldGetDescriptionFromOSSWhenUrlPresent() {
            // Given
            itemEntity.setDescriptionURL("http://oss.example.com/desc.html");
            itemEntity.setDescription(null);
            String ossDescription = "Description from OSS";
            
            when(itemMapper.findById(1L)).thenReturn(itemEntity);
            when(ossService.getItemDescription("http://oss.example.com/desc.html")).thenReturn(ossDescription);
            
            // When
            Item result = itemService.getItemById(1L);
            
            // Then
            assertEquals(ossDescription, result.getDescription());
            verify(ossService, times(1)).getItemDescription("http://oss.example.com/desc.html");
        }
        
        @Test
        @DisplayName("Should parse sub image URLs correctly")
        void shouldParseSubImageUrlsCorrectly() {
            // Given
            itemEntity.setSubImageURLs("[\"http://example.com/sub1.jpg\",\"http://example.com/sub2.jpg\"]");
            when(itemMapper.findById(1L)).thenReturn(itemEntity);
            
            // When
            Item result = itemService.getItemById(1L);
            
            // Then
            assertNotNull(result.getSubImageURLs());
            assertEquals(2, result.getSubImageURLs().size());
            assertTrue(result.getSubImageURLs().contains("http://example.com/sub1.jpg"));
            assertTrue(result.getSubImageURLs().contains("http://example.com/sub2.jpg"));
        }
        
        @Test
        @DisplayName("Should handle invalid JSON for sub image URLs gracefully")
        void shouldHandleInvalidJsonForSubImageUrls() {
            // Given
            itemEntity.setSubImageURLs("invalid json");
            when(itemMapper.findById(1L)).thenReturn(itemEntity);
            
            // When
            Item result = itemService.getItemById(1L);
            
            // Then
            assertNotNull(result.getSubImageURLs());
            assertTrue(result.getSubImageURLs().isEmpty());
        }
        
        @Test
        @DisplayName("Should handle empty sub image URLs")
        void shouldHandleEmptySubImageUrls() {
            // Given
            itemEntity.setSubImageURLs("");
            when(itemMapper.findById(1L)).thenReturn(itemEntity);
            
            // When
            Item result = itemService.getItemById(1L);
            
            // Then
            assertNull(result.getSubImageURLs());
        }
    }

    @Nested
    @DisplayName("List Items Tests")
    class ListItemsTests {
        
        @Test
        @DisplayName("Should return paginated list of items")
        void shouldReturnPaginatedListOfItems() {
            // Given
            List<ItemEntity> itemEntities = Arrays.asList(itemEntity);
            when(itemMapper.queryItemsByOptions(queryRequest)).thenReturn(itemEntities);
            
            // Mock PageHelper behavior - this is a bit tricky since PageHelper uses ThreadLocal
            // In a real test, you might want to use @MockedStatic for PageHelper
            
            // When
            Page<Item> result = itemService.listItems(queryRequest);
            
            // Then
            assertNotNull(result);
            assertNotNull(result.getData());
            assertEquals(1, result.getData().size());
            assertEquals("Test Item", result.getData().get(0).getName());
            verify(itemMapper, times(1)).queryItemsByOptions(queryRequest);
        }
        
        @Test
        @DisplayName("Should return empty page when no items found")
        void shouldReturnEmptyPageWhenNoItemsFound() {
            // Given
            when(itemMapper.queryItemsByOptions(queryRequest)).thenReturn(Collections.emptyList());
            
            // When
            Page<Item> result = itemService.listItems(queryRequest);
            
            // Then
            assertNotNull(result);
            assertNotNull(result.getData());
            assertTrue(result.getData().isEmpty());
            verify(itemMapper, times(1)).queryItemsByOptions(queryRequest);
        }
        
        @Test
        @DisplayName("Should handle query with filters")
        void shouldHandleQueryWithFilters() {
            // Given
            queryRequest.setKeyword("test");
            queryRequest.setStatus("ACTIVE");
            queryRequest.setBrandId(2L);
            
            List<ItemEntity> itemEntities = Arrays.asList(itemEntity);
            when(itemMapper.queryItemsByOptions(queryRequest)).thenReturn(itemEntities);
            
            // When
            Page<Item> result = itemService.listItems(queryRequest);
            
            // Then
            assertNotNull(result);
            assertEquals(1, result.getData().size());
            verify(itemMapper, times(1)).queryItemsByOptions(argThat(request -> 
                "test".equals(request.getKeyword()) && 
                "ACTIVE".equals(request.getStatus()) &&
                request.getBrandId().equals(2L)
            ));
        }
        
        @Test
        @DisplayName("Should handle different page sizes")
        void shouldHandleDifferentPageSizes() {
            // Given
            queryRequest.setPageSize(5);
            queryRequest.setPageNum(2);
            
            when(itemMapper.queryItemsByOptions(queryRequest)).thenReturn(Collections.emptyList());
            
            // When
            Page<Item> result = itemService.listItems(queryRequest);
            
            // Then
            assertNotNull(result);
            verify(itemMapper, times(1)).queryItemsByOptions(argThat(request -> 
                request.getPageSize() == 5 && request.getPageNum() == 2
            ));
        }
    }
    
    @Nested
    @DisplayName("Helper Methods Tests")
    class HelperMethodsTests {
        
        @Test
        @DisplayName("Should parse forbidden words correctly")
        void shouldParseForbiddenWordsCorrectly() {
            // Given
            ReflectionTestUtils.setField(itemService, "forbiddenWords", "刀,weapon,gun");
            
            // When - Test through createItem which uses getForbiddenWords internally
            createItemRequest.setName("This contains gun");
            
            // Then
            BizException exception = assertThrows(BizException.class, () -> {
                itemService.createItem(createItemRequest);
            });
            assertEquals(ErrorCode.ITEM_NAME_CONTAINS_FORBIDDEN_WORDS, exception.getErrorCode());
        }
        
        @Test
        @DisplayName("Should handle empty forbidden words configuration")
        void shouldHandleEmptyForbiddenWordsConfiguration() {
            // Given
            ReflectionTestUtils.setField(itemService, "forbiddenWords", "");
            
            when(attributeService.getAttributeById(1L)).thenReturn(attribute);
            when(categoryService.getCategoryById(1L)).thenReturn(null);
            when(brandService.getBrandById(1L)).thenReturn(null);
            when(itemMapper.insert(any(ItemEntity.class))).thenAnswer(invocation -> {
                ItemEntity entity = invocation.getArgument(0);
                entity.setId(1L);
                return 1;
            });
            
            // When - Should not throw exception even with potentially problematic name
            createItemRequest.setName("This contains 刀");
            
            // Then
            assertDoesNotThrow(() -> {
                itemService.createItem(createItemRequest);
            });
        }
        
        @Test
        @DisplayName("Should handle null forbidden words configuration")
        void shouldHandleNullForbiddenWordsConfiguration() {
            // Given
            ReflectionTestUtils.setField(itemService, "forbiddenWords", null);
            
            when(attributeService.getAttributeById(1L)).thenReturn(attribute);
            when(categoryService.getCategoryById(1L)).thenReturn(null);
            when(brandService.getBrandById(1L)).thenReturn(null);
            when(itemMapper.insert(any(ItemEntity.class))).thenAnswer(invocation -> {
                ItemEntity entity = invocation.getArgument(0);
                entity.setId(1L);
                return 1;
            });
            
            // When - Should not throw exception
            createItemRequest.setName("This contains 刀");
            
            // Then
            assertDoesNotThrow(() -> {
                itemService.createItem(createItemRequest);
            });
        }
        
        @Test
        @DisplayName("Should trim and lowercase forbidden words")
        void shouldTrimAndLowercaseForbiddenWords() {
            // Given
            ReflectionTestUtils.setField(itemService, "forbiddenWords", " WEAPON , Gun ");
            
            // When - Test with different case
            createItemRequest.setName("This contains Weapon");
            
            // Then - Should still be caught due to case-insensitive matching
            BizException exception = assertThrows(BizException.class, () -> {
                itemService.createItem(createItemRequest);
            });
            assertEquals(ErrorCode.ITEM_NAME_CONTAINS_FORBIDDEN_WORDS, exception.getErrorCode());
        }
    }
}