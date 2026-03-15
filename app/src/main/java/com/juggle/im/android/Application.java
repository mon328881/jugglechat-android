package com.juggle.im.android;

import android.text.TextUtils;
import android.util.Log;

import androidx.multidex.MultiDexApplication;

import com.juggle.im.android.core.JIMChatCore;
import com.juggle.im.android.model.ConfigUtils;
import com.juggle.im.android.utils.SecurePrefsHelper;
import com.tencent.tencentmap.mapsdk.maps.TencentMapInitializer;

import java.util.Collections;

import cn.jpush.android.api.JPushInterface;

public class Application extends MultiDexApplication {
    private static final String KEY_APP_TOKEN = "app_token";
    private static final String KEY_IM_TOKEN = "im_token";
    private static final String KEY_EXPIRE_TIME = "expire_time";
    private static final String KEY_JPUSH_REG_ID = "jpush_reg_id";
    private static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();

        // 从 BuildConfig 注入配置（来自 local.properties 或默认值）
        ConfigUtils.appKey = BuildConfig.APP_KEY;
        ConfigUtils.appServerUrl = BuildConfig.APP_SERVER_URL;
        ConfigUtils.imServer = BuildConfig.IM_SERVER;
        ConfigUtils.zegoId = BuildConfig.ZEGO_ID;
        ConfigUtils.appDownloadPageUrl = BuildConfig.APP_DOWNLOAD_PAGE_URL;

        if (BuildConfig.DEBUG) {
            try {
                android.os.StrictMode.setThreadPolicy(new android.os.StrictMode.ThreadPolicy.Builder()
                        .detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
                android.os.StrictMode.setVmPolicy(new android.os.StrictMode.VmPolicy.Builder()
                        .detectLeakedClosableObjects().detectLeakedSqlLiteObjects().penaltyLog().build());
            } catch (Throwable ignored) {
            }
        }

        // 腾讯地图隐私协议同意（必须在地图初始化之前调用）
        try {
            TencentMapInitializer.setAgreePrivacy(true);
        } catch (Throwable ignored) {
            // 如果地图SDK类不可用，不要崩溃应用启动
        }

        // 恢复上次登录态：使用加密 Prefs 读取 Token
        try {
            android.content.SharedPreferences prefs = SecurePrefsHelper.getLoginPrefs(this);
            if (prefs != null) {
                String appToken = prefs.getString(KEY_APP_TOKEN, null);
                String imToken = prefs.getString(KEY_IM_TOKEN, null);
                long expireTime = prefs.getLong(KEY_EXPIRE_TIME, 0L);
                if (appToken != null && !appToken.isEmpty() && expireTime > System.currentTimeMillis()) {
                    ConfigUtils.appToken = appToken;
                }
                if (imToken != null && !imToken.isEmpty() && expireTime > System.currentTimeMillis()) {
                    ConfigUtils.imToken = imToken;
                }
            }
        } catch (Throwable ignored) {
            // ignore any unexpected prefs errors
        }

        JIMChatCore.getInstance().init(this, Collections.singletonList(ConfigUtils.imServer), ConfigUtils.appKey);

        // 初始化极光推送（这里直接开启调试日志，如需关闭可改为 false）
        try {
            JPushInterface.setDebugMode(true);
            JPushInterface.init(this);

            String regId = JPushInterface.getRegistrationID(this);
            Log.i(TAG, "JPush registrationId raw = " + regId);
            if (!TextUtils.isEmpty(regId)) {
                Log.i(TAG, "JPush registrationId = " + regId);
                android.content.SharedPreferences prefs = SecurePrefsHelper.getLoginPrefs(this);
                if (prefs != null) {
                    prefs.edit().putString(KEY_JPUSH_REG_ID, regId).apply();
                }
                ConfigUtils.jpushRegistrationId = regId;
            }
        } catch (Throwable ignored) {
            // 极光 SDK 初始化失败不影响主功能
        }
    }
}
