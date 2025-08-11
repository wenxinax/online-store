package com.example.onlinestore.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * ValidationConfig配置类单元测试
 * 
 * 测试内容：
 * - Validator Bean创建
 * - 验证器功能正确性
 * - 约束验证能力
 */
@DisplayName("ValidationConfig配置类测试")
class ValidationConfigTest {

    private ValidationConfig validationConfig;

    @BeforeEach
    void setUp() {
        validationConfig = new ValidationConfig();
    }

    @Test
    @DisplayName("应该创建Validator Bean")
    void should_CreateValidator_When_BeanMethodCalled() {
        // When
        Validator validator = validationConfig.validator();

        // Then
        assertNotNull(validator);
        assertThat(validator).isInstanceOf(Validator.class);
    }

    @Test
    @DisplayName("创建的Validator应该能够执行验证")
    void should_CreateWorkingValidator_When_BeanMethodCalled() {
        // Given
        Validator validator = validationConfig.validator();
        TestObject validObject = new TestObject("valid_name");

        // When
        Set<ConstraintViolation<TestObject>> violations = validator.validate(validObject);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("创建的Validator应该能够检测违反约束的情况")
    void should_DetectConstraintViolations_When_ValidatingInvalidObject() {
        // Given
        Validator validator = validationConfig.validator();
        TestObject invalidObject = new TestObject(""); // 空字符串违反@NotBlank约束

        // When
        Set<ConstraintViolation<TestObject>> violations = validator.validate(invalidObject);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<TestObject> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
    }

    @Test
    @DisplayName("创建的Validator应该能够检测大小约束违反")
    void should_DetectSizeConstraintViolations_When_ValidatingObjectWithWrongSize() {
        // Given
        Validator validator = validationConfig.validator();
        TestObject invalidObject = new TestObject("a"); // 长度小于2违反@Size约束

        // When
        Set<ConstraintViolation<TestObject>> violations = validator.validate(invalidObject);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<TestObject> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
    }

    @Test
    @DisplayName("多次调用应该创建相同的验证器实例")
    void should_CreateConsistentValidator_When_BeanMethodCalledMultipleTimes() {
        // When
        Validator validator1 = validationConfig.validator();
        Validator validator2 = validationConfig.validator();

        // Then
        assertNotNull(validator1);
        assertNotNull(validator2);
        // 验证器的类型应该一致
        assertThat(validator1.getClass()).isEqualTo(validator2.getClass());
    }

    // 测试用的简单对象
    private static class TestObject {
        @NotBlank
        @Size(min = 2, max = 50)
        private String name;

        public TestObject(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}