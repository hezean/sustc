package io.sustc.service;

import java.util.List;

public interface RecommenderService {

    /**
     * Recommends videos for anonymous users, based on the popularity.
     * Evaluate the video's popularity from the following aspects:
     * <ol>
     *     <li>The number of users who have watched this video</li>
     *     <li>The rate of watched users who also liked or donated coin to this video</li>
     *     <li>The average number of danmus sent by one watched user</li>
     * </ol>
     *
     * @param pageSize the page size, if there are less than {@code pageSize} videos, return all of them
     * @param pageNum  the page number, starts from 1
     * @return a list of video {@code bv}s, sorted by the recommendation evaluation
     */
    List<String> generalRecommendations(int pageSize, int pageNum);

    /**
     * Recommends videos for a user, based on their interests.
     * The user's interests are defined as their friends' (the users they follow that also follow back)
     * watched videos, filter out the videos that the user has already watched.
     * Sort the videos by:
     * <ol>
     *     <li>The number of users who have watched the video</li>
     *     <li>The video owner's level</li>
     *     <li>The video's post time (newer videos are preferred)</li>
     * </ol>
     *
     * @param mid      the user to be recommended
     * @param pageSize the page size, if there are less than {@code pageSize} videos, return all of them
     * @param pageNum  the page number, starts from 1
     * @return a list of video {@code bv}s
     */
    List<String> recommendVideosForUser(long mid, int pageSize, int pageNum);

    /**
     * Recommends a list of top 5 similar videos for a video.
     * The similarity is defined as the number of users who have watched both videos.
     *
     * @param bv the current video
     * @return a list of video {@code bv}s
     */
    List<String> recommendNextVideo(String bv);

    /**
     * Recommends friends for a user, based on their common followings.
     * Find all users that are not currently followed by the user, and have at least one common following with the user.
     * Sort the users by the number of common followings, if two users have the same number of common followings,
     * sort them by their {@code level}.
     *
     * @param mid      the user to be recommended
     * @param pageSize the page size, if there are less than {@code pageSize} users, return all of them
     * @param pageNum  the page number, starts from 1
     * @return a list of {@code mid}s of the recommended users
     */
    List<Long> recommendFriends(long mid, int pageSize, int pageNum);
}
