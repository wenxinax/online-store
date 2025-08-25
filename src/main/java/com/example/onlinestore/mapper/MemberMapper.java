package com.example.onlinestore.mapper;

import com.example.onlinestore.entity.MemberEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberMapper {
    MemberEntity findByName(String name);
    MemberEntity findById(Long id);
    int insertMember(MemberEntity member);

}