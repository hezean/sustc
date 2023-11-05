package io.sustc.service;

import io.sustc.dto.AuthInfo;
import io.sustc.dto.RegisterUserReq;

import java.util.List;

public interface UserService {

    /**
     * Registers a new user.
     * Should at least provide either {@code phone} or {@code email} and a {@code password}.
     * {@code qq} and {@code wechat} are optional
     * <a href="https://openid.net/developers/how-connect-works/">OIDC</a> fields.
     *
     * @param req information of the new user
     * @return the new user's {@code mid}
     */
    int register(RegisterUserReq req);

    /**
     * Follows a user.
     *
     * @param auth        the authentication information of the follower
     * @param followeeMid the user who will be followed
     */
    void follow(AuthInfo auth, int followeeMid);

    /**
     * Lists all the users who follow a user.
     *
     * @param mid the user to be queried
     * @return a list of {@code mid}s of the followers
     */
    List<Integer> getFollowers(int mid);

    /**
     * Gets the required information (in DTO) of a user.
     *
     * @param mid the user to be queried
     * @return the number of coins
     */
    int getUserInfo(int mid);
}
