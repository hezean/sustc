package io.sustc.service.impl.Tools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import io.sustc.dto.AuthInfo;
import io.sustc.dto.PostVideoReq;
import io.sustc.dto.UserRecord.Identity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Authenticate {
    public static Identity authenticate(AuthInfo auth, Connection conn) throws SQLException {
        String midsql = "SELECT * FROM auth_info WHERE mid = ? or qq = ? or wechat = ?";
        PreparedStatement ps = conn.prepareStatement(midsql);
        ps.setLong(1, auth.getMid());
        ps.setString(2, auth.getQq());
        ps.setString(3, auth.getWechat());
        ResultSet rs = ps.executeQuery();

        if(!rs.next()){
            //log.error("Authentication failed: mid not found in auth_info");
            return null;
        }
        if (auth.getQq() != null && !auth.getQq().equals(rs.getString("qq"))) {
            //log.error("Authentication failed: qq not match");
            return null;
        }
        if (auth.getWechat() != null && !auth.getWechat().equals(rs.getString("wechat"))) {
            //log.error("Authentication failed: wechat not match");
            return null;
        }
        long mid = getMid(auth, conn);
        return checkIdentity(mid, conn);
        // return null;
    }

    public static Identity checkIdentity(long mid, Connection conn) throws SQLException {
        String sql = "SELECT * FROM users WHERE mid = ?;";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setLong(1, mid);
        ResultSet rs = ps.executeQuery();
        if (!rs.next()) {
            //log.error("Authentication failed: could not found user");
            return null;
        }
        //log.info("Authentication success: mid {} is {}", mid, rs.getString("identity").toUpperCase());
        return Identity.valueOf(rs.getString("identity").toUpperCase());
    }

    public static int videoAuthenticate(PostVideoReq req, AuthInfo auth, Connection conn) throws SQLException {
        if (authenticate(auth, conn) == null || req.getTitle() == null || req.getTitle() == "") {
            log.error("Authentication failed: mid not found in auth_info");
            return -1;
        } else if (req.getDuration() <= 10) {
            log.error("Authentication failed: duration is too short");
            return -1;
        } else if (req.getPublicTime() != null && req.getPublicTime().getTime() < System.currentTimeMillis()) {
            log.error("Authentication failed: publicTime is earlier than now");
            return -1;
        } else {
            String sql = "SELECT * FROM videos WHERE ownermid = ? AND title = ?;";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, auth.getMid());
            ps.setString(2, req.getTitle());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                log.info("Authentication success with duplicate title");
                return 1;
            }
            log.info("Authentication success with new title");
            return 0;
        }
    }
    public static long getMid(AuthInfo auth, Connection conn){
        try{
            String pattern = "";
            if(auth.getMid() != 0){
                return auth.getMid();
            }else if(auth.getQq() != null){
                pattern = auth.getQq();
            }else if(auth.getWechat() != null){
                pattern = auth.getWechat();
            }else{
                return -1;
            }
            String sql = "SELECT * FROM auth_info WHERE qq = ? OR wechat = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong("mid");
            } else {
                return -1;
            }
        }catch(Exception e){
            e.printStackTrace();
            return -1;
        }
    }
}
