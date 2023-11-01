package io.sustc.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class DanmuRecord {

    @CsvBindByName(column = "BV")
    private String bv;

    @CsvBindByName(column = "Mid")
    private Long mid;

    @CsvBindByName(column = "Time")
    private Float time;

    @CsvBindByName(column = "Content")
    private String content;
}
