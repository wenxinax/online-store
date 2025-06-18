package com.example.onlinestore.controller;

import com.example.onlinestore.bean.Attribute;
import com.example.onlinestore.dto.AttributeResponse;
import com.example.onlinestore.dto.CreateAttributeRequest;
import com.example.onlinestore.dto.UpdateAttributeRequest;
import com.example.onlinestore.service.AttributeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AttributeController.class)
@DisplayName("AttributeController Tests")
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

        createRequest = new CreateAttributeRequest();
        createRequest.setName("Size");
        createRequest.setDescription("Product size attribute");

        updateRequest = new UpdateAttributeRequest();
        updateRequest.setName("Updated Color");
        updateRequest.setDescription("Updated product color attribute");
    }

    @Nested
    @DisplayName("POST /api/v1/attributes - Create Attribute")
    class CreateAttributeTests {

        @Test
        @DisplayName("Should create attribute successfully with valid data")
        void shouldCreateAttributeSuccessfullyWithValidData() throws Exception {
            Attribute createdAttribute = new Attribute();
            createdAttribute.setId(2L);
            createdAttribute.setName("Size");
            createdAttribute.setDescription("Product size attribute");

            when(attributeService.createAttribute(any(CreateAttributeRequest.class)))
                    .thenReturn(createdAttribute);

            mockMvc.perform(post("/api/v1/attributes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("Success"))
                    .andExpect(jsonPath("$.data.id").value(2))
                    .andExpect(jsonPath("$.data.name").value("Size"))
                    .andExpect(jsonPath("$.data.description").value("Product size attribute"));

            verify(attributeService, times(1)).createAttribute(any(CreateAttributeRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when name is null")
        void shouldReturn400WhenNameIsNull() throws Exception {
            CreateAttributeRequest invalidRequest = new CreateAttributeRequest();
            invalidRequest.setName(null);
            invalidRequest.setDescription("Description");

            mockMvc.perform(post("/api/v1/attributes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(attributeService, never()).createAttribute(any(CreateAttributeRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when name is empty")
        void shouldReturn400WhenNameIsEmpty() throws Exception {
            CreateAttributeRequest invalidRequest = new CreateAttributeRequest();
            invalidRequest.setName("");
            invalidRequest.setDescription("Description");

            mockMvc.perform(post("/api/v1/attributes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(attributeService, never()).createAttribute(any(CreateAttributeRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when name is blank")
        void shouldReturn400WhenNameIsBlank() throws Exception {
            CreateAttributeRequest invalidRequest = new CreateAttributeRequest();
            invalidRequest.setName("   ");
            invalidRequest.setDescription("Description");

            mockMvc.perform(post("/api/v1/attributes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(attributeService, never()).createAttribute(any(CreateAttributeRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when request body is malformed JSON")
        void shouldReturn400WhenRequestBodyIsMalformedJson() throws Exception {
            String malformedJson = "{ \"name\": \"Color\", \"description\": }";

            mockMvc.perform(post("/api/v1/attributes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(malformedJson))
                    .andExpect(status().isBadRequest());

            verify(attributeService, never()).createAttribute(any(CreateAttributeRequest.class));
        }

        @Test
        @DisplayName("Should return 415 when content type is not JSON")
        void shouldReturn400WhenContentTypeIsNotJson() throws Exception {
            mockMvc.perform(post("/api/v1/attributes")
                    .contentType(MediaType.TEXT_PLAIN)
                    .content("some text"))
                    .andExpect(status().isUnsupportedMediaType());

            verify(attributeService, never()).createAttribute(any(CreateAttributeRequest.class));
        }

        @Test
        @DisplayName("Should handle service exception during creation")
        void shouldHandleServiceExceptionDuringCreation() throws Exception {
            when(attributeService.createAttribute(any(CreateAttributeRequest.class)))
                    .thenThrow(new RuntimeException("Database error"));

            mockMvc.perform(post("/api/v1/attributes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isInternalServerError());

            verify(attributeService, times(1)).createAttribute(any(CreateAttributeRequest.class));
        }

        @Test
        @DisplayName("Should accept valid request with description as null")
        void shouldAcceptValidRequestWithDescriptionAsNull() throws Exception {
            CreateAttributeRequest requestWithNullDescription = new CreateAttributeRequest();
            requestWithNullDescription.setName("Material");
            requestWithNullDescription.setDescription(null);

            Attribute createdAttribute = new Attribute();
            createdAttribute.setId(3L);
            createdAttribute.setName("Material");
            createdAttribute.setDescription(null);

            when(attributeService.createAttribute(any(CreateAttributeRequest.class)))
                    .thenReturn(createdAttribute);

            mockMvc.perform(post("/api/v1/attributes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestWithNullDescription)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("Success"))
                    .andExpect(jsonPath("$.data.id").value(3))
                    .andExpect(jsonPath("$.data.name").value("Material"))
                    .andExpect(jsonPath("$.data.description").isEmpty());

            verify(attributeService, times(1)).createAttribute(any(CreateAttributeRequest.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/attributes/{attributeId} - Get Attribute By ID")
    class GetAttributeByIdTests {

        @Test
        @DisplayName("Should return attribute when valid ID is provided")
        void shouldReturnAttributeWhenValidIdProvided() throws Exception {
            when(attributeService.getAttributeById(1L)).thenReturn(testAttribute);

            mockMvc.perform(get("/api/v1/attributes/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("Success"))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.name").value("Color"))
                    .andExpect(jsonPath("$.data.description").value("Product color attribute"));

            verify(attributeService, times(1)).getAttributeById(1L);
        }

        @Test
        @DisplayName("Should handle service exception when getting attribute by ID")
        void shouldHandleServiceExceptionWhenGettingAttributeById() throws Exception {
            when(attributeService.getAttributeById(1L))
                    .thenThrow(new RuntimeException("Database connection error"));

            mockMvc.perform(get("/api/v1/attributes/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());

            verify(attributeService, times(1)).getAttributeById(1L);
        }

        @Test
        @DisplayName("Should return 400 when invalid ID format is provided")
        void shouldReturn400WhenInvalidIdFormatProvided() throws Exception {
            mockMvc.perform(get("/api/v1/attributes/invalid")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(attributeService, never()).getAttributeById(anyLong());
        }

        @Test
        @DisplayName("Should return 400 when negative ID is provided")
        void shouldReturn400WhenNegativeIdProvided() throws Exception {
            mockMvc.perform(get("/api/v1/attributes/-1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(attributeService, never()).getAttributeById(anyLong());
        }

        @Test
        @DisplayName("Should return 400 when zero ID is provided")
        void shouldReturn400WhenZeroIdProvided() throws Exception {
            mockMvc.perform(get("/api/v1/attributes/0")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(attributeService, never()).getAttributeById(anyLong());
        }

        @Test
        @DisplayName("Should handle very large ID values")
        void shouldHandleVeryLargeIdValues() throws Exception {
            long largeId = Long.MAX_VALUE;
            when(attributeService.getAttributeById(largeId))
                    .thenThrow(new RuntimeException("Attribute not found"));

            mockMvc.perform(get("/api/v1/attributes/" + largeId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());

            verify(attributeService, times(1)).getAttributeById(largeId);
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/attributes/{attributeId} - Update Attribute")
    class UpdateAttributeTests {

        @Test
        @DisplayName("Should update attribute successfully with valid data")
        void shouldUpdateAttributeSuccessfullyWithValidData() throws Exception {
            doNothing().when(attributeService).updateAttribute(eq(1L), any(UpdateAttributeRequest.class));

            mockMvc.perform(put("/api/v1/attributes/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("Success"))
                    .andExpect(jsonPath("$.data").isEmpty());

            verify(attributeService, times(1)).updateAttribute(eq(1L), any(UpdateAttributeRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when name is null in update request")
        void shouldReturn400WhenNameIsNullInUpdateRequest() throws Exception {
            UpdateAttributeRequest invalidRequest = new UpdateAttributeRequest();
            invalidRequest.setName(null);
            invalidRequest.setDescription("Updated description");

            mockMvc.perform(put("/api/v1/attributes/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(attributeService, never()).updateAttribute(anyLong(), any(UpdateAttributeRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when name is empty in update request")
        void shouldReturn400WhenNameIsEmptyInUpdateRequest() throws Exception {
            UpdateAttributeRequest invalidRequest = new UpdateAttributeRequest();
            invalidRequest.setName("");
            invalidRequest.setDescription("Updated description");

            mockMvc.perform(put("/api/v1/attributes/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(attributeService, never()).updateAttribute(anyLong(), any(UpdateAttributeRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when name is blank in update request")
        void shouldReturn400WhenNameIsBlankInUpdateRequest() throws Exception {
            UpdateAttributeRequest invalidRequest = new UpdateAttributeRequest();
            invalidRequest.setName("   ");
            invalidRequest.setDescription("Updated description");

            mockMvc.perform(put("/api/v1/attributes/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(attributeService, never()).updateAttribute(anyLong(), any(UpdateAttributeRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when invalid ID format is provided for update")
        void shouldReturn400WhenInvalidIdFormatProvidedForUpdate() throws Exception {
            mockMvc.perform(put("/api/v1/attributes/invalid")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest());

            verify(attributeService, never()).updateAttribute(anyLong(), any(UpdateAttributeRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when request body is malformed JSON for update")
        void shouldReturn400WhenRequestBodyIsMalformedJsonForUpdate() throws Exception {
            String malformedJson = "{ \"name\": \"Updated Color\", \"description\": }";

            mockMvc.perform(put("/api/v1/attributes/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(malformedJson))
                    .andExpect(status().isBadRequest());

            verify(attributeService, never()).updateAttribute(anyLong(), any(UpdateAttributeRequest.class));
        }

        @Test
        @DisplayName("Should handle service exception during update")
        void shouldHandleServiceExceptionDuringUpdate() throws Exception {
            doThrow(new RuntimeException("Database error"))
                    .when(attributeService).updateAttribute(eq(1L), any(UpdateAttributeRequest.class));

            mockMvc.perform(put("/api/v1/attributes/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isInternalServerError());

            verify(attributeService, times(1)).updateAttribute(eq(1L), any(UpdateAttributeRequest.class));
        }

        @Test
        @DisplayName("Should accept valid update request with description as null")
        void shouldAcceptValidUpdateRequestWithDescriptionAsNull() throws Exception {
            UpdateAttributeRequest requestWithNullDescription = new UpdateAttributeRequest();
            requestWithNullDescription.setName("Updated Material");
            requestWithNullDescription.setDescription(null);

            doNothing().when(attributeService).updateAttribute(eq(1L), any(UpdateAttributeRequest.class));

            mockMvc.perform(put("/api/v1/attributes/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestWithNullDescription)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("Success"))
                    .andExpect(jsonPath("$.data").isEmpty());

            verify(attributeService, times(1)).updateAttribute(eq(1L), any(UpdateAttributeRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when negative ID is provided for update")
        void shouldReturn400WhenNegativeIdProvidedForUpdate() throws Exception {
            mockMvc.perform(put("/api/v1/attributes/-1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest());

            verify(attributeService, never()).updateAttribute(anyLong(), any(UpdateAttributeRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when zero ID is provided for update")
        void shouldReturn400WhenZeroIdProvidedForUpdate() throws Exception {
            mockMvc.perform(put("/api/v1/attributes/0")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest());

            verify(attributeService, never()).updateAttribute(anyLong(), any(UpdateAttributeRequest.class));
        }
    }
}