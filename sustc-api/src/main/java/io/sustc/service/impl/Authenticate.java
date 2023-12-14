package io.sustc.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import io.sustc.dto.AuthInfo;
import io.sustc.dto.PostVideoReq;
import io.sustc.dto.UserRecord.Identity;

public class Authenticate {
    public static Identity authenticate(AuthInfo auth, Connection conn) throws SQLException {
        String midsql = "SELECT * FROM auth_info WHERE mid = ?";
        PreparedStatement ps = conn.prepareStatement(midsql);
        ps.setLong(1, auth.getMid());
        ResultSet rs = ps.executeQuery();

        if (!rs.next()) {
            return null;
        }else if(rs.getString("password").equals(auth.getPassword())){
            if(auth.getQq() != null && !auth.getQq().equals(rs.getString("qq"))){
                return null;
            }
            if(auth.getWechat() != null && !auth.getWechat().equals(rs.getString("wechat"))){
                return null;
            }
            return checkIdentity(auth.getMid(), conn);
        }
        return null;
    }

    public static Identity checkIdentity(long mid, Connection conn) throws SQLException {
        String sql = "SELECT * FROM users WHERE mid = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setLong(1, mid);
        ResultSet rs = ps.executeQuery();
        if (!rs.next()) {
            return null;
        }
        return Identity.valueOf(rs.getString("identity").toUpperCase());
    }

    public static boolean videoAuthenticate(PostVideoReq req,AuthInfo auth, Connection conn) throws SQLException {
        if(authenticate(auth, conn) == null||req.getTitle() == null||req.getTitle() ==""){
            return false;
        }else if (req.getDuration() <= 10){
            return false;
        }else if (req.getPublicTime().getTime() < System.currentTimeMillis()){
            return false;
        }else {
            String sql = "SELECT * FROM videos WHERE ownermid = ? AND title = ?;";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, auth.getMid());
            ps.setString(2, req.getTitle());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return false;
            }
            return true;
        }
    }
}
