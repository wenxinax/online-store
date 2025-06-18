@Nested
    @DisplayName("Upload Item Description Tests")
    class UploadItemDescriptionTests {

        @Test
        @DisplayName("Should successfully upload item description with valid content")
        void shouldUploadItemDescriptionSuccessfully() {
            // Given
            String content = TEST_CONTENT;
            
            try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class)) {
                dateUtilsMock.when(DateUtils::getCurrentDate).thenReturn("2023-01-15");
                
                // When
                String result = ossService.uploadItemDescription(content);
                
                // Then
                assertNotNull(result);
                assertTrue(result.startsWith("https://" + TEST_BUCKET_NAME + "." + TEST_ENDPOINT + "/item/description/2023-01-15/"));
                verify(ossClient).putObject(eq(TEST_BUCKET_NAME), anyString(), any(InputStream.class));
            }
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when content is null")
        void shouldThrowExceptionWhenContentIsNull() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ossService.uploadItemDescription(null));
            
            assertEquals("Content cannot be blank", exception.getMessage());
            verify(ossClient, never()).putObject(anyString(), anyString(), any(InputStream.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when content is empty")
        void shouldThrowExceptionWhenContentIsEmpty() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ossService.uploadItemDescription(""));
            
            assertEquals("Content cannot be blank", exception.getMessage());
            verify(ossClient, never()).putObject(anyString(), anyString(), any(InputStream.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when content is blank")
        void shouldThrowExceptionWhenContentIsBlank() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ossService.uploadItemDescription("   "));
            
            assertEquals("Content cannot be blank", exception.getMessage());
            verify(ossClient, never()).putObject(anyString(), anyString(), any(InputStream.class));
        }

        @Test
        @DisplayName("Should throw BizException when OSS throws OSSException")
        void shouldThrowBizExceptionWhenOSSExceptionOccurs() {
            // Given
            String content = TEST_CONTENT;
            when(ossClient.putObject(anyString(), anyString(), any(InputStream.class)))
                .thenThrow(new OSSException("OSS error"));
            
            try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class)) {
                dateUtilsMock.when(DateUtils::getCurrentDate).thenReturn("2023-01-15");
                
                // When & Then
                BizException exception = assertThrows(BizException.class,
                    () -> ossService.uploadItemDescription(content));
                
                assertEquals(ErrorCode.REQUEST_OSS_FAILED, exception.getErrorCode());
            }
        }

        @Test
        @DisplayName("Should throw BizException when OSS throws ClientException")
        void shouldThrowBizExceptionWhenClientExceptionOccurs() {
            // Given
            String content = TEST_CONTENT;
            when(ossClient.putObject(anyString(), anyString(), any(InputStream.class)))
                .thenThrow(new ClientException("Client error"));
            
            try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class)) {
                dateUtilsMock.when(DateUtils::getCurrentDate).thenReturn("2023-01-15");
                
                // When & Then
                BizException exception = assertThrows(BizException.class,
                    () -> ossService.uploadItemDescription(content));
                
                assertEquals(ErrorCode.REQUEST_OSS_FAILED, exception.getErrorCode());
            }
        }

        @Test
        @DisplayName("Should handle large content upload")
        void shouldHandleLargeContentUpload() {
            // Given
            StringBuilder largeContent = new StringBuilder();
            for (int i = 0; i < 10000; i++) {
                largeContent.append("Large content line ").append(i).append("\n");
            }
            
            try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class)) {
                dateUtilsMock.when(DateUtils::getCurrentDate).thenReturn("2023-01-15");
                
                // When
                String result = ossService.uploadItemDescription(largeContent.toString());
                
                // Then
                assertNotNull(result);
                verify(ossClient).putObject(eq(TEST_BUCKET_NAME), anyString(), any(InputStream.class));
            }
        }

        @Test
        @DisplayName("Should handle content with special characters")
        void shouldHandleContentWithSpecialCharacters() {
            // Given
            String specialContent = "Content with special chars: éñüñ™®©℠ 中文 العربية русский 日本語";
            
            try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class)) {
                dateUtilsMock.when(DateUtils::getCurrentDate).thenReturn("2023-01-15");
                
                // When
                String result = ossService.uploadItemDescription(specialContent);
                
                // Then
                assertNotNull(result);
                verify(ossClient).putObject(eq(TEST_BUCKET_NAME), anyString(), any(InputStream.class));
            }
        }
    }

    @Nested
    @DisplayName("Get Item Description Tests")
    class GetItemDescriptionTests {

        @Test
        @DisplayName("Should successfully get item description with valid URL")
        void shouldGetItemDescriptionSuccessfully() throws IOException {
            // Given
            String expectedContent = TEST_CONTENT;
            InputStream contentStream = new ByteArrayInputStream(expectedContent.getBytes(StandardCharsets.UTF_8));
            
            when(httpResponse.getStatusLine()).thenReturn(statusLine);
            when(statusLine.getStatusCode()).thenReturn(200);
            when(httpResponse.getEntity()).thenReturn(httpEntity);
            when(httpEntity.getContent()).thenReturn(contentStream);
            
            try (MockedStatic<org.apache.http.impl.client.HttpClients> httpClientsMock = 
                 mockStatic(org.apache.http.impl.client.HttpClients.class)) {
                httpClientsMock.when(org.apache.http.impl.client.HttpClients::createDefault).thenReturn(httpClient);
                when(httpClient.execute(any(HttpGet.class))).thenReturn(httpResponse);
                
                // When
                String result = ossService.getItemDescription(TEST_OSS_URL);
                
                // Then
                assertEquals(expectedContent + "\n", result);
                verify(httpClient).execute(any(HttpGet.class));
            }
        }

        @Test
        @DisplayName("Should throw BizException when HTTP status is not 200")
        void shouldThrowBizExceptionWhenHttpStatusIsNot200() throws IOException {
            // Given
            when(httpResponse.getStatusLine()).thenReturn(statusLine);
            when(statusLine.getStatusCode()).thenReturn(404);
            
            try (MockedStatic<org.apache.http.impl.client.HttpClients> httpClientsMock = 
                 mockStatic(org.apache.http.impl.client.HttpClients.class)) {
                httpClientsMock.when(org.apache.http.impl.client.HttpClients::createDefault).thenReturn(httpClient);
                when(httpClient.execute(any(HttpGet.class))).thenReturn(httpResponse);
                
                // When & Then
                BizException exception = assertThrows(BizException.class,
                    () -> ossService.getItemDescription(TEST_OSS_URL));
                
                assertEquals(ErrorCode.REQUEST_OSS_FAILED, exception.getErrorCode());
            }
        }

        @Test
        @DisplayName("Should throw BizException when HTTP status is 500")
        void shouldThrowBizExceptionWhenHttpStatusIs500() throws IOException {
            // Given
            when(httpResponse.getStatusLine()).thenReturn(statusLine);
            when(statusLine.getStatusCode()).thenReturn(500);
            
            try (MockedStatic<org.apache.http.impl.client.HttpClients> httpClientsMock = 
                 mockStatic(org.apache.http.impl.client.HttpClients.class)) {
                httpClientsMock.when(org.apache.http.impl.client.HttpClients::createDefault).thenReturn(httpClient);
                when(httpClient.execute(any(HttpGet.class))).thenReturn(httpResponse);
                
                // When & Then
                BizException exception = assertThrows(BizException.class,
                    () -> ossService.getItemDescription(TEST_OSS_URL));
                
                assertEquals(ErrorCode.REQUEST_OSS_FAILED, exception.getErrorCode());
            }
        }

        @Test
        @DisplayName("Should throw BizException when IOException occurs during HTTP request")
        void shouldThrowBizExceptionWhenIOExceptionOccurs() throws IOException {
            // Given
            try (MockedStatic<org.apache.http.impl.client.HttpClients> httpClientsMock = 
                 mockStatic(org.apache.http.impl.client.HttpClients.class)) {
                httpClientsMock.when(org.apache.http.impl.client.HttpClients::createDefault).thenReturn(httpClient);
                when(httpClient.execute(any(HttpGet.class))).thenThrow(new IOException("Network error"));
                
                // When & Then
                BizException exception = assertThrows(BizException.class,
                    () -> ossService.getItemDescription(TEST_OSS_URL));
                
                assertEquals(ErrorCode.REQUEST_OSS_FAILED, exception.getErrorCode());
            }
        }

        @Test
        @DisplayName("Should handle empty response content")
        void shouldHandleEmptyResponseContent() throws IOException {
            // Given
            InputStream emptyStream = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
            
            when(httpResponse.getStatusLine()).thenReturn(statusLine);
            when(statusLine.getStatusCode()).thenReturn(200);
            when(httpResponse.getEntity()).thenReturn(httpEntity);
            when(httpEntity.getContent()).thenReturn(emptyStream);
            
            try (MockedStatic<org.apache.http.impl.client.HttpClients> httpClientsMock = 
                 mockStatic(org.apache.http.impl.client.HttpClients.class)) {
                httpClientsMock.when(org.apache.http.impl.client.HttpClients::createDefault).thenReturn(httpClient);
                when(httpClient.execute(any(HttpGet.class))).thenReturn(httpResponse);
                
                // When
                String result = ossService.getItemDescription(TEST_OSS_URL);
                
                // Then
                assertEquals("", result);
            }
        }

        @Test
        @DisplayName("Should handle multiline content")
        void shouldHandleMultilineContent() throws IOException {
            // Given
            String multilineContent = "Line 1\nLine 2\nLine 3";
            InputStream contentStream = new ByteArrayInputStream(multilineContent.getBytes(StandardCharsets.UTF_8));
            
            when(httpResponse.getStatusLine()).thenReturn(statusLine);
            when(statusLine.getStatusCode()).thenReturn(200);
            when(httpResponse.getEntity()).thenReturn(httpEntity);
            when(httpEntity.getContent()).thenReturn(contentStream);
            
            try (MockedStatic<org.apache.http.impl.client.HttpClients> httpClientsMock = 
                 mockStatic(org.apache.http.impl.client.HttpClients.class)) {
                httpClientsMock.when(org.apache.http.impl.client.HttpClients::createDefault).thenReturn(httpClient);
                when(httpClient.execute(any(HttpGet.class))).thenReturn(httpResponse);
                
                // When
                String result = ossService.getItemDescription(TEST_OSS_URL);
                
                // Then
                assertEquals("Line 1\nLine 2\nLine 3\n", result);
            }
        }

        @Test
        @DisplayName("Should handle content with special characters")
        void shouldHandleContentWithSpecialCharactersInResponse() throws IOException {
            // Given
            String specialContent = "Content with special chars: éñüñ™®©℠ 中文 العربية русский 日本語";
            InputStream contentStream = new ByteArrayInputStream(specialContent.getBytes(StandardCharsets.UTF_8));
            
            when(httpResponse.getStatusLine()).thenReturn(statusLine);
            when(statusLine.getStatusCode()).thenReturn(200);
            when(httpResponse.getEntity()).thenReturn(httpEntity);
            when(httpEntity.getContent()).thenReturn(contentStream);
            
            try (MockedStatic<org.apache.http.impl.client.HttpClients> httpClientsMock = 
                 mockStatic(org.apache.http.impl.client.HttpClients.class)) {
                httpClientsMock.when(org.apache.http.impl.client.HttpClients::createDefault).thenReturn(httpClient);
                when(httpClient.execute(any(HttpGet.class))).thenReturn(httpResponse);
                
                // When
                String result = ossService.getItemDescription(TEST_OSS_URL);
                
                // Then
                assertEquals(specialContent + "\n", result);
            }
        }
    }

    @Nested
    @DisplayName("Lifecycle and Helper Method Tests")
    class LifecycleAndHelperMethodTests {

        @Test
        @DisplayName("Should initialize OSS client in init method")
        void shouldInitializeOssClient() {
            // Given
            OssServiceImpl newOssService = new OssServiceImpl();
            ReflectionTestUtils.setField(newOssService, "ossConfig", ossConfig);
            
            try (MockedStatic<OSSClientBuilder> ossClientBuilderMock = mockStatic(OSSClientBuilder.class)) {
                OSSClientBuilder mockBuilder = mock(OSSClientBuilder.class);
                ossClientBuilderMock.when(OSSClientBuilder::new).thenReturn(mockBuilder);
                when(mockBuilder.build(TEST_ENDPOINT, TEST_ACCESS_KEY_ID, TEST_ACCESS_KEY_SECRET)).thenReturn(ossClient);
                
                // When
                ReflectionTestUtils.invokeMethod(newOssService, "init");
                
                // Then
                OSS actualOssClient = (OSS) ReflectionTestUtils.getField(newOssService, "ossClient");
                assertEquals(ossClient, actualOssClient);
                verify(mockBuilder).build(TEST_ENDPOINT, TEST_ACCESS_KEY_ID, TEST_ACCESS_KEY_SECRET);
            }
        }

        @Test
        @DisplayName("Should shutdown OSS client in destroy method")
        void shouldShutdownOssClientInDestroy() {
            // Given
            ReflectionTestUtils.setField(ossService, "ossClient", ossClient);
            
            // When
            ossService.destroy();
            
            // Then
            verify(ossClient).shutdown();
        }

        @Test
        @DisplayName("Should handle null OSS client in destroy method")
        void shouldHandleNullOssClientInDestroy() {
            // Given
            ReflectionTestUtils.setField(ossService, "ossClient", null);
            
            // When & Then - Should not throw exception
            assertDoesNotThrow(() -> ossService.destroy());
        }

        @Test
        @DisplayName("Should generate valid object name format")
        void shouldGenerateValidObjectNameFormat() {
            // Given
            try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class)) {
                dateUtilsMock.when(DateUtils::getCurrentDate).thenReturn("2023-01-15");
                
                // When
                String result = ossService.uploadItemDescription(TEST_CONTENT);
                
                // Then - Verify the URL format includes the expected object name pattern
                assertTrue(result.contains("/item/description/2023-01-15/"));
                assertTrue(result.startsWith("https://" + TEST_BUCKET_NAME + "." + TEST_ENDPOINT + "/"));
            }
        }

        @Test
        @DisplayName("Should generate different UUIDs for concurrent uploads")
        void shouldGenerateDifferentUuidsForConcurrentUploads() {
            // Given
            try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class)) {
                dateUtilsMock.when(DateUtils::getCurrentDate).thenReturn("2023-01-15");
                
                // When
                String result1 = ossService.uploadItemDescription("Content 1");
                String result2 = ossService.uploadItemDescription("Content 2");
                
                // Then
                assertNotEquals(result1, result2);
                assertTrue(result1.contains("/item/description/2023-01-15/"));
                assertTrue(result2.contains("/item/description/2023-01-15/"));
            }
        }
    }

    @Nested
    @DisplayName("Integration and Edge Case Tests")
    class IntegrationAndEdgeCaseTests {

        @Test
        @DisplayName("Should perform complete upload and retrieval cycle")
        void shouldPerformCompleteUploadRetrievalCycle() throws IOException {
            // Given
            String originalContent = "Original test content";
            String expectedUrl;
            
            try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class)) {
                dateUtilsMock.when(DateUtils::getCurrentDate).thenReturn("2023-01-15");
                
                // Upload phase
                expectedUrl = ossService.uploadItemDescription(originalContent);
                
                // Setup for retrieval phase
                InputStream contentStream = new ByteArrayInputStream(originalContent.getBytes(StandardCharsets.UTF_8));
                when(httpResponse.getStatusLine()).thenReturn(statusLine);
                when(statusLine.getStatusCode()).thenReturn(200);
                when(httpResponse.getEntity()).thenReturn(httpEntity);
                when(httpEntity.getContent()).thenReturn(contentStream);
                
                try (MockedStatic<org.apache.http.impl.client.HttpClients> httpClientsMock = 
                     mockStatic(org.apache.http.impl.client.HttpClients.class)) {
                    httpClientsMock.when(org.apache.http.impl.client.HttpClients::createDefault).thenReturn(httpClient);
                    when(httpClient.execute(any(HttpGet.class))).thenReturn(httpResponse);
                    
                    // Retrieval phase
                    String retrievedContent = ossService.getItemDescription(expectedUrl);
                    
                    // Then
                    assertNotNull(expectedUrl);
                    assertTrue(expectedUrl.contains("/item/description/2023-01-15/"));
                    assertEquals(originalContent + "\n", retrievedContent);
                    verify(ossClient).putObject(eq(TEST_BUCKET_NAME), anyString(), any(InputStream.class));
                    verify(httpClient).execute(any(HttpGet.class));
                }
            }
        }

        @Test
        @DisplayName("Should handle configuration changes")
        void shouldHandleConfigurationChanges() {
            // Given
            String newBucketName = "new-test-bucket";
            String newEndpoint = "oss-cn-beijing.aliyuncs.com";
            
            when(ossConfig.getBucketName()).thenReturn(newBucketName);
            when(ossConfig.getEndpoint()).thenReturn(newEndpoint);
            
            try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class)) {
                dateUtilsMock.when(DateUtils::getCurrentDate).thenReturn("2023-01-15");
                
                // When
                String result = ossService.uploadItemDescription(TEST_CONTENT);
                
                // Then
                assertTrue(result.startsWith("https://" + newBucketName + "." + newEndpoint + "/"));
                verify(ossClient).putObject(eq(newBucketName), anyString(), any(InputStream.class));
            }
        }

        @Test
        @DisplayName("Should handle very long content")
        void shouldHandleVeryLongContent() {
            // Given
            StringBuilder veryLongContent = new StringBuilder();
            for (int i = 0; i < 100000; i++) {
                veryLongContent.append("This is a very long content line number ").append(i).append(".\n");
            }
            
            try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class)) {
                dateUtilsMock.when(DateUtils::getCurrentDate).thenReturn("2023-01-15");
                
                // When
                String result = ossService.uploadItemDescription(veryLongContent.toString());
                
                // Then
                assertNotNull(result);
                assertTrue(result.contains("/item/description/2023-01-15/"));
                verify(ossClient).putObject(eq(TEST_BUCKET_NAME), anyString(), any(InputStream.class));
            }
        }

        @Test
        @DisplayName("Should handle HTML content")
        void shouldHandleHtmlContent() {
            // Given
            String htmlContent = "<html><body><h1>Product Description</h1><p>This is a <b>bold</b> description.</p></body></html>";
            
            try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class)) {
                dateUtilsMock.when(DateUtils::getCurrentDate).thenReturn("2023-01-15");
                
                // When
                String result = ossService.uploadItemDescription(htmlContent);
                
                // Then
                assertNotNull(result);
                verify(ossClient).putObject(eq(TEST_BUCKET_NAME), anyString(), any(InputStream.class));
            }
        }

        @Test
        @DisplayName("Should handle JSON content")
        void shouldHandleJsonContent() {
            // Given
            String jsonContent = "{\"title\":\"Product Title\",\"description\":\"Product Description\",\"price\":99.99}";
            
            try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class)) {
                dateUtilsMock.when(DateUtils::getCurrentDate).thenReturn("2023-01-15");
                
                // When
                String result = ossService.uploadItemDescription(jsonContent);
                
                // Then
                assertNotNull(result);
                verify(ossClient).putObject(eq(TEST_BUCKET_NAME), anyString(), any(InputStream.class));
            }
        }

        @Test
        @DisplayName("Should handle multiple consecutive uploads")
        void shouldHandleMultipleConsecutiveUploads() {
            // Given
            String[] contents = {"Content 1", "Content 2", "Content 3", "Content 4", "Content 5"};
            
            try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class)) {
                dateUtilsMock.when(DateUtils::getCurrentDate).thenReturn("2023-01-15");
                
                // When
                String[] results = new String[contents.length];
                for (int i = 0; i < contents.length; i++) {
                    results[i] = ossService.uploadItemDescription(contents[i]);
                }
                
                // Then
                for (int i = 0; i < results.length; i++) {
                    assertNotNull(results[i]);
                    assertTrue(results[i].contains("/item/description/2023-01-15/"));
                    for (int j = i + 1; j < results.length; j++) {
                        assertNotEquals(results[i], results[j]);
                    }
                }
                
                verify(ossClient, times(contents.length)).putObject(eq(TEST_BUCKET_NAME), anyString(), any(InputStream.class));
            }
        }
    }
}