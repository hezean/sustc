package io.sustc.service;

import java.util.List;

public interface RecommenderService {

    /**
     * Recommends videos for anonymous users, based on the popularity.
     * Evaluate the video's popularity from the following aspects:
     * <ol>
     *     <li>The number of users who have watched this video</li>
     *     <li>The number of users who have liked or donated coin to this video</li>
     *
     * </ol>
     *
     * @param pageSize the page size, if there are less than {@code pageSize} videos, return all of them
     * @param pageNum  the page number, starts from 1
     * @return a list of video {@code bv}s, sorted by the recommendation score
     */
    List<String> generalRecommendations(int pageSize, int pageNum);

    /**
     * Recommends videos for a user, based on their interests.
     *
     *
     * @param mid
     * @param pageSize
     * @param pageNum
     * @return
     */
    List<String> recommendVideosForUser(int mid, int pageSize, int pageNum);

    /**
     * Recommends friends for a user, based on their common followings.
     *
     * @param mid
     * @param pageSize
     * @param pageNum
     * @return
     */
    List<Long> recommendFriends(int mid, int pageSize, int pageNum);
}
