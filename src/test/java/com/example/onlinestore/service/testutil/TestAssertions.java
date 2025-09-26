package com.example.onlinestore.service.testutil;

import com.example.onlinestore.dto.LoginResponse;
import com.example.onlinestore.dto.PageResponse;
import com.example.onlinestore.dto.UserVO;
import com.example.onlinestore.model.User;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试断言工具类
 */
public class TestAssertions {

    /**
     * 验证登录响应的完整性
     */
    public static void assertLoginResponse(LoginResponse response) {
        assertNotNull(response, "登录响应不应为null");
        assertNotNull(response.getToken(), "Token不应为null");
        assertFalse(response.getToken().isEmpty(), "Token不应为空");
        assertNotNull(response.getExpireTime(), "过期时间不应为null");
    }

    /**
     * 验证分页响应的正确性
     */
    public static void assertPageResponse(PageResponse<UserVO> response, 
                                         int expectedPageNum, 
                                         int expectedPageSize,
                                         long expectedTotal) {
        assertNotNull(response, "分页响应不应为null");
        assertNotNull(response.getRecords(), "记录列表不应为null");
        assertEquals(expectedPageNum, response.getPageNum(), "页码不匹配");
        assertEquals(expectedPageSize, response.getPageSize(), "页大小不匹配");
        assertEquals(expectedTotal, response.getTotal(), "总数不匹配");
    }

    /**
     * 验证用户VO的字段映射
     */
    public static void assertUserVO(UserVO vo, User user) {
        assertNotNull(vo, "UserVO不应为null");
        assertEquals(user.getId(), vo.getId(), "ID不匹配");
        assertEquals(user.getUsername(), vo.getUsername(), "用户名不匹配");
        assertEquals(user.getCreatedAt(), vo.getCreatedAt(), "创建时间不匹配");
        assertEquals(user.getUpdatedAt(), vo.getUpdatedAt(), "更新时间不匹配");
    }

    /**
     * 验证用户对象的完整性
     */
    public static void assertUser(User user, String expectedUsername) {
        assertNotNull(user, "用户不应为null");
        assertEquals(expectedUsername, user.getUsername(), "用户名不匹配");
        assertNotNull(user.getToken(), "Token不应为null");
        assertNotNull(user.getTokenExpireTime(), "Token过期时间不应为null");
    }
}