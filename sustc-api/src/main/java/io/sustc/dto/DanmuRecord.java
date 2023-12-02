package io.sustc.dto;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class DanmuRecord {

    /**
     * The danmu's video {@code bv}.
     */
    private String bv;

    /**
     * The danmu's sender {@code mid}.
     */
    private long mid;

    /**
     * The danmu's display time (in seconds) since the video starts.
     */
    private float time;

    /**
     * The danmu's content.
     * The content has not been filtered.
     * You need to clean the "dirty words" when importing.
     */
    private String content;

    /**
     * The danmu's post time.
     */
    private Timestamp postTime;

    /**
     * The users' {@code mid} who liked this danmu.
     */
    private long[] likedBy;
}
