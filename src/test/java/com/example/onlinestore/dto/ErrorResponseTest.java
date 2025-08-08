package com.example.onlinestore.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ErrorResponse DTO 测试
 */
@DisplayName("错误响应DTO测试")
public class ErrorResponseTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用正常错误消息创建错误响应")
        void testCreateErrorResponseWithValidMessage() {
            // Arrange
            String errorMessage = "Something went wrong";

            // Act
            ErrorResponse errorResponse = new ErrorResponse(errorMessage);

            // Assert
            assertNotNull(errorResponse);
            assertEquals(errorMessage, errorResponse.getMessage());
        }

        @Test
        @DisplayName("使用null消息创建错误响应")
        void testCreateErrorResponseWithNullMessage() {
            // Act
            ErrorResponse errorResponse = new ErrorResponse(null);

            // Assert
            assertNotNull(errorResponse);
            assertNull(errorResponse.getMessage());
        }

        @Test
        @DisplayName("使用空字符串消息创建错误响应")
        void testCreateErrorResponseWithEmptyMessage() {
            // Arrange
            String emptyMessage = "";

            // Act
            ErrorResponse errorResponse = new ErrorResponse(emptyMessage);

            // Assert
            assertNotNull(errorResponse);
            assertEquals(emptyMessage, errorResponse.getMessage());
        }

        @Test
        @DisplayName("使用长错误消息创建错误响应")
        void testCreateErrorResponseWithLongMessage() {
            // Arrange
            String longMessage = "This is a very long error message that contains detailed information about what went wrong in the system and how to potentially fix it. ".repeat(3);

            // Act
            ErrorResponse errorResponse = new ErrorResponse(longMessage);

            // Assert
            assertNotNull(errorResponse);
            assertEquals(longMessage, errorResponse.getMessage());
        }

        @Test
        @DisplayName("使用包含特殊字符的错误消息创建错误响应")
        void testCreateErrorResponseWithSpecialCharacters() {
            // Arrange
            String specialMessage = "Error: Invalid input! @#$%^&*()_+{}|:<>?[]\\;',./";

            // Act
            ErrorResponse errorResponse = new ErrorResponse(specialMessage);

            // Assert
            assertNotNull(errorResponse);
            assertEquals(specialMessage, errorResponse.getMessage());
        }

        @Test
        @DisplayName("使用中文错误消息创建错误响应")
        void testCreateErrorResponseWithChineseMessage() {
            // Arrange
            String chineseMessage = "发生了一个错误：用户名或密码无效";

            // Act
            ErrorResponse errorResponse = new ErrorResponse(chineseMessage);

            // Assert
            assertNotNull(errorResponse);
            assertEquals(chineseMessage, errorResponse.getMessage());
        }
    }

    @Nested
    @DisplayName("Getter和Setter测试")
    class GetterSetterTests {

        @Test
        @DisplayName("测试getMessage方法")
        void testGetMessage() {
            // Arrange
            String originalMessage = "Original error message";
            ErrorResponse errorResponse = new ErrorResponse(originalMessage);

            // Act
            String retrievedMessage = errorResponse.getMessage();

            // Assert
            assertEquals(originalMessage, retrievedMessage);
        }

        @Test
        @DisplayName("测试setMessage方法")
        void testSetMessage() {
            // Arrange
            String originalMessage = "Original message";
            String newMessage = "Updated error message";
            ErrorResponse errorResponse = new ErrorResponse(originalMessage);

            // Act
            errorResponse.setMessage(newMessage);

            // Assert
            assertEquals(newMessage, errorResponse.getMessage());
            assertNotEquals(originalMessage, errorResponse.getMessage());
        }

        @Test
        @DisplayName("多次设置消息")
        void testMultipleSetMessage() {
            // Arrange
            ErrorResponse errorResponse = new ErrorResponse("Initial message");
            String message1 = "First update";
            String message2 = "Second update";
            String message3 = "Final update";

            // Act & Assert
            errorResponse.setMessage(message1);
            assertEquals(message1, errorResponse.getMessage());

            errorResponse.setMessage(message2);
            assertEquals(message2, errorResponse.getMessage());

            errorResponse.setMessage(message3);
            assertEquals(message3, errorResponse.getMessage());
        }

        @Test
        @DisplayName("设置null消息")
        void testSetNullMessage() {
            // Arrange
            ErrorResponse errorResponse = new ErrorResponse("Initial message");

            // Act
            errorResponse.setMessage(null);

            // Assert
            assertNull(errorResponse.getMessage());
        }

        @Test
        @DisplayName("设置空字符串消息")
        void testSetEmptyMessage() {
            // Arrange
            ErrorResponse errorResponse = new ErrorResponse("Initial message");
            String emptyMessage = "";

            // Act
            errorResponse.setMessage(emptyMessage);

            // Assert
            assertEquals(emptyMessage, errorResponse.getMessage());
        }
    }

    @Nested
    @DisplayName("业务场景测试")
    class BusinessScenarioTests {

        @Test
        @DisplayName("认证失败错误响应")
        void testAuthenticationFailureErrorResponse() {
            // Arrange
            String authError = "Invalid username or password";

            // Act
            ErrorResponse errorResponse = new ErrorResponse(authError);

            // Assert
            assertEquals(authError, errorResponse.getMessage());
            assertNotNull(errorResponse.getMessage());
            assertFalse(errorResponse.getMessage().isEmpty());
        }

        @Test
        @DisplayName("授权失败错误响应")
        void testAuthorizationFailureErrorResponse() {
            // Arrange
            String authzError = "Access denied. Insufficient permissions.";

            // Act
            ErrorResponse errorResponse = new ErrorResponse(authzError);

            // Assert
            assertEquals(authzError, errorResponse.getMessage());
        }

        @Test
        @DisplayName("验证失败错误响应")
        void testValidationFailureErrorResponse() {
            // Arrange
            String validationError = "Validation failed: Page number must be greater than 0";

            // Act
            ErrorResponse errorResponse = new ErrorResponse(validationError);

            // Assert
            assertEquals(validationError, errorResponse.getMessage());
        }

        @Test
        @DisplayName("系统内部错误响应")
        void testInternalServerErrorResponse() {
            // Arrange
            String internalError = "Internal server error occurred";

            // Act
            ErrorResponse errorResponse = new ErrorResponse(internalError);

            // Assert
            assertEquals(internalError, errorResponse.getMessage());
        }

        @Test
        @DisplayName("资源未找到错误响应")
        void testResourceNotFoundErrorResponse() {
            // Arrange
            String notFoundError = "User not found";

            // Act
            ErrorResponse errorResponse = new ErrorResponse(notFoundError);

            // Assert
            assertEquals(notFoundError, errorResponse.getMessage());
        }

        @Test
        @DisplayName("国际化错误消息场景")
        void testInternationalizedErrorMessages() {
            // Arrange
            String englishError = "User not found";
            String chineseError = "用户未找到";

            // Act
            ErrorResponse englishResponse = new ErrorResponse(englishError);
            ErrorResponse chineseResponse = new ErrorResponse(chineseError);

            // Assert
            assertEquals(englishError, englishResponse.getMessage());
            assertEquals(chineseError, chineseResponse.getMessage());
            assertNotEquals(englishResponse.getMessage(), chineseResponse.getMessage());
        }

        @Test
        @DisplayName("错误消息更新场景")
        void testErrorMessageUpdateScenario() {
            // Arrange - 初始错误消息
            String initialError = "Generic error occurred";
            ErrorResponse errorResponse = new ErrorResponse(initialError);

            // Act - 更新为更具体的错误消息
            String specificError = "Database connection timeout after 30 seconds";
            errorResponse.setMessage(specificError);

            // Assert
            assertEquals(specificError, errorResponse.getMessage());
            assertNotEquals(initialError, errorResponse.getMessage());
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("极长错误消息")
        void testVeryLongErrorMessage() {
            // Arrange
            String veryLongMessage = "Error: ".repeat(1000) + "End";

            // Act
            ErrorResponse errorResponse = new ErrorResponse(veryLongMessage);

            // Assert
            assertEquals(veryLongMessage, errorResponse.getMessage());
            assertTrue(errorResponse.getMessage().length() > 5000);
        }

        @Test
        @DisplayName("包含换行符的错误消息")
        void testErrorMessageWithNewlines() {
            // Arrange
            String messageWithNewlines = "Line 1\nLine 2\nLine 3";

            // Act
            ErrorResponse errorResponse = new ErrorResponse(messageWithNewlines);

            // Assert
            assertEquals(messageWithNewlines, errorResponse.getMessage());
            assertTrue(errorResponse.getMessage().contains("\n"));
        }

        @Test
        @DisplayName("包含制表符的错误消息")
        void testErrorMessageWithTabs() {
            // Arrange
            String messageWithTabs = "Column1\tColumn2\tColumn3";

            // Act
            ErrorResponse errorResponse = new ErrorResponse(messageWithTabs);

            // Assert
            assertEquals(messageWithTabs, errorResponse.getMessage());
            assertTrue(errorResponse.getMessage().contains("\t"));
        }

        @Test
        @DisplayName("只包含空格的错误消息")
        void testErrorMessageWithOnlySpaces() {
            // Arrange
            String spacesOnlyMessage = "   ";

            // Act
            ErrorResponse errorResponse = new ErrorResponse(spacesOnlyMessage);

            // Assert
            assertEquals(spacesOnlyMessage, errorResponse.getMessage());
            assertTrue(errorResponse.getMessage().trim().isEmpty());
        }
    }
}