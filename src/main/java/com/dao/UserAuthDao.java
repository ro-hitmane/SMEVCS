package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import com.bean.User;
import com.connection.DBConnection;
import com.util.SecurityUtil;

public class UserAuthDao {
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_TIME_MS = 15 * 60 * 1000; // 15 minutes lock

    public static class AuthResult {
        public boolean success;
        public String errorMessage;
        public User user; // Contains name, email, vehicle_no for the session
    }

    public AuthResult authenticateUser(String email, String enteredPassword) {
        AuthResult result = new AuthResult();
        result.success = false;
        result.user = null;

        // Assuming your table is named 'tbl_user' based on standard naming
        String sql = "SELECT name, email, vehicle_no, password, failed_attempts, account_locked_until "
                   + "FROM tbl_user WHERE LOWER(TRIM(email)) = LOWER(TRIM(?))";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    int failedAttempts = rs.getInt("failed_attempts");
                    Timestamp lockedUntil = rs.getTimestamp("account_locked_until");

                    // 1. Enforce Account Lockout
                    if (lockedUntil != null && lockedUntil.getTime() > System.currentTimeMillis()) {
                        long minsLeft = (lockedUntil.getTime() - System.currentTimeMillis()) / 60000;
                        result.errorMessage = "Account locked due to multiple failed attempts. Try again in " + minsLeft + " minutes.";
                        return result;
                    }

                    // 2. Verify Cryptographic Password
                    if (SecurityUtil.verifyPassword(enteredPassword, storedHash)) {
                        // Success: Reset failed attempts and populate User bean
                        resetFailedAttempts(email, con);
                        
                        User u = new User();
                        u.setName(rs.getString("name"));
                        u.setEmail(rs.getString("email"));
                        u.setVehicle_no(rs.getString("vehicle_no"));
                        u.setPassword(storedHash); // Don't store plaintext in bean
                        
                        result.success = true;
                        result.user = u;
                    } else {
                        // Failure: Increment attempts and lock if threshold reached
                        handleFailedAttempt(email, failedAttempts + 1, con);
                        result.errorMessage = "Invalid email or password.";
                    }
                } else {
                    // Prevent username enumeration by returning the exact same generic error
                    result.errorMessage = "Invalid email or password."; 
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.errorMessage = "System error during authentication.";
        }
        return result;
    }

    private void handleFailedAttempt(String email, int newFailCount, Connection con) {
        try {
            if (newFailCount >= MAX_FAILED_ATTEMPTS) {
                Timestamp lockTime = new Timestamp(System.currentTimeMillis() + LOCK_TIME_MS);
                String sql = "UPDATE tbl_user SET failed_attempts = ?, account_locked_until = ? WHERE LOWER(email) = LOWER(?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, newFailCount);
                    ps.setTimestamp(2, lockTime);
                    ps.setString(3, email);
                    ps.executeUpdate();
                }
            } else {
                String sql = "UPDATE tbl_user SET failed_attempts = ? WHERE LOWER(email) = LOWER(?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, newFailCount);
                    ps.setString(2, email);
                    ps.executeUpdate();
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void resetFailedAttempts(String email, Connection con) {
        String sql = "UPDATE tbl_user SET failed_attempts = 0, account_locked_until = NULL WHERE LOWER(email) = LOWER(?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }
}