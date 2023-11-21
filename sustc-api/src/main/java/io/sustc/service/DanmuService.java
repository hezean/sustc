package io.sustc.service;

import io.sustc.dto.AuthInfo;

import java.util.List;

public interface DanmuService {

    /**
     * Sends a danmu to a video.
     *
     * @param auth    the current user's authentication information
     * @param bv      the video's bv
     * @param content the content of danmu
     * @param time    seconds since the video starts
     * @return the generated danmu id
     */
    long sendDanmu(AuthInfo auth, String bv, String content, float time);

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
     * @return a list of danmus id, sorted by {@code time}
     */
    List<Long> displayDanmu(String bv, float timeStart, float timeEnd, boolean filter);

    /**
     * Likes a danmu.
     * If the user already liked the danmu, this operation will cancel the like status.
     *
     * @param auth the current user's authentication information
     * @param id   the danmu's id
     */
    void likeDanmu(AuthInfo auth, long id);
}
