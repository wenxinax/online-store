package com.example.onlinestore.dto;

import com.example.onlinestore.constants.Constants;
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
    @Pattern(regexp = Constants.MEMBER_NAME_PATTERN)
    private String username;

    @NotNull
    @Pattern(regexp = Constants.MEMBER_PASSWORD_PATTERN)
    @Size(min = 8, max = 16)
    private String password;

}