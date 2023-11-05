package io.sustc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthInfo {

    /**
     * The user's mid.
     */
    private final long mid;

    /**
     * The password used when login by mid.
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
