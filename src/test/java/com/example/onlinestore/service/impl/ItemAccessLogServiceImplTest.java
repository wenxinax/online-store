@BeforeEach
    void setUp() {
        validItemId = 1L;
        validUserId = 100L;
        testDateTime = LocalDateTime.now();
        
        sampleLog = createItemAccessLog(1L, validItemId, validUserId, testDateTime, "VIEW");
        
        sampleLogs = Arrays.asList(
            sampleLog,
            createItemAccessLog(2L, 2L, 101L, testDateTime.minusHours(1), "VIEW"),
            createItemAccessLog(3L, 3L, 102L, testDateTime.minusHours(2), "PURCHASE"),
            createItemAccessLog(4L, 1L, 103L, testDateTime.minusHours(3), "ADD_TO_CART"),
            createItemAccessLog(5L, 4L, 100L, testDateTime.minusHours(4), "FAVORITE")
        );
    }

    private ItemAccessLog createItemAccessLog(Long id, Long itemId, Long userId, 
                                              LocalDateTime accessedAt, String accessType) {
        ItemAccessLog log = new ItemAccessLog();
        log.setId(id);
        log.setItemId(itemId);
        log.setUserId(userId);
        log.setAccessedAt(accessedAt);
        log.setAccessType(accessType);
        return log;
    }

    @Nested
    @DisplayName("Log Access Tests")
    class LogAccessTests {

        @Test
        @DisplayName("Should successfully log item access with valid parameters")
        void shouldLogItemAccessSuccessfully() {
            // Given
            when(itemAccessLogRepository.save(any(ItemAccessLog.class))).thenReturn(sampleLog);

            // When
            ItemAccessLog result = itemAccessLogService.logAccess(validItemId, validUserId, "VIEW");

            // Then
            assertNotNull(result);
            assertEquals(validItemId, result.getItemId());
            assertEquals(validUserId, result.getUserId());
            assertEquals("VIEW", result.getAccessType());
            assertNotNull(result.getAccessedAt());
            
            verify(itemAccessLogRepository).save(argThat(log -> 
                log.getItemId().equals(validItemId) && 
                log.getUserId().equals(validUserId) && 
                "VIEW".equals(log.getAccessType())
            ));
        }

        @Test
        @DisplayName("Should throw exception when itemId is null")
        void shouldThrowExceptionWhenItemIdIsNull() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> itemAccessLogService.logAccess(null, validUserId, "VIEW"));
            
            assertEquals("Item ID cannot be null", exception.getMessage());
            verify(itemAccessLogRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when userId is null")
        void shouldThrowExceptionWhenUserIdIsNull() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> itemAccessLogService.logAccess(validItemId, null, "VIEW"));
            
            assertEquals("User ID cannot be null", exception.getMessage());
            verify(itemAccessLogRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when accessType is null")
        void shouldThrowExceptionWhenAccessTypeIsNull() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> itemAccessLogService.logAccess(validItemId, validUserId, null));
            
            assertEquals("Access type cannot be null or empty", exception.getMessage());
            verify(itemAccessLogRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when accessType is empty")
        void shouldThrowExceptionWhenAccessTypeIsEmpty() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> itemAccessLogService.logAccess(validItemId, validUserId, ""));
            
            assertEquals("Access type cannot be null or empty", exception.getMessage());
            verify(itemAccessLogRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when accessType is blank")
        void shouldThrowExceptionWhenAccessTypeIsBlank() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> itemAccessLogService.logAccess(validItemId, validUserId, "   "));
            
            assertEquals("Access type cannot be null or empty", exception.getMessage());
            verify(itemAccessLogRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should handle repository exception gracefully")
        void shouldHandleRepositoryExceptionGracefully() {
            // Given
            when(itemAccessLogRepository.save(any(ItemAccessLog.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> itemAccessLogService.logAccess(validItemId, validUserId, "VIEW"));
            
            assertEquals("Database connection failed", exception.getMessage());
        }

        @Test
        @DisplayName("Should log different access types correctly")
        void shouldLogDifferentAccessTypesCorrectly() {
            // Given
            String[] accessTypes = {"VIEW", "PURCHASE", "ADD_TO_CART", "FAVORITE", "SHARE", "REVIEW"};
            
            for (String accessType : accessTypes) {
                ItemAccessLog expectedLog = createItemAccessLog(1L, validItemId, validUserId, testDateTime, accessType);
                when(itemAccessLogRepository.save(any(ItemAccessLog.class))).thenReturn(expectedLog);

                // When
                ItemAccessLog result = itemAccessLogService.logAccess(validItemId, validUserId, accessType);

                // Then
                assertEquals(accessType, result.getAccessType());
                assertEquals(validItemId, result.getItemId());
                assertEquals(validUserId, result.getUserId());
            }
            
            verify(itemAccessLogRepository, times(accessTypes.length)).save(any(ItemAccessLog.class));
        }

        @Test
        @DisplayName("Should handle negative item ID")
        void shouldHandleNegativeItemId() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> itemAccessLogService.logAccess(-1L, validUserId, "VIEW"));
            
            assertEquals("Item ID must be positive", exception.getMessage());
            verify(itemAccessLogRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should handle zero item ID")
        void shouldHandleZeroItemId() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> itemAccessLogService.logAccess(0L, validUserId, "VIEW"));
            
            assertEquals("Item ID must be positive", exception.getMessage());
            verify(itemAccessLogRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Retrieve Access Logs Tests")
    class RetrieveAccessLogsTests {

        @Test
        @DisplayName("Should find access logs by item ID successfully")
        void shouldFindAccessLogsByItemIdSuccessfully() {
            // Given
            List<ItemAccessLog> itemLogs = sampleLogs.stream()
                .filter(log -> log.getItemId().equals(validItemId))
                .toList();
            when(itemAccessLogRepository.findByItemIdOrderByAccessedAtDesc(validItemId)).thenReturn(itemLogs);

            // When
            List<ItemAccessLog> result = itemAccessLogService.findByItemId(validItemId);

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertTrue(result.stream().allMatch(log -> log.getItemId().equals(validItemId)));
            
            verify(itemAccessLogRepository).findByItemIdOrderByAccessedAtDesc(validItemId);
        }

        @Test
        @DisplayName("Should return empty list when no logs found for item")
        void shouldReturnEmptyListWhenNoLogsFoundForItem() {
            // Given
            when(itemAccessLogRepository.findByItemIdOrderByAccessedAtDesc(validItemId))
                .thenReturn(Collections.emptyList());

            // When
            List<ItemAccessLog> result = itemAccessLogService.findByItemId(validItemId);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            
            verify(itemAccessLogRepository).findByItemIdOrderByAccessedAtDesc(validItemId);
        }

        @Test
        @DisplayName("Should find access logs by user ID successfully")
        void shouldFindAccessLogsByUserIdSuccessfully() {
            // Given
            List<ItemAccessLog> userLogs = sampleLogs.stream()
                .filter(log -> log.getUserId().equals(validUserId))
                .toList();
            when(itemAccessLogRepository.findByUserIdOrderByAccessedAtDesc(validUserId)).thenReturn(userLogs);

            // When
            List<ItemAccessLog> result = itemAccessLogService.findByUserId(validUserId);

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertTrue(result.stream().allMatch(log -> log.getUserId().equals(validUserId)));
            
            verify(itemAccessLogRepository).findByUserIdOrderByAccessedAtDesc(validUserId);
        }

        @Test
        @DisplayName("Should throw exception when finding by null item ID")
        void shouldThrowExceptionWhenFindingByNullItemId() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> itemAccessLogService.findByItemId(null));
            
            assertEquals("Item ID cannot be null", exception.getMessage());
            verify(itemAccessLogRepository, never()).findByItemIdOrderByAccessedAtDesc(any());
        }

        @Test
        @DisplayName("Should throw exception when finding by null user ID")
        void shouldThrowExceptionWhenFindingByNullUserId() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> itemAccessLogService.findByUserId(null));
            
            assertEquals("User ID cannot be null", exception.getMessage());
            verify(itemAccessLogRepository, never()).findByUserIdOrderByAccessedAtDesc(any());
        }

        @Test
        @DisplayName("Should find access logs by date range successfully")
        void shouldFindAccessLogsByDateRangeSuccessfully() {
            // Given
            LocalDateTime startDate = testDateTime.minusDays(1);
            LocalDateTime endDate = testDateTime.plusDays(1);
            when(itemAccessLogRepository.findByAccessedAtBetweenOrderByAccessedAtDesc(startDate, endDate))
                .thenReturn(sampleLogs);

            // When
            List<ItemAccessLog> result = itemAccessLogService.findByDateRange(startDate, endDate);

            // Then
            assertNotNull(result);
            assertEquals(5, result.size());
            
            verify(itemAccessLogRepository).findByAccessedAtBetweenOrderByAccessedAtDesc(startDate, endDate);
        }

        @Test
        @DisplayName("Should throw exception when start date is after end date")
        void shouldThrowExceptionWhenStartDateIsAfterEndDate() {
            // Given
            LocalDateTime startDate = testDateTime.plusDays(1);
            LocalDateTime endDate = testDateTime.minusDays(1);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> itemAccessLogService.findByDateRange(startDate, endDate));
            
            assertEquals("Start date cannot be after end date", exception.getMessage());
            verify(itemAccessLogRepository, never()).findByAccessedAtBetweenOrderByAccessedAtDesc(any(), any());
        }

        @Test
        @DisplayName("Should find access logs by access type successfully")
        void shouldFindAccessLogsByAccessTypeSuccessfully() {
            // Given
            String accessType = "VIEW";
            List<ItemAccessLog> viewLogs = sampleLogs.stream()
                .filter(log -> accessType.equals(log.getAccessType()))
                .toList();
            when(itemAccessLogRepository.findByAccessTypeOrderByAccessedAtDesc(accessType)).thenReturn(viewLogs);

            // When
            List<ItemAccessLog> result = itemAccessLogService.findByAccessType(accessType);

            // Then
            assertNotNull(result);
            assertTrue(result.stream().allMatch(log -> accessType.equals(log.getAccessType())));
            
            verify(itemAccessLogRepository).findByAccessTypeOrderByAccessedAtDesc(accessType);
        }

        @Test
        @DisplayName("Should find recent access logs with limit")
        void shouldFindRecentAccessLogsWithLimit() {
            // Given
            int limit = 3;
            List<ItemAccessLog> recentLogs = sampleLogs.subList(0, limit);
            when(itemAccessLogRepository.findTopNByOrderByAccessedAtDesc(limit)).thenReturn(recentLogs);

            // When
            List<ItemAccessLog> result = itemAccessLogService.findRecentLogs(limit);

            // Then
            assertNotNull(result);
            assertEquals(limit, result.size());
            
            verify(itemAccessLogRepository).findTopNByOrderByAccessedAtDesc(limit);
        }
    }

    @Nested
    @DisplayName("Delete Access Logs Tests")
    class DeleteAccessLogsTests {

        @Test
        @DisplayName("Should delete old access logs successfully")
        void shouldDeleteOldAccessLogsSuccessfully() {
            // Given
            LocalDateTime cutoffDate = testDateTime.minusDays(30);
            when(itemAccessLogRepository.deleteByAccessedAtBefore(cutoffDate)).thenReturn(5);

            // When
            int deletedCount = itemAccessLogService.deleteOldLogs(cutoffDate);

            // Then
            assertEquals(5, deletedCount);
            verify(itemAccessLogRepository).deleteByAccessedAtBefore(cutoffDate);
        }

        @Test
        @DisplayName("Should return zero when no old logs to delete")
        void shouldReturnZeroWhenNoOldLogsToDelete() {
            // Given
            LocalDateTime cutoffDate = testDateTime.minusDays(30);
            when(itemAccessLogRepository.deleteByAccessedAtBefore(cutoffDate)).thenReturn(0);

            // When
            int deletedCount = itemAccessLogService.deleteOldLogs(cutoffDate);

            // Then
            assertEquals(0, deletedCount);
            verify(itemAccessLogRepository).deleteByAccessedAtBefore(cutoffDate);
        }

        @Test
        @DisplayName("Should delete logs by item ID successfully")
        void shouldDeleteLogsByItemIdSuccessfully() {
            // Given
            when(itemAccessLogRepository.deleteByItemId(validItemId)).thenReturn(3);

            // When
            int deletedCount = itemAccessLogService.deleteByItemId(validItemId);

            // Then
            assertEquals(3, deletedCount);
            verify(itemAccessLogRepository).deleteByItemId(validItemId);
        }

        @Test
        @DisplayName("Should throw exception when deleting by null item ID")
        void shouldThrowExceptionWhenDeletingByNullItemId() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> itemAccessLogService.deleteByItemId(null));
            
            assertEquals("Item ID cannot be null", exception.getMessage());
            verify(itemAccessLogRepository, never()).deleteByItemId(any());
        }

        @Test
        @DisplayName("Should delete logs by user ID successfully")
        void shouldDeleteLogsByUserIdSuccessfully() {
            // Given
            when(itemAccessLogRepository.deleteByUserId(validUserId)).thenReturn(2);

            // When
            int deletedCount = itemAccessLogService.deleteByUserId(validUserId);

            // Then
            assertEquals(2, deletedCount);
            verify(itemAccessLogRepository).deleteByUserId(validUserId);
        }

        @Test
        @DisplayName("Should throw exception when cutoff date is null")
        void shouldThrowExceptionWhenCutoffDateIsNull() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> itemAccessLogService.deleteOldLogs(null));
            
            assertEquals("Cutoff date cannot be null", exception.getMessage());
            verify(itemAccessLogRepository, never()).deleteByAccessedAtBefore(any());
        }
    }

    @Nested
    @DisplayName("Count and Statistics Tests")
    class CountAndStatisticsTests {

        @Test
        @DisplayName("Should count access logs by item ID successfully")
        void shouldCountAccessLogsByItemIdSuccessfully() {
            // Given
            when(itemAccessLogRepository.countByItemId(validItemId)).thenReturn(5L);

            // When
            long count = itemAccessLogService.countByItemId(validItemId);

            // Then
            assertEquals(5L, count);
            verify(itemAccessLogRepository).countByItemId(validItemId);
        }

        @Test
        @DisplayName("Should count access logs by access type successfully")
        void shouldCountAccessLogsByAccessTypeSuccessfully() {
            // Given
            String accessType = "VIEW";
            when(itemAccessLogRepository.countByAccessType(accessType)).thenReturn(10L);

            // When
            long count = itemAccessLogService.countByAccessType(accessType);

            // Then
            assertEquals(10L, count);
            verify(itemAccessLogRepository).countByAccessType(accessType);
        }

        @Test
        @DisplayName("Should count total access logs successfully")
        void shouldCountTotalAccessLogsSuccessfully() {
            // Given
            when(itemAccessLogRepository.count()).thenReturn(100L);

            // When
            long count = itemAccessLogService.getTotalAccessCount();

            // Then
            assertEquals(100L, count);
            verify(itemAccessLogRepository).count();
        }

        @Test
        @DisplayName("Should get most accessed items successfully")
        void shouldGetMostAccessedItemsSuccessfully() {
            // Given
            List<Object[]> mockResults = Arrays.asList(
                new Object[]{1L, 10L},
                new Object[]{2L, 8L},
                new Object[]{3L, 5L}
            );
            when(itemAccessLogRepository.findMostAccessedItems(anyInt())).thenReturn(mockResults);

            // When
            List<Object[]> result = itemAccessLogService.getMostAccessedItems(3);

            // Then
            assertNotNull(result);
            assertEquals(3, result.size());
            assertEquals(1L, result.get(0)[0]);
            assertEquals(10L, result.get(0)[1]);
            verify(itemAccessLogRepository).findMostAccessedItems(3);
        }

        @Test
        @DisplayName("Should get access statistics by date range successfully")
        void shouldGetAccessStatisticsByDateRangeSuccessfully() {
            // Given
            LocalDateTime startDate = testDateTime.minusDays(7);
            LocalDateTime endDate = testDateTime;
            List<Object[]> mockStats = Arrays.asList(
                new Object[]{"VIEW", 50L},
                new Object[]{"PURCHASE", 20L},
                new Object[]{"ADD_TO_CART", 30L}
            );
            when(itemAccessLogRepository.getAccessStatsByDateRange(startDate, endDate)).thenReturn(mockStats);

            // When
            List<Object[]> result = itemAccessLogService.getAccessStatistics(startDate, endDate);

            // Then
            assertNotNull(result);
            assertEquals(3, result.size());
            verify(itemAccessLogRepository).getAccessStatsByDateRange(startDate, endDate);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling Tests")
    class EdgeCasesAndErrorHandlingTests {

        @Test
        @DisplayName("Should handle very long access type strings")
        void shouldHandleVeryLongAccessTypeStrings() {
            // Given
            String longAccessType = "A".repeat(1000);
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> itemAccessLogService.logAccess(validItemId, validUserId, longAccessType));
            
            assertEquals("Access type length cannot exceed 255 characters", exception.getMessage());
        }

        @Test
        @DisplayName("Should handle concurrent access logging")
        void shouldHandleConcurrentAccessLogging() {
            // Given
            when(itemAccessLogRepository.save(any(ItemAccessLog.class)))
                .thenReturn(sampleLog)
                .thenReturn(createItemAccessLog(2L, validItemId, validUserId, testDateTime, "VIEW"));

            // When - Simulate concurrent calls
            ItemAccessLog result1 = itemAccessLogService.logAccess(validItemId, validUserId, "VIEW");
            ItemAccessLog result2 = itemAccessLogService.logAccess(validItemId, validUserId, "VIEW");

            // Then
            assertNotNull(result1);
            assertNotNull(result2);
            verify(itemAccessLogRepository, times(2)).save(any(ItemAccessLog.class));
        }

        @Test
        @DisplayName("Should handle database timeout exception")
        void shouldHandleDatabaseTimeoutException() {
            // Given
            when(itemAccessLogRepository.save(any(ItemAccessLog.class)))
                .thenThrow(new org.springframework.dao.QueryTimeoutException("Query timeout"));

            // When & Then
            assertThrows(org.springframework.dao.QueryTimeoutException.class, 
                () -> itemAccessLogService.logAccess(validItemId, validUserId, "VIEW"));
        }

        @Test
        @DisplayName("Should validate limit parameter for recent logs")
        void shouldValidateLimitParameterForRecentLogs() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> itemAccessLogService.findRecentLogs(0));
            
            assertEquals("Limit must be positive", exception.getMessage());

            exception = assertThrows(IllegalArgumentException.class, 
                () -> itemAccessLogService.findRecentLogs(-1));
            
            assertEquals("Limit must be positive", exception.getMessage());
        }

        @Test
        @DisplayName("Should handle empty database gracefully")
        void shouldHandleEmptyDatabaseGracefully() {
            // Given
            when(itemAccessLogRepository.findByItemIdOrderByAccessedAtDesc(any())).thenReturn(Collections.emptyList());
            when(itemAccessLogRepository.countByItemId(any())).thenReturn(0L);

            // When
            List<ItemAccessLog> logs = itemAccessLogService.findByItemId(validItemId);
            long count = itemAccessLogService.countByItemId(validItemId);

            // Then
            assertTrue(logs.isEmpty());
            assertEquals(0L, count);
        }
    }
}