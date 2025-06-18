package com.example.onlinestore.dto.converter;

import com.example.onlinestore.bean.Item;
import com.example.onlinestore.dto.ItemResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ItemResponseConverter Unit Tests")
class ItemResponseConverterTest {

    @InjectMocks
    private ItemResponseConverter itemResponseConverter;

    @BeforeEach
    void setUp() {
        // No initialization required at this moment
    }

    @Test
    @DisplayName("Should successfully convert valid item with all fields to ItemResponse")
    void shouldConvertValidItemWithAllFields() {
        Item item = createItemWithAllFields();
        ItemResponse response = itemResponseConverter.convert(item);

        assertNotNull(response);
        assertEquals(item.getId(), response.getId());
        assertEquals(item.getName(), response.getName());
        assertEquals(item.getDescription(), response.getDescription());
        assertEquals(item.getMainImageURL(), response.getMainImageURL());
        assertEquals(item.getSubImageURLs(), response.getSubImageURLs());
        assertEquals(item.getCategoryId(), response.getCategoryId());
        assertEquals(item.getBrandId(), response.getBrandId());
        assertEquals(item.getAttributes(), response.getAttributes());
    }

    @Test
    @DisplayName("Should convert item with only required fields")
    void shouldConvertItemWithOnlyRequiredFields() {
        Item item = createItemWithRequiredFieldsOnly();
        ItemResponse response = itemResponseConverter.convert(item);

        assertNotNull(response);
        assertEquals(item.getId(), response.getId());
        assertEquals(item.getName(), response.getName());
        assertEquals(item.getCategoryId(), response.getCategoryId());
        assertEquals(item.getBrandId(), response.getBrandId());
        assertNull(response.getDescription());
        assertNull(response.getMainImageURL());
        assertNull(response.getSubImageURLs());
        assertNull(response.getAttributes());
    }

    @Test
    @DisplayName("Should convert item with empty collections properly")
    void shouldConvertItemWithEmptyCollections() {
        Item item = createItemWithEmptyCollections();
        ItemResponse response = itemResponseConverter.convert(item);

        assertNotNull(response);
        assertEquals(item.getId(), response.getId());
        // Based on CollectionUtils.isNotEmpty logic, empty collections are treated as null
        assertNull(response.getAttributes());
    }

    @Test
    @DisplayName("Should return null when input item is null")
    void shouldReturnNullWhenInputItemIsNull() {
        ItemResponse response = itemResponseConverter.convert(null);
        assertNull(response);
    }

    @Test
    @DisplayName("Should handle item with null optional fields gracefully")
    void shouldHandleItemWithNullOptionalFields() {
        Item item = createItemWithNullOptionalFields();
        ItemResponse response = itemResponseConverter.convert(item);

        assertNotNull(response);
        assertEquals(item.getId(), response.getId());
        assertEquals(item.getName(), response.getName());
        assertNull(response.getDescription());
        assertNull(response.getMainImageURL());
        assertNull(response.getSubImageURLs());
        assertNull(response.getAttributes());
    }

    @Test
    @DisplayName("Should handle item with maximum length strings")
    void shouldHandleItemWithMaximumLengthStrings() {
        String longDescription = "A".repeat(1000);
        String longName = "B".repeat(255);
        Item item = createItemWithLongStrings(longName, longDescription);
        ItemResponse response = itemResponseConverter.convert(item);

        assertNotNull(response);
        assertEquals(longName, response.getName());
        assertEquals(longDescription, response.getDescription());
    }

    @Test
    @DisplayName("Should handle item with special characters in strings")
    void shouldHandleItemWithSpecialCharacters() {
        String specialName = "Item™ with 特殊字符 & émojis 🎯";
        String specialDescription = "Description with\nnewlines\tand\ttabs & <special> chars";
        Item item = createItemWithSpecialCharacters(specialName, specialDescription);
        ItemResponse response = itemResponseConverter.convert(item);

        assertNotNull(response);
        assertEquals(specialName, response.getName());
        assertEquals(specialDescription, response.getDescription());
    }

