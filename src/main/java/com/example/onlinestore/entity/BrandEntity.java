package com.example.onlinestore.entity;

import com.example.onlinestore.bean.Brand;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class BrandEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = -7991442075493692536L;

    // @Id, 平台ID
    private Long id;

    // 品牌名称
    private String name;

    // 品牌描述
    private String description;

    // 品牌logo
    private String logo;

    // 品牌故事
    private String story;

    // 排行分, 越大越在前
    private Integer sortScore;

    // 显示状态, 0: 不显示, 1: 显示
    private Integer showStatus;

    // 创建时间
    private LocalDateTime createdAt;

    // 更新时间
    private LocalDateTime updatedAt;

    // 转换为 Brand 对象
    public Brand toBrand() {
        Brand brand = new Brand();
        brand.setId(id);
        brand.setName(name);
        brand.setDescription(description);
        brand.setLogo(logo);
        brand.setStory(story);
        brand.setSortScore(sortScore);
        brand.setShowStatus(showStatus);
        return brand;
    }
}
