package io.sustc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthInfo {

    /**
     * The user's mid.
     */
    private long mid;

    /**
     * The password used when login by mid.
     */
    private String password;

    /**
     * OIDC login by QQ, does not require a password.
     */
    private String qq;

    /**
     * OIDC login by WeChat, does not require a password.
     */
    private String wechat;
}
