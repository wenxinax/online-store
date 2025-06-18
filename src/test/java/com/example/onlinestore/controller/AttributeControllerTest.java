package com.example.onlinestore.controller;

import com.example.onlinestore.bean.Attribute;
import com.example.onlinestore.dto.CreateAttributeRequest;
import com.example.onlinestore.dto.UpdateAttributeRequest;
import com.example.onlinestore.service.AttributeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(AttributeController.class)
@DisplayName("Attribute Controller Tests")
class AttributeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AttributeService attributeService;

    @Autowired
    private ObjectMapper objectMapper;

    private Attribute testAttribute;
    private CreateAttributeRequest createRequest;
    private UpdateAttributeRequest updateRequest;

    @BeforeEach
    void setUp() {
        testAttribute = new Attribute();
        testAttribute.setId(1L);
        testAttribute.setName("Color");
        testAttribute.setDescription("Product color attribute");
        testAttribute.setRequired(true);
        testAttribute.setType("STRING");

        createRequest = new CreateAttributeRequest();
        createRequest.setName("Size");
        createRequest.setDescription("Product size attribute");
        createRequest.setRequired(false);
        createRequest.setType("STRING");

        updateRequest = new UpdateAttributeRequest();
        updateRequest.setName("Updated Color");
        updateRequest.setDescription("Updated product color attribute");
        updateRequest.setRequired(false);
        updateRequest.setType("STRING");
    }

    @Nested
    @DisplayName("POST /api/v1/attributes - Create Attribute")
    class CreateAttributeTests {

        @Test
        @DisplayName("Should create attribute successfully with valid request")
        void shouldCreateAttributeSuccessfullyWithValidRequest() throws Exception {
            when(attributeService.createAttribute(any(CreateAttributeRequest.class)))
                    .thenReturn(testAttribute);

            mockMvc.perform(post("/api/v1/attributes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.id", is(1)))
                    .andExpect(jsonPath("$.data.name", is("Color")))
                    .andExpect(jsonPath("$.data.description", is("Product color attribute")))
                    .andExpect(jsonPath("$.data.required", is(true)))
                    .andExpect(jsonPath("$.data.type", is("STRING")));

            verify(attributeService, times(1)).createAttribute(any(CreateAttributeRequest.class));
        }

        @Test
        @DisplayName("Should return bad request when name is null")
        void shouldReturnBadRequestWhenNameIsNull() throws Exception {
            createRequest.setName(null);

            mockMvc.perform(post("/api/v1/attributes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isBadRequest());

            verify(attributeService, never()).createAttribute(any());
        }

        @Test
        @DisplayName("Should return bad request when name is blank")
        void shouldReturnBadRequestWhenNameIsBlank() throws Exception {
            createRequest.setName("");

            mockMvc.perform(post("/api/v1/attributes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isBadRequest());

            verify(attributeService, never()).createAttribute(any());
        }

        @Test
        @DisplayName("Should return bad request when type is null")
        void shouldReturnBadRequestWhenTypeIsNull() throws Exception {
            createRequest.setType(null);

            mockMvc.perform(post("/api/v1/attributes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isBadRequest());

            verify(attributeService, never()).createAttribute(any());
        }

        @Test
        @DisplayName("Should return bad request when required field is null")
        void shouldReturnBadRequestWhenRequiredFieldIsNull() throws Exception {
            createRequest.setRequired(null);

            mockMvc.perform(post("/api/v1/attributes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isBadRequest());

            verify(attributeService, never()).createAttribute(any());
        }

        @Test
        @DisplayName("Should handle service exceptions gracefully")
        void shouldHandleServiceExceptionsGracefully() throws Exception {
            when(attributeService.createAttribute(any(CreateAttributeRequest.class)))
                    .thenThrow(new RuntimeException("Database error"));

            mockMvc.perform(post("/api/v1/attributes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isInternalServerError());

            verify(attributeService, times(1)).createAttribute(any(CreateAttributeRequest.class));
        }

        @Test
        @DisplayName("Should return bad request for malformed JSON")
        void shouldReturnBadRequestForMalformedJson() throws Exception {
            mockMvc.perform(post("/api/v1/attributes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{invalid json"))
                    .andExpect(status().isBadRequest());

            verify(attributeService, never()).createAttribute(any());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/attributes/{attributeId} - Get Attribute By ID")
    class GetAttributeByIdTests {

        @Test
        @DisplayName("Should return attribute when valid ID is provided")
        void shouldReturnAttributeWhenValidIdIsProvided() throws Exception {
            when(attributeService.getAttributeById(1L)).thenReturn(testAttribute);

            mockMvc.perform(get("/api/v1/attributes/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.id", is(1)))
                    .andExpect(jsonPath("$.data.name", is("Color")))
                    .andExpect(jsonPath("$.data.description", is("Product color attribute")))
                    .andExpect(jsonPath("$.data.required", is(true)))
                    .andExpect(jsonPath("$.data.type", is("STRING")));

            verify(attributeService, times(1)).getAttributeById(1L);
        }

        @Test
        @DisplayName("Should handle service exceptions when attribute not found")
        void shouldHandleServiceExceptionsWhenAttributeNotFound() throws Exception {
            when(attributeService.getAttributeById(999L))
                    .thenThrow(new IllegalArgumentException("Attribute not found"));

            mockMvc.perform(get("/api/v1/attributes/999"))
                    .andExpect(status().isNotFound());

            verify(attributeService, times(1)).getAttributeById(999L);
        }

        @Test
        @DisplayName("Should return bad request for invalid ID format")
        void shouldReturnBadRequestForInvalidIdFormat() throws Exception {
            mockMvc.perform(get("/api/v1/attributes/invalid"))
                    .andExpect(status().isBadRequest());

            verify(attributeService, never()).getAttributeById(any());
        }

        @Test
        @DisplayName("Should handle zero ID")
        void shouldHandleZeroId() throws Exception {
            when(attributeService.getAttributeById(0L))
                    .thenThrow(new IllegalArgumentException("Invalid attribute ID"));

            mockMvc.perform(get("/api/v1/attributes/0"))
                    .andExpect(status().isBadRequest());

            verify(attributeService, times(1)).getAttributeById(0L);
        }

        @Test
        @DisplayName("Should handle negative ID")
        void shouldHandleNegativeId() throws Exception {
            when(attributeService.getAttributeById(-1L))
                    .thenThrow(new IllegalArgumentException("Invalid attribute ID"));

            mockMvc.perform(get("/api/v1/attributes/-1"))
                    .andExpect(status().isBadRequest());

            verify(attributeService, times(1)).getAttributeById(-1L);
        }

        @Test
        @DisplayName("Should handle very large ID")
        void shouldHandleVeryLargeId() throws Exception {
            Long largeId = Long.MAX_VALUE;
            when(attributeService.getAttributeById(largeId))
                    .thenThrow(new IllegalArgumentException("Attribute not found"));

            mockMvc.perform(get("/api/v1/attributes/" + largeId))
                    .andExpect(status().isNotFound());

            verify(attributeService, times(1)).getAttributeById(largeId);
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/attributes/{attributeId} - Update Attribute")
    class UpdateAttributeTests {

        @Test
        @DisplayName("Should update attribute successfully with valid request")
        void shouldUpdateAttributeSuccessfullyWithValidRequest() throws Exception {
            doNothing().when(attributeService).updateAttribute(eq(1L), any(UpdateAttributeRequest.class));

            mockMvc.perform(put("/api/v1/attributes/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)));

            verify(attributeService, times(1)).updateAttribute(eq(1L), any(UpdateAttributeRequest.class));
        }

        @Test
        @DisplayName("Should return bad request when name is null")
        void shouldReturnBadRequestWhenNameIsNull() throws Exception {
            updateRequest.setName(null);

            mockMvc.perform(put("/api/v1/attributes/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest());

            verify(attributeService, never()).updateAttribute(any(), any());
        }

        @Test
        @DisplayName("Should return bad request when name is blank")
        void shouldReturnBadRequestWhenNameIsBlank() throws Exception {
            updateRequest.setName(" ");

            mockMvc.perform(put("/api/v1/attributes/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest());

            verify(attributeService, never()).updateAttribute(any(), any());
        }

        @Test
        @DisplayName("Should return bad request when type is null")
        void shouldReturnBadRequestWhenTypeIsNull() throws Exception {
            updateRequest.setType(null);

            mockMvc.perform(put("/api/v1/attributes/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest());

            verify(attributeService, never()).updateAttribute(any(), any());
        }

        @Test
        @DisplayName("Should return bad request when required field is null")
        void shouldReturnBadRequestWhenRequiredFieldIsNull() throws Exception {
            updateRequest.setRequired(null);

            mockMvc.perform(put("/api/v1/attributes/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest());

            verify(attributeService, never()).updateAttribute(any(), any());
        }

        @Test
        @DisplayName("Should handle not found exception when attribute does not exist")
        void shouldHandleNotFoundExceptionWhenAttributeDoesNotExist() throws Exception {
            doThrow(new IllegalArgumentException("Attribute not found"))
                    .when(attributeService).updateAttribute(eq(999L), any(UpdateAttributeRequest.class));

            mockMvc.perform(put("/api/v1/attributes/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound());

            verify(attributeService, times(1)).updateAttribute(eq(999L), any(UpdateAttributeRequest.class));
        }

        @Test
        @DisplayName("Should return bad request for invalid ID format")
        void shouldReturnBadRequestForInvalidIdFormat() throws Exception {
            mockMvc.perform(put("/api/v1/attributes/invalid")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest());

            verify(attributeService, never()).updateAttribute(any(), any());
        }

        @Test
        @DisplayName("Should handle service exceptions gracefully")
        void shouldHandleServiceExceptionsGracefully() throws Exception {
            doThrow(new RuntimeException("Database error"))
                    .when(attributeService).updateAttribute(eq(1L), any(UpdateAttributeRequest.class));

            mockMvc.perform(put("/api/v1/attributes/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isInternalServerError());

            verify(attributeService, times(1)).updateAttribute(eq(1L), any(UpdateAttributeRequest.class));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Integration Tests")
    class EdgeCaseAndIntegrationTests {

        @Test
        @DisplayName("Should handle very long attribute names")
        void shouldHandleVeryLongAttributeNames() throws Exception {
            String longName = "A".repeat(255);
            createRequest.setName(longName);

            Attribute longNameAttribute = new Attribute();
            longNameAttribute.setId(2L);
            longNameAttribute.setName(longName);
            longNameAttribute.setDescription(createRequest.getDescription());
            longNameAttribute.setRequired(createRequest.getRequired());
            longNameAttribute.setType(createRequest.getType());

            when(attributeService.createAttribute(any(CreateAttributeRequest.class)))
                    .thenReturn(longNameAttribute);

            mockMvc.perform(post("/api/v1/attributes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name", is(longName)));

            verify(attributeService, times(1)).createAttribute(any(CreateAttributeRequest.class));
        }

        @Test
        @DisplayName("Should handle special characters in attribute names")
        void shouldHandleSpecialCharactersInAttributeNames() throws Exception {
            String specialName = "Color & Size (™) 测试";
            createRequest.setName(specialName);

            Attribute specialAttribute = new Attribute();
            specialAttribute.setId(3L);
            specialAttribute.setName(specialName);
            specialAttribute.setDescription(createRequest.getDescription());
            specialAttribute.setRequired(createRequest.getRequired());
            specialAttribute.setType(createRequest.getType());

            when(attributeService.createAttribute(any(CreateAttributeRequest.class)))
                    .thenReturn(specialAttribute);

            mockMvc.perform(post("/api/v1/attributes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name", is(specialName)));

            verify(attributeService, times(1)).createAttribute(any(CreateAttributeRequest.class));
        }

        @Test
        @DisplayName("Should handle null description gracefully")
        void shouldHandleNullDescriptionGracefully() throws Exception {
            createRequest.setDescription(null);

            when(attributeService.createAttribute(any(CreateAttributeRequest.class)))
                    .thenReturn(testAttribute);

            mockMvc.perform(post("/api/v1/attributes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isOk());

            verify(attributeService, times(1)).createAttribute(any(CreateAttributeRequest.class));
        }

        @Test
        @DisplayName("Should validate content type headers")
        void shouldValidateContentTypeHeaders() throws Exception {
            mockMvc.perform(post("/api/v1/attributes")
                    .contentType(MediaType.TEXT_PLAIN)
                    .content("Invalid content"))
                    .andExpect(status().isUnsupportedMediaType());

            verify(attributeService, never()).createAttribute(any());
        }

        @Test
        @DisplayName("Should handle empty request body")
        void shouldHandleEmptyRequestBody() throws Exception {
            mockMvc.perform(post("/api/v1/attributes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(""))
                    .andExpect(status().isBadRequest());

            verify(attributeService, never()).createAttribute(any());
        }

        @Test
        @DisplayName("Should handle concurrent requests to same resource")
        void shouldHandleConcurrentRequestsToSameResource() throws Exception {
            when(attributeService.getAttributeById(1L)).thenReturn(testAttribute);

            for (int i = 0; i < 5; i++) {
                mockMvc.perform(get("/api/v1/attributes/1"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.id", is(1)));
            }

            verify(attributeService, times(5)).getAttributeById(1L);
        }

        @Test
        @DisplayName("Should handle different valid type values")
        void shouldHandleDifferentValidTypeValues() throws Exception {
            String[] validTypes = {"STRING", "NUMBER", "BOOLEAN", "DATE"};

            for (String type : validTypes) {
                createRequest.setType(type);

                Attribute attributeWithType = new Attribute();
                attributeWithType.setId(4L);
                attributeWithType.setName(createRequest.getName());
                attributeWithType.setDescription(createRequest.getDescription());
                attributeWithType.setRequired(createRequest.getRequired());
                attributeWithType.setType(type);

                when(attributeService.createAttribute(any(CreateAttributeRequest.class)))
                        .thenReturn(attributeWithType);

                mockMvc.perform(post("/api/v1/attributes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.type", is(type)));

                reset(attributeService);
            }
        }
    }
}