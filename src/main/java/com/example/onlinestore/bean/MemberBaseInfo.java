package com.example.onlinestore.bean;

import com.example.onlinestore.enums.GenderType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
public class MemberBaseInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 7202771544778274481L;
    private String name;
    private String nickName;
    private String password;
    private String phone;
    private GenderType gender;
    private int age;



    @Override
    public int hashCode() {
        int result = Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(nickName);
        result = 31 * result + Objects.hashCode(password);
        result = 31 * result + Objects.hashCode(phone);
        result = 31 * result + Objects.hashCode(gender);
        result = 31 * result + age;
        return result;
    }

    @Override
    public String toString() {
        return "MemberBaseInfo{" +
                "name='" + name + '\'' +
                ", nickName='" + nickName + '\'' +
                ", phone='" + phone + '\'' +
                ", gender=" + gender +
                ", age=" + age +
                '}';
    }
}
