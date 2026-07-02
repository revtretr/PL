package com.ucv.lab12.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
public final class PasswordUtil {

    private static final String ALGORITMO_HASH = "SHA-256";
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {}

    public static String generarSalt() {
        byte[] saltBytes = new byte[16];
        RANDOM.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }

    public static String hash(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITMO_HASH);
            digest.update(salt.getBytes("UTF-8"));
            byte[] hashedBytes = digest.digest(password.getBytes("UTF-8"));
            return bytesToHex(hashedBytes);
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("No se pudo calcular el hash de la contraseña", e);
        }
    }

    public static boolean verificar(String passwordPlano, String salt, String hashAlmacenado) {
        if (passwordPlano == null || salt == null || hashAlmacenado == null) return false;
        String hashCalculado = hash(passwordPlano, salt);
        return constantTimeEquals(hashCalculado, hashAlmacenado);
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
