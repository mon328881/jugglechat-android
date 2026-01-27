package com.juggle.im.android.app;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.juggle.im.android.R;

/**
 * 隐私协议详情页：
 * - 以纯文本形式展示本应用的隐私说明；
 * - 重点说明定位权限仅在用户主动进入"发送位置"页时按需申请，不在启动时后台定位。
 */
public class PrivacyPolicyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
    }
}
