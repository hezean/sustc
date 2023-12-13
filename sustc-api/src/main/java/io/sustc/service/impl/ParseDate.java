package io.sustc.service.impl;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ParseDate {
    
    public static LocalDate parseDate(String dateString) {
        DateTimeFormatter formatterMMDD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter formatterChinese = DateTimeFormatter.ofPattern("yyyy年M月d日");

        try {
            // 尝试第一种格式
            return LocalDate.parse("1900-" + dateString, formatterMMDD);
        } catch (DateTimeParseException e) {
            try {
                // 尝试第二种格式
                return LocalDate.parse("1900年" + dateString, formatterChinese);
            } catch (DateTimeParseException ex) {
                // 两种格式都不对
                return null;
            }
        }
    }
}
