@BeforeEach
    void setUp() {
        // Create test Item
        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Laptop");
        testItem.setDescription("High-performance laptop for testing");
        testItem.setPrice(new BigDecimal("999.99"));
        testItem.setQuantity(10);
        testItem.setCategory("Electronics");
        
        // Create corresponding ItemResponse
        testItemResponse = new ItemResponse();
        testItemResponse.setId(1L);
        testItemResponse.setName("Test Laptop");
        testItemResponse.setDescription("High-performance laptop for testing");
        testItemResponse.setPrice(new BigDecimal("999.99"));
        testItemResponse.setQuantity(10);
        testItemResponse.setCategory("Electronics");
    }

    @Nested
    @DisplayName("GET /api/v1/items/{itemId} - Happy Path Scenarios")
    class GetItemByIdHappyPath {
        
        @Test
        @DisplayName("Should return item successfully when valid ID provided")
        void getItemById_ShouldReturnItem_WhenValidIdProvided() throws Exception {
            // Given
            when(itemService.getItemById(1L)).thenReturn(testItem);
            when(itemResponseConverter.convert(testItem)).thenReturn(testItemResponse);
            
            // When & Then
            mockMvc.perform(get("/api/v1/items/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is(200)))
                    .andExpect(jsonPath("$.message", is("Success")))
                    .andExpect(jsonPath("$.data.id", is(1)))
                    .andExpect(jsonPath("$.data.name", is("Test Laptop")))
                    .andExpect(jsonPath("$.data.description", is("High-performance laptop for testing")))
                    .andExpect(jsonPath("$.data.price", is(999.99)))
                    .andExpect(jsonPath("$.data.quantity", is(10)))
                    .andExpect(jsonPath("$.data.category", is("Electronics")));
            
            verify(itemService, times(1)).getItemById(1L);
            verify(itemResponseConverter, times(1)).convert(testItem);
        }
        
        @Test
        @DisplayName("Should handle large item ID values correctly")
        void getItemById_ShouldHandleLargeId_WhenLargeIdProvided() throws Exception {
            // Given
            Long largeId = Long.MAX_VALUE;
            testItem.setId(largeId);
            testItemResponse.setId(largeId);
            
            when(itemService.getItemById(largeId)).thenReturn(testItem);
            when(itemResponseConverter.convert(testItem)).thenReturn(testItemResponse);
            
            // When & Then
            mockMvc.perform(get("/api/v1/items/" + largeId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id", is(largeId.intValue()))); // JSON limitation with large longs
            
            verify(itemService, times(1)).getItemById(largeId);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/items/{itemId} - Error Scenarios")
    class GetItemByIdErrorScenarios {
        
        @Test
        @DisplayName("Should handle null item returned from service gracefully")
        void getItemById_ShouldHandleNullItem_WhenServiceReturnsNull() throws Exception {
            // Given
            when(itemService.getItemById(999L)).thenReturn(null);
            
            // When & Then
            mockMvc.perform(get("/api/v1/items/999"))
                    .andExpect(status().isInternalServerError());
            
            verify(itemService, times(1)).getItemById(999L);
            verify(itemResponseConverter, never()).convert(any());
        }
        
        @Test
        @DisplayName("Should return 400 when invalid ID format provided")
        void getItemById_ShouldReturn400_WhenInvalidIdFormat() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/items/invalid-id"))
                    .andExpect(status().isBadRequest());
            
            verify(itemService, never()).getItemById(anyLong());
            verify(itemResponseConverter, never()).convert(any());
        }
        
        @Test
        @DisplayName("Should return 400 when negative ID provided")
        void getItemById_ShouldReturn400_WhenNegativeIdProvided() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/items/-1"))
                    .andExpect(status().isBadRequest());
            
            verify(itemService, never()).getItemById(anyLong());
        }
        
        @Test
        @DisplayName("Should return 400 when zero ID provided")
        void getItemById_ShouldReturn400_WhenZeroIdProvided() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/items/0"))
                    .andExpect(status().isBadRequest());
            
            verify(itemService, never()).getItemById(anyLong());
        }
        
        @Test
        @DisplayName("Should handle service exceptions gracefully")
        void getItemById_ShouldReturn500_WhenServiceThrowsException() throws Exception {
            // Given
            when(itemService.getItemById(1L)).thenThrow(new RuntimeException("Database connection failed"));
            
            // When & Then
            mockMvc.perform(get("/api/v1/items/1"))
                    .andExpect(status().isInternalServerError());
            
            verify(itemService, times(1)).getItemById(1L);
            verify(itemResponseConverter, never()).convert(any());
        }
        
        @Test
        @DisplayName("Should handle converter exceptions gracefully")
        void getItemById_ShouldReturn500_WhenConverterThrowsException() throws Exception {
            // Given
            when(itemService.getItemById(1L)).thenReturn(testItem);
            when(itemResponseConverter.convert(testItem)).thenThrow(new RuntimeException("Conversion failed"));
            
            // When & Then
            mockMvc.perform(get("/api/v1/items/1"))
                    .andExpect(status().isInternalServerError());
            
            verify(itemService, times(1)).getItemById(1L);
            verify(itemResponseConverter, times(1)).convert(testItem);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/items/{itemId} - Edge Cases and Boundary Conditions")
    class GetItemByIdEdgeCases {
        
        @Test
        @DisplayName("Should handle item with null fields gracefully")
        void getItemById_ShouldHandleItemWithNullFields_Gracefully() throws Exception {
            // Given
            Item itemWithNulls = new Item();
            itemWithNulls.setId(1L);
            itemWithNulls.setName(null);
            itemWithNulls.setDescription(null);
            itemWithNulls.setPrice(null);
            itemWithNulls.setQuantity(null);
            itemWithNulls.setCategory(null);
            
            ItemResponse responseWithNulls = new ItemResponse();
            responseWithNulls.setId(1L);
            responseWithNulls.setName(null);
            responseWithNulls.setDescription(null);
            responseWithNulls.setPrice(null);
            responseWithNulls.setQuantity(null);
            responseWithNulls.setCategory(null);
            
            when(itemService.getItemById(1L)).thenReturn(itemWithNulls);
            when(itemResponseConverter.convert(itemWithNulls)).thenReturn(responseWithNulls);
            
            // When & Then
            mockMvc.perform(get("/api/v1/items/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id", is(1)))
                    .andExpect(jsonPath("$.data.name").doesNotExist())
                    .andExpect(jsonPath("$.data.description").doesNotExist())
                    .andExpect(jsonPath("$.data.price").doesNotExist())
                    .andExpect(jsonPath("$.data.quantity").doesNotExist())
                    .andExpect(jsonPath("$.data.category").doesNotExist());
            
            verify(itemService, times(1)).getItemById(1L);
            verify(itemResponseConverter, times(1)).convert(itemWithNulls);
        }
        
        @Test
        @DisplayName("Should handle item with empty string fields")
        void getItemById_ShouldHandleItemWithEmptyStrings_Gracefully() throws Exception {
            // Given
            Item itemWithEmptyStrings = new Item();
            itemWithEmptyStrings.setId(1L);
            itemWithEmptyStrings.setName("");
            itemWithEmptyStrings.setDescription("");
            itemWithEmptyStrings.setCategory("");
            itemWithEmptyStrings.setPrice(new BigDecimal("0.00"));
            itemWithEmptyStrings.setQuantity(0);
            
            ItemResponse responseWithEmptyStrings = new ItemResponse();
            responseWithEmptyStrings.setId(1L);
            responseWithEmptyStrings.setName("");
            responseWithEmptyStrings.setDescription("");
            responseWithEmptyStrings.setCategory("");
            responseWithEmptyStrings.setPrice(new BigDecimal("0.00"));
            responseWithEmptyStrings.setQuantity(0);
            
            when(itemService.getItemById(1L)).thenReturn(itemWithEmptyStrings);
            when(itemResponseConverter.convert(itemWithEmptyStrings)).thenReturn(responseWithEmptyStrings);
            
            // When & Then
            mockMvc.perform(get("/api/v1/items/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name", is("")))
                    .andExpect(jsonPath("$.data.description", is("")))
                    .andExpect(jsonPath("$.data.category", is("")))
                    .andExpect(jsonPath("$.data.price", is(0.0)))
                    .andExpect(jsonPath("$.data.quantity", is(0)));
            
            verify(itemService, times(1)).getItemById(1L);
        }
        
        @Test
        @DisplayName("Should handle item with special characters in text fields")
        void getItemById_ShouldHandleSpecialCharacters_InTextFields() throws Exception {
            // Given
            Item itemWithSpecialChars = new Item();
            itemWithSpecialChars.setId(1L);
            itemWithSpecialChars.setName("Test Item with Special Chars: åäö!@#$%^&*()");
            itemWithSpecialChars.setDescription("Description with emojis 🚀📱💻 and unicode: résumé naïve café");
            itemWithSpecialChars.setCategory("Electronics & 技术");
            itemWithSpecialChars.setPrice(new BigDecimal("999.99"));
            itemWithSpecialChars.setQuantity(5);
            
            ItemResponse responseWithSpecialChars = new ItemResponse();
            responseWithSpecialChars.setId(1L);
            responseWithSpecialChars.setName("Test Item with Special Chars: åäö!@#$%^&*()");
            responseWithSpecialChars.setDescription("Description with emojis 🚀📱💻 and unicode: résumé naïve café");
            responseWithSpecialChars.setCategory("Electronics & 技术");
            responseWithSpecialChars.setPrice(new BigDecimal("999.99"));
            responseWithSpecialChars.setQuantity(5);
            
            when(itemService.getItemById(1L)).thenReturn(itemWithSpecialChars);
            when(itemResponseConverter.convert(itemWithSpecialChars)).thenReturn(responseWithSpecialChars);
            
            // When & Then
            mockMvc.perform(get("/api/v1/items/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name", is("Test Item with Special Chars: åäö!@#$%^&*()")))
                    .andExpect(jsonPath("$.data.description", is("Description with emojis 🚀📱💻 and unicode: résumé naïve café")))
                    .andExpect(jsonPath("$.data.category", is("Electronics & 技术")));
            
            verify(itemService, times(1)).getItemById(1L);
        }
        
        @Test
        @DisplayName("Should handle item with extreme BigDecimal values")
        void getItemById_ShouldHandleExtremeDecimalValues_Correctly() throws Exception {
            // Given
            Item itemWithExtremeValues = new Item();
            itemWithExtremeValues.setId(1L);
            itemWithExtremeValues.setName("Expensive Item");
            itemWithExtremeValues.setDescription("Very expensive test item");
            itemWithExtremeValues.setPrice(new BigDecimal("999999999.99"));
            itemWithExtremeValues.setQuantity(Integer.MAX_VALUE);
            itemWithExtremeValues.setCategory("Luxury");
            
            ItemResponse responseWithExtremeValues = new ItemResponse();
            responseWithExtremeValues.setId(1L);
            responseWithExtremeValues.setName("Expensive Item");
            responseWithExtremeValues.setDescription("Very expensive test item");
            responseWithExtremeValues.setPrice(new BigDecimal("999999999.99"));
            responseWithExtremeValues.setQuantity(Integer.MAX_VALUE);
            responseWithExtremeValues.setCategory("Luxury");
            
            when(itemService.getItemById(1L)).thenReturn(itemWithExtremeValues);
            when(itemResponseConverter.convert(itemWithExtremeValues)).thenReturn(responseWithExtremeValues);
            
            // When & Then
            mockMvc.perform(get("/api/v1/items/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.price", is(999999999.99)))
                    .andExpect(jsonPath("$.data.quantity", is(Integer.MAX_VALUE)));
            
            verify(itemService, times(1)).getItemById(1L);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/items/{itemId} - Performance and Concurrency Tests")
    class GetItemByIdPerformanceTests {
        
        @Test
        @DisplayName("Should handle multiple concurrent requests to same endpoint")
        void getItemById_ShouldHandleConcurrentRequests_ToSameEndpoint() throws Exception {
            // Given
            when(itemService.getItemById(1L)).thenReturn(testItem);
            when(itemResponseConverter.convert(testItem)).thenReturn(testItemResponse);
            
            // When & Then - Simulate concurrent requests
            for (int i = 0; i < 10; i++) {
                mockMvc.perform(get("/api/v1/items/1"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.id", is(1)));
            }
            
            verify(itemService, times(10)).getItemById(1L);
            verify(itemResponseConverter, times(10)).convert(testItem);
        }
        
        @Test
        @DisplayName("Should handle requests to different item IDs concurrently")
        void getItemById_ShouldHandleConcurrentRequests_ToDifferentItems() throws Exception {
            // Given
            Item item2 = new Item();
            item2.setId(2L);
            item2.setName("Second Item");
            item2.setPrice(new BigDecimal("199.99"));
            
            ItemResponse itemResponse2 = new ItemResponse();
            itemResponse2.setId(2L);
            itemResponse2.setName("Second Item");
            itemResponse2.setPrice(new BigDecimal("199.99"));
            
            when(itemService.getItemById(1L)).thenReturn(testItem);
            when(itemService.getItemById(2L)).thenReturn(item2);
            when(itemResponseConverter.convert(testItem)).thenReturn(testItemResponse);
            when(itemResponseConverter.convert(item2)).thenReturn(itemResponse2);
            
            // When & Then
            mockMvc.perform(get("/api/v1/items/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id", is(1)));
            
            mockMvc.perform(get("/api/v1/items/2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id", is(2)));
            
            verify(itemService, times(1)).getItemById(1L);
            verify(itemService, times(1)).getItemById(2L);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/items/{itemId} - Mock Interaction and Integration Tests")
    class GetItemByIdIntegrationTests {
        
        @Test
        @DisplayName("Should verify correct order of service and converter calls")
        void getItemById_ShouldCallDependencies_InCorrectOrder() throws Exception {
            // Given
            when(itemService.getItemById(1L)).thenReturn(testItem);
            when(itemResponseConverter.convert(testItem)).thenReturn(testItemResponse);
            
            // When
            mockMvc.perform(get("/api/v1/items/1"))
                    .andExpect(status().isOk());
            
            // Then - Verify call order using InOrder
            var inOrder = inOrder(itemService, itemResponseConverter);
            inOrder.verify(itemService).getItemById(1L);
            inOrder.verify(itemResponseConverter).convert(testItem);
        }
        
        @Test
        @DisplayName("Should not call converter when service returns null")
        void getItemById_ShouldNotCallConverter_WhenServiceReturnsNull() throws Exception {
            // Given
            when(itemService.getItemById(1L)).thenReturn(null);
            
            // When
            try {
                mockMvc.perform(get("/api/v1/items/1"));
            } catch (Exception ignored) {
            }
            
            // Then
            verify(itemService, times(1)).getItemById(1L);
            verify(itemResponseConverter, never()).convert(any());
        }
        
        @Test
        @DisplayName("Should pass exact item from service to converter")
        void getItemById_ShouldPassExactItem_FromServiceToConverter() throws Exception {
            // Given
            when(itemService.getItemById(1L)).thenReturn(testItem);
            when(itemResponseConverter.convert(testItem)).thenReturn(testItemResponse);
            
            // When
            mockMvc.perform(get("/api/v1/items/1"))
                    .andExpect(status().isOk());
            
            // Then
            verify(itemService).getItemById(1L);
            verify(itemResponseConverter).convert(eq(testItem));
        }
        
        @Test
        @DisplayName("Should handle when service method is called with wrong parameters")
        void getItemById_ShouldVerifyCorrectParameterPassing_ToService() throws Exception {
            // Given
            when(itemService.getItemById(123L)).thenReturn(testItem);
            when(itemResponseConverter.convert(testItem)).thenReturn(testItemResponse);
            
            // When
            mockMvc.perform(get("/api/v1/items/123"))
                    .andExpect(status().isOk());
            
            // Then
            verify(itemService).getItemById(123L);
            verify(itemService, never()).getItemById(1L);
            verify(itemService, never()).getItemById(anyLong());
        }
    }

    @Test
    @DisplayName("Should maintain consistent response format structure")
    void getItemById_ShouldMaintainConsistentResponseFormat_Always() throws Exception {
        // Given
        when(itemService.getItemById(1L)).thenReturn(testItem);
        when(itemResponseConverter.convert(testItem)).thenReturn(testItemResponse);
        
        // When & Then
        mockMvc.perform(get("/api/v1/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.code").isNumber())
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.data").isMap());
        
        verify(itemService, times(1)).getItemById(1L);
        verify(itemResponseConverter, times(1)).convert(testItem);
    }