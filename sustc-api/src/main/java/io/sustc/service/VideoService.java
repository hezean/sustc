package io.sustc.service;

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
     * Get the hotspot of a video.
     * With splitting the video into 10-second chunks, hotspots are defined as chunks with the most danmus.
     *
     * @param bv the video's {@code bv}
     * @return the index of hotspot chunks (start from 0)
     */
    Set<Integer> getHotspot(String bv);
}
