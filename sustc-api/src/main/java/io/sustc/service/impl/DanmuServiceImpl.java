package io.sustc.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.sustc.dto.AuthInfo;
import io.sustc.service.DanmuService;
import io.sustc.service.impl.Tools.Authenticate;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class DanmuServiceImpl implements DanmuService {
    @Autowired
    private DataSource dataSource;

    @Override
    public long sendDanmu(AuthInfo auth, String bv, String content, float time) {
        try (Connection conn = dataSource.getConnection();) {

            if (auth == null || Authenticate.authenticate(auth, conn) == null) {
                return -1;
            } else {
                auth.setMid(Authenticate.getMid(auth, conn));
                String isPublic = "SELECT * FROM videos WHERE bv = ? AND ispublic = true";
                PreparedStatement ps1 = conn.prepareStatement(isPublic);
                ps1.setString(1, bv);
                ResultSet rs1 = ps1.executeQuery();
                if (!rs1.next()) {
                    log.error("Cannot find video or video is not public: {}", bv);
                    return -1;
                }
                String videoduration = "SELECT duration FROM videos WHERE bv = ?";
                PreparedStatement ps2 = conn.prepareStatement(videoduration);
                ps2.setString(1, bv);
                ResultSet rs2 = ps2.executeQuery();
                if (rs2.next() && time > rs2.getFloat("duration")) {
                    log.error("Time out of range: {}", time);
                    return -1;
                }
                String sql = "INSERT INTO danmus (bv, mid, content, time, posttime) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, bv);
                ps.setLong(2, auth.getMid());
                ps.setString(3, content);
                ps.setFloat(4, time);
                ps.setTimestamp(5, new java.sql.Timestamp(System.currentTimeMillis()));
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    long id = rs.getLong(1);
                    rs.close();
                    ps.close();
                    conn.close();
                    log.info("Successfully send danmu: {}", id);
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
        try (Connection conn = dataSource.getConnection();) {
            String sql;
            if (filter) {
                sql = "SELECT MIN(id) as id FROM danmus WHERE bv = ? AND time >= ? AND time <= ? GROUP BY content";
            } else {
                sql = "SELECT id FROM danmus WHERE bv = ? AND time >= ? AND time <= ?";
            }
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, bv);
            ps.setFloat(2, timeStart);
            ps.setFloat(3, timeEnd);
            ResultSet rs = ps.executeQuery();
            List<Long> list = new ArrayList<>();
            while (rs.next()) {
                list.add(rs.getLong("id"));
            }
            log.info("Successfully display danmu: {} were found", list.size());
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean likeDanmu(AuthInfo auth, long id) {
        try (Connection conn = dataSource.getConnection();) {

            if (auth == null || Authenticate.authenticate(auth, conn) == null) {
                return false;
            } else {
                auth.setMid(Authenticate.getMid(auth, conn));
                String sql = "INSERT INTO danmu_like (mid, danmuid) VALUES (?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setLong(1, auth.getMid());
                ps.setLong(2, id);
                ps.executeUpdate();
                ps.close();
                conn.close();
                log.info("Successfully like danmu: {}", id);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
