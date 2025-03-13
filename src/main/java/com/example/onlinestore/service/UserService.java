package com.example.onlinestore.service;

import com.example.onlinestore.bean.User;
import com.example.onlinestore.dto.LoginRequest;
import com.example.onlinestore.dto.LoginResponse;
import com.example.onlinestore.dto.PageResponse;
import com.example.onlinestore.dto.UserPageRequest;
import com.example.onlinestore.dto.UserVO;


public interface UserService {
    LoginResponse login(LoginRequest request);
    PageResponse<UserVO> listUsers(UserPageRequest request);
    User getUserByToken(String token);
} 