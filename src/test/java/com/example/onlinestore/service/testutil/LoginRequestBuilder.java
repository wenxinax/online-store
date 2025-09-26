package com.example.onlinestore.service.testutil;

import com.example.onlinestore.dto.LoginRequest;

/**
 * 登录请求测试数据构建器
 */
public class LoginRequestBuilder {
    
    private String username = "testuser";
    private String password = "testpass";

    public static LoginRequestBuilder builder() {
        return new LoginRequestBuilder();
    }

    public LoginRequestBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    public LoginRequestBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public LoginRequest build() {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }
}