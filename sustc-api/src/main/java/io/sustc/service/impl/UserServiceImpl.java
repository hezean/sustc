package io.sustc.service.impl;

import java.time.LocalDate;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import com.zaxxer.hikari.HikariDataSource;

import io.sustc.dto.AuthInfo;
import io.sustc.dto.RegisterUserReq;
import io.sustc.dto.UserInfoResp;
import io.sustc.dto.RegisterUserReq.Gender;
import io.sustc.service.UserService;

import io.sustc.service.impl.ParseDate;
import lombok.extern.slf4j.Slf4j;

import javax.management.Query;
import javax.sql.DataSource;

import java.security.Timestamp;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private DataSource dataSource = new HikariDataSource();

    @Override
    public long register(RegisterUserReq req) {
        // check if req is valid
        if (req.getPassword() == null) {
            return -1;
        }
        if (req.getName() == null || req.getName().equals("")) {
            req.setName("fuck" + System.currentTimeMillis());
        }
        if (req.getSex() == null) {
            req.setSex(Gender.UNKNOWN);
        }

        try {
            Connection conn = dataSource.getConnection();
            String sql = "SELECT * FROM auth_info WHERE qq = ? OR wechat = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, req.getQq());
            ps.setString(2, req.getWechat());
            if (ps.executeQuery().next()) {
                return -1;
            }
            sql = "SELECT * FROM users WHERE name = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, req.getName());
            if (ps.executeQuery().next()) {
                return -1;
            }
            long newUserId = createNewUser(req, conn);
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteAccount'");
    }

    @Override
    public boolean follow(AuthInfo auth, long followeeMid) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'follow'");
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
            if(usersrs.next())
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

}
