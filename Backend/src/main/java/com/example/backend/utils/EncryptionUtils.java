package com.example.backend.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AI API 密钥等敏感数据的加解密工具类
 * 使用 AES/ECB/PKCS5Padding 对称加密
 */
public class EncryptionUtils {
    private static final String ALGORITHM = "AES";
    
    // 16 字节密钥，对应 AES-128
    private static final String SECRET_KEY = "IMS_SecretKey_12"; 

    /**
     * 加密明文字符串
     *
     * @param value 明文
     * @return Base64编码的密文
     */
    public static String encrypt(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("AES encryption failed", e);
        }
    }

    /**
     * 解密密文字符串
     *
     * @param encryptedValue Base64编码的密文
     * @return 解密后的明文
     */
    public static String decrypt(String encryptedValue) {
        if (encryptedValue == null || encryptedValue.isEmpty()) {
            return encryptedValue;
        }
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedValue));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES decryption failed", e);
        }
    }
}
