package com.university.sms.dao;

import com.university.sms.db.DBConnection;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HexFormat;

/** Verifies admin login credentials against the admins table. */
public class AdminDAO {

    /** Returns true if the username exists and the password matches its stored salted hash. */
    public boolean authenticate(String username, char[] password) throws SQLException {
        String sql = "SELECT password_hash, password_salt FROM admins WHERE username = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                String storedHash = rs.getString("password_hash");
                byte[] salt = HexFormat.of().parseHex(rs.getString("password_salt"));
                return storedHash.equals(hash(password, salt));
            }
        }
    }

    private static String hash(char[] password, byte[] salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            byte[] hashed = digest.digest(new String(password).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is a mandatory algorithm on every JDK implementation
            throw new IllegalStateException(e);
        }
    }
}
