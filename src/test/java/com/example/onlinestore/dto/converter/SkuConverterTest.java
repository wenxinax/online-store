package com.example.onlinestore.dto.converter;

import com.example.onlinestore.dto.SkuDto;
import com.example.onlinestore.entity.Sku;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@DisplayName("SkuConverter Tests")
class SkuConverterTest {

    private SkuConverter skuConverter;

    @BeforeEach
    void setUp() {
        skuConverter = new SkuConverter();
    }

    @Nested
    @DisplayName("Entity to DTO Conversion Tests")
    class EntityToDtoTests {

        @Test
        @DisplayName("Should convert valid Sku entity to SkuDto with all fields")
        void shouldConvertValidSkuEntityToDto() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            Sku sku = createSku(1L, "SKU-001", "Test Product", 29.99, 100, true, now, now);

            // When
            SkuDto result = skuConverter.toDto(sku);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getSkuCode()).isEqualTo("SKU-001");
            assertThat(result.getProductName()).isEqualTo("Test Product");
            assertThat(result.getPrice()).isEqualTo(29.99);
            assertThat(result.getQuantityInStock()).isEqualTo(100);
            assertThat(result.getActive()).isTrue();
            assertThat(result.getCreatedAt()).isEqualTo(now);
            assertThat(result.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when entity is null")
        void shouldThrowExceptionWhenEntityIsNull() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> skuConverter.toDto(null)
            );
            assertThat(exception.getMessage()).isEqualTo("Sku entity cannot be null");
        }

        @Test
        @DisplayName("Should convert entity with null optional fields")
        void shouldConvertEntityWithNullOptionalFields() {
            Sku sku = new Sku();
            sku.setId(1L);
            sku.setSkuCode("SKU-001");
            // Other fields left as null

            SkuDto result = skuConverter.toDto(sku);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getSkuCode()).isEqualTo("SKU-001");
            assertThat(result.getProductName()).isNull();
            assertThat(result.getPrice()).isNull();
            assertThat(result.getQuantityInStock()).isNull();
            assertThat(result.getActive()).isNull();
            assertThat(result.getCreatedAt()).isNull();
            assertThat(result.getUpdatedAt()).isNull();
        }

        @ParameterizedTest
        @ValueSource(doubles = {0.01, 99.99, 999.99, 1000.00, 0.0})
        @DisplayName("Should convert various price values correctly")
        void shouldConvertVariousPriceValues(double price) {
            Sku sku = createBasicSku();
            sku.setPrice(price);

            SkuDto result = skuConverter.toDto(sku);
            assertThat(result.getPrice()).isEqualTo(price);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 100, 1000, -5})
        @DisplayName("Should convert various quantity values correctly")
        void shouldConvertVariousQuantityValues(int quantity) {
            Sku sku = createBasicSku();
            sku.setQuantityInStock(quantity);

            SkuDto result = skuConverter.toDto(sku);
            assertThat(result.getQuantityInStock()).isEqualTo(quantity);
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        @DisplayName("Should convert active status correctly")
        void shouldConvertActiveStatus(boolean active) {
            Sku sku = createBasicSku();
            sku.setActive(active);

            SkuDto result = skuConverter.toDto(sku);
            assertThat(result.getActive()).isEqualTo(active);
        }
    }

    @Nested
    @DisplayName("DTO to Entity Conversion Tests")
    class DtoToEntityTests {

        @Test
        @DisplayName("Should convert valid SkuDto to Sku entity with all fields")
        void shouldConvertValidSkuDtoToEntity() {
            LocalDateTime now = LocalDateTime.now();
            SkuDto skuDto = createSkuDto(1L, "SKU-001", "Test Product", 29.99, 100, true, now, now);

            Sku result = skuConverter.toEntity(skuDto);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getSkuCode()).isEqualTo("SKU-001");
            assertThat(result.getProductName()).isEqualTo("Test Product");
            assertThat(result.getPrice()).isEqualTo(29.99);
            assertThat(result.getQuantityInStock()).isEqualTo(100);
            assertThat(result.getActive()).isTrue();
            assertThat(result.getCreatedAt()).isEqualTo(now);
            assertThat(result.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when DTO is null")
        void shouldThrowExceptionWhenDtoIsNull() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> skuConverter.toEntity(null)
            );
            assertThat(exception.getMessage()).isEqualTo("SkuDto cannot be null");
        }

        @Test
        @DisplayName("Should convert DTO with null optional fields")
        void shouldConvertDtoWithNullOptionalFields() {
            SkuDto skuDto = new SkuDto();
            skuDto.setId(1L);
            skuDto.setSkuCode("SKU-001");
            // Other fields left as null

            Sku result = skuConverter.toEntity(skuDto);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getSkuCode()).isEqualTo("SKU-001");
            assertThat(result.getProductName()).isNull();
            assertThat(result.getPrice()).isNull();
            assertThat(result.getQuantityInStock()).isNull();
            assertThat(result.getActive()).isNull();
            assertThat(result.getCreatedAt()).isNull();
            assertThat(result.getUpdatedAt()).isNull();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "SKU", "SKU-12345678901234567890", "SKU-001-ABC"})
        @DisplayName("Should handle various SKU code formats")
        void shouldHandleVariousSkuCodeFormats(String skuCode) {
            SkuDto skuDto = createBasicSkuDto();
            skuDto.setSkuCode(skuCode);

            Sku result = skuConverter.toEntity(skuDto);
            assertThat(result.getSkuCode()).isEqualTo(skuCode);
        }
    }

    @Nested
    @DisplayName("Entity Update Tests")
    class EntityUpdateTests {

        @Test
        @DisplayName("Should update existing entity with DTO data")
        void shouldUpdateExistingEntityWithDtoData() {
            LocalDateTime originalCreated = LocalDateTime.now().minusDays(1);
            LocalDateTime newUpdated = LocalDateTime.now();

            Sku existingEntity = createSku(1L, "OLD-SKU", "Old Product", 19.99, 50, false, originalCreated, originalCreated);
            SkuDto updateDto = createSkuDto(1L, "NEW-SKU", "New Product", 49.99, 150, true, originalCreated, newUpdated);

            Sku result = skuConverter.updateEntity(existingEntity, updateDto);

            assertThat(result).isSameAs(existingEntity);
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getSkuCode()).isEqualTo("NEW-SKU");
            assertThat(result.getProductName()).isEqualTo("New Product");
            assertThat(result.getPrice()).isEqualTo(49.99);
            assertThat(result.getQuantityInStock()).isEqualTo(150);
            assertThat(result.getActive()).isTrue();
            assertThat(result.getCreatedAt()).isEqualTo(originalCreated);
            assertThat(result.getUpdatedAt()).isEqualTo(newUpdated);
        }

        @Test
        @DisplayName("Should throw exception when existing entity is null")
        void shouldThrowExceptionWhenExistingEntityIsNull() {
            SkuDto updateDto = createBasicSkuDto();

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> skuConverter.updateEntity(null, updateDto)
            );
            assertThat(exception.getMessage()).isEqualTo("Existing entity cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when update DTO is null")
        void shouldThrowExceptionWhenUpdateDtoIsNull() {
            Sku existingEntity = createBasicSku();

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> skuConverter.updateEntity(existingEntity, null)
            );
            assertThat(exception.getMessage()).isEqualTo("SkuDto cannot be null");
        }

        @Test
        @DisplayName("Should update entity with null values from DTO")
        void shouldUpdateEntityWithNullValuesFromDto() {
            Sku existingEntity = createSku(1L, "SKU-001", "Product", 29.99, 100, true, LocalDateTime.now(), LocalDateTime.now());
            SkuDto updateDto = new SkuDto();
            updateDto.setId(1L);
            updateDto.setSkuCode(null);
            updateDto.setProductName(null);
            updateDto.setPrice(null);
            updateDto.setQuantityInStock(null);
            updateDto.setActive(null);
            updateDto.setUpdatedAt(LocalDateTime.now());

            Sku result = skuConverter.updateEntity(existingEntity, updateDto);

            assertThat(result.getSkuCode()).isNull();
            assertThat(result.getProductName()).isNull();
            assertThat(result.getPrice()).isNull();
            assertThat(result.getQuantityInStock()).isNull();
            assertThat(result.getActive()).isNull();
        }
    }

    @Nested
    @DisplayName("Collection Conversion Tests")
    class CollectionConversionTests {

        @Test
        @DisplayName("Should convert list of entities to list of DTOs")
        void shouldConvertEntityListToDtoList() {
            List<Sku> entities = Arrays.asList(
                createSku(1L, "SKU-001", "Product 1", 19.99, 50, true, LocalDateTime.now(), LocalDateTime.now()),
                createSku(2L, "SKU-002", "Product 2", 29.99, 75, false, LocalDateTime.now(),	LocalDateTime.now()),
                createSku(3L, "SKU-003", "Product 3", 39.99, 100, true, LocalDateTime.now(), LocalDateTime.now())
            );

            List<SkuDto> result = skuConverter.toDtoList(entities);

            assertThat(result).hasSize(3);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(0).getSkuCode()).isEqualTo("SKU-001");
            assertThat(result.get(0).getProductName()).isEqualTo("Product 1");
            assertThat(result.get(0).getPrice()).isEqualTo(19.99);

            assertThat(result.get(1).getId()).isEqualTo(2L);
            assertThat(result.get(1).getSkuCode()).isEqualTo("SKU-002");
            assertThat(result.get(1).getActive()).isFalse();

            assertThat(result.get(2).getId()).isEqualTo(3L);
            assertThat(result.get(2).getSkuCode()).isEqualTo("SKU-003");
            assertThat(result.get(2).getQuantityInStock()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should convert list of DTOs to list of entities")
        void shouldConvertDtoListToEntityList() {
            List<SkuDto> dtos = Arrays.asList(
                createSkuDto(1L, "SKU-001", "Product 1", 19.99, 50, true, LocalDateTime.now(), LocalDateTime.now()),
                createSkuDto(2L, "SKU-002", "Product 2", 29.99, 75, false, LocalDateTime.now(), LocalDateTime.now()),
                createSkuDto(3L, "SKU-003", "Product 3", 39.99, 100, true, LocalDateTime.now(), LocalDateTime.now())
            );

            List<Sku> result = skuConverter.toEntityList(dtos);

            assertThat(result).hasSize(3);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(0).getSkuCode()).isEqualTo("SKU-001");
            assertThat(result.get(0).getProductName()).isEqualTo("Product 1");
            assertThat(result.get(0).getPrice()).isEqualTo(19.99);

            assertThat(result.get(1).getId()).isEqualTo(2L);
            assertThat(result.get(1).getSkuCode()).isEqualTo("SKU-002");
            assertThat(result.get(1).getActive()).isFalse();

            assertThat(result.get(2).getId()).isEqualTo(3L);
            assertThat(result.get(2).getSkuCode()).isEqualTo("SKU-003");
            assertThat(result.get(2).getQuantityInStock()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should handle empty entity list gracefully")
        void shouldHandleEmptyEntityListGracefully() {
            List<Sku> emptyEntityList = Collections.emptyList();
            List<SkuDto> result = skuConverter.toDtoList(emptyEntityList);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle empty DTO list gracefully")
        void shouldHandleEmptyDtoListGracefully() {
            List<SkuDto> emptyDtoList = Collections.emptyList();
            List<Sku> result = skuConverter.toEntityList(emptyDtoList);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle null entity list gracefully")
        void shouldHandleNullEntityListGracefully() {
            List<SkuDto> result = skuConverter.toDtoList(null);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle null DTO list gracefully")
        void shouldHandleNullDtoListGracefully() {
            List<Sku> result = skuConverter.toEntityList(null);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle large lists efficiently")
        void shouldHandleLargeListsEfficiently() {
            List<Sku> largeEntityList = Stream.iterate(1, i -> i + 1)
                .limit(1000)
                .map(i -> createSku(Long.valueOf(i), "SKU-" + String.format("%03d", i), "Product " + i,
                    Math.random() * 100, (int) (Math.random() * 1000), true, LocalDateTime.now(), LocalDateTime.now()))
                .collect(Collectors.toList());

            List<SkuDto> result = skuConverter.toDtoList(largeEntityList);

            assertThat(result).hasSize(1000);
            assertThat(result.get(0).getSkuCode()).isEqualTo("SKU-001");
            assertThat(result.get(999).getSkuCode()).isEqualTo("SKU-1000");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle negative quantities gracefully")
        void shouldHandleNegativeQuantities() {
            Sku sku = createBasicSku();
            sku.setQuantityInStock(-5);
            SkuDto result = skuConverter.toDto(sku);
            assertThat(result.getQuantityInStock()).isEqualTo(-5);
        }

        @Test
        @DisplayName("Should handle negative prices gracefully")
        void shouldHandleNegativePrices() {
            Sku sku = createBasicSku();
            sku.setPrice(-10.50);
            SkuDto result = skuConverter.toDto(sku);
            assertThat(result.getPrice()).isEqualTo(-10.50);
        }

        @Test
        @DisplayName("Should handle very large ID values")
        void shouldHandleVeryLargeIdValues() {
            Long largeId = Long.MAX_VALUE;
            Sku sku = createBasicSku();
            sku.setId(largeId);
            SkuDto result = skuConverter.toDto(sku);
            assertThat(result.getId()).isEqualTo(largeId);
        }

        @Test
        @DisplayName("Should handle zero ID values")
        void shouldHandleZeroIdValues() {
            Sku sku = createBasicSku();
            sku.setId(0L);
            SkuDto result = skuConverter.toDto(sku);
            assertThat(result.getId()).isEqualTo(0L);
        }

        @Test
        @DisplayName("Should handle special characters in product names")
        void shouldHandleSpecialCharactersInProductNames() {
            String specialName = "Product with émojis 🎉 & symbols @#$%^&*()_+-=[]{}|;':\",./<>?";
            Sku sku = createBasicSku();
            sku.setProductName(specialName);
            SkuDto result = skuConverter.toDto(sku);
            assertThat(result.getProductName()).isEqualTo(specialName);
        }

        @Test
        @DisplayName("Should handle very long product names")
        void shouldHandleVeryLongProductNames() {
            String longName = "A".repeat(1000);
            Sku sku = createBasicSku();
            sku.setProductName(longName);
            SkuDto result = skuConverter.toDto(sku);
            assertThat(result.getProductName()).isEqualTo(longName);
        }

        @Test
        @DisplayName("Should handle very long SKU codes")
        void shouldHandleVeryLongSkuCodes() {
            String longSku = "SKU-" + "X".repeat(100);
            Sku sku = createBasicSku();
            sku.setSkuCode(longSku);
            SkuDto result = skuConverter.toDto(sku);
            assertThat(result.getSkuCode()).isEqualTo(longSku);
        }

        @ParameterizedTest
        @MethodSource("extremeValues")
        @DisplayName("Should handle extreme numeric values")
        void shouldHandleExtremeNumericValues(Double price, Integer quantity) {
            Sku sku = createBasicSku();
            sku.setPrice(price);
            sku.setQuantityInStock(quantity);
            SkuDto result = skuConverter.toDto(sku);
            assertThat(result.getPrice()).isEqualTo(price);
            assertThat(result.getQuantityInStock()).isEqualTo(quantity);
        }

        static Stream<Arguments> extremeValues() {
            return Stream.of(
                Arguments.of(Double.MAX_VALUE, Integer.MAX_VALUE),
                Arguments.of(Double.MIN_VALUE, Integer.MIN_VALUE),
                Arguments.of(0.0, 0),
                Arguments.of(Double.POSITIVE_INFINITY, Integer.MAX_VALUE),
                Arguments.of(Double.NEGATIVE_INFINITY, Integer.MIN_VALUE)
            );
        }

        @Test
        @DisplayName("Should handle timestamp precision correctly")
        void shouldHandleTimestampPrecisionCorrectly() {
            LocalDateTime preciseTime = LocalDateTime.of(2023, 12, 25, 14, 30, 45, 123456789);
            Sku sku = createBasicSku();
            sku.setCreatedAt(preciseTime);
            sku.setUpdatedAt(preciseTime);
            SkuDto result = skuConverter.toDto(sku);
            assertThat(result.getCreatedAt()).isEqualTo(preciseTime);
            assertThat(result.getUpdatedAt()).isEqualTo(preciseTime);
        }
    }

    // Helper methods
    private Sku createBasicSku() {
        Sku sku = new Sku();
        sku.setId(1L);
        sku.setSkuCode("SKU-001");
        sku.setProductName("Test Product");
        sku.setPrice(29.99);
        sku.setQuantityInStock(100);
        sku.setActive(true);
        sku.setCreatedAt(LocalDateTime.now());
        sku.setUpdatedAt(LocalDateTime.now());
        return sku;
    }

    private Sku createSku(Long id, String skuCode, String productName, Double price,
                         Integer quantity, Boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        Sku sku = new Sku();
        sku.setId(id);
        sku.setSkuCode(skuCode);
        sku.setProductName(productName);
        sku.setPrice(price);
        sku.setQuantityInStock(quantity);
        sku.setActive(active);
        sku.setCreatedAt(createdAt);
        sku.setUpdatedAt(updatedAt);
        return sku;
    }

    private SkuDto createBasicSkuDto() {
        SkuDto skuDto = new SkuDto();
        skuDto.setId(1L);
        skuDto.setSkuCode("SKU-001");
        skuDto.setProductName("Test Product");
        skuDto.setPrice(29.99);
        skuDto.setQuantityInStock(100);
        skuDto.setActive(true);
        skuDto.setCreatedAt(LocalDateTime.now());
        skuDto.setUpdatedAt(LocalDateTime.now());
        return skuDto;
    }

    private SkuDto createSkuDto(Long id, String skuCode, String productName, Double price,
                                Integer quantity, Boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        SkuDto skuDto = new SkuDto();
        skuDto.setId(id);
        skuDto.setSkuCode(skuCode);
        skuDto.setProductName(productName);
        skuDto.setPrice(price);
        skuDto.setQuantityInStock(quantity);
        skuDto.setActive(active);
        skuDto.setCreatedAt(createdAt);
        skuDto.setUpdatedAt(updatedAt);
        return skuDto;
    }
}