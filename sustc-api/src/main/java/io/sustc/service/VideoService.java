package io.sustc.service;

import io.sustc.dto.AuthInfo;

import java.util.Set;

public interface VideoService {

    /**
     * Calculates the average view rate of a video.
     * The view rate is defined as the user's view time divided by the video's duration.
     *
     * @param bv the video's {@code bv}
     * @return the average view rate
     */
    double getAverageViewRate(String bv);

    /**
     * Gets the hotspot of a video.
     * With splitting the video into 10-second chunks, hotspots are defined as chunks with the most danmus.
     *
     * @param bv the video's {@code bv}
     * @return the index of hotspot chunks (start from 0)
     */
    Set<Integer> getHotspot(String bv);

    // FIXME: update should invalidate review info
    // return if the video needs to be re-reviewed (previously reviewed)
    boolean updateVideoInfo(/* TODO */);

    /**
     * Reviews a video by a super admin.
     *
     * @param auth the current user's authentication information
     * @param bv   the video's {@code bv}
     * @param pass whether to pass the review
     */
    void reviewVideo(AuthInfo auth, String bv, boolean pass);

    /**
     * Donates one coin to the video.
     *
     * @param auth the current user's authentication information
     * @param bv   the video's {@code bv}
     */
    void coinVideo(AuthInfo auth, String bv);
}
