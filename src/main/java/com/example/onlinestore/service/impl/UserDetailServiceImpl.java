package com.example.onlinestore.service.impl;

import com.example.onlinestore.entity.MemberEntity;
import com.example.onlinestore.mapper.MemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private MemberMapper memberMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        MemberEntity memberEntity = memberMapper.findByName(username);
        if (memberEntity == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        return new User(memberEntity.getName(), memberEntity.getPassword(), new ArrayList<>());
    }
}
