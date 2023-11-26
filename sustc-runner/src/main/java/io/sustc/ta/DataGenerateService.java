package io.sustc.ta;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import io.sustc.ta.dto.RawDanmuRecord;
import io.sustc.ta.dto.RawUserRecord;
import io.sustc.ta.dto.RawVideoRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

public interface DataGenerateService {

    List<RawDanmuRecord> loadDanmuToBean(
            BufferedReader danmuReader
    ) throws IOException, CsvDataTypeMismatchException;


    List<RawUserRecord> loadUserToBean(
            BufferedReader userReader
    ) throws IOException;


    List<RawVideoRecord> loadVideoToBean(
            BufferedReader videoReader
    ) throws IOException;

    /**
     * Imports data and generate new data from csv files.
     *
     * @param danmuRecords a stream of danmu records
     * @param userRecords  a stream of user records
     * @param videoRecords a stream of video records
     * @return the sum of successfully imported users, videos and danmus
     */
    long loadGenerateSave(
            List<RawDanmuRecord> danmuRecords,
            List<RawUserRecord> userRecords,
            List<RawVideoRecord> videoRecords
    );

}
