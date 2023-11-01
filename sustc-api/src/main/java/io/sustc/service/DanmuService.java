package io.sustc.service;

import io.sustc.dto.AuthInfo;

public interface DanmuService {

    /**
     * Sends a danmu to a video.
     *
     * @param auth    the current user's authentication information
     * @param bv      the video's bv
     * @param content the content of danmu
     * @param time    seconds since the video starts
     */
    void sendDanmu(AuthInfo auth, String bv, String content, Float time);
}
