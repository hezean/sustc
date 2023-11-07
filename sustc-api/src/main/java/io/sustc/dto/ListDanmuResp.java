package io.sustc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListDanmuResp {

    /**
     * The danmu's sender mid.
     */
    private long mid;

    /**
     * The danmu's display time (in seconds) since the video starts.
     */
    private float time;

    /**
     * The danmu's content.
     */
    private String content;

    /**
     * The number of users who liked this danmu.
     */
    private int likedBy;
}
