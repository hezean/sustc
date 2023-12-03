package io.sustc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResp {

    /**
     * The user's {@code mid}.
     */
    private long mid;

    /**
     * The number of user's coins that he/she currently owns.
     */
    private int coin;

    /**
     * The user's following {@code mid}s.
     */
    private long[] following;

    /**
     * The user's follower {@code mid}s.
     */
    private long[] follower;

    /**
     * The videos' {@code bv}s watched by this user.
     */
    private String[] watched;

    /**
     * The videos' {@code bv}s liked by this user.
     */
    private String[] liked;

    /**
     * The videos' {@code bv}s collected by this user.
     */
    private String[] collected;

    /**
     * The videos' {@code bv}s posted by this user.
     */
    private String[] posted;
}
