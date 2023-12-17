package io.sustc.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

import io.sustc.dto.*;

import javax.sql.DataSource;

public class UserDataUploader {
    private DataSource dataSource;
    private static final int THREAD_POOL_SIZE = 10;
    private static final int batchsize = 1000;

    @Autowired
    public UserDataUploader(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void uploadData(List<UserRecord> userRecords) throws SQLException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        // 分割userRecords为多个批次
        List<List<UserRecord>> batches = splitIntoBatches(userRecords, batchsize);

        for (List<UserRecord> batch : batches) {
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

    private void insertBatch(List<UserRecord> batch) throws SQLException {

        String userSql = "INSERT INTO users (mid, name, sex, birthday, level, sign, identity, coin) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String authSql = "INSERT INTO auth_info (mid, password, qq, wechat) VALUES (?, ?, ?, ?)";
        String followerSql = "INSERT INTO user_relationships (followerMid, followingMid) VALUES (?, ?)";
        Connection conn = dataSource.getConnection();
        conn.setAutoCommit(false);
        String disableSql = "SET session_replication_role = 'replica'";
        try (PreparedStatement disableStmt = conn.prepareStatement(disableSql)) {
            disableStmt.execute();
        }
        try (PreparedStatement userStmt = conn.prepareStatement(userSql);
                PreparedStatement authStmt = conn.prepareStatement(authSql);
                PreparedStatement followerStmt = conn.prepareStatement(followerSql)) {

            for (UserRecord user : batch) {
                // User statement
                userStmt.setLong(1, user.getMid());
                userStmt.setString(2, user.getName());
                userStmt.setString(3, user.getSex());
                userStmt.setInt(5, user.getLevel());
                userStmt.setString(6, user.getSign());
                userStmt.setString(7, user.getIdentity().toString());
                userStmt.setLong(8, user.getCoin());
                // Handle nullable fields
                String birthday = user.getBirthday();
                userStmt.setDate(4, (birthday.equals("null") || birthday.equals("")) ? null : Date.valueOf(ParseDate.parseDate(birthday)));
                userStmt.addBatch();

                // Auth statement
                authStmt.setLong(1, user.getMid());
                authStmt.setString(2, user.getPassword());
                authStmt.setString(3, user.getQq());
                authStmt.setString(4, user.getWechat());
                authStmt.addBatch();

                // Follower statement
                followerStmt.setLong(1, user.getMid());
                for (long mid : user.getFollowing()) {
                    followerStmt.setLong(2, mid);
                    followerStmt.addBatch();
                }
            }

            userStmt.executeBatch();
            authStmt.executeBatch();
            followerStmt.executeBatch();
        }
        conn.commit();
        conn.close();
    }

    private List<List<UserRecord>> splitIntoBatches(List<UserRecord> userRecords, int batchSize) {
        List<List<UserRecord>> batches = new ArrayList<>();
        int totalSize = userRecords.size();
        for (int i = 0; i < totalSize; i += batchSize) {
            batches.add(new ArrayList<>(
                    userRecords.subList(i, Math.min(i + batchSize, totalSize))));
        }
        return batches;
    }

}
