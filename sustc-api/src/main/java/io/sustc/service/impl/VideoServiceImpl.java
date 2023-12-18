package io.sustc.service.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;
import java.sql.Connection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.sustc.dto.AuthInfo;
import io.sustc.dto.PostVideoReq;
import io.sustc.dto.UserRecord.Identity;
import io.sustc.service.VideoService;
import io.sustc.service.impl.Tools.Authenticate;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class VideoServiceImpl implements VideoService {

    @Autowired
    private DataSource dataSource;

    @Override
    public String postVideo(AuthInfo auth, PostVideoReq req) {
        try {
            if (Authenticate.videoAuthenticate(req, auth, dataSource.getConnection())) {
                // generate an uuid by using UUID.randomUUID().toString()
                String bv = UUID.randomUUID().toString();
                String sql = "INSERT INTO videos (bv, ownermid, title, description, duration, committime, ispublic) VALUES (?, ?, ?, ?, ?, ?, ?);";
                PreparedStatement ps = dataSource.getConnection().prepareStatement(sql);
                ps.setString(1, bv);
                ps.setLong(2, auth.getMid());
                ps.setString(3, req.getTitle());
                ps.setString(4, req.getDescription());
                ps.setFloat(5, req.getDuration());
                ps.setTimestamp(6, req.getPublicTime());
                ps.setBoolean(7, false);
                ps.executeUpdate();
                log.info("Successfully post video: {}", bv);
                return bv;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean deleteVideo(AuthInfo auth, String bv) {
        try {
            Connection conn = dataSource.getConnection();
            Identity identity = Authenticate.authenticate(auth, conn);
            if (identity == null)
                return false;
            String sql = "SELECT ownermid FROM videos WHERE bv = ?;";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, bv);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                log.error("Delete video failed: bv not found");
                return false;
            }
            if (rs.getLong("ownermid") == auth.getMid() || identity == Identity.SUPERUSER) {
                sql = "DELETE FROM videos WHERE bv = ?;";
                ps = conn.prepareStatement(sql);
                ps.setString(1, bv);
                ps.executeUpdate();
                log.info("Successfully delete video: {}", bv);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateVideoInfo(AuthInfo auth, String bv, PostVideoReq req) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateVideoInfo'");
    }

    @Override
    public List<String> searchVideo(AuthInfo auth, String keywords, int pageSize, int pageNum) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'searchVideo'");
    }

    @Override
    public double getAverageViewRate(String bv) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAverageViewRate'");
    }

    @Override
    public Set<Integer> getHotspot(String bv) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getHotspot'");
    }

    @Override
    public boolean reviewVideo(AuthInfo auth, String bv) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'reviewVideo'");
    }

    @Override
    public boolean coinVideo(AuthInfo auth, String bv) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'coinVideo'");
    }

    @Override
    public boolean likeVideo(AuthInfo auth, String bv) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'likeVideo'");
    }

    @Override
    public boolean collectVideo(AuthInfo auth, String bv) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'collectVideo'");
    }

}
