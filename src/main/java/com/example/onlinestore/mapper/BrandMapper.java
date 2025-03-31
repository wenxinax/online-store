package com.example.onlinestore.mapper;

import com.example.onlinestore.dto.BrandListQueryOptions;
import com.example.onlinestore.entity.BrandEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BrandMapper {

    /**
     * 插入品牌实体数据
     *
     * @param brandEntity 需要插入的品牌实体对象，包含品牌属性信息
     * @return 受影响的数据行数，通常1表示成功，0表示失败
     */
    int insert(BrandEntity brandEntity);

    /**
     * 根据品牌ID查询品牌实体
     *
     * @param id 品牌唯一标识符，对应数据库主键
     * @return 匹配的品牌实体对象，未找到时返回null
     */
    BrandEntity findById(Long id);

    /**
     * 根据品牌名称查询品牌实体
     *
     * @param name 需要精确匹配的品牌名称
     * @return 匹配的品牌实体对象，未找到时返回null
     */
    BrandEntity findByName(String name);

    /**
     * 获取符合查询条件的所有品牌列表
     *
     * @param options 品牌查询选项对象，包含分页参数、排序规则、过滤条件等
     * @return 品牌实体集合，当无符合条件数据时返回空集合
     */
    List<BrandEntity> findAllBrands(@Param("options") BrandListQueryOptions options);

    /**
     * 根据品牌ID删除品牌记录
     *
     * @param id 需要删除的品牌唯一标识符
     * @return 受影响的数据行数，通常1表示成功，0表示未找到对应记录
     */
    int deleteById(Long id);

}
