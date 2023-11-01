package io.sustc.service;

import io.sustc.dto.DanmuRecord;
import io.sustc.dto.UserRecord;
import io.sustc.dto.VideoRecord;

import java.util.List;
import java.util.stream.Stream;

public interface DatabaseService {

    /**
     * Acknowledges the authors of this project.
     *
     * @return a list of group members' student-id
     */
    List<Integer> getGroupMembers();

    /**
     * Imports data to an empty database from csv files.
     *
     * @param danmuRecords a stream of danmu records
     * @param userRecords  a stream of user records
     * @param videoRecords a stream of video records
     * @return the sum of successfully imported users, videos and danmus
     */
    long importData(
            Stream<DanmuRecord> danmuRecords,
            Stream<UserRecord> userRecords,
            Stream<VideoRecord> videoRecords
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
     * This method only demonstrates how to access database using raw java.sql classes.
     *
     * @return the sum of all student-ids
     */
    Integer sum(int a, int b);
}
