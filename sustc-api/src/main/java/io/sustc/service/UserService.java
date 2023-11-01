package io.sustc.service;

import io.sustc.dto.AuthInfo;
import io.sustc.dto.RegisterUserRequest;

import java.util.List;

public interface UserService {

    /**
     * Registers a new user.
     *
     * @param req information of the new user
     * @return the new user's {@code mid}
     */
    int register(RegisterUserRequest req);

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
     * Gets the number of coins a user has.
     *
     * @param mid the user to be queried
     * @return the number of coins
     */
    int getCoins(int mid);
}
