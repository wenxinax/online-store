package com.example.onlinestore.bean;

import com.example.onlinestore.enums.GenderType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class MemberBaseInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 7202771544778274481L;
    /**
     * 用户真实姓名
     */
    @NotNull
    @Size(min = 2, max = 16)
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{2,16}$")
    private String name;
    /**
     * 用户昵称（非必填）
     */
    @Size(max = 16)
    private String nickName;
    /**
     * 用户登录密码（明文或加密存储形式）
     */
    @NotNull
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,16}$")
    @Size(min = 8, max = 16)
    private String password;
    /**
     * 用户绑定的手机号码
     */
    @NotNull
    @Pattern(regexp = "^1[3-9]\\d{9}$")
    private String phone;
    /**
     * 用户性别（枚举值，如 MALE/FEMALE/UNKNOWN）
     */
    @NotNull
    private GenderType gender;
    /**
     * 用户年龄（有效值应大于等于18）
     */
    @Min(18)
    private int age;

}
