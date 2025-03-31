package com.example.onlinestore.bean;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Brand implements Serializable {
    @Serial
    private static final long serialVersionUID = -8605879756585481300L;

    /**
     * 实体唯一标识符
     * - 主键字段
     */
    private Long id;

    /**
     * 名称字段
     * - 最大长度限制：64字符
     * - 非空约束
     */
    @NotNull
    @Size( max = 64)
    private String name;

    /**
     * 描述信息字段
     * - 最大长度限制：1024字符
     * - 非空约束
     */
    @NotNull
    @Size(max = 1024)
    private String description;

    /**
     * 品牌/组织LOGO字段
     * - 存储LOGO路径或标识符
     * - 最大长度限制：256字符
     * - 非空约束
     */
    @NotNull
    @Size(max = 256)
    private String logo;

    /**
     * 品牌故事/详情字段
     * - 最大长度限制：1024字符
     * - 非空约束
     */
    @NotNull
    @Size(max = 1024)
    private String story;

    /**
     * 排序权重字段
     * - 用于系统排序的数值型权重
     * - 非空约束
     */
    @NotNull
    private Integer sortScore;

    /**
     * 显示状态字段
     * - 控制前端展示状态（如：0-隐藏，1-显示）
     * - 非空约束
     */
    @NotNull
    private Integer showStatus;


}
