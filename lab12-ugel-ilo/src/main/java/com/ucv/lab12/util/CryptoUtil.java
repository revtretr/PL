package com.ucv.lab12.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public final class CryptoUtil {

    private static final String TRANSFORMACION = "AES/CBC/PKCS5Padding";
    private static final String ALGORITMO      = "AES";

    // Llave AES-128 (16 bytes) — solo para fines demostrativos del prototipo.
    private static final byte[] LLAVE = "UGELIloDeuda2026".getBytes(StandardCharsets.UTF_8);

    private static final SecureRandom RANDOM = new SecureRandom();

    private CryptoUtil() {}

    public static String encriptar(String textoPlano) {
        if (textoPlano == null || textoPlano.isEmpty()) return textoPlano;
        try {
            byte[] iv = new byte[16];
            RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMACION);
            SecretKeySpec keySpec = new SecretKeySpec(LLAVE, ALGORITMO);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));

            byte[] cifrado = cipher.doFinal(textoPlano.getBytes(StandardCharsets.UTF_8));

            byte[] resultado = new byte[iv.length + cifrado.length];
            System.arraycopy(iv, 0, resultado, 0, iv.length);
            System.arraycopy(cifrado, 0, resultado, iv.length, cifrado.length);

            return Base64.getEncoder().encodeToString(resultado);
        } catch (Exception e) {
            throw new RuntimeException("Error al encriptar dato confidencial", e);
        }
    }

    public static String desencriptar(String textoCifradoBase64) {
        if (textoCifradoBase64 == null || textoCifradoBase64.isEmpty()) return textoCifradoBase64;
        try {
            byte[] datos = Base64.getDecoder().decode(textoCifradoBase64);
            byte[] iv = new byte[16];
            byte[] cifrado = new byte[datos.length - 16];
            System.arraycopy(datos, 0, iv, 0, 16);
            System.arraycopy(datos, 16, cifrado, 0, cifrado.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMACION);
            SecretKeySpec keySpec = new SecretKeySpec(LLAVE, ALGORITMO);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));

            byte[] resultado = cipher.doFinal(cifrado);
            return new String(resultado, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error al desencriptar dato confidencial", e);
        }
    }

    /** Enmascara un DNI en texto plano para mostrarlo en pantalla, ej: ******345. */
    public static String enmascarar(String dni) {
        if (dni == null || dni.length() < 4) return "****";
        return "*".repeat(dni.length() - 3) + dni.substring(dni.length() - 3);
    }
}
