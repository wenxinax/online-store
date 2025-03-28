package com.example.onlinestore.exceptions;

import lombok.Getter;

@Getter
public enum ErrorCode {
    MEMBER_NOT_FOUND("ErrorCode.Member.NotFound", "会员:{0}不存在"),
    ITEM_NOT_FOUND("ErrorCode.Item.NotFound", "商品不存在"),
    ;
    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
    // 错误码
    private final String code;

    // 默认错误信息, 支持MessageFormat.format()方式的参数替换
    private final String defaultMessage;

    @Override
    public String toString() {
        return code + ": " + defaultMessage;
    }
}