    @Test
    @DisplayName("Should handle item with populated attributes collection")
    void shouldHandleItemWithPopulatedAttributesCollection() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("color", "blue");
        attributes.put("size", "large");
        attributes.put("weight", 1.5);
        Item item = createItemWithAttributes(attributes);
        ItemResponse response = itemResponseConverter.convert(item);

        assertNotNull(response);
        assertNotNull(response.getAttributes());
        assertEquals(attributes, response.getAttributes());
        assertEquals("blue", response.getAttributes().get("color"));
        assertEquals("large", response.getAttributes().get("size"));
        assertEquals(1.5, response.getAttributes().get("weight"));
    }

    @Test
    @DisplayName("Should handle item with null attributes collection")
    void shouldHandleItemWithNullAttributesCollection() {
        Item item = createItemWithNullAttributes();
        ItemResponse response = itemResponseConverter.convert(item);

        assertNotNull(response);
        assertNull(response.getAttributes());
    }

    @Test
    @DisplayName("Should handle item with multiple sub image URLs")
    void shouldHandleItemWithMultipleSubImageUrls() {
        List<String> subImageURLs = Arrays.asList(
            "https://example.com/image1.jpg",
            "https://example.com/image2.jpg",
            "https://example.com/image3.jpg"
        );
        Item item = createItemWithSubImages(subImageURLs);
        ItemResponse response = itemResponseConverter.convert(item);

        assertNotNull(response);
        assertEquals(subImageURLs, response.getSubImageURLs());
        assertEquals(3, response.getSubImageURLs().size());
    }

    @Test
    @DisplayName("Should preserve all field values exactly without modification")
    void shouldPreserveAllFieldValuesExactly() {
        Long expectedId = 12345L;
        String expectedName = "Test Product Name";
        String expectedDescription = "Detailed product description";
        String expectedMainImageURL = "https://example.com/main.jpg";
        List<String> expectedSubImageURLs = Arrays.asList("https://example.com/sub1.jpg", "https://example.com/sub2.jpg");
        Long expectedCategoryId = 98L;
        Long expectedBrandId = 76L;
        Map<String, Object> expectedAttributes = Map.of("attr1", "value1", "attr2", 42);

        Item item = createCompleteItem(
            expectedId, expectedName, expectedDescription,
            expectedMainImageURL, expectedSubImageURLs,
            expectedCategoryId, expectedBrandId, expectedAttributes
        );
        ItemResponse response = itemResponseConverter.convert(item);

        assertNotNull(response);
        assertEquals(expectedId, response.getId());
        assertEquals(expectedName, response.getName());
        assertEquals(expectedDescription, response.getDescription());
        assertEquals(expectedMainImageURL, response.getMainImageURL());
        assertEquals(expectedSubImageURLs, response.getSubImageURLs());
        assertEquals(expectedCategoryId, response.getCategoryId());
        assertEquals(expectedBrandId, response.getBrandId());
        assertEquals(expectedAttributes, response.getAttributes());
    }

    @Test
    @DisplayName("Should handle multiple conversions consistently")
    void shouldHandleMultipleConversionsConsistently() {
        Item item = createItemWithAllFields();
        ItemResponse response1 = itemResponseConverter.convert(item);
        ItemResponse response2 = itemResponseConverter.convert(item);
        ItemResponse response3 = itemResponseConverter.convert(item);

        assertNotNull(response1);
        assertNotNull(response2);
        assertNotNull(response3);
        assertEquals(response1.getId(), response2.getId());
        assertEquals(response1.getId(), response3.getId());
        assertEquals(response1.getName(), response2.getName());
        assertEquals(response1.getName(), response3.getName());
        assertEquals(response1.getDescription(), response2.getDescription());
        assertEquals(response1.getDescription(), response3.getDescription());
    }

    @Test
    @DisplayName("Should not modify input item during conversion")
    void shouldNotModifyInputItemDuringConversion() {
        Item originalItem = createItemWithAllFields();
        Long originalId = originalItem.getId();
        String originalName = originalItem.getName();
        String originalDescription = originalItem.getDescription();

        ItemResponse response = itemResponseConverter.convert(originalItem);

        assertNotNull(response);
        assertEquals(originalId, originalItem.getId());
        assertEquals(originalName, originalItem.getName());
        assertEquals(originalDescription, originalItem.getDescription());
    }

    // Helper methods for test data creation

    private Item createItemWithAllFields() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Complete Test Item");
        item.setDescription("Complete test description");
        item.setMainImageURL("https://example.com/main-image.jpg");
        item.setSubImageURLs(Arrays.asList("https://example.com/sub1.jpg", "https://example.com/sub2.jpg"));
        item.setCategoryId(10L);
        item.setBrandId(20L);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("color", "red");
        attributes.put("size", "medium");
        item.setAttributes(attributes);
        return item;
    }

    private Item createItemWithRequiredFieldsOnly() {
        Item item = new Item();
        item.setId(2L);
        item.setName("Minimal Test Item");
        item.setCategoryId(11L);
        item.setBrandId(21L);
        return item;
    }

    private Item createItemWithEmptyCollections() {
        Item item = new Item();
        item.setId(3L);
        item.setName("Empty Collections Item");
        item.setCategoryId(12L);
        item.setBrandId(22L);
        item.setSubImageURLs(Collections.emptyList());
        item.setAttributes(Collections.emptyMap());
        return item;
    }

    private Item createItemWithNullOptionalFields() {
        Item item = new Item();
        item.setId(4L);
        item.setName("Null Fields Item");
        item.setCategoryId(13L);
        item.setBrandId(23L);
        item.setDescription(null);
        item.setMainImageURL(null);
        item.setSubImageURLs(null);
        item.setAttributes(null);
        return item;
    }

    private Item createItemWithLongStrings(String name, String description) {
        Item item = new Item();
        item.setId(5L);
        item.setName(name);
        item.setDescription(description);
        item.setCategoryId(14L);
        item.setBrandId(24L);
        return item;
    }

    private Item createItemWithSpecialCharacters(String name, String description) {
        Item item = new Item();
        item.setId(6L);
        item.setName(name);
        item.setDescription(description);
        item.setCategoryId(15L);
        item.setBrandId(25L);
        return item;
    }

    private Item createItemWithAttributes(Map<String, Object> attributes) {
        Item item = new Item();
        item.setId(7L);
        item.setName("Attributes Test Item");
        item.setCategoryId(16L);
        item.setBrandId(26L);
        item.setAttributes(attributes);
        return item;
    }

    private Item createItemWithNullAttributes() {
        Item item = new Item();
        item.setId(8L);
        item.setName("Null Attributes Item");
        item.setCategoryId(17L);
        item.setBrandId(27L);
        item.setAttributes(null);
        return item;
    }

    private Item createItemWithSubImages(List<String> subImageURLs) {
        Item item = new Item();
        item.setId(9L);
        item.setName("Sub Images Item");
        item.setCategoryId(18L);
        item.setBrandId(28L);
        item.setSubImageURLs(subImageURLs);
        return item;
    }

    private Item createCompleteItem(
            Long id,
            String name,
            String description,
            String mainImageURL,
            List<String> subImageURLs,
            Long categoryId,
            Long brandId,
            Map<String, Object> attributes
    ) {
        Item item = new Item();
        item.setId(id);
        item.setName(name);
        item.setDescription(description);
        item.setMainImageURL(mainImageURL);
        item.setSubImageURLs(subImageURLs);
        item.setCategoryId(categoryId);
        item.setBrandId(brandId);
        item.setAttributes(attributes);
        return item;
    }
}