package com.example.onlinestore.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class ItemAttributeRequest {

    @NotNull(message = "attributeId不能为空")
    @Min(value = 1, message = "attributeId的值只能为正整数")
    private Long attributeId;

    @NotNull(message = "attributeValueId不能为空")
    @Min(value = 1, message = "attributeValueId的值只能为正整数")
    private Long attributeValueId;

    @Size(max = 255, message = "输入值不能超过255个字符")
    private String value;
}
