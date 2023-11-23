package io.sustc.service;

import io.sustc.dto.AuthInfo;
import io.sustc.dto.PostVideoReq;

import java.util.List;
import java.util.Set;

public interface VideoService {

    /**
     * Posts a video.
     *
     * @param auth the current user's authentication information
     * @param req  the video's information
     * @return the video's {@code bv}
     */
    String postVideo(AuthInfo auth, PostVideoReq req);

    /**
     * Deletes a video.
     * This operation can be performed by the video owner or a superuser.
     *
     * @param auth the current user's authentication information
     * @param bv   the video's {@code bv}
     */
    void deleteVideo(AuthInfo auth, String bv);

    /**
     * Updates the video's information.
     * Only the owner of the video can update the video's information.
     * If the video was reviewed before, the review info should be invalidated.
     *
     * @return {@code true} if the video needs to be re-reviewed (was reviewed before), {@code false} otherwise
     */
    boolean updateVideoInfo(AuthInfo auth, String bv, PostVideoReq req);

    /**
     * Search the videos by keywords (split by space).
     * You should try to match the keywords case-insensitively in the following fields:
     * <ol>
     *     <li>title</li>
     *     <li>description</li>
     *     <li>owner name</li>
     * </ol>
     * <p>
     * Sort the results by the relevance (sum up the number of keywords matched in the three fields).
     * <ul>
     *     <li>If a keyword occurs multiple times, it should be counted more than once.</li>
     *     <li>
     *         A character in these fields can only be counted once for each keyword
     *         but can be counted for different keywords.
     *     </li>
     *     <li>If two videos have the same relevance, sort them by the number of views.</li>
     * </ul>
     * <p>
     * Examples:
     * <ol>
     *     <li>
     *         If the title is "1122" and the keywords are "11 12",
     *         then the relevance in the title is 2 (one for "11" and one for "12").
     *     </li>
     *     <li>
     *         If the title is "111" and the keyword is "11",
     *         then the relevance in the title is 1 (one for the occurrence of "11").
     *     </li>
     *     <li>
     *         Consider a video with title "Java Tutorial", description "Basic to Advanced Java", owner name "John Doe".
     *         If the search keywords are "Java Advanced",
     *         then the relevance is 3 (one occurrence in the title and two in the description).
     *     </li>
     * </ol>
     * <p>
     * Unreviewed or unpublished videos are only visible to superusers or the video owner.
     *
     * @param auth     the current user's authentication information
     * @param keywords the keywords to search, e.g. "sustech database final review"
     * @param pageSize the page size, if there are less than {@code pageSize} videos, return all of them
     * @param pageNum  the page number, starts from 1
     * @return a list of video {@code bv}s
     */
    List<String> searchVideo(AuthInfo auth, String keywords, int pageSize, int pageNum);

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
     * Reviews a video by a super admin.
     * If the video is already reviewed, do not modify the review info.
     *
     * @param auth the current user's authentication information
     * @param bv   the video's {@code bv}
     * @return {@code true} if the video is newly successfully reviewed, {@code false} otherwise
     */
    boolean reviewVideo(AuthInfo auth, String bv);

    /**
     * Donates one coin to the video.
     * A user can at most donate 2 coins to a video.
     *
     * @param auth the current user's authentication information
     * @param bv   the video's {@code bv}
     * @return whether a coin is successfully donated
     */
    boolean coinVideo(AuthInfo auth, String bv);

    /**
     * Likes a video.
     * The user can only like a video if he/she can search it.
     * If the user already liked the video, the operation will cancel the like.
     *
     * @param auth the current user's authentication information
     * @param bv   the video's {@code bv}
     */
    void likeVideo(AuthInfo auth, String bv);

    /**
     * Collects a video.
     * The user can only collect a video if he/she can search it.
     * If the user already collected the video, the operation will cancel the collection.
     *
     * @param auth the current user's authentication information
     * @param bv   the video's {@code bv}
     */
    void collectVideo(AuthInfo auth, String bv);
}
