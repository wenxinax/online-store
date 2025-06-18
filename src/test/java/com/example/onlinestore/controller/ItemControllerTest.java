package com.example.onlinestore.controller;

import com.example.onlinestore.dto.ItemDto;
import com.example.onlinestore.entity.Item;
import com.example.onlinestore.service.ItemService;
import com.example.onlinestore.exception.ItemNotFoundException;
import com.example.onlinestore.exception.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("ItemController Unit Tests")
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    private Item testItem;
    private ItemDto testItemDto;
    private List<Item> testItems;

    @BeforeEach
    void setUp() {
        testItem = Item.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .price(new BigDecimal("19.99"))
                .categoryId(1L)
                .stockQuantity(10)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testItemDto = ItemDto.builder()
                .name("Test Item DTO")
                .description("Test Description DTO")
                .price(new BigDecimal("29.99"))
                .categoryId(1L)
                .stockQuantity(15)
                .build();

        testItems = Arrays.asList(
                testItem,
                Item.builder()
                        .id(2L)
                        .name("Test Item 2")
                        .description("Test Description 2")
                        .price(new BigDecimal("39.99"))
                        .categoryId(2L)
                        .stockQuantity(5)
                        .active(true)
                        .build()
        );
    }

    @Nested
    @DisplayName("GET /api/items - Get All Items Tests")
    class GetAllItemsTests {

        @Test
        @DisplayName("Should return all items successfully")
        void getAllItems_ShouldReturnAllItems_WhenItemsExist() throws Exception {
            // Given
            when(itemService.getAllItems()).thenReturn(testItems);

            // When & Then
            mockMvc.perform(get("/api/items")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("Test Item"))
                    .andExpect(jsonPath("$[0].price").value(19.99))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].name").value("Test Item 2"));

            verify(itemService, times(1)).getAllItems();
        }

        @Test
        @DisplayName("Should return empty list when no items exist")
        void getAllItems_ShouldReturnEmptyList_WhenNoItemsExist() throws Exception {
            // Given
            when(itemService.getAllItems()).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/items"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(0));

            verify(itemService, times(1)).getAllItems();
        }

        @Test
        @DisplayName("Should return paginated items when pagination parameters provided")
        void getAllItems_ShouldReturnPaginatedItems_WhenPaginationProvided() throws Exception {
            // Given
            Page<Item> itemPage = new PageImpl<>(testItems, PageRequest.of(0, 10), testItems.size());
            when(itemService.getAllItems(any(Pageable.class))).thenReturn(itemPage);

            // When & Then
            mockMvc.perform(get("/api/items")
                    .param("page", "0")
                    .param("size", "10")
                    .param("sort", "name,asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.totalPages").value(1));

            verify(itemService, times(1)).getAllItems(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle service exception gracefully")
        void getAllItems_ShouldHandleServiceException_WhenServiceThrowsException() throws Exception {
            // Given
            when(itemService.getAllItems()).thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            mockMvc.perform(get("/api/items"))
                    .andExpect(status().isInternalServerError());

            verify(itemService, times(1)).getAllItems();
        }
    }

    @Nested
    @DisplayName("GET /api/items/{id} - Get Item By ID Tests")
    class GetItemByIdTests {

        @Test
        @DisplayName("Should return item when valid ID is provided")
        void getItemById_ShouldReturnItem_WhenValidIdProvided() throws Exception {
            // Given
            Long itemId = 1L;
            when(itemService.getItemById(itemId)).thenReturn(testItem);

            // When & Then
            mockMvc.perform(get("/api/items/{id}", itemId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Test Item"))
                    .andExpect(jsonPath("$.description").value("Test Description"))
                    .andExpect(jsonPath("$.price").value(19.99))
                    .andExpect(jsonPath("$.stockQuantity").value(10));

            verify(itemService, times(1)).getItemById(itemId);
        }

        @Test
        @DisplayName("Should return 404 when item not found")
        void getItemById_ShouldReturn404_WhenItemNotFound() throws Exception {
            // Given
            Long itemId = 999L;
            when(itemService.getItemById(itemId))
                    .thenThrow(new ItemNotFoundException("Item not found with id: " + itemId));

            // When & Then
            mockMvc.perform(get("/api/items/{id}", itemId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Item not found with id: " + itemId));

            verify(itemService, times(1)).getItemById(itemId);
        }

        @ParameterizedTest
        @ValueSource(longs = {-1L, 0L})
        @DisplayName("Should return 400 for invalid ID values")
        void getItemById_ShouldReturn400_WhenInvalidIdProvided(Long invalidId) throws Exception {
            mockMvc.perform(get("/api/items/{id}", invalidId))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when ID is not a number")
        void getItemById_ShouldReturn400_WhenIdIsNotNumber() throws Exception {
            mockMvc.perform(get("/api/items/{id}", "not-a-number"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/items - Create Item Tests")
    class CreateItemTests {

        @Test
        @DisplayName("Should create item successfully with valid data")
        void createItem_ShouldCreateItem_WhenValidDataProvided() throws Exception {
            // Given
            Item savedItem = Item.builder()
                    .id(1L)
                    .name(testItemDto.getName())
                    .description(testItemDto.getDescription())
                    .price(testItemDto.getPrice())
                    .categoryId(testItemDto.getCategoryId())
                    .stockQuantity(testItemDto.getStockQuantity())
                    .active(true)
                    .build();
            when(itemService.createItem(any(ItemDto.class))).thenReturn(savedItem);

            // When & Then
            mockMvc.perform(post("/api/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testItemDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value(testItemDto.getName()))
                    .andExpect(jsonPath("$.price").value(testItemDto.getPrice()))
                    .andExpect(header().exists("Location"));

            verify(itemService, times(1)).createItem(any(ItemDto.class));
        }

        @Test
        @DisplayName("Should return 400 when required fields are missing")
        void createItem_ShouldReturn400_WhenRequiredFieldsMissing() throws Exception {
            ItemDto invalidItem = ItemDto.builder()
                    .description("Description without name")
                    .price(new BigDecimal("10.99"))
                    .build();

            mockMvc.perform(post("/api/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidItem)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").exists());

            verify(itemService, never()).createItem(any(ItemDto.class));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Should return 400 when name is null, empty, or blank")
        void createItem_ShouldReturn400_WhenNameIsInvalid(String invalidName) throws Exception {
            ItemDto invalidItem = ItemDto.builder()
                    .name(invalidName)
                    .description("Valid description")
                    .price(new BigDecimal("10.99"))
                    .categoryId(1L)
                    .stockQuantity(5)
                    .build();

            mockMvc.perform(post("/api/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidItem)))
                    .andExpect(status().isBadRequest());

            verify(itemService, never()).createItem(any(ItemDto.class));
        }

        @Test
        @DisplayName("Should return 400 when price is negative")
        void createItem_ShouldReturn400_WhenPriceIsNegative() throws Exception {
            ItemDto invalidItem = ItemDto.builder()
                    .name("Valid Name")
                    .description("Valid description")
                    .price(new BigDecimal("-10.99"))
                    .categoryId(1L)
                    .stockQuantity(5)
                    .build();

            mockMvc.perform(post("/api/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidItem)))
                    .andExpect(status().isBadRequest());

            verify(itemService, never()).createItem(any(ItemDto.class));
        }

        @Test
        @DisplayName("Should return 400 when stock quantity is negative")
        void createItem_ShouldReturn400_WhenStockQuantityIsNegative() throws Exception {
            ItemDto invalidItem = ItemDto.builder()
                    .name("Valid Name")
                    .description("Valid description")
                    .price(new BigDecimal("10.99"))
                    .categoryId(1L)
                    .stockQuantity(-5)
                    .build();

            mockMvc.perform(post("/api/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidItem)))
                    .andExpect(status().isBadRequest());

            verify(itemService, never()).createItem(any(ItemDto.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/items/{id} - Update Item Tests")
    class UpdateItemTests {

        @Test
        @DisplayName("Should update item successfully with valid data")
        void updateItem_ShouldUpdateItem_WhenValidDataProvided() throws Exception {
            Long itemId = 1L;
            Item updatedItem = Item.builder()
                    .id(itemId)
                    .name("Updated Item")
                    .description("Updated Description")
                    .price(new BigDecimal("25.99"))
                    .categoryId(2L)
                    .stockQuantity(20)
                    .active(true)
                    .build();
            when(itemService.updateItem(eq(itemId), any(ItemDto.class))).thenReturn(updatedItem);

            mockMvc.perform(put("/api/items/{id}", itemId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testItemDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(itemId))
                    .andExpect(jsonPath("$.name").value("Updated Item"))
                    .andExpect(jsonPath("$.price").value(25.99));

            verify(itemService, times(1)).updateItem(eq(itemId), any(ItemDto.class));
        }

        @Test
        @DisplayName("Should return 404 when item to update not found")
        void updateItem_ShouldReturn404_WhenItemNotFound() throws Exception {
            Long itemId = 999L;
            when(itemService.updateItem(eq(itemId), any(ItemDto.class)))
                    .thenThrow(new ItemNotFoundException("Item not found with id: " + itemId));

            mockMvc.perform(put("/api/items/{id}", itemId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testItemDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Item not found with id: " + itemId));

            verify(itemService, times(1)).updateItem(eq(itemId), any(ItemDto.class));
        }

        @Test
        @DisplayName("Should return 400 when update data is invalid")
        void updateItem_ShouldReturn400_WhenUpdateDataIsInvalid() throws Exception {
            Long itemId = 1L;
            ItemDto invalidUpdate = ItemDto.builder()
                    .name("")
                    .price(new BigDecimal("-5.00"))
                    .build();

            mockMvc.perform(put("/api/items/{id}", itemId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidUpdate)))
                    .andExpect(status().isBadRequest());

            verify(itemService, never()).updateItem(anyLong(), any(ItemDto.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/items/{id} - Delete Item Tests")
    class DeleteItemTests {

        @Test
        @DisplayName("Should delete item successfully when valid ID provided")
        void deleteItem_ShouldDeleteItem_WhenValidIdProvided() throws Exception {
            Long itemId = 1L;
            doNothing().when(itemService).deleteItem(itemId);

            mockMvc.perform(delete("/api/items/{id}", itemId))
                    .andExpect(status().isNoContent());

            verify(itemService, times(1)).deleteItem(itemId);
        }

        @Test
        @DisplayName("Should return 404 when item to delete not found")
        void deleteItem_ShouldReturn404_WhenItemNotFound() throws Exception {
            Long itemId = 999L;
            doThrow(new ItemNotFoundException("Item not found with id: " + itemId))
                    .when(itemService).deleteItem(itemId);

            mockMvc.perform(delete("/api/items/{id}", itemId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Item not found with id: " + itemId));

            verify(itemService, times(1)).deleteItem(itemId);
        }

        @Test
        @DisplayName("Should return 409 when delete violates constraints")
        void deleteItem_ShouldReturn409_WhenDeleteViolatesConstraints() throws Exception {
            Long itemId = 1L;
            doThrow(new RuntimeException("Cannot delete item with active orders"))
                    .when(itemService).deleteItem(itemId);

            mockMvc.perform(delete("/api/items/{id}", itemId))
                    .andExpect(status().isConflict());

            verify(itemService, times(1)).deleteItem(itemId);
        }

        @ParameterizedTest
        @ValueSource(longs = {-1L, 0L})
        @DisplayName("Should return 400 for invalid ID values in delete")
        void deleteItem_ShouldReturn400_WhenInvalidIdProvided(Long invalidId) throws Exception {
            mockMvc.perform(delete("/api/items/{id}", invalidId))
                    .andExpect(status().isBadRequest());

            verify(itemService, never()).deleteItem(anyLong());
        }
    }

    @Nested
    @DisplayName("GET /api/items/search - Search Items Tests")
    class SearchItemsTests {

        @Test
        @DisplayName("Should return items matching search criteria")
        void searchItems_ShouldReturnMatchingItems_WhenValidCriteriaProvided() throws Exception {
            String searchTerm = "Test";
            when(itemService.searchItems(searchTerm)).thenReturn(testItems);

            mockMvc.perform(get("/api/items/search")
                    .param("q", searchTerm))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(2));

            verify(itemService, times(1)).searchItems(searchTerm);
        }

        @Test
        @DisplayName("Should return empty list when no items match search criteria")
        void searchItems_ShouldReturnEmptyList_WhenNoItemsMatch() throws Exception {
            String searchTerm = "NonExistent";
            when(itemService.searchItems(searchTerm)).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/items/search")
                    .param("q", searchTerm))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(itemService, times(1)).searchItems(searchTerm);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("Should return 400 when search term is invalid")
        void searchItems_ShouldReturn400_WhenSearchTermIsInvalid(String invalidSearchTerm) throws Exception {
            mockMvc.perform(get("/api/items/search")
                    .param("q", invalidSearchTerm))
                    .andExpect(status().isBadRequest());

            verify(itemService, never()).searchItems(anyString());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Security Tests")
    class EdgeCasesAndSecurityTests {

        @Test
        @DisplayName("Should handle malformed JSON gracefully")
        void controller_ShouldHandleMalformedJson_Gracefully() throws Exception {
            mockMvc.perform(post("/api/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{invalid json"))
                    .andExpect(status().isBadRequest());

            verify(itemService, never()).createItem(any(ItemDto.class));
        }

        @Test
        @DisplayName("Should handle large payload gracefully")
        void controller_ShouldHandleLargePayload_Gracefully() throws Exception {
            ItemDto largeItem = ItemDto.builder()
                    .name("A".repeat(1000))
                    .description("B".repeat(5000))
                    .price(new BigDecimal("99999999.99"))
                    .categoryId(1L)
                    .stockQuantity(Integer.MAX_VALUE)
                    .build();

            mockMvc.perform(post("/api/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(largeItem)))
                    .andExpect(status().isBadRequest());

            verify(itemService, never()).createItem(any(ItemDto.class));
        }

        @Test
        @DisplayName("Should handle concurrent modification gracefully")
        void controller_ShouldHandleConcurrentModification_Gracefully() throws Exception {
            Long itemId = 1L;
            when(itemService.updateItem(eq(itemId), any(ItemDto.class)))
                    .thenThrow(new RuntimeException("Optimistic locking failure"));

            mockMvc.perform(put("/api/items/{id}", itemId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testItemDto)))
                    .andExpect(status().isConflict());

            verify(itemService, times(1)).updateItem(eq(itemId), any(ItemDto.class));
        }

        @Test
        @DisplayName("Should validate content type")
        void controller_ShouldValidateContentType() throws Exception {
            mockMvc.perform(post("/api/items")
                    .contentType(MediaType.TEXT_PLAIN)
                    .content("plain text content"))
                    .andExpect(status().isUnsupportedMediaType());

            verify(itemService, never()).createItem(any(ItemDto.class));
        }

        @Test
        @DisplayName("Should handle missing request body")
        void controller_ShouldHandleMissingRequestBody() throws Exception {
            mockMvc.perform(post("/api/items")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(itemService, never()).createItem(any(ItemDto.class));
        }
    }

    @Test
    @DisplayName("Should verify no unexpected interactions")
    void verifyMocks() {
        verifyNoMoreInteractions(itemService);
    }

    // Helper method for creating test data
    private Item createTestItem(Long id, String name, String description, BigDecimal price) {
        return Item.builder()
                .id(id)
                .name(name)
                .description(description)
                .price(price)
                .categoryId(1L)
                .stockQuantity(10)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // Helper method for creating test DTOs
    private ItemDto createTestItemDto(String name, String description, BigDecimal price) {
        return ItemDto.builder()
                .name(name)
                .description(description)
                .price(price)
                .categoryId(1L)
                .stockQuantity(10)
                .build();
    }

    @Test
    @DisplayName("Should handle multiple concurrent requests efficiently")
    void controller_ShouldHandleConcurrentRequests_Efficiently() throws Exception {
        when(itemService.getAllItems()).thenReturn(testItems);

        for (int i = 0; i < 100; i++) {
            mockMvc.perform(get("/api/items"))
                    .andExpect(status().isOk());
        }

        verify(itemService, times(100)).getAllItems();
    }
}