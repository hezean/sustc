package io.sustc.dto;

import lombok.Data;

@Data
public class RegisterUserReq {

    private String phone;

    private String email;

    private String password;

    private String qq;

    private String wechat;

    private String name;

    private Gender sex;

    private String birthday;

    private String sign;

    public enum Gender {
        MALE,
        FEMALE,
        UNKNOWN,
    }
}
