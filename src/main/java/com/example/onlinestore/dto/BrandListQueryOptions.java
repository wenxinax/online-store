package com.example.onlinestore.dto;

import jakarta.validation.Valid;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.util.List;
import java.util.Objects;

/**
 * 品牌列表查询选项类，继承自分页请求基类PageRequest，用于封装品牌列表查询时的分页参数及附加查询条件。
 * 该类通过继承PageRequest继承分页相关功能（如页码、每页数量等），并可扩展添加品牌查询特有的过滤条件。
 */
@Getter
@Setter
@Valid
@EqualsAndHashCode(callSuper = true)
@ToString
public class BrandListQueryOptions extends PageRequest{
    @Serial
    private static final long serialVersionUID = 1406567832771578631L;

    /**
     * 显示状态标识
     * - 0: 隐藏状态
     * - 1: 显示状态
     * - null: 表示不进行状态过滤
     */
    private Integer showStatus;

    /**
     * 排序字段名称
     * - 用于指定结果集的排序规则
     */
    private String orderBy;

    /**
     * 品牌ID集合
     * - 用于存储需要过滤的品牌标识列表
     * - 空列表表示不进行品牌过滤
     * - 包含非空元素时表示需要匹配指定品牌
     */
    private List<Long> brandIds;

}
