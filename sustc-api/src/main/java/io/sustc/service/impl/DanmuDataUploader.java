package io.sustc.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.sql.Statement;

import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

import io.sustc.dto.*;

import javax.sql.DataSource;

public class DanmuDataUploader {
    private DataSource dataSource;
    private static final int THREAD_POOL_SIZE = 10;
    private static final int batchsize = 1000;

    @Autowired
    public DanmuDataUploader(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void uploadData(List<DanmuRecord> danmuRecords) throws SQLException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        // 分割userRecords为多个批次
        List<List<DanmuRecord>> batches = splitIntoBatches(danmuRecords, batchsize);

        for (List<DanmuRecord> batch : batches) {
            executor.submit(() -> {
                try {
                    insertBatch(batch);
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

    private void insertBatch(List<DanmuRecord> batch) throws SQLException {

        String danmuSql = "INSERT INTO danmus (bv, mid, time, content, postTime) VALUES (?, ?, ?, ?, ?)";
        String danmuLikeSql = "INSERT INTO danmu_like (danmuId, mid) VALUES (?, ?)";

        Connection conn = dataSource.getConnection();
        conn.setAutoCommit(false);
        String disableSql = "SET session_replication_role = 'replica'";
        try (PreparedStatement disableStmt = conn.prepareStatement(disableSql)) {
            disableStmt.execute();
        }

        try (PreparedStatement danmuStmt = conn.prepareStatement(danmuSql, Statement.RETURN_GENERATED_KEYS);
                PreparedStatement danmuLikeStmt = conn.prepareStatement(danmuLikeSql);) {
            for (DanmuRecord danmu : batch) {
                danmuStmt.setString(1, danmu.getBv());
                danmuStmt.setLong(2, danmu.getMid());
                danmuStmt.setFloat(3, danmu.getTime());
                danmuStmt.setString(4, danmu.getContent());
                danmuStmt.setTimestamp(5, danmu.getPostTime());
                danmuStmt.addBatch();
            }

            danmuStmt.executeBatch();
            ResultSet id = danmuStmt.getGeneratedKeys();
                int i = 0;
                while (id.next()) {
                    danmuLikeStmt.setInt(1, id.getInt(1));
                    for (long mid : batch.get(i++).getLikedBy()) {
                        danmuLikeStmt.setLong(2, mid);
                        danmuLikeStmt.addBatch();
                    }
                }
            danmuLikeStmt.executeBatch();
        }
        conn.commit();
        conn.close();
    }

    private <T> List<List<T>> splitIntoBatches(List<T> list, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        int size = list.size();
        for (int i = 0; i < size; i += batchSize) {
            if (i + batchSize < size) {
                batches.add(list.subList(i, i + batchSize));
            } else {
                batches.add(list.subList(i, size));
            }
        }
        return batches;
    }

}
