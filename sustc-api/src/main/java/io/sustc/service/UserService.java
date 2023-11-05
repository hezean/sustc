package io.sustc.service;

import io.sustc.dto.AuthInfo;
import io.sustc.dto.RegisterUserReq;
import io.sustc.dto.UserInfoResp;
import org.springframework.lang.Nullable;

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
     * Deletes a user.
     * If the current user is a regular user, only the current user can be deleted.
     * If the current user is a superuser, and {@code mid} is not null, the user with {@code mid} will be deleted.
     * If the current user is a superuser, and {@code mid} is null, the current user will be deleted.
     *
     * @param auth indicates the current user
     * @param mid  the user to be deleted, or null to delete the current user
     */
    void deleteAccount(AuthInfo auth, @Nullable Long mid);

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
    UserInfoResp getUserInfo(int mid);
}
