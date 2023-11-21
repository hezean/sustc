package io.sustc.service;

import io.sustc.dto.DanmuRecord;
import io.sustc.dto.UserRecord;
import io.sustc.dto.VideoRecord;

import java.util.List;

public interface DatabaseService {

    /**
     * Acknowledges the authors of this project.
     *
     * @return a list of group members' student-id
     */
    List<Integer> getGroupMembers();

    /**
     * Imports data to an empty database.
     *
     * @param danmuRecords danmu records parsed from csv
     * @param userRecords  user records parsed from csv
     * @param videoRecords video records parsed from csv
     */
    void importData(
            List<DanmuRecord> danmuRecords,
            List<UserRecord> userRecords,
            List<VideoRecord> videoRecords
    );

    /**
     * Truncates all tables in the database.
     * <p>
     * This would only be used in local benchmarking to help you
     * clean the database without dropping it, and won't affect your score.
     */
    void truncate();

    /**
     * Sums up two numbers via Postgres.
     * This method only demonstrates how to access database via JDBC.
     *
     * @param a the first number
     * @param b the second number
     * @return the sum of all student-ids
     */
    Integer sum(int a, int b);
}
