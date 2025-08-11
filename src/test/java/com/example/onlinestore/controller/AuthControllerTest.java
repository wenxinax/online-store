package com.example.onlinestore.controller;

import com.example.onlinestore.dto.LoginRequest;
import com.example.onlinestore.dto.LoginResponse;
import com.example.onlinestore.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("认证控制器测试")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private MessageSource messageSource;

    private LoginRequest request;
    private LoginResponse response;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        response = new LoginResponse();
        response.setToken("test-token-12345");
        response.setExpireTime(LocalDateTime.now().plusDays(1));

        // 设置 MessageSource 默认行为
        when(messageSource.getMessage(eq("error.system.internal"), isNull(), any(Locale.class)))
                .thenReturn("系统内部错误");
        when(messageSource.getMessage(eq("error.invalid.credentials"), isNull(), any(Locale.class)))
                .thenReturn("用户名或密码错误");
    }

    @Nested
    @DisplayName("登录接口测试")
    class LoginTests {

        @Test
        @DisplayName("登录成功 - 返回token和过期时间")
        void whenLoginSucceeds_thenReturnToken() throws Exception {
            // Given
            when(userService.login(any(LoginRequest.class))).thenReturn(response);

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value(response.getToken()))
                    .andExpect(jsonPath("$.expireTime").exists());
        }

        @Test
        @DisplayName("登录失败 - 无效凭据")
        void whenInvalidCredentials_thenReturnBadRequest() throws Exception {
            // Given
            request.setPassword("wrongpassword");
            when(userService.login(any(LoginRequest.class)))
                    .thenThrow(new IllegalArgumentException("用户名或密码错误"));

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("用户名或密码错误"));
        }

        @Test
        @DisplayName("系统异常 - 返回内部服务器错误")
        void whenSystemError_thenReturnInternalServerError() throws Exception {
            // Given
            when(userService.login(any(LoginRequest.class)))
                    .thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string("系统内部错误"));
        }

        @Test
        @DisplayName("空用户名 - JSON格式验证")
        void whenEmptyUsername_thenProcessRequest() throws Exception {
            // Given
            request.setUsername("");
            when(userService.login(any(LoginRequest.class)))
                    .thenThrow(new IllegalArgumentException("用户名不能为空"));

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("用户名不能为空"));
        }

        @Test
        @DisplayName("空密码 - JSON格式验证")
        void whenEmptyPassword_thenProcessRequest() throws Exception {
            // Given
            request.setPassword("");
            when(userService.login(any(LoginRequest.class)))
                    .thenThrow(new IllegalArgumentException("密码不能为空"));

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("密码不能为空"));
        }

        @Test
        @DisplayName("无效的JSON格式 - 返回400")
        void whenInvalidJson_thenReturnBadRequest() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{ invalid json }"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("多语言支持 - 英文错误信息")
        void whenLoginFailsWithEnglishLocale_thenReturnEnglishError() throws Exception {
            // Given
            when(messageSource.getMessage(eq("error.system.internal"), isNull(), eq(Locale.ENGLISH)))
                    .thenReturn("Internal server error");
            when(userService.login(any(LoginRequest.class)))
                    .thenThrow(new RuntimeException("System error"));

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Accept-Language", "en")
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string("Internal server error"));
        }

        @Test
        @DisplayName("多语言支持 - 中文错误信息")
        void whenLoginFailsWithChineseLocale_thenReturnChineseError() throws Exception {
            // Given
            when(messageSource.getMessage(eq("error.system.internal"), isNull(), eq(Locale.SIMPLIFIED_CHINESE)))
                    .thenReturn("系统内部错误");
            when(userService.login(any(LoginRequest.class)))
                    .thenThrow(new RuntimeException("System error"));

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Accept-Language", "zh-CN")
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string("系统内部错误"));
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("null请求体")
        void whenNullRequestBody_thenHandleGracefully() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("极长用户名")
        void whenVeryLongUsername_thenProcessCorrectly() throws Exception {
            // Given
            String longUsername = "a".repeat(1000);
            request.setUsername(longUsername);
            when(userService.login(any(LoginRequest.class)))
                    .thenThrow(new IllegalArgumentException("用户名过长"));

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("特殊字符用户名")
        void whenSpecialCharacterUsername_thenProcessCorrectly() throws Exception {
            // Given
            request.setUsername("user@test.com");
            when(userService.login(any(LoginRequest.class))).thenReturn(response);

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }
}