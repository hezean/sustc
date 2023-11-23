package io.sustc.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class UserRecord {

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
    private Identity identity;

    @CsvBindByName(column = "password")
    private String password;

    @CsvBindByName(column = "qq")
    private String qq;

    @CsvBindByName(column = "wechat")
    private String wechat;

    public enum Identity {
        USER,
        SUPERUSER,
    }
}
