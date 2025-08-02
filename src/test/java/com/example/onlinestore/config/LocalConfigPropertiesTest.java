package com.example.onlinestore.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * LocalConfigProperties配置类单元测试
 * 
 * 测试内容：
 * - 条件配置加载测试
 * - 本地配置文件加载验证
 * - Nacos禁用时的行为测试
 */
@DisplayName("LocalConfigProperties配置类测试")
class LocalConfigPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    @DisplayName("当Nacos禁用时应该加载LocalConfigProperties")
    void should_LoadLocalConfigProperties_When_NacosDisabled() {
        // When & Then
        contextRunner
                .withPropertyValues("spring.cloud.nacos.enabled=false")
                .withUserConfiguration(LocalConfigProperties.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(LocalConfigProperties.class);
                });
    }

    @Test
    @DisplayName("当Nacos启用时不应该加载LocalConfigProperties")
    void should_NotLoadLocalConfigProperties_When_NacosEnabled() {
        // When & Then
        contextRunner
                .withPropertyValues("spring.cloud.nacos.enabled=true")
                .withUserConfiguration(LocalConfigProperties.class)
                .run(context -> {
                    assertThat(context).doesNotHaveBean(LocalConfigProperties.class);
                });
    }

    @Test
    @DisplayName("当未设置Nacos属性时不应该加载LocalConfigProperties")
    void should_NotLoadLocalConfigProperties_When_PropertyNotSet() {
        // When & Then
        contextRunner
                .withUserConfiguration(LocalConfigProperties.class)
                .run(context -> {
                    assertThat(context).doesNotHaveBean(LocalConfigProperties.class);
                });
    }

    @Test
    @DisplayName("LocalConfigProperties应该有正确的注解配置")
    void should_HaveCorrectAnnotations_When_CheckingLocalConfigProperties() {
        // Given
        Class<LocalConfigProperties> configClass = LocalConfigProperties.class;

        // Then
        assertThat(configClass.isAnnotationPresent(Configuration.class)).isTrue();
        assertThat(configClass.isAnnotationPresent(ConditionalOnProperty.class)).isTrue();
        assertThat(configClass.isAnnotationPresent(PropertySource.class)).isTrue();

        // 验证ConditionalOnProperty注解的配置
        ConditionalOnProperty conditionalOnProperty = configClass.getAnnotation(ConditionalOnProperty.class);
        assertThat(conditionalOnProperty.name()).isEqualTo("spring.cloud.nacos.enabled");
        assertThat(conditionalOnProperty.havingValue()).isEqualTo("false");
        assertThat(conditionalOnProperty.matchIfMissing()).isFalse();

        // 验证PropertySource注解的配置
        PropertySource propertySource = configClass.getAnnotation(PropertySource.class);
        assertThat(propertySource.value()).containsExactly("classpath:application-local.yml");
        assertThat(propertySource.factory()).isEqualTo(YamlPropertySourceFactory.class);
    }

    @Test
    @DisplayName("LocalConfigProperties与NacosConfig应该是互斥的")
    void should_BeMutuallyExclusiveWithNacosConfig_When_CheckingConditions() {
        // Test Nacos enabled scenario
        contextRunner
                .withPropertyValues("spring.cloud.nacos.enabled=true")
                .withUserConfiguration(LocalConfigProperties.class, NacosConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(NacosConfig.class);
                    assertThat(context).doesNotHaveBean(LocalConfigProperties.class);
                });

        // Test Nacos disabled scenario
        contextRunner
                .withPropertyValues("spring.cloud.nacos.enabled=false")
                .withUserConfiguration(LocalConfigProperties.class, NacosConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(LocalConfigProperties.class);
                    assertThat(context).doesNotHaveBean(NacosConfig.class);
                });
    }

    @Test
    @DisplayName("LocalConfigProperties类应该是公共的")
    void should_BePublicClass_When_CheckingLocalConfigProperties() {
        // Given & When
        Class<LocalConfigProperties> configClass = LocalConfigProperties.class;

        // Then
        assertThat(configClass.getModifiers() & java.lang.reflect.Modifier.PUBLIC).isNotZero();
    }
}