package io.sustc.service.impl;

import java.util.List;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zaxxer.hikari.HikariDataSource;

import io.sustc.dto.AuthInfo;
import io.sustc.service.DanmuService;

@Service
public class DanmuServiceImpl implements DanmuService {
    @Autowired
    private DataSource dataSource = new HikariDataSource();

    @Override
    public long sendDanmu(AuthInfo auth, String bv, String content, float time) {
        try{
            Connection conn = dataSource.getConnection();
            if(auth == null || Authenticate.authenticate(auth, conn) == null){
                return -1;
            }else{
                String sql = "INSERT INTO danmus (bv, mid, content, time, posttime) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, bv);
                ps.setLong(2, auth.getMid());
                ps.setString(3, content);
                ps.setFloat(4, time);
                ps.setTimestamp(5, new java.sql.Timestamp(System.currentTimeMillis()));
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if(rs.next()){
                    long id = rs.getLong(1);
                    rs.close();
                    ps.close();
                    conn.close();
                    return id;
                }
                rs.close();
                ps.close();
                conn.close();
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public List<Long> displayDanmu(String bv, float timeStart, float timeEnd, boolean filter) {
        try{
            Connection conn = dataSource.getConnection();
            String sql = "SELECT * FROM danmus WHERE bv = ? AND time >= ? AND time <= ?";
            if(filter){
                sql += " GROUP BY content";
            }
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, bv);
            ps.setFloat(2, timeStart);
            ps.setFloat(3, timeEnd);
            ResultSet rs = ps.executeQuery();
            List<Long> list = new java.util.ArrayList<>();
            while(rs.next()){
                list.add(rs.getLong("id"));
            }
            rs.close();
            ps.close();
            conn.close();
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean likeDanmu(AuthInfo auth, long id) {
        try{
            Connection conn = dataSource.getConnection();
            if(auth == null || Authenticate.authenticate(auth, conn) == null){
                return false;
            }else{
                String sql = "INSERT INTO danmu_like (mid, danmuid) VALUES (?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setLong(1, auth.getMid());
                ps.setLong(2, id);
                ps.executeUpdate();
                ps.close();
                conn.close();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
