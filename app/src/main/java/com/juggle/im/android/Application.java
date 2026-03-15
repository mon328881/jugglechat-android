package com.juggle.im.android;

import android.text.TextUtils;
import android.util.Log;

import androidx.multidex.MultiDexApplication;

import com.juggle.im.android.core.JIMChatCore;
import com.juggle.im.android.model.ConfigUtils;
import com.tencent.tencentmap.mapsdk.maps.TencentMapInitializer;

import java.util.Collections;

import cn.jpush.android.api.JPushInterface;

public class Application extends MultiDexApplication {
    private static final String PREFS_NAME = "login_prefs";
    private static final String KEY_APP_TOKEN = "app_token";
    private static final String KEY_IM_TOKEN = "im_token";
    private static final String KEY_EXPIRE_TIME = "expire_time";
    private static final String KEY_JPUSH_REG_ID = "jpush_reg_id";
    private static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();
<<<<<<< HEAD

=======
        
        // 腾讯地图隐私协议同意（必须在地图初始化之前调用）
        try {
            TencentMapInitializer.setAgreePrivacy(true);
        } catch (Throwable ignored) {
            // 如果地图SDK类不可用，不要崩溃应用启动
        }
        
        // 主题通过 AndroidManifest.xml 中的 android:theme 属性应用
>>>>>>> c403ce15fa0088025ee02734a798ba9c97c3ab8c
        JIMChatCore.getInstance().init(this, Collections.singletonList(ConfigUtils.imServer), ConfigUtils.appKey);

        // 初始化极光推送
        try {
            JPushInterface.setDebugMode(true);
            JPushInterface.init(this);

            String regId = JPushInterface.getRegistrationID(this);
            Log.i(TAG, "JPush registrationId raw = " + regId);
            if (!TextUtils.isEmpty(regId)) {
                Log.i(TAG, "JPush registrationId = " + regId);
                android.content.SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                prefs.edit().putString(KEY_JPUSH_REG_ID, regId).apply();
                ConfigUtils.jpushRegistrationId = regId;
            }
        } catch (Throwable ignored) {
            // 极光 SDK 初始化失败不影响主功能
        }
    }
}
