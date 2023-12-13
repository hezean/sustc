package io.sustc.service.impl;

import java.time.LocalDate;

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

import javax.sql.DataSource;

import java.security.Timestamp;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
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
        if(req.getName() == null || req.getName().equals("")) {
            req.setName("fuck" + System.currentTimeMillis());
        }
        if(req.getSex() == null){
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
            if(ps.executeQuery().next()){
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
        if(birthday == null)
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
        String sql = "SELECT * FROM users WHERE mid = ?";
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, mid);
            UserInfoResp resp = new UserInfoResp();
            resp.setMid(mid);
            resp.setName(ps.executeQuery().getString("name"));
            resp.setCoin(ps.executeQuery().getInt("coin"));
            //TODO: set following, follower, watched, liked, collected, posted
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

}
