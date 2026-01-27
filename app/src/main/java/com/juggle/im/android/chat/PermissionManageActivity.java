package com.juggle.im.android.chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.juggle.im.android.R;

/**
 * 权限管理页：引导用户在系统设置中放开自启动、后台运行、省电等限制，以提升消息/推送送达率。
 */
public class PermissionManageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_manage);

        TextView tvBack = findViewById(R.id.tv_back);
        Button btnAppSettings = findViewById(R.id.btn_app_settings);
        Button btnBattery = findViewById(R.id.btn_battery);

        tvBack.setOnClickListener(v -> finish());

        // 前往应用设置（应用详情页，可在此找到自启动、后台、省电等）
        btnAppSettings.setOnClickListener(v -> openAppDetailSettings());

        // 电池与省电设置（部分机型可直接进入「电池优化」列表）
        btnBattery.setOnClickListener(v -> openBatterySettings());
    }

    private void openAppDetailSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "无法打开应用设置", Toast.LENGTH_SHORT).show();
        }
    }

    private void openBatterySettings() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                startActivity(intent);
            } else {
                openAppDetailSettings();
            }
        } catch (Exception e) {
            openAppDetailSettings();
        }
    }
}
