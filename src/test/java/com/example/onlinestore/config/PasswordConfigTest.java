package com.example.onlinestore.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PasswordConfig配置类单元测试
 * 
 * 测试内容：
 * - PasswordEncoder Bean创建
 * - BCrypt密码编码功能
 * - 密码验证功能
 */
@DisplayName("PasswordConfig配置类测试")
class PasswordConfigTest {

    private PasswordConfig passwordConfig;

    @BeforeEach
    void setUp() {
        passwordConfig = new PasswordConfig();
    }

    @Test
    @DisplayName("应该创建PasswordEncoder Bean")
    void should_CreatePasswordEncoder_When_BeanMethodCalled() {
        // When
        PasswordEncoder passwordEncoder = passwordConfig.passwordEncoder();

        // Then
        assertNotNull(passwordEncoder);
    }

    @Test
    @DisplayName("创建的PasswordEncoder应该能够编码密码")
    void should_EncodePassword_When_PasswordEncoderUsed() {
        // Given
        PasswordEncoder passwordEncoder = passwordConfig.passwordEncoder();
        String rawPassword = "testPassword123";

        // When
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Then
        assertNotNull(encodedPassword);
        assertThat(encodedPassword).isNotEqualTo(rawPassword);
        assertThat(encodedPassword).startsWith("$2a$");  // BCrypt prefix
    }

    @Test
    @DisplayName("创建的PasswordEncoder应该能够验证密码")
    void should_VerifyPassword_When_PasswordEncoderUsed() {
        // Given
        PasswordEncoder passwordEncoder = passwordConfig.passwordEncoder();
        String rawPassword = "testPassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // When & Then
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
        assertThat(passwordEncoder.matches("wrongPassword", encodedPassword)).isFalse();
    }

    @Test
    @DisplayName("相同密码的多次编码应该产生不同的结果")
    void should_ProduceDifferentHashes_When_EncodingSamePasswordMultipleTimes() {
        // Given
        PasswordEncoder passwordEncoder = passwordConfig.passwordEncoder();
        String rawPassword = "testPassword123";

        // When
        String encodedPassword1 = passwordEncoder.encode(rawPassword);
        String encodedPassword2 = passwordEncoder.encode(rawPassword);

        // Then
        assertThat(encodedPassword1).isNotEqualTo(encodedPassword2);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword1));
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword2));
    }
}