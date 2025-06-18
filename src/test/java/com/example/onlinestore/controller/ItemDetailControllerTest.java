package com.example.onlinestore.controller;

import com.example.onlinestore.bean.Item;
import com.example.onlinestore.bean.ItemDetail;
import com.example.onlinestore.bean.Member;
import com.example.onlinestore.dto.ItemDetailResponse;
import com.example.onlinestore.dto.Response;
import com.example.onlinestore.dto.converter.ItemDetailConverter;
import com.example.onlinestore.service.ItemAccessLogService;
import com.example.onlinestore.service.ItemDetailService;
import com.example.onlinestore.service.MemberService;
import com.example.onlinestore.utils.WebUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ItemDetailController.class)
@DisplayName("ItemDetailController Tests")
@TestPropertySource(properties = {"async-record-access-log=true"})
class ItemDetailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemDetailService itemDetailService;

    @MockBean
    private ItemDetailConverter itemDetailConverter;

    @MockBean
    private ItemAccessLogService itemAccessLogService;

    @MockBean
    private MemberService memberService;

    @Autowired
    private ObjectMapper objectMapper;

    private Item sampleItem;
    private ItemDetail sampleItemDetail;
    private ItemDetailResponse sampleItemDetailResponse;
    private Member sampleMember;
    private HttpSession mockSession;

    @BeforeEach
    void setUp() {
        // Setup sample Item
        sampleItem = new Item();
        sampleItem.setId(1L);
        sampleItem.setName("Test Item");
        sampleItem.setDescription("Test Description");
        sampleItem.setPrice(new BigDecimal("99.99"));
        sampleItem.setCategory("Electronics");
        sampleItem.setStock(10);

        // Setup sample ItemDetail
        sampleItemDetail = new ItemDetail();
        sampleItemDetail.setId(1L);
        sampleItemDetail.setItem(sampleItem);
        sampleItemDetail.setSpecifications("Test specifications");
        sampleItemDetail.setBrand("Test Brand");
        sampleItemDetail.setRating(4.5);
        sampleItemDetail.setReviewCount(100);
        sampleItemDetail.setCreatedAt(LocalDateTime.now());
        sampleItemDetail.setUpdatedAt(LocalDateTime.now());

        // Setup sample ItemDetailResponse
        sampleItemDetailResponse = new ItemDetailResponse();
        sampleItemDetailResponse.setId(1L);
        sampleItemDetailResponse.setName("Test Item");
        sampleItemDetailResponse.setDescription("Test Description");
        sampleItemDetailResponse.setPrice(new BigDecimal("99.99"));
        sampleItemDetailResponse.setCategory("Electronics");
        sampleItemDetailResponse.setStock(10);
        sampleItemDetailResponse.setBrand("Test Brand");
        sampleItemDetailResponse.setSpecifications("Test specifications");
        sampleItemDetailResponse.setRating(4.5);
        sampleItemDetailResponse.setReviewCount(100);

        // Setup sample Member
        sampleMember = new Member();
        sampleMember.setId(1L);
        // sampleMember.getBaseInfo().setName("Test User");

        // Setup mock session
        mockSession = mock(HttpSession.class);
        when(mockSession.getId()).thenReturn("test-session-id");
    }

    @Test
    @DisplayName("Should return item details successfully with logged-in member and async logging")
    void testGetItemDetail_Success_LoggedInMember_AsyncLogging() throws Exception {
        Long itemId = 1L;
        given(itemDetailService.getItemDetail(itemId)).willReturn(sampleItemDetail);
        given(itemDetailConverter.convert(sampleItemDetail)).willReturn(sampleItemDetailResponse);
        given(memberService.getLoginMember()).willReturn(sampleMember);

        try (MockedStatic<WebUtils> webUtilsMock = mockStatic(WebUtils.class)) {
            webUtilsMock.when(() -> WebUtils.getClientIp(any(HttpServletRequest.class)))
                        .thenReturn("192.168.1.1");

            mockMvc.perform(get("/api/v1/items/{itemId}/detail", itemId)
                    .header("User-Agent", "Test-Agent")
                    .header("Referer", "http://test.com")
                    .sessionAttr("JSESSIONID", "test-session-id"))
                   .andExpect(status().isOk())
                   .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                   .andExpect(jsonPath("$.success").value(true))
                   .andExpect(jsonPath("$.data.id").value(1L))
                   .andExpect(jsonPath("$.data.name").value("Test Item"))
                   .andExpect(jsonPath("$.data.description").value("Test Description"))
                   .andExpect(jsonPath("$.data.price").value(99.99))
                   .andExpect(jsonPath("$.data.category").value("Electronics"))
                   .andExpect(jsonPath("$.data.stock").value(10))
                   .andExpect(jsonPath("$.data.brand").value("Test Brand"))
                   .andExpect(jsonPath("$.data.specifications").value("Test specifications"))
                   .andExpect(jsonPath("$.data.rating").value(4.5))
                   .andExpect(jsonPath("$.data.reviewCount").value(100));

            verify(itemDetailService).getItemDetail(itemId);
            verify(itemDetailConverter).convert(sampleItemDetail);
            verify(memberService).getLoginMember();
            verify(itemAccessLogService).asyncRecordAccessLog(
                    eq(itemId),
                    eq("Test Item"),
                    eq("1"),
                    anyString(),
                    eq("192.168.1.1"),
                    eq("Test-Agent"),
                    eq("http://test.com"),
                    anyString()
            );
        }
    }

    @Test
    @DisplayName("Should return item details successfully with anonymous user and sync logging")
    @TestPropertySource(properties = {"async-record-access-log=false"})
    void testGetItemDetail_Success_AnonymousUser_SyncLogging() throws Exception {
        Long itemId = 1L;
        given(itemDetailService.getItemDetail(itemId)).willReturn(sampleItemDetail);
        given(itemDetailConverter.convert(sampleItemDetail)).willReturn(sampleItemDetailResponse);
        given(memberService.getLoginMember()).willReturn(null);

        try (MockedStatic<WebUtils> webUtilsMock = mockStatic(WebUtils.class)) {
            webUtilsMock.when(() -> WebUtils.getClientIp(any(HttpServletRequest.class)))
                        .thenReturn("10.0.0.1");

            mockMvc.perform(get("/api/v1/items/{itemId}/detail", itemId)
                    .header("User-Agent", "Mozilla/5.0")
                    .header("Referer", "http://example.com"))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.success").value(true))
                   .andExpect(jsonPath("$.data.id").value(1L));

            verify(itemAccessLogService).recordAccess(
                    eq(itemId),
                    eq("Test Item"),
                    eq(""),
                    eq(""),
                    eq("10.0.0.1"),
                    eq("Mozilla/5.0"),
                    eq("http://example.com"),
                    anyString()
            );
        }
    }

    @Test
    @DisplayName("Should handle item not found scenario")
    void testGetItemDetail_ItemNotFound() throws Exception {
        Long nonExistentItemId = 999L;
        given(itemDetailService.getItemDetail(nonExistentItemId))
            .willThrow(new RuntimeException("Item not found"));

        mockMvc.perform(get("/api/v1/items/{itemId}/detail", nonExistentItemId))
               .andExpect(status().isInternalServerError());

        verify(itemDetailService).getItemDetail(nonExistentItemId);
        verify(itemDetailConverter, never()).convert(any());
        verify(itemAccessLogService, never())
            .asyncRecordAccessLog(anyLong(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should handle invalid item ID format")
    void testGetItemDetail_InvalidItemIdFormat() throws Exception {
        mockMvc.perform(get("/api/v1/items/{itemId}/detail", "invalid-id"))
               .andExpect(status().isBadRequest());

        verify(itemDetailService, never()).getItemDetail(anyLong());
    }

    @Test
    @DisplayName("Should handle null item ID")
    void testGetItemDetail_NullItemId() throws Exception {
        mockMvc.perform(get("/api/v1/items/null/detail"))
               .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle negative item ID")
    void testGetItemDetail_NegativeItemId() throws Exception {
        Long negativeItemId = -1L;
        given(itemDetailService.getItemDetail(negativeItemId))
            .willThrow(new IllegalArgumentException("Item ID must be positive"));

        mockMvc.perform(get("/api/v1/items/{itemId}/detail", negativeItemId))
               .andExpect(status().isInternalServerError());

        verify(itemDetailService).getItemDetail(negativeItemId);
    }

    @Test
    @DisplayName("Should handle zero item ID")
    void testGetItemDetail_ZeroItemId() throws Exception {
        Long zeroItemId = 0L;
        given(itemDetailService.getItemDetail(zeroItemId))
            .willThrow(new IllegalArgumentException("Item ID must be positive"));

        mockMvc.perform(get("/api/v1/items/{itemId}/detail", zeroItemId))
               .andExpect(status().isInternalServerError());

        verify(itemDetailService).getItemDetail(zeroItemId);
    }

    @Test
    @DisplayName("Should handle ItemDetailService exception")
    void testGetItemDetail_ServiceException() throws Exception {
        Long itemId = 1L;
        given(itemDetailService.getItemDetail(itemId))
            .willThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(get("/api/v1/items/{itemId}/detail", itemId))
               .andExpect(status().isInternalServerError());

        verify(itemDetailService).getItemDetail(itemId);
        verify(itemDetailConverter, never()).convert(any());
    }

    @Test
    @DisplayName("Should handle ItemDetailConverter exception")
    void testGetItemDetail_ConverterException() throws Exception {
        Long itemId = 1L;
        given(itemDetailService.getItemDetail(itemId)).willReturn(sampleItemDetail);
        given(itemDetailConverter.convert(sampleItemDetail))
            .willThrow(new RuntimeException("Conversion failed"));

        mockMvc.perform(get("/api/v1/items/{itemId}/detail", itemId))
               .andExpect(status().isInternalServerError());

        verify(itemDetailService).getItemDetail(itemId);
        verify(itemDetailConverter).convert(sampleItemDetail);
    }

    @Test
    @DisplayName("Should handle MemberService exception gracefully")
    void testGetItemDetail_MemberServiceException() throws Exception {
        Long itemId = 1L;
        given(itemDetailService.getItemDetail(itemId)).willReturn(sampleItemDetail);
        given(itemDetailConverter.convert(sampleItemDetail)).willReturn(sampleItemDetailResponse);
        given(memberService.getLoginMember())
            .willThrow(new RuntimeException("Member service unavailable"));

        try (MockedStatic<WebUtils> webUtilsMock = mockStatic(WebUtils.class)) {
            webUtilsMock.when(() -> WebUtils.getClientIp(any(HttpServletRequest.class)))
                        .thenReturn("192.168.1.1");

            mockMvc.perform(get("/api/v1/items/{itemId}/detail", itemId)
                    .header("User-Agent", "Test-Agent"))
                   .andExpect(status().isInternalServerError());
        }
    }

    @Test
    @DisplayName("Should handle access logging service exception gracefully")
    void testGetItemDetail_AccessLogServiceException() throws Exception {
        Long itemId = 1L;
        given(itemDetailService.getItemDetail(itemId)).willReturn(sampleItemDetail);
        given(itemDetailConverter.convert(sampleItemDetail)).willReturn(sampleItemDetailResponse);
        given(memberService.getLoginMember()).willReturn(sampleMember);

        doThrow(new RuntimeException("Logging service failed"))
            .when(itemAccessLogService)
            .asyncRecordAccessLog(anyLong(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString());

        try (MockedStatic<WebUtils> webUtilsMock = mockStatic(WebUtils.class)) {
            webUtilsMock.when(() -> WebUtils.getClientIp(any(HttpServletRequest.class)))
                        .thenReturn("192.168.1.1");

            mockMvc.perform(get("/api/v1/items/{itemId}/detail", itemId)
                    .header("User-Agent", "Test-Agent"))
                   .andExpect(status().isInternalServerError());
        }
    }

    @Test
    @DisplayName("Should handle missing request headers gracefully")
    void testGetItemDetail_MissingHeaders() throws Exception {
        Long itemId = 1L;
        given(itemDetailService.getItemDetail(itemId)).willReturn(sampleItemDetail);
        given(itemDetailConverter.convert(sampleItemDetail)).willReturn(sampleItemDetailResponse);
        given(memberService.getLoginMember()).willReturn(null);

        try (MockedStatic<WebUtils> webUtilsMock = mockStatic(WebUtils.class)) {
            webUtilsMock.when(() -> WebUtils.getClientIp(any(HttpServletRequest.class)))
                        .thenReturn("unknown");

            mockMvc.perform(get("/api/v1/items/{itemId}/detail", itemId))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.success").value(true));

            verify(itemAccessLogService).asyncRecordAccessLog(
                    eq(itemId),
                    eq("Test Item"),
                    eq(""),
                    eq(""),
                    eq("unknown"),
                    isNull(),
                    isNull(),
                    anyString()
            );
        }
    }

    @Test
    @DisplayName("Should handle session without ID")
    void testGetItemDetail_NoSessionId() throws Exception {
        Long itemId = 1L;
        given(itemDetailService.getItemDetail(itemId)).willReturn(sampleItemDetail);
        given(itemDetailConverter.convert(sampleItemDetail)).willReturn(sampleItemDetailResponse);
        given(memberService.getLoginMember()).willReturn(null);

        try (MockedStatic<WebUtils> webUtilsMock = mockStatic(WebUtils.class)) {
            webUtilsMock.when(() -> WebUtils.getClientIp(any(HttpServletRequest.class)))
                        .thenReturn("192.168.1.1");

            mockMvc.perform(get("/api/v1/items/{itemId}/detail", itemId))
                   .andExpect(status().isOk());

            verify(itemAccessLogService).asyncRecordAccessLog(
                    eq(itemId),
                    eq("Test Item"),
                    eq(""),
                    eq(""),
                    eq("192.168.1.1"),
                    isNull(),
                    isNull(),
                    eq("")
            );
        }
    }

    @Test
    @DisplayName("Should handle WebUtils.getClientIp exception")
    void testGetItemDetail_WebUtilsException() throws Exception {
        Long itemId = 1L;
        given(itemDetailService.getItemDetail(itemId)).willReturn(sampleItemDetail);
        given(itemDetailConverter.convert(sampleItemDetail)).willReturn(sampleItemDetailResponse);
        given(memberService.getLoginMember()).willReturn(null);

        try (MockedStatic<WebUtils> webUtilsMock = mockStatic(WebUtils.class)) {
            webUtilsMock.when(() -> WebUtils.getClientIp(any(HttpServletRequest.class)))
                        .willThrow(new RuntimeException("Failed to get client IP"));

            mockMvc.perform(get("/api/v1/items/{itemId}/detail", itemId))
                   .andExpect(status().isInternalServerError());
        }
    }

    @Test
    @DisplayName("Should handle very large item ID")
    void testGetItemDetail_LargeItemId() throws Exception {
        Long largeItemId = Long.MAX_VALUE;
        given(itemDetailService.getItemDetail(largeItemId)).willReturn(sampleItemDetail);
        given(itemDetailConverter.convert(sampleItemDetail)).willReturn(sampleItemDetailResponse);
        given(memberService.getLoginMember()).willReturn(null);

        try (MockedStatic<WebUtils> webUtilsMock = mockStatic(WebUtils.class)) {
            webUtilsMock.when(() -> WebUtils.getClientIp(any(HttpServletRequest.class)))
                        .thenReturn("192.168.1.1");

            mockMvc.perform(get("/api/v1/items/{itemId}/detail", largeItemId))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.success").value(true));

            verify(itemDetailService).getItemDetail(largeItemId);
        }
    }
}