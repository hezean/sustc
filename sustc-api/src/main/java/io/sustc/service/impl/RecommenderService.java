package io.sustc.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.naming.spi.DirStateFactory.Result;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.sustc.dto.AuthInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
@Service
public class RecommenderService implements io.sustc.service.RecommenderService{
    @Autowired
    DataSource dataSource;
    @Override
    public List<String> recommendNextVideo(String bv) {
        try{
            Connection conn = dataSource.getConnection();
            String sql = "SELECT v.bv, COUNT(uvw.mid) AS common_viewers " +
                         "FROM user_video_watch uvw " +
                         "JOIN videos v ON uvw.bv = v.bv " +
                         "WHERE uvw.mid IN ( " +
                         "    SELECT mid " +
                         "    FROM user_video_watch " +
                         "    WHERE bv = ? " +
                         ") " +
                         "AND uvw.bv != ? " +
                         "GROUP BY v.bv " +
                         "ORDER BY common_viewers DESC " +
                         "LIMIT 5;";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, bv);
            ps.setString(2, bv);
            ps.execute();

            ResultSet rs = ps.getResultSet();
            List<String> result = new ArrayList<String>();
            while(rs.next()){
                result.add(rs.getString("bv"));
            }
            if(result.size() == 0){
                log.info("No similar videos found, or the video does not exist");
                return null;
            }
            log.info("Successfully get the result of recommendNextVideo");
            return result;
        } catch (Exception e){
            log.error("Failed to get the result of recommendNextVideo");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<String> generalRecommendations(int pageSize, int pageNum) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'generalRecommendations'");
    }

    @Override
    public List<String> recommendVideosForUser(AuthInfo auth, int pageSize, int pageNum) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recommendVideosForUser'");
    }

    @Override
    public List<Long> recommendFriends(AuthInfo auth, int pageSize, int pageNum) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recommendFriends'");
    }
    
}
