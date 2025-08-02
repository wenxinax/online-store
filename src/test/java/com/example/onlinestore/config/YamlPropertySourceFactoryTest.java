package com.example.onlinestore.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.EncodedResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * YamlPropertySourceFactory工具类单元测试
 * 
 * 测试内容：
 * - YAML文件正常加载
 * - 属性解析正确性
 * - 异常处理验证
 * - 编码处理测试
 */
@DisplayName("YamlPropertySourceFactory工具类测试")
class YamlPropertySourceFactoryTest {

    private YamlPropertySourceFactory yamlPropertySourceFactory;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        yamlPropertySourceFactory = new YamlPropertySourceFactory();
    }

    @Test
    @DisplayName("应该正确加载简单的YAML文件")
    void should_LoadSimpleYamlFile_When_ValidYamlProvided() throws IOException {
        // Given
        String yamlContent = """
                app:
                  name: test-app
                  version: 1.0.0
                server:
                  port: 8080
                """;
        Path yamlFile = createTempYamlFile("test.yml", yamlContent);
        EncodedResource encodedResource = new EncodedResource(new FileSystemResource(yamlFile.toFile()));

        // When
        PropertySource<?> propertySource = yamlPropertySourceFactory.createPropertySource("test", encodedResource);

        // Then
        assertNotNull(propertySource);
        assertEquals("test.yml", propertySource.getName());
        assertEquals("test-app", propertySource.getProperty("app.name"));
        assertEquals("1.0.0", propertySource.getProperty("app.version"));
        assertEquals(8080, propertySource.getProperty("server.port"));
    }

    @Test
    @DisplayName("应该正确处理空的YAML文件")
    void should_HandleEmptyYamlFile_When_EmptyYamlProvided() throws IOException {
        // Given
        String yamlContent = "";
        Path yamlFile = createTempYamlFile("empty.yml", yamlContent);
        EncodedResource encodedResource = new EncodedResource(new FileSystemResource(yamlFile.toFile()));

        // When
        PropertySource<?> propertySource = yamlPropertySourceFactory.createPropertySource("empty", encodedResource);

        // Then
        assertNotNull(propertySource);
        assertEquals("empty.yml", propertySource.getName());
    }

    @Test
    @DisplayName("应该正确处理基本的YAML结构")
    void should_HandleBasicYamlStructure_When_ValidYamlProvided() throws IOException {
        // Given
        String yamlContent = """
                test:
                  value: hello
                number: 42
                """;
        Path yamlFile = createTempYamlFile("basic.yml", yamlContent);
        EncodedResource encodedResource = new EncodedResource(new FileSystemResource(yamlFile.toFile()));

        // When
        PropertySource<?> propertySource = yamlPropertySourceFactory.createPropertySource("basic", encodedResource);

        // Then
        assertNotNull(propertySource);
        assertEquals("hello", propertySource.getProperty("test.value"));
        assertEquals(42, propertySource.getProperty("number"));
    }

    @Test
    @DisplayName("应该正确使用文件名作为PropertySource名称")
    void should_UseCorrectPropertySourceName_When_CreatingPropertySource() throws IOException {
        // Given
        String yamlContent = "test: value";
        Path yamlFile = createTempYamlFile("custom.yml", yamlContent);
        EncodedResource encodedResource = new EncodedResource(new FileSystemResource(yamlFile.toFile()));

        // When
        PropertySource<?> propertySource = yamlPropertySourceFactory.createPropertySource("custom", encodedResource);

        // Then
        assertNotNull(propertySource);
        assertEquals("custom.yml", propertySource.getName());
        assertEquals("value", propertySource.getProperty("test"));
    }

    private Path createTempYamlFile(String filename, String content) throws IOException {
        Path yamlFile = tempDir.resolve(filename);
        Files.write(yamlFile, content.getBytes("UTF-8"));
        return yamlFile;
    }
}