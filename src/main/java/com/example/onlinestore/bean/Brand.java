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
@Valid
@ToString
@EqualsAndHashCode
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
    @NotNull
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

}
