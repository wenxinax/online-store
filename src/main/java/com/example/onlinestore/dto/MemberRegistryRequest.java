package com.example.onlinestore.dto;

import com.example.onlinestore.enums.GenderType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class MemberRegistryRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -7490516187847553182L;

    @NotBlank
    @Size(min = 1, max = 24)
    private String name;

    @NotBlank
    @Size(min = 6, max = 12)
    private String password;

    @Size(max = 24)
    private String nickName;

    private GenderType gender;

    @Min(value = 18)
    @Max(value = 150)
    private int age;

    @NotBlank
    private String phone;

}
