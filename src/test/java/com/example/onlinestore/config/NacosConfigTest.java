package com.example.onlinestore.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NacosConfig配置类单元测试
 * 
 * 测试内容：
 * - 条件配置加载测试
 * - Nacos启用/禁用场景测试
 * - 注解配置验证
 */
@DisplayName("NacosConfig配置类测试")
class NacosConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    @DisplayName("当Nacos启用时应该加载NacosConfig")
    void should_LoadNacosConfig_When_NacosEnabled() {
        // When & Then
        contextRunner
                .withPropertyValues("spring.cloud.nacos.enabled=true")
                .withUserConfiguration(NacosConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(NacosConfig.class);
                });
    }

    @Test
    @DisplayName("当Nacos禁用时不应该加载NacosConfig")
    void should_NotLoadNacosConfig_When_NacosDisabled() {
        // When & Then
        contextRunner
                .withPropertyValues("spring.cloud.nacos.enabled=false")
                .withUserConfiguration(NacosConfig.class)
                .run(context -> {
                    assertThat(context).doesNotHaveBean(NacosConfig.class);
                });
    }

    @Test
    @DisplayName("当未设置Nacos属性时应该默认加载NacosConfig")
    void should_LoadNacosConfigByDefault_When_PropertyNotSet() {
        // When & Then
        contextRunner
                .withUserConfiguration(NacosConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(NacosConfig.class);
                });
    }

    @Test
    @DisplayName("NacosConfig应该有正确的注解配置")
    void should_HaveCorrectAnnotations_When_CheckingNacosConfig() {
        // Given
        Class<NacosConfig> configClass = NacosConfig.class;

        // Then
        assertThat(configClass.isAnnotationPresent(Configuration.class)).isTrue();
        assertThat(configClass.isAnnotationPresent(EnableDiscoveryClient.class)).isTrue();
        assertThat(configClass.isAnnotationPresent(ConditionalOnProperty.class)).isTrue();

        // 验证ConditionalOnProperty注解的配置
        ConditionalOnProperty conditionalOnProperty = configClass.getAnnotation(ConditionalOnProperty.class);
        assertThat(conditionalOnProperty.name()).isEqualTo("spring.cloud.nacos.enabled");
        assertThat(conditionalOnProperty.havingValue()).isEqualTo("true");
        assertThat(conditionalOnProperty.matchIfMissing()).isTrue();
    }

    @Test
    @DisplayName("当Nacos属性为其他值时不应该加载NacosConfig")
    void should_NotLoadNacosConfig_When_NacosPropertyHasOtherValue() {
        // When & Then
        contextRunner
                .withPropertyValues("spring.cloud.nacos.enabled=invalid")
                .withUserConfiguration(NacosConfig.class)
                .run(context -> {
                    assertThat(context).doesNotHaveBean(NacosConfig.class);
                });
    }

    @Test
    @DisplayName("NacosConfig类应该是公共的")
    void should_BePublicClass_When_CheckingNacosConfig() {
        // Given & When
        Class<NacosConfig> configClass = NacosConfig.class;

        // Then
        assertThat(configClass.getModifiers() & java.lang.reflect.Modifier.PUBLIC).isNotZero();
    }
}