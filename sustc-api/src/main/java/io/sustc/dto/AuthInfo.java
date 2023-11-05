package io.sustc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthInfo {

    /**
     * The user's phone number. If it's provided, the {@code email} field will be null.
     */
    private final String phone;

    /**
     * The user's email address. If it's provided, the {@code phone} field will be null.
     */
    private final String email;

    /**
     * The password used when login by phone/email.
     */
    private final String password;

    /**
     * OIDC login by QQ, does not require a password.
     */
    private final String qq;

    /**
     * OIDC login by WeChat, does not require a password.
     */
    private final String wechat;
}
