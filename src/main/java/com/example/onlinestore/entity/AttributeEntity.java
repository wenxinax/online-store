package com.example.onlinestore.entity;

import com.example.onlinestore.enums.AttributeValueType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Setter
@Getter
public class AttributeEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1071950200762803926L;

    private Long id;
    private String name;
    private AttributeValueType valueType;
    private Integer enable;
    private Long categoryId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
