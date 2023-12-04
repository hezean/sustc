package io.sustc.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * The user record used for data import
 * @implNote You may implement your own {@link java.lang.Object#toString()} since the default one in {@link lombok.Data} prints all array values.
 */
@Data
public class UserRecord implements Serializable {

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
     * The user's current number of coins
     */
    private int coin;

    /**
     * The user's personal sign, can be null or empty
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
     * The user's qq, may be null or empty
     */
    private String qq;

    /**
     * The user's wechat, may be null or empty
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
