package com.example.onlinestore.service;

import com.example.onlinestore.bean.Member;
import com.example.onlinestore.dto.LoginRequest;
import com.example.onlinestore.dto.LoginResponse;
import com.example.onlinestore.dto.MemberRegistryRequest;


public interface MemberService {
    // 登录
    LoginResponse login(LoginRequest request);

    // 注册
    Member registry(MemberRegistryRequest request);

    // 根据id查询会员信息
    Member getMemberById(Long id);
}