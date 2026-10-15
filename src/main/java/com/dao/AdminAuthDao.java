package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.connection.DBConnection;
import com.util.SecurityUtil;

public class AdminAuthDao {

    public static class AuthResult {
        public boolean success;
        public String errorMessage;
        public String adminEmail;
    }

    public AuthResult authenticateAdmin(String email, String enteredPassword) {
        AuthResult result = new AuthResult();
        result.success = false;

        String sql = "SELECT email, password FROM tbl_admin WHERE LOWER(TRIM(email)) = LOWER(TRIM(?))";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    
                    if (SecurityUtil.verifyPassword(enteredPassword, storedHash)) {
                        result.success = true;
                        result.adminEmail = rs.getString("email");
                    } else {
                        result.errorMessage = "Invalid Admin email or password.";
                    }
                } else {
                    result.errorMessage = "Invalid Admin email or password.";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.errorMessage = "System error during admin authentication.";
        }
        return result;
    }
}