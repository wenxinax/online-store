package com.example.onlinestore.bean;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class SkuSpec implements Serializable  {
    @Serial
    private static final long serialVersionUID = -3686283498974131636L;

    /**
     * 存储键值对中的键
     * <p>
     * 用于标识数据项的唯一键值，通常作为数据检索的依据
     */
    private String key;

    /**
     * 存储键值对中的值
     * <p>
     * 表示与键相关联的具体数据内容，通过键可以快速访问对应的值
     */
    private String value;

}
