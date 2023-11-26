package io.sustc.ta.dto;

import com.opencsv.bean.AbstractCsvConverter;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import io.sustc.dto.DanmuRecord;
import lombok.Data;
import lombok.val;

import java.sql.Timestamp;
import java.util.regex.Pattern;

@Data
public class RawDanmuRecord {

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


    public DanmuRecord buildDanmuRecord(Timestamp postTime, Long[] likedBy){
        DanmuRecord danmuRecord = new DanmuRecord();
        danmuRecord.setBv(this.getBv());
        danmuRecord.setTime(this.getTime());
        danmuRecord.setMid(this.getMid());
        danmuRecord.setContent(this.getContent());
        danmuRecord.setPostTime(postTime);
        danmuRecord.setLikedBy(likedBy);
        return danmuRecord;
    }
}
