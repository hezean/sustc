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

import io.sustc.dto.DanmuRecord;
import io.sustc.dto.UserRecord;
import io.sustc.dto.VideoRecord;
import io.sustc.service.DatabaseService;
import lombok.extern.slf4j.Slf4j;

/**
 * It's important to mark your implementation class with {@link Service}
 * annotation.
 * As long as the class is annotated and implements the corresponding interface,
 * you can place it under any package.
 */
@Service
@Slf4j
public class DatabaseServiceImpl implements DatabaseService {

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
        // throw new UnsupportedOperationException("TODO: replace this with your own
        // student id");
        return Arrays.asList(12210216, 12212522);
    }

    @Override
    public void importData(
            List<DanmuRecord> danmuRecords,
            List<UserRecord> userRecords,
            List<VideoRecord> videoRecords) {
        // TODO: implement your import logic
        System.out.println(danmuRecords.size());
        System.out.println(userRecords.size());
        System.out.println(videoRecords.size());

        final int batchcount = 0;
        int batchsize = 1000;
        Connection conn = null;
        try {
            // Open the connection
            conn = dataSource.getConnection();
            conn.setAutoCommit(false); // Start transaction
            //Upload in UserRecord
            String userSql = "INSERT INTO users (mid, name, sex, birthday, level, sign, identity, coin) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            String authSql = "INSERT INTO auth (mid, password, qq, wechat) VALUES (?, ?, ?, ?)";
            String followerSql = "INSERT INTO user_relationships (followerMid, followingMid) VALUES (?, ?)";
            //Upload in VideoRecord
            String videoSql = "INSERT INTO videos (bv, title, ownerMid, commitTime, reviewTime, publicTime, duration, description, isPublic, reviewer) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            String interactionSql = "INSERT INTO user_video_interaction (mid, bv, is_liked, is_coined, is_favorited) VALUES (?, ?, ?, ?, ?)";
            String watchSql = "INSERT INTO user_video_watch (mid, bv, watch_time) VALUES (?, ?, ?)";
                        //Upload in DanmuRecord
            String danmuSql = "INSERT INTO danmus (bv, mid, time, content, postTime) VALUES (?, ?, ?, ?, ?)";
            String danmuLikeSql = "INSERT INTO danmu_like (danmuId, mid) VALUES (?, ?)";
            // Insert User Records
            try (PreparedStatement userStmt = conn.prepareStatement(userSql);
                    PreparedStatement authStmt = conn.prepareStatement(authSql);
                    PreparedStatement followerStmt = conn.prepareStatement(followerSql);) {
                for (UserRecord user : userRecords) {
                    userStmt.setLong(1, user.getMid());
                    userStmt.setString(2, user.getName());
                    userStmt.setString(3, user.getSex());
                    userStmt.setString(4, user.getBirthday());
                    userStmt.setInt(5, user.getLevel());
                    userStmt.setString(6, user.getSign());
                    if (user.getIdentity().equals("user"))
                        userStmt.setString(7, "user");
                    else
                        userStmt.setString(0, "superuser");
                    userStmt.setInt(8, user.getCoin());
                    userStmt.addBatch();

                    authStmt.setLong(1, user.getMid());
                    authStmt.setString(2, user.getPassword());
                    authStmt.setString(3, user.getQq());
                    authStmt.setString(4, user.getWechat());
                    authStmt.addBatch();
                    followerStmt.setLong(1, user.getMid());
                    for (long mid : user.getFollowing()) {
                        followerStmt.setLong(2, mid);
                        followerStmt.addBatch();
                    }
                    batchsize++;
                    if (batchsize == batchcount) {
                        userStmt.executeBatch();
                        authStmt.executeBatch();
                        followerStmt.executeBatch();
                        batchsize = 0;

                    }
                }
                authStmt.executeBatch();
                userStmt.executeBatch();
                followerStmt.executeBatch();
            }
            // Insert Video Records
            try (PreparedStatement videoStmt = conn.prepareStatement(videoSql);
                    PreparedStatement interactionStmt = conn.prepareStatement(interactionSql);
                    PreparedStatement watchStmt = conn.prepareStatement(watchSql);
                    ) {
                for (VideoRecord video : videoRecords) {
                    videoStmt.setString(1, video.getBv());
                    videoStmt.setString(2, video.getTitle());
                    videoStmt.setLong(3, video.getOwnerMid());
                    videoStmt.setTimestamp(4, video.getCommitTime());
                    videoStmt.setTimestamp(5, video.getReviewTime());
                    videoStmt.setTimestamp(6, video.getPublicTime());
                    videoStmt.setFloat(7, video.getDuration());
                    videoStmt.setString(8, video.getDescription());
                    videoStmt.setBoolean(9, false);
                    videoStmt.setLong(10, video.getReviewer());
                    videoStmt.addBatch();

                    for (long mid : video.getLike()) {
                        interactionStmt.setLong(1, mid);
                        interactionStmt.setString(2, video.getBv());
                        interactionStmt.setBoolean(3, true);
                        interactionStmt.setBoolean(4, false);
                        interactionStmt.setBoolean(5, false);
                        interactionStmt.addBatch();
                    }

                    for (long mid : video.getCoin()) {
                        interactionStmt.setLong(1, mid);
                        interactionStmt.setString(2, video.getBv());
                        interactionStmt.setBoolean(3, false);
                        interactionStmt.setBoolean(4, true);
                        interactionStmt.setBoolean(5, false);
                        interactionStmt.addBatch();
                    }

                    for (long mid : video.getFavorite()) {
                        interactionStmt.setLong(1, mid);
                        interactionStmt.setString(2, video.getBv());
                        interactionStmt.setBoolean(3, false);
                        interactionStmt.setBoolean(4, false);
                        interactionStmt.setBoolean(5, true);
                        interactionStmt.addBatch();
                    }
                    int temp = 0;
                    for (long mid : video.getViewerMids()) {
                        watchStmt.setLong(1, mid);
                        watchStmt.setString(2, video.getBv());
                        watchStmt.setFloat(3, video.getViewTime()[temp++]);
                        watchStmt.addBatch();
                    }
                    batchsize++;
                    if (batchsize == batchcount) {
                        videoStmt.executeBatch();
                        interactionStmt.executeBatch();
                        watchStmt.executeBatch();
                        batchsize = 0;
                    }
                }
                videoStmt.executeBatch();
                interactionStmt.executeBatch();
                watchStmt.executeBatch();
            }
            // Insert Danmu Records
            try (PreparedStatement danmuStmt = conn.prepareStatement(danmuSql);
                    PreparedStatement danmuLikeStmt = conn.prepareStatement(danmuLikeSql);
                    ) {
                for (DanmuRecord danmu : danmuRecords) {
                    danmuStmt.setString(1, danmu.getBv());
                    danmuStmt.setLong(2, danmu.getMid());
                    danmuStmt.setFloat(3, danmu.getTime());
                    danmuStmt.setString(4, danmu.getContent());
                    danmuStmt.setTimestamp(5, danmu.getPostTime());
                    danmuStmt.addBatch();
                    int id = danmuStmt.getGeneratedKeys().getInt(1);
                    for (long mid : danmu.getLikedBy()) {
                        danmuLikeStmt.setLong(1, id);
                        danmuLikeStmt.setLong(2, mid);
                        danmuLikeStmt.addBatch();
                    }
                    batchsize++;
                    if (batchsize == batchcount) {
                        danmuStmt.executeBatch();
                        danmuLikeStmt.executeBatch();
                        batchsize = 0;
                    }
                }
                danmuStmt.executeBatch();
                danmuLikeStmt.executeBatch();
            }

        } catch (SQLException e) {
            // If any error occurs, rollback the transaction
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e1) {
                throw new RuntimeException(e1);
            }
            throw new RuntimeException(e);
        } finally {
            // Close the connection
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
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
