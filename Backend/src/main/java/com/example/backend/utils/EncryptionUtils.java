package com.example.backend.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AI API 密钥等敏感数据的加解密工具类
 * 使用 AES/CBC/PKCS5Padding 对称加密，每次加密随机生成 IV
 * 密钥通过环境变量 ENCRYPTION_SECRET_KEY 注入，默认值仅用于开发环境
 */
public class EncryptionUtils {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String LEGACY_TRANSFORMATION = "AES"; // AES/ECB for backward compat
    private static final int IV_LENGTH = 16;

    // 密钥优先从环境变量读取，未设置时使用开发环境默认值
    private static final String SECRET_KEY;

    static {
        String envKey = System.getenv("ENCRYPTION_SECRET_KEY");
        if (envKey != null && envKey.length() == 16) {
            SECRET_KEY = envKey;
        } else {
            SECRET_KEY = "IMS_SecretKey_12"; // 16 bytes for AES-128, dev only
        }
    }

    /**
     * 加密明文字符串 (AES-CBC，随机 IV 拼接到密文前部)
     *
     * @param value 明文
     * @return Base64编码的 "IV + 密文"
     */
    public static String encrypt(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);

            // 随机生成 16 字节 IV
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

            // IV + 密文 拼接后统一 Base64 编码
            byte[] combined = new byte[IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
            System.arraycopy(encrypted, 0, combined, IV_LENGTH, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("AES-CBC encryption failed", e);
        }
    }

    /**
     * 解密密文字符串
     * 自动兼容新 AES-CBC 格式和旧 AES-ECB 格式
     *
     * @param encryptedValue Base64编码的密文
     * @return 解密后的明文
     */
    public static String decrypt(String encryptedValue) {
        if (encryptedValue == null || encryptedValue.isEmpty()) {
            return encryptedValue;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedValue);
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);

            // 判断格式：AES-CBC 的密文至少包含 16 字节 IV + 16 字节密文（PKCS5 最小一个 block）
            // 且总长度应大于 IV_LENGTH 且为 16 的倍数减去 IV 后仍然是 16 的倍数
            if (decoded.length > IV_LENGTH && (decoded.length - IV_LENGTH) % 16 == 0) {
                try {
                    // 尝试 AES-CBC 解密
                    byte[] iv = new byte[IV_LENGTH];
                    System.arraycopy(decoded, 0, iv, 0, IV_LENGTH);
                    byte[] cipherText = new byte[decoded.length - IV_LENGTH];
                    System.arraycopy(decoded, IV_LENGTH, cipherText, 0, cipherText.length);

                    Cipher cipher = Cipher.getInstance(TRANSFORMATION);
                    cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
                    byte[] decrypted = cipher.doFinal(cipherText);
                    return new String(decrypted, StandardCharsets.UTF_8);
                } catch (Exception cbcEx) {
                    // CBC 解密失败，回退到 ECB 尝试
                }
            }

            // 回退：旧 AES-ECB 格式解密
            Cipher cipher = Cipher.getInstance(LEGACY_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES decryption failed", e);
        }
    }
}
