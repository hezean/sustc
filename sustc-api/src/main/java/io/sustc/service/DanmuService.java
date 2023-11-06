package io.sustc.service;

import io.sustc.dto.AuthInfo;
import io.sustc.dto.ListDanmuResp;

import java.util.List;

public interface DanmuService {

    /**
     * Sends a danmu to a video.
     * <ul>
     *     <li>Remember to clean the "dirty words" in the content.</li>
     *     <li>The danmu will be sent to the database directly.</li>
     *     <li>The danmu will be displayed on the video page after 5 minutes.</li>
     * </ul>
     *
     * @param auth    the current user's authentication information
     * @param bv      the video's bv
     * @param content the content of danmu
     * @param time    seconds since the video starts
     */
    void sendDanmu(AuthInfo auth, String bv, String content, float time);

    /**
     * Display the danmus in a time range.
     * Similar to bilibili's mechanism, user can choose to only display part of the danmus
     * to have a better watch experience.
     *
     * @param bv        the video's bv
     * @param timeStart the start time of the range
     * @param timeEnd   the end time of the range
     * @param filter    whether to remove the duplicated content,
     *                  if {@code true}, only the earliest posted danmu with the same content will be displayed
     * @return a list of danmus, sorted by {@code time}
     */
    List<ListDanmuResp> displayDanmu(String bv, float timeStart, float timeEnd, boolean filter);

    /**
     * Likes a danmu.
     * If the user already liked the danmu, this operation will cancel the like status.
     *
     * @param auth the current user's authentication information
     * @param bv   the video's bv
     * @param mid  the danmu sender's mid
     * @param time the danmu's time
     */
    void likeDanmu(AuthInfo auth, String bv, long mid, float time);
}
