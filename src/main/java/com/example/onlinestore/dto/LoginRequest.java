package com.example.onlinestore.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@EqualsAndHashCode
@ToString
public class LoginRequest {
    @NotNull
    @Size(min = 2, max = 16)
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{2,16}$")
    private String username;

    @NotNull
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,16}$")
    @Size(min = 8, max = 16)
    private String password;

}