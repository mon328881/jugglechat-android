package com.juggle.im.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

/**
 * 提供加密的 SharedPreferences，用于存储 Token 等敏感数据。
 * 使用 EncryptedSharedPreferences，避免明文存储在设备上。
 */
public final class SecurePrefsHelper {

    private static final String PREFS_NAME = "login_prefs";
    /** 加密存储使用独立文件名，避免与旧版明文 login_prefs.xml 冲突 */
    private static final String ENCRYPTED_PREFS_NAME = "login_prefs_encrypted";
    private static volatile SharedPreferences instance;

    private SecurePrefsHelper() {
    }

    /**
     * 获取登录相关加密 Prefs（Token、过期时间、极光 regId 等）。
     * 若加密初始化失败则回退到普通 SharedPreferences，保证兼容性。
     */
    public static SharedPreferences getLoginPrefs(Context context) {
        if (context == null) return null;
        Context app = context.getApplicationContext();
        if (instance == null) {
            synchronized (SecurePrefsHelper.class) {
                if (instance == null) {
                    try {
                        MasterKey masterKey = new MasterKey.Builder(app)
                                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                                .build();
                        SharedPreferences encrypted = EncryptedSharedPreferences.create(
                                app,
                                ENCRYPTED_PREFS_NAME,
                                masterKey,
                                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                        );
                        // 首次使用：从明文 prefs 迁移到加密 prefs，避免老用户被登出
                        migrateFromPlainIfNeeded(app, encrypted);
                        instance = encrypted;
                    } catch (Throwable e) {
                        android.util.Log.w("SecurePrefsHelper", "EncryptedSharedPreferences init failed, fallback to plain", e);
                        instance = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    }
                }
            }
        }
        return instance;
    }

    private static void migrateFromPlainIfNeeded(Context app, SharedPreferences encrypted) {
        if (encrypted.contains("app_token")) return; // 已有加密数据，无需迁移
        SharedPreferences plain = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (!plain.contains("app_token")) return;
        SharedPreferences.Editor editor = encrypted.edit();
        for (String key : plain.getAll().keySet()) {
            Object v = plain.getAll().get(key);
            if (v == null) continue;
            if (v instanceof String) editor.putString(key, (String) v);
            else if (v instanceof Long) editor.putLong(key, (Long) v);
            else if (v instanceof Integer) editor.putInt(key, (Integer) v);
            else if (v instanceof Boolean) editor.putBoolean(key, (Boolean) v);
            else if (v instanceof java.util.Set) editor.putStringSet(key, (java.util.Set<String>) v);
        }
        editor.apply();
    }
}
