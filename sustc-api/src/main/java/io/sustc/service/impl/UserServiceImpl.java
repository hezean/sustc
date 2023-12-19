package io.sustc.service.impl;

import java.time.LocalDate;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.sustc.dto.AuthInfo;
import io.sustc.dto.RegisterUserReq;
import io.sustc.dto.UserInfoResp;
import io.sustc.dto.RegisterUserReq.Gender;
import io.sustc.dto.UserRecord.Identity;
import io.sustc.service.UserService;
import io.sustc.service.impl.Tools.Authenticate;
import io.sustc.service.impl.Tools.ParseDate;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {
    @Autowired
    private DataSource dataSource;

    @Override
    public long register(RegisterUserReq req) {
        // check if req is valid
        if (req.getPassword() == null) {
            log.error("Password is null.");
            return -1;
        }
        if (req.getName() == null || req.getName().equals("")) {
            req.setName("fuck" + System.currentTimeMillis());
        }
        if (req.getSex() == null) {
            log.warn("Sex is null, set to UNKNOWN.");
            req.setSex(Gender.UNKNOWN);
        }

        try {
            Connection conn = dataSource.getConnection();
            String sql = "SELECT * FROM auth_info WHERE qq = ? OR wechat = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, req.getQq());
            ps.setString(2, req.getWechat());
            if (ps.executeQuery().next()) {
                log.error("This social media account have already used. Try to change another to sign up.");
                return -1;
            }
            sql = "SELECT * FROM users WHERE name = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, req.getName());
            if (ps.executeQuery().next()) {
                log.error("There's another person used this name, try to change one.");
                return -1;
            }
            long newUserId = createNewUser(req, conn);
            log.info("Successfully create new user: " + newUserId);
            return newUserId;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private long createNewUser(RegisterUserReq req, Connection conn) throws SQLException {

        long mid = System.currentTimeMillis();

        String user = "INSERT INTO users (mid, name, sex, birthday, level, sign, identity, coin) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
        String auth = "INSERT INTO auth_info (mid, password, qq, wechat) VALUES (?, ?, ?, ?);";

        PreparedStatement psuser = conn.prepareStatement(user);
        PreparedStatement psauth = conn.prepareStatement(auth);

        psuser.setLong(1, mid);
        psuser.setString(2, req.getName());
        psuser.setString(3, req.getSex().toString());
        LocalDate birthday = ParseDate.parseDate(req.getBirthday());
        if (birthday == null)
            birthday = LocalDate.now();
        psuser.setDate(4, Date.valueOf(birthday));
        psuser.setInt(5, 0);
        psuser.setString(7, "user");
        psuser.setString(6, null);
        psuser.setInt(8, 0);

        psauth.setLong(1, mid);
        psauth.setString(2, req.getPassword());
        psauth.setString(3, req.getQq());
        psauth.setString(4, req.getWechat());

        psuser.executeUpdate();
        psauth.executeUpdate();

        return mid;
    }

    @Override
    public boolean deleteAccount(AuthInfo auth, long mid) {
        try {
            Connection conn = dataSource.getConnection();
            Identity identity = Authenticate.authenticate(auth, conn);
            if (identity == null) {
                return false;
            } else {
                if (identity == Identity.USER && auth.getMid() != mid) {
                    log.error("Authentication failed: user can only delete his own account.");
                    return false;
                } else if (identity == Identity.SUPERUSER && auth.getMid() != mid
                        && Authenticate.checkIdentity(mid, conn) != Identity.USER) {
                    log.error("Authentication failed: superuser can't delete another superuser's account.");
                    return false;
                }
                String usersql = "DELETE FROM users WHERE mid = ?";
                String authsql = "DELETE FROM auth_info WHERE mid = ?";
                String relationsql = "DELETE FROM user_relationships WHERE followermid = ? OR followingmid = ?";
                String videosql = "DELETE FROM videos WHERE ownermid = ?";

                PreparedStatement userps = conn.prepareStatement(usersql);
                PreparedStatement authps = conn.prepareStatement(authsql);
                PreparedStatement relationps = conn.prepareStatement(relationsql);
                PreparedStatement videops = conn.prepareStatement(videosql);

                userps.setLong(1, mid);
                authps.setLong(1, mid);
                relationps.setLong(1, mid);
                relationps.setLong(2, mid);
                videops.setLong(1, mid);
                
                String disableSql = "SET session_replication_role = 'replica'";
                try (PreparedStatement disableStmt = conn.prepareStatement(disableSql)) {
                    disableStmt.execute();
                }
                userps.executeUpdate();
                authps.executeUpdate();
                relationps.executeUpdate();
                videops.executeUpdate();

                String enableSql = "SET session_replication_role = 'origin'";
                try (PreparedStatement enableStmt = conn.prepareStatement(enableSql)) {
                    enableStmt.execute();
                }
                log.info("Successfully delete user: " + mid);
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean follow(AuthInfo auth, long followeeMid) {

        try {
            Connection conn = dataSource.getConnection();
            Identity identity = Authenticate.authenticate(auth, conn);
            if (identity == null) {
                return false;
            } else {
                String sql = "SELECT * FROM user_relationships WHERE followermid = ? AND followingmid = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setLong(1, auth.getMid());
                ps.setLong(2, followeeMid);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String sql1 = "DELETE FROM user_relationships WHERE followermid = ? AND followingmid = ?";
                    PreparedStatement ps1 = conn.prepareStatement(sql1);
                    ps1.setLong(1, auth.getMid());
                    ps1.setLong(2, followeeMid);
                    ps1.executeUpdate();
                    log.info("Successfully unfollow user: " + followeeMid);
                    return true;
                } else {
                    String sql1 = "INSERT INTO user_relationships (followermid, followingmid) VALUES (?, ?)";
                    PreparedStatement ps1 = conn.prepareStatement(sql1);
                    ps1.setLong(1, auth.getMid());
                    ps1.setLong(2, followeeMid);
                    ps1.executeUpdate();
                    log.info("Successfully follow user: " + followeeMid);
                    return true;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public UserInfoResp getUserInfo(long mid) {
        String users = "SELECT * FROM users WHERE mid = ?";
        String inter_info = "SELECT * FROM user_video_interaction WHERE mid = ?";
        String videos = "SELECT * FROM videos WHERE ownermid = ?";
        String watches = "SELECT * FROM user_video_watch WHERE mid = ?";
        String follower = "SELECT * FROM user_relationships WHERE followermid = ?";
        String following = "SELECT * FROM user_relationships WHERE followingmid = ?";
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement usersps = conn.prepareStatement(users);
            PreparedStatement interps = conn.prepareStatement(inter_info);
            PreparedStatement videosps = conn.prepareStatement(videos);
            PreparedStatement watchesps = conn.prepareStatement(watches);
            PreparedStatement followerps = conn.prepareStatement(follower);
            PreparedStatement followingps = conn.prepareStatement(following);

            ArrayList<String> watched = new ArrayList<String>();
            ArrayList<String> liked = new ArrayList<String>();
            ArrayList<String> collected = new ArrayList<String>();
            ArrayList<String> posted = new ArrayList<String>();
            ArrayList<Long> followerList = new ArrayList<Long>();
            ArrayList<Long> followingList = new ArrayList<Long>();

            usersps.setLong(1, mid);
            ResultSet usersrs = usersps.executeQuery(); // Store the query result in a variable

            UserInfoResp resp = new UserInfoResp();
            resp.setMid(mid);
            if (usersrs.next())
                resp.setCoin(usersrs.getInt("coin"));

            interps.setLong(1, mid);
            ResultSet interrs = interps.executeQuery();

            while (interrs.next()) {
                String bv = interrs.getString("bv");
                Boolean isLiked = interrs.getBoolean("is_liked");
                Boolean isCollected = interrs.getBoolean("is_favorited");
                if (isLiked) {
                    liked.add(bv);
                }

                if (isCollected) {
                    collected.add(bv);
                }
            }
            resp.setLiked(liked.toArray(new String[liked.size()]));
            resp.setCollected(collected.toArray(new String[collected.size()]));

            videosps.setLong(1, mid);
            ResultSet videosrs = videosps.executeQuery();
            while (videosrs.next()) {
                String bv = videosrs.getString("bv");
                posted.add(bv);
            }
            resp.setPosted(posted.toArray(new String[posted.size()]));

            watchesps.setLong(1, mid);
            ResultSet watchesrs = watchesps.executeQuery();
            while (watchesrs.next()) {
                String bv = watchesrs.getString("bv");
                watched.add(bv);
            }
            resp.setWatched(watched.toArray(new String[posted.size()]));

            followerps.setLong(1, mid);
            ResultSet followerrs = followerps.executeQuery();
            while (followerrs.next()) {
                Long follower_id = followerrs.getLong("followingmid");
                followerList.add(follower_id);
            }
            long[] longArray = new long[followerList.size()];
            for (int i = 0; i < followerList.size(); i++) {
                longArray[i] = followerList.get(i);
            }
            resp.setFollower(longArray);

            followingps.setLong(1, mid);
            ResultSet followingrs = followingps.executeQuery();
            while (followingrs.next()) {
                Long following_id = followingrs.getLong("followermid");
                followingList.add(following_id);
            }
            long[] longArray2 = new long[followingList.size()];
            for (int i = 0; i < followingList.size(); i++) {
                longArray2[i] = followingList.get(i);
            }
            resp.setFollowing(longArray2);
            return resp;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
