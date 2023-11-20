package io.sustc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostVideoReq {

    /**
     * The video's title.
     */
    private String title;

    /**
     * The video's description.
     */
    private String description;

    /**
     * The video's duration (in seconds).
     */
    private long duration;

    /**
     * The video's public time.
     * <p>
     * When posting a video, the owner can decide when to make it public.
     * Before the public time, the video is only visible to the owner and superusers.
     * <p>
     * If the video is already published
     * (when this DTO is used to {@link io.sustc.service.VideoService#updateVideoInfo(AuthInfo, String, PostVideoReq)}
     * update a video), this field should be ignored.
     */
    private Timestamp publicTime;
}
