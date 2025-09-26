package com.example.onlinestore.service.testutil;

import com.example.onlinestore.dto.UserPageRequest;

/**
 * 用户分页请求测试数据构建器
 */
public class UserPageRequestBuilder {
    
    private int pageNum = 1;
    private int pageSize = 10;

    public static UserPageRequestBuilder builder() {
        return new UserPageRequestBuilder();
    }

    public UserPageRequestBuilder withPageNum(int pageNum) {
        this.pageNum = pageNum;
        return this;
    }

    public UserPageRequestBuilder withPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public UserPageRequest build() {
        UserPageRequest request = new UserPageRequest();
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        return request;
    }
}