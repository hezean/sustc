package io.sustc.service;

import java.util.List;

public interface RecommenderService {

    /**
     * @param pageSize
     * @param pageNum
     * @return
     */
    List<String> generalRecommendations(int pageSize, int pageNum);

    /**
     * @param mid
     * @param pageSize
     * @param pageNum
     * @return
     */
    List<String> recommendVideosForUser(int mid, int pageSize, int pageNum);

    /**
     * Recommends friends for a user, based on their common interests.
     *
     * @param mid
     * @param pageSize
     * @param pageNum
     * @return
     */
    List<Long> recommendFriends(int mid, int pageSize, int pageNum);
}
