package io.sustc.dto;

import lombok.Data;

@Data
public class RegisterUserReq {

    private String phone;

    // FIXME: multi-credential support?
    private String password;

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
