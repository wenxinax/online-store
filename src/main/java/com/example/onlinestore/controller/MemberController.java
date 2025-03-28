package com.example.onlinestore.controller;

import com.example.onlinestore.bean.Member;
import com.example.onlinestore.dto.MemberRegistryRequest;
import com.example.onlinestore.dto.MemberResponse;
import com.example.onlinestore.dto.Response;
import com.example.onlinestore.service.MemberService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/v1/members")
public class MemberController {
    private static final Logger logger = LoggerFactory.getLogger(MemberController.class);

    @Autowired
    private MemberService memberService;


    @PostMapping("/registry")
    public Response<MemberResponse> registry(@Valid @RequestBody MemberRegistryRequest request) {
        logger.info("registry member: {}", request);
        Member member =  memberService.registry(request);
        return Response.success(MemberResponse.of(member));
    }

} 