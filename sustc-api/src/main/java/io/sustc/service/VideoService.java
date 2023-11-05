package io.sustc.service;

import io.sustc.dto.AuthInfo;

import java.util.List;
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

    /**
     * Updates the video's information.
     * If the video was reviewed before, the review info should be invalidated.
     *
     * @return {@code true} if the video needs to be re-reviewed (was reviewed before), {@code false} otherwise
     */
    boolean updateVideoInfo(/* TODO */);

    /**
     * Reviews a video by a super admin.
     * If the video is already reviewed, do not modify the review info.
     *
     * @param auth the current user's authentication information
     * @param bv   the video's {@code bv}
     * @return {@code true} if the video is successfully reviewed, {@code false} if the video is already reviewed
     */
    boolean reviewVideo(AuthInfo auth, String bv);

    /**
     * Donates one coin to the video.
     *
     * @param auth the current user's authentication information
     * @param bv   the video's {@code bv}
     */
    void coinVideo(AuthInfo auth, String bv);

    /**
     * Search the videos by keywords (split by space).
     * You should try to match the keywords case-insensitively in the following fields:
     * <ol>
     *     <li>title</li>
     *     <li>description</li>
     *     <li>owner name</li>
     * </ol>
     * Sort the results by the relevance (the number of matched keywords,
     * if a keyword occurs multiple times, it should only be counted more than once).
     * If two videos have the same relevance, sort them by the number of views.
     *
     * @param keywords the keywords to search, e.g. "sustech database final review"
     * @param pageSize the page size, if there are less than {@code pageSize} videos, return all of them
     * @param pageNum  the page number, starts from 1
     * @return a list of video {@code bv}s
     */
    List<String> searchVideo(String keywords, int pageSize, int pageNum);
}
