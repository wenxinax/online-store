package com.example.onlinestore.bean;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Valid
public class Brand implements Serializable {
    @Serial
    private static final long serialVersionUID = -8605879756585481300L;

    // id
    private Long id;

    // 品牌名称
    @NotNull
    @Size( max = 64)
    private String name;

    // 品牌描述
    @Size(max = 1024)
    private String description;

    // 品牌LOGO
    @NotNull
    @Size(max = 256)
    private String logo;

    // 品牌故事
    @NotNull
    @Size(max = 1024)
    private String story;

    // 排序
    @NotNull
    private Integer sortScore;

    // 显示状态 0:不显示， 1:显示
    private Integer showStatus;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Brand brand = (Brand) o;
        return Objects.equals(id, brand.id) && Objects.equals(name, brand.name) && Objects.equals(description, brand.description) && Objects.equals(logo, brand.logo) && Objects.equals(story, brand.story) && Objects.equals(sortScore, brand.sortScore) && Objects.equals(showStatus, brand.showStatus);
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
        return result;
    }

    @Override
    public String toString() {
        return "Brand{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", logo='" + logo + '\'' +
                ", story='" + story + '\'' +
                ", sortScore=" + sortScore +
                ", showStatus=" + showStatus +
                '}';
    }
}
