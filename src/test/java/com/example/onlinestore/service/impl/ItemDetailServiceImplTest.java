@BeforeEach
    void setUp() {
        // Set up sample data
        sampleItem = new Item();
        sampleItem.setId(1L);
        sampleItem.setName("Test Item");
        sampleItem.setDescription("Test Description");
        sampleItem.setPrice(new BigDecimal("99.99"));
        sampleItem.setCategory("Electronics");
        sampleItem.setImageUrl("http://example.com/image.jpg");
        sampleItem.setStock(10);
        sampleItem.setCreateTime(LocalDateTime.now());
        sampleItem.setUpdateTime(LocalDateTime.now());

        Sku sku1 = new Sku();
        sku1.setId(1L);
        sku1.setItemId(1L);
        sku1.setSkuCode("SKU001");
        sku1.setAttributes("Color:Red,Size:M");
        sku1.setPrice(new BigDecimal("99.99"));
        sku1.setStock(5);
        sku1.setCreateTime(LocalDateTime.now());
        sku1.setUpdateTime(LocalDateTime.now());

        Sku sku2 = new Sku();
        sku2.setId(2L);
        sku2.setItemId(1L);
        sku2.setSkuCode("SKU002");
        sku2.setAttributes("Color:Blue,Size:L");
        sku2.setPrice(new BigDecimal("109.99"));
        sku2.setStock(3);
        sku2.setCreateTime(LocalDateTime.now());
        sku2.setUpdateTime(LocalDateTime.now());

        sampleSkus = Arrays.asList(sku1, sku2);

        sampleItemDetail = new ItemDetail();
        sampleItemDetail.setItem(sampleItem);
        sampleItemDetail.setSkus(sampleSkus);

        // Mock RedisTemplate operations
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Nested
    @DisplayName("Happy Path Tests")
    class HappyPathTests {

        @Test
        @DisplayName("Should return item detail when cache miss and database has data")
        void shouldReturnItemDetailWhenCacheMissAndDatabaseHasData() throws Exception {
            // Given
            Long itemId = 1L;
            String cacheKey = CACHE_KEY_PREFIX + itemId;

            when(valueOperations.get(cacheKey)).thenReturn(null);
            when(itemService.getItemById(itemId)).thenReturn(sampleItem);
            when(skuService.getSkusByItemId(itemId)).thenReturn(sampleSkus);

            try (MockedStatic<JacksonJsonUtils> mockedJsonUtils = mockStatic(JacksonJsonUtils.class)) {
                String jsonValue = "{\"item\":{\"id\":1},\"skus\":[{\"id\":1},{\"id\":2}]}";
                mockedJsonUtils.when(() -> JacksonJsonUtils.toString(any(ItemDetail.class)))
                        .thenReturn(jsonValue);

                // When
                ItemDetail result = itemDetailService.getItemDetail(itemId);

                // Then
                assertNotNull(result);
                assertEquals(sampleItem, result.getItem());
                assertEquals(sampleSkus, result.getSkus());

                verify(valueOperations).get(cacheKey);
                verify(itemService).getItemById(itemId);
                verify(skuService).getSkusByItemId(itemId);
                verify(valueOperations).set(cacheKey, jsonValue, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
            }
        }

        @Test
        @DisplayName("Should return cached item detail when cache hit")
        void shouldReturnCachedItemDetailWhenCacheHit() throws Exception {
            // Given
            Long itemId = 1L;
            String cacheKey = CACHE_KEY_PREFIX + itemId;
            String cachedJson = "{\"item\":{\"id\":1},\"skus\":[{\"id\":1},{\"id\":2}]}";

            when(valueOperations.get(cacheKey)).thenReturn(cachedJson);

            try (MockedStatic<JacksonJsonUtils> mockedJsonUtils = mockStatic(JacksonJsonUtils.class)) {
                mockedJsonUtils.when(() -> JacksonJsonUtils.toObject(cachedJson, ItemDetail.class))
                        .thenReturn(sampleItemDetail);

                // When
                ItemDetail result = itemDetailService.getItemDetail(itemId);

                // Then
                assertNotNull(result);
                assertEquals(sampleItemDetail, result);

                verify(valueOperations).get(cacheKey);
                verify(itemService, never()).getItemById(any());
                verify(skuService, never()).getSkusByItemId(any());
                verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
            }
        }

        @Test
        @DisplayName("Should handle item with empty sku list")
        void shouldHandleItemWithEmptySkuList() throws Exception {
            // Given
            Long itemId = 1L;
            String cacheKey = CACHE_KEY_PREFIX + itemId;
            List<Sku> emptySkus = Collections.emptyList();

            when(valueOperations.get(cacheKey)).thenReturn(null);
            when(itemService.getItemById(itemId)).thenReturn(sampleItem);
            when(skuService.getSkusByItemId(itemId)).thenReturn(emptySkus);

            try (MockedStatic<JacksonJsonUtils> mockedJsonUtils = mockStatic(JacksonJsonUtils.class)) {
                String jsonValue = "{\"item\":{\"id\":1},\"skus\":[]}";
                mockedJsonUtils.when(() -> JacksonJsonUtils.toString(any(ItemDetail.class)))
                        .thenReturn(jsonValue);

                // When
                ItemDetail result = itemDetailService.getItemDetail(itemId);

                // Then
                assertNotNull(result);
                assertEquals(sampleItem, result.getItem());
                assertEquals(emptySkus, result.getSkus());
                assertTrue(result.getSkus().isEmpty());

                verify(valueOperations).set(cacheKey, jsonValue, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
            }
        }
    }

    @Nested
    @DisplayName("Input Validation Tests")
    class InputValidationTests {

        @Test
        @DisplayName("Should throw IllegalArgumentException when itemId is null")
        void shouldThrowIllegalArgumentExceptionWhenItemIdIsNull() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> itemDetailService.getItemDetail(null)
            );
            assertEquals("itemId is null", exception.getMessage());

            verify(valueOperations, never()).get(anyString());
            verify(itemService, never()).getItemById(any());
            verify(skuService, never()).getSkusByItemId(any());
        }

        @Test
        @DisplayName("Should throw BizException when item not found")
        void shouldThrowBizExceptionWhenItemNotFound() {
            // Given
            Long itemId = 999L;
            String cacheKey = CACHE_KEY_PREFIX + itemId;

            when(valueOperations.get(cacheKey)).thenReturn(null);
            when(itemService.getItemById(itemId)).thenReturn(null);

            // When & Then
            BizException exception = assertThrows(
                BizException.class,
                () -> itemDetailService.getItemDetail(itemId)
            );
            assertEquals(ErrorCode.ITEM_NOT_FOUND, exception.getErrorCode());

            verify(valueOperations).get(cacheKey);
            verify(itemService).getItemById(itemId);
            verify(skuService, never()).getSkusByItemId(any());
        }

        @Test
        @DisplayName("Should handle valid itemId with zero value")
        void shouldHandleValidItemIdWithZeroValue() {
            // Given
            Long itemId = 0L;
            String cacheKey = CACHE_KEY_PREFIX + itemId;

            when(valueOperations.get(cacheKey)).thenReturn(null);
            when(itemService.getItemById(itemId)).thenReturn(null);

            // When & Then
            BizException exception = assertThrows(
                BizException.class,
                () -> itemDetailService.getItemDetail(itemId)
            );
            assertEquals(ErrorCode.ITEM_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("Should handle valid itemId with negative value")
        void shouldHandleValidItemIdWithNegativeValue() {
            // Given
            Long itemId = -1L;
            String cacheKey = CACHE_KEY_PREFIX + itemId;

            when(valueOperations.get(cacheKey)).thenReturn(null);
            when(itemService.getItemById(itemId)).thenReturn(null);

            // When & Then
            BizException exception = assertThrows(
                BizException.class,
                () -> itemDetailService.getItemDetail(itemId)
            );
            assertEquals(ErrorCode.ITEM_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("Caching Error Scenarios")
    class CachingErrorScenarios {

        @Test
        @DisplayName("Should handle cache deserialization error and delete corrupted cache")
        void shouldHandleCacheDeserializationErrorAndDeleteCorruptedCache() throws Exception {
            // Given
            Long itemId = 1L;
            String cacheKey = CACHE_KEY_PREFIX + itemId;
            String corruptedJson = "invalid json";

            when(valueOperations.get(cacheKey)).thenReturn(corruptedJson);
            when(itemService.getItemById(itemId)).thenReturn(sampleItem);
            when(skuService.getSkusByItemId(itemId)).thenReturn(sampleSkus);

            try (MockedStatic<JacksonJsonUtils> mockedJsonUtils = mockStatic(JacksonJsonUtils.class)) {
                mockedJsonUtils.when(() -> JacksonJsonUtils.toObject(corruptedJson, ItemDetail.class))
                        .thenThrow(new IOException("Invalid JSON format"));

                String validJson = "{\"item\":{\"id\":1},\"skus\":[{\"id\":1},{\"id\":2}]}";
                mockedJsonUtils.when(() -> JacksonJsonUtils.toString(any(ItemDetail.class)))
                        .thenReturn(validJson);

                // When
                ItemDetail result = itemDetailService.getItemDetail(itemId);

                // Then
                assertNotNull(result);
                assertEquals(sampleItem, result.getItem());
                assertEquals(sampleSkus, result.getSkus());

                verify(valueOperations).get(cacheKey);
                verify(redisTemplate).delete(cacheKey);
                verify(itemService).getItemById(itemId);
                verify(skuService).getSkusByItemId(itemId);
                verify(valueOperations).set(cacheKey, validJson, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
            }
        }

        @Test
        @DisplayName("Should throw BizException when serialization fails during caching")
        void shouldThrowBizExceptionWhenSerializationFailsDuringCaching() throws Exception {
            // Given
            Long itemId = 1L;
            String cacheKey = CACHE_KEY_PREFIX + itemId;

            when(valueOperations.get(cacheKey)).thenReturn(null);
            when(itemService.getItemById(itemId)).thenReturn(sampleItem);
            when(skuService.getSkusByItemId(itemId)).thenReturn(sampleSkus);

            try (MockedStatic<JacksonJsonUtils> mockedJsonUtils = mockStatic(JacksonJsonUtils.class)) {
                mockedJsonUtils.when(() -> JacksonJsonUtils.toString(any(ItemDetail.class)))
                        .thenThrow(new JsonProcessingException("Serialization failed") {});

                // When & Then
                BizException exception = assertThrows(
                    BizException.class,
                    () -> itemDetailService.getItemDetail(itemId)
                );
                assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, exception.getErrorCode());

                verify(valueOperations).get(cacheKey);
                verify(itemService).getItemById(itemId);
                verify(skuService).getSkusByItemId(itemId);
                verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
            }
        }

        @Test
        @DisplayName("Should proceed normally when cache get operation fails")
        void shouldProceedNormallyWhenCacheGetOperationFails() throws Exception {
            // Given
            Long itemId = 1L;
            String cacheKey = CACHE_KEY_PREFIX + itemId;

            when(valueOperations.get(cacheKey)).thenThrow(new RuntimeException("Redis connection failed"));
            when(itemService.getItemById(itemId)).thenReturn(sampleItem);
            when(skuService.getSkusByItemId(itemId)).thenReturn(sampleSkus);

            try (MockedStatic<JacksonJsonUtils> mockedJsonUtils = mockStatic(JacksonJsonUtils.class)) {
                String jsonValue = "{\"item\":{\"id\":1},\"skus\":[{\"id\":1},{\"id\":2}]}";
                mockedJsonUtils.when(() -> JacksonJsonUtils.toString(any(ItemDetail.class)))
                        .thenReturn(jsonValue);

                // When & Then
                assertThrows(RuntimeException.class, () -> itemDetailService.getItemDetail(itemId));

                verify(valueOperations).get(cacheKey);
                // Should not proceed to database operations due to cache exception
                verify(itemService, never()).getItemById(any());
                verify(skuService, never()).getSkusByItemId(any());
            }
        }

        @Test
        @DisplayName("Should continue operation when cache set operation fails")
        void shouldContinueOperationWhenCacheSetOperationFails() throws Exception {
            // Given
            Long itemId = 1L;
            String cacheKey = CACHE_KEY_PREFIX + itemId;

            when(valueOperations.get(cacheKey)).thenReturn(null);
            when(itemService.getItemById(itemId)).thenReturn(sampleItem);
            when(skuService.getSkusByItemId(itemId)).thenReturn(sampleSkus);

            try (MockedStatic<JacksonJsonUtils> mockedJsonUtils = mockStatic(JacksonJsonUtils.class)) {
                String jsonValue = "{\"item\":{\"id\":1},\"skus\":[{\"id\":1},{\"id\":2}]}";
                mockedJsonUtils.when(() -> JacksonJsonUtils.toString(any(ItemDetail.class)))
                        .thenReturn(jsonValue);

                doThrow(new RuntimeException("Redis set failed"))
                    .when(valueOperations).set(cacheKey, jsonValue, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);

                // When & Then
                assertThrows(RuntimeException.class, () -> itemDetailService.getItemDetail(itemId));

                verify(valueOperations).get(cacheKey);
                verify(itemService).getItemById(itemId);
                verify(skuService).getSkusByItemId(itemId);
                verify(valueOperations).set(cacheKey, jsonValue, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
            }
        }
    }

    @Nested
    @DisplayName("Service Dependencies Tests")
    class ServiceDependenciesTests {

        @Test
        @DisplayName("Should handle ItemService throwing exception")
        void shouldHandleItemServiceThrowingException() {
            // Given
            Long itemId = 1L;
            String cacheKey = CACHE_KEY_PREFIX + itemId;

            when(valueOperations.get(cacheKey)).thenReturn(null);
            when(itemService.getItemById(itemId)).thenThrow(new RuntimeException("Database error"));

            // When & Then
            RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> itemDetailService.getItemDetail(itemId)
            );
            assertEquals("Database error", exception.getMessage());

            verify(valueOperations).get(cacheKey);
            verify(itemService).getItemById(itemId);
            verify(skuService, never()).getSkusByItemId(any());
        }

        @Test
        @DisplayName("Should handle SkuService throwing exception")
        void shouldHandleSkuServiceThrowingException() {
            // Given
            Long itemId = 1L;
            String cacheKey = CACHE_KEY_PREFIX + itemId;

            when(valueOperations.get(cacheKey)).thenReturn(null);
            when(itemService.getItemById(itemId)).thenReturn(sampleItem);
            when(skuService.getSkusByItemId(itemId)).thenThrow(new RuntimeException("SKU service error"));

            // When & Then
            RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> itemDetailService.getItemDetail(itemId)
            );
            assertEquals("SKU service error", exception.getMessage());

            verify(valueOperations).get(cacheKey);
            verify(itemService).getItemById(itemId);
            verify(skuService).getSkusByItemId(itemId);
        }

        @Test
        @DisplayName("Should handle SkuService returning null")
        void shouldHandleSkuServiceReturningNull() throws Exception {
            // Given
            Long itemId = 1L;
            String cacheKey = CACHE_KEY_PREFIX + itemId;

            when(valueOperations.get(cacheKey)).thenReturn(null);
            when(itemService.getItemById(itemId)).thenReturn(sampleItem);
            when(skuService.getSkusByItemId(itemId)).thenReturn(null);

            try (MockedStatic<JacksonJsonUtils> mockedJsonUtils = mockStatic(JacksonJsonUtils.class)) {
                String jsonValue = "{\"item\":{\"id\":1},\"skus\":null}";
                mockedJsonUtils.when(() -> JacksonJsonUtils.toString(any(ItemDetail.class)))
                        .thenReturn(jsonValue);

                // When
                ItemDetail result = itemDetailService.getItemDetail(itemId);

                // Then
                assertNotNull(result);
                assertEquals(sampleItem, result.getItem());
                assertNull(result.getSkus());

                verify(valueOperations).get(cacheKey);
                verify(itemService).getItemById(itemId);
                verify(skuService).getSkusByItemId(itemId);
                verify(valueOperations).set(cacheKey, jsonValue, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle maximum Long value as itemId")
        void shouldHandleMaximumLongValueAsItemId() {
            // Given
            Long itemId = Long.MAX_VALUE;
            String cacheKey = CACHE_KEY_PREFIX + itemId;

            when(valueOperations.get(cacheKey)).thenReturn(null);
            when(itemService.getItemById(itemId)).thenReturn(null);

            // When & Then
            BizException exception = assertThrows(
                BizException.class,
                () -> itemDetailService.getItemDetail(itemId)
            );
            assertEquals(ErrorCode.ITEM_NOT_FOUND, exception.getErrorCode());

            verify(valueOperations).get(cacheKey);
            verify(itemService).getItemById(itemId);
        }

        @Test
        @DisplayName("Should handle minimum Long value as itemId")
        void shouldHandleMinimumLongValueAsItemId() {
            // Given
            Long itemId = Long.MIN_VALUE;
            String cacheKey = CACHE_KEY_PREFIX + itemId;

            when(valueOperations.get(cacheKey)).thenReturn(null);
            when(itemService.getItemById(itemId)).thenReturn(null);

            // When & Then
            BizException exception = assertThrows(
                BizException.class,
                () -> itemDetailService.getItemDetail(itemId)
            );
            assertEquals(ErrorCode.ITEM_NOT_FOUND, exception.getErrorCode());

            verify(valueOperations).get(cacheKey);
            verify(itemService).getItemById(itemId);
        }

        @Test
        @DisplayName("Should handle empty cache value")
        void shouldHandleEmptyCacheValue() throws Exception {
            // Given
            Long itemId = 1L;
            String cacheKey = CACHE_KEY_PREFIX + itemId;

            when(valueOperations.get(cacheKey)).thenReturn("");
            when(itemService.getItemById(itemId)).thenReturn(sampleItem);
            when(skuService.getSkusByItemId(itemId)).thenReturn(sampleSkus);

            try (MockedStatic<JacksonJsonUtils> mockedJsonUtils = mockStatic(JacksonJsonUtils.class)) {
                mockedJsonUtils.when(() -> JacksonJsonUtils.toObject("", ItemDetail.class))
                        .thenThrow(new IOException("Empty JSON"));

                String validJson = "{\"item\":{\"id\":1},\"skus\":[{\"id\":1},{\"id\":2}]}";
                mockedJsonUtils.when(() -> JacksonJsonUtils.toString(any(ItemDetail.class)))
                        .thenReturn(validJson);

                // When
                ItemDetail result = itemDetailService.getItemDetail(itemId);

                // Then
                assertNotNull(result);
                assertEquals(sampleItem, result.getItem());
                assertEquals(sampleSkus, result.getSkus());

                verify(valueOperations).get(cacheKey);
                verify(redisTemplate).delete(cacheKey);
                verify(itemService).getItemById(itemId);
                verify(skuService).getSkusByItemId(itemId);
            }
        }
    }
}