package com.sgswit.fx.utils.sign;

import cn.hutool.core.util.StrUtil;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Cross-platform AES util
 * - AES/CBC/PKCS5Padding
 * - auto IV (16 bytes) prefixed to ciphertext
 * - output: URL-safe base64 without '=' padding -> shorter & URL-safe
 * - optional GZIP compression before encryption to reduce size for larger plaintexts
 *
 * Usage:
 *  - key must be 16 bytes (AES-128) or 32 bytes (AES-256)
 */
public class CrossPlatformAesUtil {

    private static final String ALGO = "AES/CBC/PKCS5Padding";
    private static final int IV_LEN = 16;

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String key = "zAB8#1tIx$knW9z0";

    /**
     * Encrypt with given raw key string (must be 16 or 32 bytes).
     * @param plaintext plain text
     * @param rawKey raw key string (exact 16 or 32 bytes in UTF-8)
     * @param useCompression if true, gzip compress plaintext before encryption
     * @return url-safe base64 (no '=') of IV + ciphertext
     */
    private static String encrypt(String plaintext, String rawKey, boolean useCompression) throws Exception {
        byte[] keyBytes = rawKey.getBytes(StandardCharsets.UTF_8);
        if (!(keyBytes.length == 16 || keyBytes.length == 32)) {
            throw new IllegalArgumentException("rawKey must be 16 (AES-128) or 32 (AES-256) bytes");
        }

        byte[] input = plaintext.getBytes(StandardCharsets.UTF_8);
        if (useCompression) input = gzipCompress(input);

        byte[] iv = new byte[IV_LEN];
        RANDOM.nextBytes(iv);

        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance(ALGO);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
        byte[] encrypted = cipher.doFinal(input);

        // prepend IV
        byte[] out = new byte[IV_LEN + encrypted.length];
        System.arraycopy(iv, 0, out, 0, IV_LEN);
        System.arraycopy(encrypted, 0, out, IV_LEN, encrypted.length);

        // url-safe base64, remove padding "=" to shorten
        String b64 = Base64.getUrlEncoder().withoutPadding().encodeToString(out);
        return b64;
    }

    /**
     * Decrypt the string produced by encrypt(...)
     * @param cipherText url-safe base64 string (no '=')
     * @param rawKey same rawKey used for encryption
     * @param useCompression true if data was compressed before encryption
     * @return decrypted plaintext
     */
    private static String decrypt(String cipherText, String rawKey, boolean useCompression) throws Exception {
        byte[] keyBytes = rawKey.getBytes(StandardCharsets.UTF_8);
        if (!(keyBytes.length == 16 || keyBytes.length == 32)) {
            throw new IllegalArgumentException("rawKey must be 16 (AES-128) or 32 (AES-256) bytes");
        }

        // restore base64 by using URL decoder (no need to pad)
        byte[] all = Base64.getUrlDecoder().decode(cipherText);

        if (all.length < IV_LEN + 1) throw new IllegalArgumentException("cipherText too short");

        byte[] iv = new byte[IV_LEN];
        System.arraycopy(all, 0, iv, 0, IV_LEN);
        byte[] encrypted = new byte[all.length - IV_LEN];
        System.arraycopy(all, IV_LEN, encrypted, 0, encrypted.length);

        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance(ALGO);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
        byte[] plain = cipher.doFinal(encrypted);

        if (useCompression) plain = gzipDecompress(plain);

        return new String(plain, StandardCharsets.UTF_8);
    }

    // --- gzip helpers ---
    private static byte[] gzipCompress(byte[] data) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gos = new GZIPOutputStream(baos)) {
            gos.write(data);
        }
        return baos.toByteArray();
    }

    private static byte[] gzipDecompress(byte[] data) throws Exception {
        try (GZIPInputStream gis = new GZIPInputStream(new java.io.ByteArrayInputStream(data));
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int n;
            while ((n = gis.read(buffer)) != -1) {
                baos.write(buffer, 0, n);
            }
            return baos.toByteArray();
        }
    }

    private static String decryptWithCompression(String cipherText, String rawKey) throws Exception {
        return decrypt(cipherText, rawKey, true);
    }
    private static String encryptWithCompression(String plaintext, String rawKey) throws Exception {
        return encrypt(plaintext, rawKey, true);
    }
    public static String decryptWithCompression(String cipherText) {
        try {
            if (StrUtil.isBlank(cipherText)) {
                return "";
            }
            return decrypt(cipherText, key, true);
        } catch (Exception e) {
            return "";
        }
    }
    public static String encryptWithCompression(String plaintext){
        try {
            if (StrUtil.isBlank(plaintext)) {
                return "";
            }
            return encrypt(plaintext, key, true);
        } catch (Exception e) {
            return "";
        }
    }


}
