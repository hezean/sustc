package io.sustc.dto;

import lombok.Data;

@Data
public class UserRecord {

    /**
     * The user's ID
     */
    private long mid;

    /**
     * The user's name
     */
    private String name;

    /**
     * The user's sex
     */
    private String sex;

    /**
     * The user's birthday
     */
    private String birthday;

    /**
     * The user's level
     */
    private short level;

    /**
     * The user's personal sign, can be empty
     */
    private String sign;

    /**
     * The user's identity
     */
    private Identity identity;

    /**
     * The user's password
     */
    private String password;

    /**
     * The user's qq
     */
    private String qq;

    /**
     * The user's wechat
     */
    private String wechat;

    /**
     * The users' {@code mid}s who followed this user
     */
    private long[] following;

    public enum Identity {
        USER,
        SUPERUSER,
    }
}
