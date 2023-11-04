package io.sustc.service;

import io.sustc.dto.AuthInfo;
import io.sustc.dto.ListDanmuResp;

import java.util.List;

public interface DanmuService {

    // FIXME: earn 2 coins??
    // earn 1 additionally in birthday month?
    /**
     * Sends a danmu to a video.
     *
     * @param auth    the current user's authentication information
     * @param bv      the video's bv
     * @param content the content of danmu
     * @param time    seconds since the video starts
     */
    void sendDanmu(AuthInfo auth, String bv, String content, Float time);

    /**
     * Lists all danmus of a video.
     *
     * @param bv the video's bv
     * @return a list of danmus
     */
    List<ListDanmuResp> listDanmu(String bv);
}
