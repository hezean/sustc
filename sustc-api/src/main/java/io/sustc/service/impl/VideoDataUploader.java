package io.sustc.service.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Connection;

import io.sustc.dto.*;

import javax.sql.DataSource;

public class VideoDataUploader {
    private static final int THREAD_POOL_SIZE = 10; // 根据需要调整线程池大小
    private final DataSource dataSource;
    private static final int BATCH_SIZE = 50;

    @Autowired
    public VideoDataUploader(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void uploadVideoData(List<VideoRecord> videoRecords) throws SQLException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        // 将videoRecords分割为多个批次
        List<List<VideoRecord>> batches = splitIntoBatches(videoRecords, BATCH_SIZE);

        for (List<VideoRecord> batch : batches) {
            executor.submit(() -> {
                try {
                    processBatch(batch);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void processBatch(List<VideoRecord> batch) throws SQLException {
        String videoSql = "INSERT INTO videos (bv, title, ownerMid, commitTime, reviewTime, publicTime, duration, description, isPublic, reviewer) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String interactionSql = "INSERT INTO user_video_interaction (mid, bv, is_liked, is_coined, is_favorited) VALUES (?, ?, ?, ?, ?)";
        String watchSql = "INSERT INTO user_video_watch (mid, bv, watch_time) VALUES (?, ?, ?)";
        String likeSql = "INSERT INTO user_video_like (mid, bv) VALUES (?, ?)";
        String coinSql = "INSERT INTO user_video_coin (mid, bv) VALUES (?, ?)";
        String favoriteSql = "INSERT INTO user_video_favorite (mid, bv) VALUES (?, ?)";

        Connection conn = dataSource.getConnection();
        conn.setAutoCommit(false);
        String disableSql = "SET session_replication_role = 'replica'";
        try (PreparedStatement disableStmt = conn.prepareStatement(disableSql)) {
            disableStmt.execute();
        }

        try (PreparedStatement videoStmt = conn.prepareStatement(videoSql);
             PreparedStatement interactionStmt = conn.prepareStatement(interactionSql);
             PreparedStatement watchStmt = conn.prepareStatement(watchSql);
             PreparedStatement likeStmt = conn.prepareStatement(likeSql);
             PreparedStatement coinStmt = conn.prepareStatement(coinSql);
             PreparedStatement favoriteStmt = conn.prepareStatement(favoriteSql)) {

            for (VideoRecord video: batch) {
                // 插入video
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

                int temp = 0;
                for (long mid : video.getViewerMids()) {
                    watchStmt.setLong(1, mid);
                    watchStmt.setString(2, video.getBv());
                    watchStmt.setFloat(3, video.getViewTime()[temp++]);
                    watchStmt.addBatch();
                }
                watchStmt.executeBatch();
            }
            videoStmt.executeBatch();
            insertVideoInteractions(batch, conn);
            conn.commit();
            conn.close();
        }
    }

    private List<List<VideoRecord>> splitIntoBatches(List<VideoRecord> videoRecords, int batchSize) {
        List<List<VideoRecord>> batches = new ArrayList<>();
        for (int i = 0; i < videoRecords.size(); i += batchSize) {
            batches.add(videoRecords.subList(i, Math.min(i + batchSize, videoRecords.size())));
        }
        return batches;
    }

    private static void insertVideoInteractions(List<VideoRecord> videoList, Connection conn) throws SQLException {
        conn.setAutoCommit(false);

        createTempTables(conn);
        batchInsertToTempTables(conn, videoList, "temp_likes");
        batchInsertToTempTables(conn, videoList, "temp_coins");
        batchInsertToTempTables(conn, videoList, "temp_favorites");
        mergeDataWithMainTable(conn);
        dropTempTables(conn);

        conn.commit();
    }

    private static void createTempTables(Connection conn) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "CREATE TEMP TABLE IF NOT EXISTS temp_likes (mid BIGINT, bv VARCHAR(50));" +
                        "CREATE TEMP TABLE IF NOT EXISTS temp_coins (mid BIGINT, bv VARCHAR(50));" +
                        "CREATE TEMP TABLE IF NOT EXISTS temp_favorites (mid BIGINT, bv VARCHAR(50));")) {
            pstmt.execute();
        }
    }

    private static void batchInsertToTempTables(Connection conn, List<VideoRecord> videoList, String tableName)
            throws SQLException {
        String sql = "INSERT INTO " + tableName + " (mid, bv) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (VideoRecord video : videoList) {
                long[] userIds = new long[0];
                if ("temp_likes".equals(tableName)) {
                    userIds = video.getLike();
                } else if ("temp_coins".equals(tableName)) {
                    userIds = video.getCoin();
                } else if ("temp_favorites".equals(tableName)) {
                    userIds = video.getFavorite();
                }

                for (long userId : userIds) {
                    pstmt.setLong(1, userId);
                    pstmt.setString(2, video.getBv());
                    pstmt.addBatch();
                }
            }
            pstmt.executeBatch();
        }
    }

    private static void mergeDataWithMainTable(Connection conn) throws SQLException {
        String updateSql = "WITH combined AS (" +
                "SELECT DISTINCT mid, bv FROM temp_likes " +
                "UNION SELECT DISTINCT mid, bv FROM temp_coins " +
                "UNION SELECT DISTINCT mid, bv FROM temp_favorites" +
                "), " +
                "likes AS (" +
                "SELECT mid, bv, TRUE as is_liked FROM temp_likes" +
                "), " +
                "coins AS (" +
                "SELECT mid, bv, TRUE as is_coined FROM temp_coins" +
                "), " +
                "favorites AS (" +
                "SELECT mid, bv, TRUE as is_favorited FROM temp_favorites" +
                ") " +
                "INSERT INTO user_video_interaction (mid, bv, is_liked, is_coined, is_favorited) " +
                "SELECT c.mid, c.bv, " +
                "COALESCE(l.is_liked, FALSE), " +
                "COALESCE(cn.is_coined, FALSE), " +
                "COALESCE(f.is_favorited, FALSE) " +
                "FROM combined c " +
                "LEFT JOIN likes l ON c.mid = l.mid AND c.bv = l.bv " +
                "LEFT JOIN coins cn ON c.mid = cn.mid AND c.bv = cn.bv " +
                "LEFT JOIN favorites f ON c.mid = f.mid AND c.bv = f.bv " +
                "ON CONFLICT (mid, bv) DO UPDATE SET " +
                "is_liked = EXCLUDED.is_liked, " +
                "is_coined = EXCLUDED.is_coined, " +
                "is_favorited = EXCLUDED.is_favorited;";
        try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
            pstmt.execute();
        }
    }

    private static void dropTempTables(Connection conn) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "DROP TABLE IF EXISTS temp_likes;" +
                        "DROP TABLE IF EXISTS temp_coins;" +
                        "DROP TABLE IF EXISTS temp_favorites;")) {
            pstmt.execute();
        }
    }
}
