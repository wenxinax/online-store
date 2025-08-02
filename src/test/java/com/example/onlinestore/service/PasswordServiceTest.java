package com.example.onlinestore.service;

import com.example.onlinestore.service.impl.PasswordServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * PasswordService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordServiceImpl passwordService;

    @Test
    void testEncodePassword_Success() {
        // Arrange
        String rawPassword = "testPassword123";
        String encodedPassword = "$2a$10$encoded.password.hash";
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        // Act
        String result = passwordService.encodePassword(rawPassword);

        // Assert
        assertEquals(encodedPassword, result);
        verify(passwordEncoder).encode(rawPassword);
    }

    @Test
    void testEncodePassword_NullPassword_ThrowsException() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> passwordService.encodePassword(null));
        assertEquals("原始密码不能为空", exception.getMessage());
        
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void testEncodePassword_EmptyPassword_ThrowsException() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> passwordService.encodePassword(""));
        assertEquals("原始密码不能为空", exception.getMessage());
        
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void testEncodePassword_EncoderThrowsException() {
        // Arrange
        String rawPassword = "testPassword123";
        when(passwordEncoder.encode(rawPassword)).thenThrow(new RuntimeException("Encoder error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> passwordService.encodePassword(rawPassword));
        assertEquals("密码加密失败", exception.getMessage());
        
        verify(passwordEncoder).encode(rawPassword);
    }

    @Test
    void testMatches_Success_True() {
        // Arrange
        String rawPassword = "testPassword123";
        String encodedPassword = "$2a$10$encoded.password.hash";
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        // Act
        boolean result = passwordService.matches(rawPassword, encodedPassword);

        // Assert
        assertTrue(result);
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }

    @Test
    void testMatches_Success_False() {
        // Arrange
        String rawPassword = "testPassword123";
        String encodedPassword = "$2a$10$encoded.password.hash";
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        // Act
        boolean result = passwordService.matches(rawPassword, encodedPassword);

        // Assert
        assertFalse(result);
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }

    @Test
    void testMatches_NullRawPassword() {
        // Arrange
        String encodedPassword = "$2a$10$encoded.password.hash";

        // Act
        boolean result = passwordService.matches(null, encodedPassword);

        // Assert
        assertFalse(result);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testMatches_EmptyRawPassword() {
        // Arrange
        String encodedPassword = "$2a$10$encoded.password.hash";

        // Act
        boolean result = passwordService.matches("", encodedPassword);

        // Assert
        assertFalse(result);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testMatches_NullEncodedPassword() {
        // Arrange
        String rawPassword = "testPassword123";

        // Act
        boolean result = passwordService.matches(rawPassword, null);

        // Assert
        assertFalse(result);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testMatches_EncoderThrowsException() {
        // Arrange
        String rawPassword = "testPassword123";
        String encodedPassword = "$2a$10$encoded.password.hash";
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenThrow(new RuntimeException("Match error"));

        // Act
        boolean result = passwordService.matches(rawPassword, encodedPassword);

        // Assert
        assertFalse(result);
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }
}