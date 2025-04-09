package com.example.onlinestore.mapper;

import com.example.onlinestore.dto.ItemListQueryRequest;
import com.example.onlinestore.entity.ItemEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ItemMapper {
    /**
     * 插入新的Item实体
     *
     * @param itemEntity 待插入的Item实体对象，包含所有需要持久化的字段信息
     * @return 受影响的数据行数，通常1表示成功，0表示失败
     */
    int insert(ItemEntity itemEntity);

    /**
     * 更新已存在的Item实体
     *
     * @param itemEntity 待更新的Item实体对象，必须包含有效的主键标识
     * @return 受影响的数据行数，通常1表示成功，0表示未找到对应记录
     */
    int update(ItemEntity itemEntity);

    /**
     * 根据主键ID删除Item记录
     *
     * @param id 要删除的Item记录主键ID
     * @return 受影响的数据行数，通常1表示成功，0表示未找到对应记录
     */
    int deleteById(Long id);

    /**
     * 根据主键ID查询Item实体
     *
     * @param id 要查询的Item记录主键ID
     * @return 查找到的Item实体对象，未找到时返回null
     */
    ItemEntity findById(Long id);

    /**
     * 根据查询条件参数获取符合条件的项目实体列表
     *
     * @param options 查询条件参数对象，包含分页、过滤条件等查询参数
     * @return 符合条件的ItemEntity列表，返回结果不为null但可能为空集合
     */
    List<ItemEntity> queryItemsByOptions(@Param("options") ItemListQueryRequest options);

}

