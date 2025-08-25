package com.example.onlinestore.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConditionalOnProperty(name = "spring.cloud.nacos.enabled", havingValue = "false")
@PropertySource(value = "classpath:application-local.yaml", factory = YamlPropertySourceFactory.class)
public class LocalConfigProperties {
    // 这个类只是用来加载本地配置文件，不需要任何属性或方法
} 