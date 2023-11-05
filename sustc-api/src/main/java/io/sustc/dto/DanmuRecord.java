package io.sustc.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class DanmuRecord {

    /**
     * The danmu's video bv.
     */
    @CsvBindByName(column = "BV")
    private String bv;

    /**
     * The danmu's sender mid.
     */
    @CsvBindByName(column = "Mid")
    private Long mid;

    /**
     * The danmu's display time (in seconds) since the video starts.
     */
    @CsvBindByName(column = "Time")
    private Float time;

    /**
     * The danmu's content.
     * The content has not been filtered.
     * You need to clean the "dirty words" when importing.
     */
    @CsvBindByName(column = "Content")
    private String content;

    /**
     * The danmu's post time.
     */
    @CsvBindByName(column = "Post Time")
    private Timestamp postTime;

    /**
     * The users' mid who liked this danmu.
     */
    @CsvBindByName(column = "Liked By")
    private Long[] likedBy;
}
