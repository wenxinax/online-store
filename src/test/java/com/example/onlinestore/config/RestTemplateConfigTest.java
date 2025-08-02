package com.example.onlinestore.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * RestTemplateConfig配置类单元测试
 * 
 * 测试内容：
 * - RestTemplate Bean创建
 * - Bean配置正确性验证
 * - 默认配置验证
 */
@DisplayName("RestTemplateConfig配置类测试")
class RestTemplateConfigTest {

    private RestTemplateConfig restTemplateConfig;

    @BeforeEach
    void setUp() {
        restTemplateConfig = new RestTemplateConfig();
    }

    @Test
    @DisplayName("应该创建RestTemplate Bean")
    void should_CreateRestTemplate_When_BeanMethodCalled() {
        // When
        RestTemplate restTemplate = restTemplateConfig.restTemplate();

        // Then
        assertNotNull(restTemplate);
        assertThat(restTemplate).isInstanceOf(RestTemplate.class);
    }

    @Test
    @DisplayName("创建的RestTemplate应该有默认配置")
    void should_CreateRestTemplateWithDefaultConfig_When_BeanMethodCalled() {
        // When
        RestTemplate restTemplate = restTemplateConfig.restTemplate();

        // Then
        assertNotNull(restTemplate);
        // 验证RestTemplate有默认的消息转换器
        assertThat(restTemplate.getMessageConverters()).isNotEmpty();
        // 验证默认的错误处理器存在
        assertNotNull(restTemplate.getErrorHandler());
        // 验证默认的请求工厂存在
        assertNotNull(restTemplate.getRequestFactory());
    }

    @Test
    @DisplayName("多次调用应该创建不同的RestTemplate实例")
    void should_CreateDifferentInstances_When_BeanMethodCalledMultipleTimes() {
        // When
        RestTemplate restTemplate1 = restTemplateConfig.restTemplate();
        RestTemplate restTemplate2 = restTemplateConfig.restTemplate();

        // Then
        assertNotNull(restTemplate1);
        assertNotNull(restTemplate2);
        assertThat(restTemplate1).isNotSameAs(restTemplate2);
    }

    @Test
    @DisplayName("创建的RestTemplate应该是可用的")
    void should_CreateUsableRestTemplate_When_BeanMethodCalled() {
        // When
        RestTemplate restTemplate = restTemplateConfig.restTemplate();

        // Then
        assertNotNull(restTemplate);
        // 验证RestTemplate可以正常使用（不会抛出异常）
        assertThat(restTemplate.getUriTemplateHandler()).isNotNull();
    }
}