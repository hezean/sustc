package io.sustc.ta.dto;

import com.opencsv.bean.AbstractCsvConverter;
import com.opencsv.bean.CsvBindAndSplitByName;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import io.sustc.dto.UserRecord;
import io.sustc.dto.VideoRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.val;

import java.sql.Timestamp;
import java.util.List;
import java.util.regex.Pattern;

@Data
public class RawVideoRecord {

    @CsvBindByName(column = "BV")
    private String bv;

    @CsvBindByName(column = "Title")
    private String title;

    @CsvBindByName(column = "Owner Mid")
    private Long ownerMid;

    @CsvBindByName(column = "Owner Name")
    private String ownerName;

    @CsvBindByName(column = "Commit Time")
    private Timestamp commitTime;

    @CsvBindByName(column = "Review Time")
    private Timestamp reviewTime;

    @CsvBindByName(column = "Public Time")
    private Timestamp publicTime;

    @CsvBindByName(column = "Duration")
    private Long duration;

    @CsvBindByName(column = "Description")
    private String description;

    @CsvBindByName(column = "Reviewer")
    private Long reviewer;

    @CsvBindByName(column = "Like")
    private Long[] like;

    @CsvBindByName(column = "Coin")
    private Long[] coin;

    @CsvBindByName(column = "Favorite")
    private Long[] favorite;

    @CsvBindAndSplitByName(
            column = "View",
            elementType = ViewRecord.class,
            converter = ViewRecordConverter.class,
            splitOn = "(?<=\\)),\\s*(?=\\()"
    )
    private List<ViewRecord> view;

    @Data
    @AllArgsConstructor
    public static class ViewRecord {

        private Long mid;

        private Float timestamp;
    }

    public static class ViewRecordConverter extends AbstractCsvConverter {

        private static final Pattern PATTERN = Pattern.compile("\\('(?<mid>\\d+)', (?<ts>\\d+)\\)");

        @Override
        public Object convertToRead(String value) throws CsvDataTypeMismatchException {
            val matcher = PATTERN.matcher(value);
            if (!matcher.find()) {
                throw new CsvDataTypeMismatchException(value, Long.class);
            }
            return new ViewRecord(
                    Long.parseLong(matcher.group("mid")),
                    Float.parseFloat(matcher.group("ts"))
            );
        }
    }


//    public VideoRecord buildVideoRecord(String password, String qq, String wechat){
//        UserRecord userRecord = new UserRecord();
//        userRecord.setMid(this.getMid());
//        userRecord.setName(this.getName());
//        userRecord.setBirthday(this.getBirthday());
//        userRecord.setLevel(this.getLevel());
//        userRecord.setSign(this.getSign());
//        userRecord.setFollowing(this.getFollowing());
//        userRecord.setIdentity(this.getIdentity());
//        userRecord.setPassword(password);
//        userRecord.setQq(qq);
//        userRecord.setWechat(wechat);
//        return userRecord;
//    }
}