package com.juggle.im.android.app;

import static com.juggle.im.android.app.LoginActivity.KEY_APP_TOKEN;
import static com.juggle.im.android.app.LoginActivity.KEY_IM_TOKEN;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

import com.juggle.im.android.R;
import com.juggle.im.android.core.JIMChatCore;
import com.juggle.im.android.model.ConfigUtils;
import com.juggle.im.android.server.http.ServiceManager;
import com.juggle.im.android.utils.SecurePrefsHelper;

import java.util.Date;

public class FlashActivity extends AppCompatActivity {
    private static final String TAG = "FlashActivity";
    private static final String KEY_EXPIRE_TIME = "expire_time";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable goToLoginRunnable = this::goToLogin;
    private final Runnable goToMainRunnable = this::goToMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash);

        if (hasValidToken()) {
            handler.postDelayed(goToMainRunnable, 1200);
        } else {
            handler.postDelayed(goToLoginRunnable, 1500);
        }
        Window window = getWindow();
        window.setNavigationBarColor(getColor(R.color.primary_bg_light));
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private boolean hasValidToken() {
        SharedPreferences prefs = SecurePrefsHelper.getLoginPrefs(this);
        if (prefs == null) return false;
        String token = prefs.getString(KEY_APP_TOKEN, null);
        long expireTime = prefs.getLong(KEY_EXPIRE_TIME, 0);
        String imToken = prefs.getString(KEY_IM_TOKEN, null);

        if (token != null && imToken != null && expireTime > System.currentTimeMillis()) {
            ConfigUtils.appToken = token;
            ConfigUtils.imToken = imToken;
            return true;
        }
        return false;
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}