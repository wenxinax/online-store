package com.example.onlinestore.service.testutil;

import org.mockito.MockedStatic;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Redis测试辅助工具类
 */
public class RedisTestHelper {

    /**
     * 配置Redis模拟行为 - 正常操作
     */
    public static void setupRedisNormalBehavior(StringRedisTemplate redisTemplate, 
                                               ValueOperations<String, String> valueOperations) {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any());
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);
        when(valueOperations.increment(anyString())).thenReturn(1L);
    }

    /**
     * 配置Redis模拟行为 - 抛出异常
     */
    public static void setupRedisExceptionBehavior(StringRedisTemplate redisTemplate, 
                                                  ValueOperations<String, String> valueOperations) {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doThrow(new RuntimeException("Redis连接异常")).when(valueOperations)
            .set(anyString(), anyString(), anyLong(), any());
    }

    /**
     * 配置Redis获取用户信息 - 返回有效JSON
     */
    public static void setupRedisGetUser(ValueOperations<String, String> valueOperations, 
                                        String token, 
                                        String userJson) {
        when(valueOperations.get("token:" + token)).thenReturn(userJson);
    }

    /**
     * 配置Redis获取用户信息 - 返回null
     */
    public static void setupRedisGetUserNull(ValueOperations<String, String> valueOperations, 
                                            String token) {
        when(valueOperations.get("token:" + token)).thenReturn(null);
    }

    /**
     * 配置Redis失败登录计数
     */
    public static void setupRedisFailedLoginCount(ValueOperations<String, String> valueOperations, 
                                                 StringRedisTemplate redisTemplate,
                                                 Long count) {
        when(valueOperations.increment("login:fail")).thenReturn(count);
        when(redisTemplate.expire("login:fail", 1, java.util.concurrent.TimeUnit.DAYS))
            .thenReturn(true);
    }
}