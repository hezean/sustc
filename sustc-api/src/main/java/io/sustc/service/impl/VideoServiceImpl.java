package io.sustc.service.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.sql.DataSource;
import java.sql.Connection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.sustc.dto.AuthInfo;
import io.sustc.dto.PostVideoReq;
import io.sustc.dto.UserRecord.Identity;
import io.sustc.service.VideoService;
import io.sustc.service.impl.Tools.Authenticate;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class VideoServiceImpl implements VideoService {

    @Autowired
    private DataSource dataSource;

    @Override
    public String postVideo(AuthInfo auth, PostVideoReq req) {
        try {
            if (Authenticate.videoAuthenticate(req, auth, dataSource.getConnection()) == 0) {
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
        try {
            Connection conn = dataSource.getConnection();
            if (Authenticate.videoAuthenticate(req, auth, conn) == 0) {
                try {
                    PostVideoReq oldreq = getVideoReq(auth, bv);
                    if (oldreq == null) {
                        log.error("Update video failed: bv not found");
                        return false;
                    }
                    if (oldreq.getDuration() != req.getDuration()) {
                        log.error("Update video failed: duration cannot be changed");
                        return false;
                    } else if (oldreq.getDescription() == req.getDescription() && oldreq.getTitle() == req.getTitle()
                            && oldreq.getPublicTime() == req.getPublicTime()) {
                        log.error("Update video failed: no change");
                        return false;
                    }
                    String sql = "UPDATE videos SET title = ?, description = ?, duration = ?, committime = ? WHERE bv = ?;";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, req.getTitle());
                    ps.setString(2, req.getDescription());
                    ps.setFloat(3, req.getDuration());
                    ps.setTimestamp(4, req.getPublicTime());
                    ps.setString(5, bv);
                    ps.executeUpdate();
                    log.info("Successfully update video: {}", bv);
                    return true;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                log.error("Update video failed: authentication failed");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<String> searchVideo(AuthInfo auth, String keywords, int pageSize, int pageNum) {
        Connection conn;
        try {
            conn = dataSource.getConnection();
            if (keywords == null || keywords == "") {
                log.error("Search video failed: keywords is null");
                return null;
            } else if (Authenticate.authenticate(auth, conn) == null) {
                log.error("Search video failed: authentication failed");
                return null;
            } else {
                try {
                    // split keywords by space
                    String[] keyword = keywords.split(" ");
                    String titlesql = "SELECT bv FROM videos WHERE title LIKE ?;";
                    String descriptionsql = "SELECT bv FROM videos WHERE description LIKE ?;";
                    PreparedStatement titleps = conn.prepareStatement(titlesql);
                    PreparedStatement descriptionps = conn.prepareStatement(descriptionsql);
                    List<String> bvlist = new ArrayList<String>();
                    for (int i = 0; i < keyword.length; i++) {
                        titleps.setString(1, "%" + keyword[i] + "%");
                        ResultSet rs1 = titleps.executeQuery();
                        while (rs1.next()) {
                            bvlist.add(rs1.getString("bv"));
                        }
                        descriptionps.setString(1, "%" + keyword[i] + "%");
                        ResultSet rs2 = descriptionps.executeQuery();
                        while (rs2.next()) {
                            bvlist.add(rs2.getString("bv"));
                        }
                    }
                    for (int i = 0; i < keyword.length; i++) {
                        descriptionps.setString(1, "%" + keyword[i] + "%");
                        ResultSet rs = descriptionps.executeQuery();
                        while (rs.next()) {
                            bvlist.add(rs.getString("bv"));
                        }
                    }
                    List<String> sortedBvList = bvlist.stream()
                            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting())) // 计算每个元素的出现次数
                            .entrySet().stream()
                            .sorted(Map.Entry.<String, Long>comparingByValue().reversed()) // 根据出现次数进行排序
                            .map(Map.Entry::getKey) // 获取元素
                            .collect(Collectors.toList()); // 转换为 List
                    log.info("Successfully search video: {}", bvlist);
                    return sortedBvList;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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
        try{
            Connection conn = dataSource.getConnection();
            Identity identity = Authenticate.authenticate(auth, conn);
            if (identity == null || !checkVideoExists(bv)) {
                log.error("Coin video failed: bv not found or user not authenticated");
                return false;
            }
            if (isUserVideoOwner(auth, bv)) {
                log.error("Coin video failed: user is the owner of the video");
                return false;
            }

            Boolean isCoined = getUserVideoInteractionStatus(auth, bv, "is_coined");
            if (isCoined != null) {
                updateUserVideoInteraction(auth, bv, "is_coined", !isCoined);
            } else {
                String sql = "INSERT INTO user_video_interaction (mid, bv, is_favorited, is_coined, is_liked) VALUES (?, ?, ?, ?, ?);";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setLong(1, auth.getMid());
                ps.setString(2, bv);
                ps.setBoolean(3, false);
                ps.setBoolean(4, true);
                ps.setBoolean(5, false);
                ps.executeUpdate();
            }
            log.info("Successfully coin video: {}", bv);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean likeVideo(AuthInfo auth, String bv) {
        try {
            Connection conn = dataSource.getConnection();
            Identity identity = Authenticate.authenticate(auth, conn);
            if (identity == null || !checkVideoExists(bv)) {
                log.error("Like video failed: bv not found or user not authenticated");
                return false;
            }
            if (isUserVideoOwner(auth, bv)) {
                log.error("Like video failed: user is the owner of the video");
                return false;
            }

            Boolean isLiked = getUserVideoInteractionStatus(auth, bv, "is_liked");
            if (isLiked != null) {
                updateUserVideoInteraction(auth, bv, "is_liked", !isLiked);
            } else {
                String sql = "INSERT INTO user_video_interaction (mid, bv, is_favorited, is_coined, is_liked) VALUES (?, ?, ?, ?, ?);";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setLong(1, auth.getMid());
                ps.setString(2, bv);
                ps.setBoolean(3, false);
                ps.setBoolean(4, false);
                ps.setBoolean(5, true);
                ps.executeUpdate();
            }
            log.info("Successfully like video: {}", bv);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean collectVideo(AuthInfo auth, String bv) {
        try {
            Connection conn = dataSource.getConnection();
            Identity identity = Authenticate.authenticate(auth, conn);
            if (identity == null || !checkVideoExists(bv)) {
                log.error("Collect video failed: bv not found or user not authenticated");
                return false;
            }
            if (isUserVideoOwner(auth, bv)) {
                log.error("Collect video failed: user is the owner of the video");
                return false;
            }

            Boolean isFavorited = getUserVideoInteractionStatus(auth, bv, "is_favorited");
            if (isFavorited != null) {
                updateUserVideoInteraction(auth, bv, "is_favorited", !isFavorited);
            } else {
                String sql = "INSERT INTO user_video_interaction (mid, bv, is_favorited, is_coined, is_liked) VALUES (?, ?, ?, ?, ?);";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setLong(1, auth.getMid());
                ps.setString(2, bv);
                ps.setBoolean(3, true);
                ps.setBoolean(4, false);
                ps.setBoolean(5, false);
                ps.executeUpdate();
            }
            log.info("Successfully collect video: {}", bv);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Boolean checkVideoExists(String bv) throws SQLException {
        String sql = "SELECT * FROM videos WHERE bv = ?;";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bv);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Boolean getUserVideoInteractionStatus(AuthInfo auth, String bv, String column) throws SQLException {
        String sql = "SELECT * FROM user_video_interaction WHERE bv = ? AND mid = ?;";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bv);
            ps.setLong(2, auth.getMid());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(column);
                } else {
                    return null;
                }
            }
        }
    }

    private void updateUserVideoInteraction(AuthInfo auth, String bv, String column, boolean status)
            throws SQLException {
        String sql = "UPDATE user_video_interaction SET " + column + " = ? WHERE bv = ? AND mid = ?;";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, status);
            ps.setString(2, bv);
            ps.setLong(3, auth.getMid());
            ps.executeUpdate();
        }
    }

    private PostVideoReq getVideoReq(AuthInfo auth, String bv) throws SQLException {
        Connection conn = dataSource.getConnection();
        String sql = "SELECT * FROM videos WHERE bv = ?;";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, bv);
        ResultSet rs = ps.executeQuery();
        if (!rs.next()) {
            log.error("Get video info failed: bv not found");
            return null;
        }
        PostVideoReq req = new PostVideoReq();
        req.setTitle(rs.getString("title"));
        req.setDescription(rs.getString("description"));
        req.setDuration(rs.getFloat("duration"));
        req.setPublicTime(rs.getTimestamp("committime"));
        return req;
    }

    private boolean isUserVideoOwner(AuthInfo auth, String bv) throws SQLException {
        String sql = "SELECT ownermid FROM videos WHERE bv = ?;";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bv);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("ownermid") == auth.getMid();
                }
                return false;
            }
        }
    }

}
