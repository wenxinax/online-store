package com.example.onlinestore.bean;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
public class Member implements Serializable {
    @Serial
    private static final long serialVersionUID = 1099483498189107702L;

    private Long id;
    // 会员基本信息
    private MemberBaseInfo baseInfo;

}
