package io.sustc.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.sustc.dto.DanmuRecord;
import io.sustc.dto.UserRecord;
import io.sustc.dto.VideoRecord;
import io.sustc.service.DatabaseService;
import io.sustc.service.impl.Uploaders.DanmuDataUploader;
import io.sustc.service.impl.Uploaders.UserDataUploader;
import io.sustc.service.impl.Uploaders.VideoDataUploader;
import lombok.extern.slf4j.Slf4j;

/**
 * It's important to mark your implementation class with {@link Service}
 * annotation.
 * As long as the class is annotated and implements the corresponding interface,
 * you can place it under any package.
 */
@Service
@Slf4j
@Transactional
public class DatabaseServiceImpl implements DatabaseService {
    final static int batchsize = 1000;

    /**
     * Getting a {@link DataSource} instance from the framework, whose connections
     * are managed by HikariCP.
     * <p>
     * Marking a field with {@link Autowired} annotation enables our framework to
     * automatically
     * provide you a well-configured instance of {@link DataSource}.
     * Learn more:
     * <a href="https://www.baeldung.com/spring-dependency-injection">Dependency
     * Injection</a>
     */
    @Autowired
    private DataSource dataSource;

    @Override
    public List<Integer> getGroupMembers() {
        return Arrays.asList(12210216, 12212522);
    }

    @Override
    public void importData(
            List<DanmuRecord> danmuRecords,
            List<UserRecord> userRecords,
            List<VideoRecord> videoRecords) {

        System.out.println("Total danmu records: " + danmuRecords.size());
        System.out.println("Total user records: " + userRecords.size());
        System.out.println("Total video records: " + videoRecords.size());
        log.info("Importing data...");
        long start = System.currentTimeMillis();
        try {
            truncate();
            UserDataUploader userUploader = new UserDataUploader(dataSource);
            userUploader.uploadData(userRecords);
            VideoDataUploader videoUploader = new VideoDataUploader(dataSource);
            videoUploader.uploadVideoData(videoRecords);
            DanmuDataUploader danmuUploader = new DanmuDataUploader(dataSource);
            danmuUploader.uploadData(danmuRecords);
            long end = System.currentTimeMillis();
            log.info("Importing data finished, time: {}ms", end - start);

        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    /*
     * The following code is just a quick example of using jdbc datasource.
     * Practically, the code interacts with database is usually written in a DAO
     * layer.
     *
     * Reference: [Data Access Object
     * pattern](https://www.baeldung.com/java-dao-pattern)
     */

    @Override
    public void truncate() {
        // You can use the default truncate script provided by us in most cases,
        // but if it doesn't work properly, you may need to modify it.

        String sql = "DO $$\n" +
                "DECLARE\n" +
                "    tables CURSOR FOR\n" +
                "        SELECT tablename\n" +
                "        FROM pg_tables\n" +
                "        WHERE schemaname = 'public';\n" +
                "BEGIN\n" +
                "    FOR t IN tables\n" +
                "    LOOP\n" +
                "        EXECUTE 'TRUNCATE TABLE ' || QUOTE_IDENT(t.tablename) || ' CASCADE;';\n" +
                "    END LOOP;\n" +
                "END $$;\n";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Integer sum(int a, int b) {
        String sql = "SELECT ?+?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, a);
            stmt.setInt(2, b);
            log.info("SQL: {}", stmt);

            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
