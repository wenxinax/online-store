package com.example.onlinestore.mapper;

import com.example.onlinestore.dto.BrandListQueryOptions;
import com.example.onlinestore.entity.BrandEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BrandMapper {

    // 插入品牌
    int insert(BrandEntity brandEntity);

    // 根据id查询品牌
    BrandEntity findById(Long id);

    // 根据name查询品牌
    BrandEntity findByName(String name);

    // 查询平台列表
    List<BrandEntity> findAllBrands(@Param("options") BrandListQueryOptions options);

    // 删除品牌
    int deleteById(Long id);
}
