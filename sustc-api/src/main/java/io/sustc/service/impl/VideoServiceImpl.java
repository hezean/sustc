package io.sustc.service.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

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
                    PostVideoReq oldreq = getVideoReq(auth, bv, conn);
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
                    String sql = "UPDATE videos SET title = ?, description = ?, duration = ?, committime = ?, ispublic = ? WHERE bv = ?;";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, req.getTitle());
                    ps.setString(2, req.getDescription());
                    ps.setFloat(3, req.getDuration());
                    ps.setTimestamp(4, req.getPublicTime());
                    ps.setString(5, bv);
                    ps.setBoolean(6, false);
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
                    Map<String, Integer> viewMap = new HashMap<>();
                    Map<String, Integer> countMap = new HashMap<>();

                    for (int i = 0; i < keyword.length; i++) {
                        titleps.setString(1, "%" + keyword[i] + "%");
                        descriptionps.setString(1, "%" + keyword[i] + "%");

                        ResultSet titlers = titleps.executeQuery();
                        while (titlers.next()) {
                            String bv = titlers.getString("bv");
                            int viewCount = getViewCount(bv, conn);
                            viewMap.put(bv, viewCount);
                            countMap.put(bv, countMap.getOrDefault(bv, 0) + 1);
                        }

                        ResultSet descriptionsrs = descriptionps.executeQuery();
                        while (descriptionsrs.next()) {
                            String bv = descriptionsrs.getString("bv");
                            int viewCount = getViewCount(bv, conn);
                            viewMap.put(bv, viewCount);
                            countMap.put(bv, countMap.getOrDefault(bv, 0) + 1);
                        }
                    }

                    // sort by count, then by view count
                    List<Map.Entry<String, Integer>> list = new ArrayList<>(countMap.entrySet());
                    Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
                        @Override
                        public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                            int countCompare = o2.getValue().compareTo(o1.getValue());
                            if (countCompare == 0) {
                                return viewMap.get(o2.getKey()).compareTo(viewMap.get(o1.getKey()));
                            }
                            return countCompare;
                        }
                    });

                    List<String> result = new ArrayList<>();
                    for (Map.Entry<String, Integer> entry : list) {
                        result.add(entry.getKey());
                    }
                    // caculate the pages
                    int totalPage = result.size() / pageSize;
                    if (result.size() % pageSize != 0) {
                        totalPage++;
                    }
                    if (pageNum > totalPage || pageNum < 1) {
                        log.error("Search video failed: page number out of range");
                        return null;
                    }
                    int start = (pageNum - 1) * pageSize;
                    int end = pageNum * pageSize;
                    if (end > result.size()) {
                        end = result.size();
                    }
                    log.info("Successfully search video: {}", result.subList(start, end));
                    return result.subList(start, end);
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
        try {
            Connection conn = dataSource.getConnection();
            if (!checkVideoExists(bv, conn)) {
                log.error("Get average view rate failed: bv not found");
                return -1;
            }
            String sql = "SELECT * FROM user_video_watch WHERE bv = ?;";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, bv);
            ResultSet rs = ps.executeQuery();
            int count = 0;
            double sum = 0;
            while (rs.next()) {
                count++;
                sum += rs.getDouble("watch_time");
            }
            if (count == 0) {
                log.error("Get average view rate failed: bv not found");
                return -1;
            }
            sql = "SELECT * FROM videos WHERE bv = ?;";
            ps = conn.prepareStatement(sql);
            ps.setString(1, bv);
            rs = ps.executeQuery();
            rs.next();
            double duration = rs.getDouble("duration");
            log.info("Successfully get average view rate: {}", sum / (duration * count));
            return sum / (duration * count);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public Set<Integer> getHotspot(String bv) {
        try {
            Connection conn = dataSource.getConnection();
            if (!checkVideoExists(bv, conn)) {
                log.error("Get hotspot failed: bv not found");
                return null;
            }

            String video = "SELECT duration FROM videos WHERE bv = ?;";
            PreparedStatement videops = conn.prepareStatement(video);
            videops.setString(1, bv);
            ResultSet videors = videops.executeQuery();
            videors.next();
            int duration = (int) videors.getFloat("duration");

            String danmu = "SELECT time FROM danmus WHERE bv = ?;";
            PreparedStatement danmups = conn.prepareStatement(danmu);
            danmups.setString(1, bv);
            ArrayList<Integer> Scores = new ArrayList<>(duration / 10 + 1);
            for (int i = 0; i < duration / 10 + 1; i++) {
                Scores.add(0);
            }
            ResultSet danmurs = danmups.executeQuery();

            while (danmurs.next()) {
                Float time = danmurs.getFloat("time");
                Scores.set((int) (time / 10), Scores.get((int) (time / 10)) + 1);
            }
            // find the max value in Scores
            int max = 0;
            for (int i = 0; i < Scores.size(); i++) {
                if (Scores.get(i) > max) {
                    max = i;
                }
            }
            if (max == 0 && Scores.get(0) == 0) {
                log.error("Get hotspot failed: no danmu");
                return null;
            }

            Set<Integer> result = new HashSet<>();
            result.add(max * 10);
            result.add(max * 10 + 10);
            log.info("Successfully get hotspot: {}", result);
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean reviewVideo(AuthInfo auth, String bv) {
        try {
            Connection conn = dataSource.getConnection();
            Identity identity = Authenticate.authenticate(auth, conn);
            if (identity != Identity.SUPERUSER) {
                log.error("Review video failed: permission denied");
                return false;
            }
            if (!checkVideoExists(bv, conn)) {
                log.error("Review video failed: bv not found");
                return false;
            }
            if (isUserVideoOwner(auth, bv, conn)) {
                log.error("Review video failed: user is the owner of the video");
                return false;
            }
            String sql = "SELECT * FROM videos WHERE bv = ?;";
            PreparedStatement checkps = conn.prepareStatement(sql);
            checkps.setString(1, bv);
            ResultSet rs = checkps.executeQuery();
            if (rs.getBoolean("ispublic")) {
                log.error("Review video failed: video has been reviewed");
                return false;
            }

            sql = "UPDATE videos SET ispublic = true, reviewtime = now() WHERE bv = ?;";
            PreparedStatement updateps = conn.prepareStatement(sql);
            updateps.setString(1, bv);
            updateps.executeUpdate();
            log.info("Successfully review video: {}", bv);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;

        }
    }

    @Override
    public boolean coinVideo(AuthInfo auth, String bv) {
        try {
            Connection conn = dataSource.getConnection();
            Identity identity = Authenticate.authenticate(auth, conn);
            if (identity == null || !checkVideoExists(bv, conn)) {
                log.error("Coin video failed: bv not found or user not authenticated");
                return false;
            }
            if (isUserVideoOwner(auth, bv, conn)) {
                log.error("Coin video failed: user is the owner of the video");
                return false;
            }
            if (canUserViewVideo(auth, bv, conn)) {
                log.error("Coin video failed: user cannot view the video");
                return false;
            }

            Boolean isCoined = getUserVideoInteractionStatus(auth, bv, "is_coined", conn);
            if (isCoined != null) {
                updateUserVideoInteraction(auth, bv, "is_coined", !isCoined, conn);
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
            if (identity == null || !checkVideoExists(bv, conn)) {
                log.error("Like video failed: bv not found or user not authenticated");
                return false;
            }
            if (isUserVideoOwner(auth, bv, conn)) {
                log.error("Like video failed: user is the owner of the video");
                return false;
            }
            if (canUserViewVideo(auth, bv, conn)) {
                log.error("Like video failed: user cannot view the video");
                return false;
            }

            Boolean isLiked = getUserVideoInteractionStatus(auth, bv, "is_liked", conn);
            if (isLiked != null) {
                updateUserVideoInteraction(auth, bv, "is_liked", !isLiked, conn);
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
            if (identity == null || !checkVideoExists(bv, conn)) {
                log.error("Collect video failed: bv not found or user not authenticated");
                return false;
            }
            if (isUserVideoOwner(auth, bv, conn)) {
                log.error("Collect video failed: user is the owner of the video");
                return false;
            }
            if (canUserViewVideo(auth, bv, conn)) {
                log.error("Collect video failed: user cannot view the video");
                return false;
            }

            Boolean isFavorited = getUserVideoInteractionStatus(auth, bv, "is_favorited", conn);
            if (isFavorited != null) {
                updateUserVideoInteraction(auth, bv, "is_favorited", !isFavorited, conn);
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

    private boolean checkVideoExists(String bv, Connection conn) throws SQLException {
        String sql = "SELECT * FROM videos WHERE bv = ?;";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bv);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Boolean getUserVideoInteractionStatus(AuthInfo auth, String bv, String column, Connection conn)
            throws SQLException {
        String sql = "SELECT * FROM user_video_interaction WHERE bv = ? AND mid = ?;";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
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

    private void updateUserVideoInteraction(AuthInfo auth, String bv, String column, boolean status, Connection conn)
            throws SQLException {
        String sql = "UPDATE user_video_interaction SET " + column + " = ? WHERE bv = ? AND mid = ?;";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, status);
            ps.setString(2, bv);
            ps.setLong(3, auth.getMid());
            ps.executeUpdate();
        }
    }

    private PostVideoReq getVideoReq(AuthInfo auth, String bv, Connection conn) throws SQLException {
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

    private boolean isUserVideoOwner(AuthInfo auth, String bv, Connection conn) throws SQLException {
        String sql = "SELECT ownermid FROM videos WHERE bv = ?;";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bv);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("ownermid") == auth.getMid();
                }
                return false;
            }
        }
    }

    private boolean canUserViewVideo(AuthInfo auth, String bv, Connection conn) throws SQLException {
        String sql = "SELECT * FROM videos WHERE bv = ?;";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            Identity identity = Authenticate.authenticate(auth, conn);
            ps.setString(1, bv);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // get public time
                    Timestamp publicTime = rs.getTimestamp("committime");
                    if (System.currentTimeMillis() < publicTime.getTime()) {
                        return identity == Identity.SUPERUSER || isUserVideoOwner(auth, bv, conn);
                    }

                    return rs.getBoolean("ispublic") || identity == Identity.SUPERUSER;
                }
                return false;
            }
        }
    }

    private int getViewCount(String bv, Connection conn) throws SQLException {
        String sql = "SELECT * FROM user_video_watch WHERE bv = ? GROUP BY bv;";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, bv);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("count");
            }
            return 0;
        }
    }
}
