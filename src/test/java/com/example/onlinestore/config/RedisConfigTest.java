package com.example.onlinestore.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * RedisConfig配置类单元测试
 * 
 * 测试内容：
 * - StringRedisTemplate Bean创建
 * - RedisConnectionFactory依赖注入
 * - 配置正确性验证
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RedisConfig配置类测试")
class RedisConfigTest {

    @Mock
    private RedisConnectionFactory redisConnectionFactory;

    private RedisConfig redisConfig;

    @BeforeEach
    void setUp() {
        redisConfig = new RedisConfig();
    }

    @Test
    @DisplayName("应该创建StringRedisTemplate Bean")
    void should_CreateStringRedisTemplate_When_BeanMethodCalled() {
        // When
        StringRedisTemplate stringRedisTemplate = redisConfig.stringRedisTemplate(redisConnectionFactory);

        // Then
        assertNotNull(stringRedisTemplate);
        assertThat(stringRedisTemplate.getConnectionFactory()).isEqualTo(redisConnectionFactory);
    }

    @Test
    @DisplayName("应该使用提供的RedisConnectionFactory")
    void should_UseProvidedConnectionFactory_When_CreatingStringRedisTemplate() {
        // When
        StringRedisTemplate stringRedisTemplate = redisConfig.stringRedisTemplate(redisConnectionFactory);

        // Then
        assertThat(stringRedisTemplate.getConnectionFactory()).isSameAs(redisConnectionFactory);
    }

    @Test
    @DisplayName("创建的StringRedisTemplate应该是可用的")
    void should_CreateUsableStringRedisTemplate_When_BeanMethodCalled() {
        // When
        StringRedisTemplate stringRedisTemplate = redisConfig.stringRedisTemplate(redisConnectionFactory);

        // Then
        assertNotNull(stringRedisTemplate);
        assertThat(stringRedisTemplate).isInstanceOf(StringRedisTemplate.class);
        // 验证默认序列化器设置正确
        assertNotNull(stringRedisTemplate.getKeySerializer());
        assertNotNull(stringRedisTemplate.getValueSerializer());
        assertNotNull(stringRedisTemplate.getHashKeySerializer());
        assertNotNull(stringRedisTemplate.getHashValueSerializer());
    }
}