package io.sustc.ta.dto;

import com.opencsv.bean.CsvBindByName;
import io.sustc.dto.DanmuRecord;
import io.sustc.dto.UserRecord;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class RawUserRecord {

    @CsvBindByName(column = "Mid")
    private Long mid;

    @CsvBindByName(column = "Name")
    private String name;

    @CsvBindByName(column = "Sex")
    private String sex;

    @CsvBindByName(column = "Birthday")
    private String birthday;

    @CsvBindByName(column = "Level")
    private Short level;

    @CsvBindByName(column = "Sign")
    private String sign;

    @CsvBindByName(column = "following")
    private Long[] following;

    @CsvBindByName(column = "identity")
    private UserRecord.Identity identity;


    public UserRecord buildUserRecord(String password, String qq, String wechat){
        UserRecord userRecord = new UserRecord();
        userRecord.setMid(this.getMid());
        userRecord.setName(this.getName());
        userRecord.setBirthday(this.getBirthday());
        userRecord.setLevel(this.getLevel());
        userRecord.setSign(this.getSign());
        userRecord.setFollowing(this.getFollowing());
        userRecord.setIdentity(this.getIdentity());
        userRecord.setPassword(password);
        userRecord.setQq(qq);
        userRecord.setWechat(wechat);
        return userRecord;
    }

}
