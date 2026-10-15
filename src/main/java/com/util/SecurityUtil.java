package com.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class SecurityUtil {
    // OWASP Recommended iterations for PBKDF2
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    /**
     * Call this when registering a new user or updating a password.
     * Generates a new salt and returns the stored format: "base64Salt:base64Hash"
     */
    public static String hashPassword(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            
            byte[] hash = generateHash(password.toCharArray(), salt);
            return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Call this during login to compare the entered plaintext password with the DB hash.
     */
    public static boolean verifyPassword(String enteredPassword, String storedHashSequence) {
        try {
            if (storedHashSequence == null || !storedHashSequence.contains(":")) {
                // Fallback for legacy plaintext passwords during transition (optional)
                // Remove this once all passwords in DB are hashed!
                return enteredPassword.equals(storedHashSequence); 
            }

            String[] parts = storedHashSequence.split(":");
            if (parts.length != 2) return false;
            
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] storedHash = Base64.getDecoder().decode(parts[1]);
            byte[] testHash = generateHash(enteredPassword.toCharArray(), salt);
            
            // Constant-time comparison to prevent timing side-channel attacks
            int diff = storedHash.length ^ testHash.length;
            for (int i = 0; i < storedHash.length && i < testHash.length; i++) {
                diff |= storedHash[i] ^ testHash[i];
            }
            return diff == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] generateHash(char[] password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
        return skf.generateSecret(spec).getEncoded();
    }
}