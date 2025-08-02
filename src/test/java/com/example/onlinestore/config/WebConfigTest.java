package com.example.onlinestore.config;

import com.example.onlinestore.interceptor.AuthInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * WebConfig配置类单元测试
 * 
 * 测试内容：
 * - 拦截器注册功能
 * - 路径匹配规则
 * - 排除路径配置
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WebConfig配置类测试")
class WebConfigTest {

    @Mock
    private AuthInterceptor authInterceptor;

    @Mock
    private InterceptorRegistry interceptorRegistry;

    @Mock
    private InterceptorRegistration interceptorRegistration;

    @InjectMocks
    private WebConfig webConfig;

    @BeforeEach
    void setUp() {
        when(interceptorRegistry.addInterceptor(any())).thenReturn(interceptorRegistration);
        when(interceptorRegistration.addPathPatterns(any(String.class))).thenReturn(interceptorRegistration);
        when(interceptorRegistration.excludePathPatterns(any(String.class))).thenReturn(interceptorRegistration);
    }

    @Test
    @DisplayName("应该注册AuthInterceptor拦截器")
    void should_RegisterAuthInterceptor_When_AddingInterceptors() {
        // When
        webConfig.addInterceptors(interceptorRegistry);

        // Then
        verify(interceptorRegistry, times(1)).addInterceptor(authInterceptor);
    }

    @Test
    @DisplayName("应该配置正确的拦截路径模式")
    void should_ConfigureCorrectPathPatterns_When_AddingInterceptors() {
        // When
        webConfig.addInterceptors(interceptorRegistry);

        // Then
        verify(interceptorRegistration, times(1)).addPathPatterns("/api/**");
    }

    @Test
    @DisplayName("应该排除登录接口路径")
    void should_ExcludeLoginPath_When_AddingInterceptors() {
        // When
        webConfig.addInterceptors(interceptorRegistry);

        // Then
        verify(interceptorRegistration, times(1)).excludePathPatterns("/api/auth/login");
    }

    @Test
    @DisplayName("应该按正确顺序配置拦截器")
    void should_ConfigureInterceptorInCorrectOrder_When_AddingInterceptors() {
        // When
        webConfig.addInterceptors(interceptorRegistry);

        // Then
        // 验证调用顺序
        var inOrder = inOrder(interceptorRegistry, interceptorRegistration);
        inOrder.verify(interceptorRegistry).addInterceptor(authInterceptor);
        inOrder.verify(interceptorRegistration).addPathPatterns("/api/**");
        inOrder.verify(interceptorRegistration).excludePathPatterns("/api/auth/login");
    }
}