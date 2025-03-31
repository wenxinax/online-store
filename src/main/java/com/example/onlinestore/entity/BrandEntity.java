package com.example.onlinestore.entity;

import com.example.onlinestore.bean.Brand;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        BrandEntity that = (BrandEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(logo, that.logo) && Objects.equals(story, that.story) && Objects.equals(sortScore, that.sortScore) && Objects.equals(showStatus, that.showStatus) && Objects.equals(createdAt, that.createdAt) && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(description);
        result = 31 * result + Objects.hashCode(logo);
        result = 31 * result + Objects.hashCode(story);
        result = 31 * result + Objects.hashCode(sortScore);
        result = 31 * result + Objects.hashCode(showStatus);
        result = 31 * result + Objects.hashCode(createdAt);
        result = 31 * result + Objects.hashCode(updatedAt);
        return result;
    }

    @Override
    public String toString() {
        return "BrandEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", logo='" + logo + '\'' +
                ", story='" + story + '\'' +
                ", sortScore=" + sortScore +
                ", showStatus=" + showStatus +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
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
