package com.juggle.im.android.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import com.juggle.im.android.utils.LogUtil;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.juggle.im.android.R;
import com.juggle.im.android.core.JIMChatCore;
import com.juggle.im.android.model.ConfigUtils;
import com.juggle.im.android.server.beans.LoginRequest;
import com.juggle.im.android.server.beans.LoginResult;
import com.juggle.im.android.server.http.ApiCallback;
import com.juggle.im.android.server.http.ServiceManager;
import com.juggle.im.android.utils.SecurePrefsHelper;

public class LoginActivity extends AppCompatActivity {
    private EditText phoneInput;
    private EditText passwordInput;
    private Button loginButton;
    private Button registerButton;
    private ProgressBar loginProgress;
    private CheckBox rememberAccountCheckbox;
    private TextView serviceAgreementText;
    private TextView privacyPolicyText;

    public static final String PREFS_NAME = "login_prefs";
    public static final String KEY_APP_TOKEN = "app_token";
    public static final String KEY_IM_TOKEN = "im_token";
    public static final String KEY_EXPIRE_TIME = "expire_time";
    public static final String KEY_REMEMBER_ACCOUNT = "remember_account";
    public static final String KEY_LAST_ACCOUNT = "last_account";
    // 默认 token 有效期为 30 天（与后端保持一致）
    private static final long DEFAULT_TOKEN_VALIDITY_DURATION = 30 * 24 * 60 * 60 * 1000;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        initViews();
        setupListeners();
        loadRememberedAccount();
    }

    private void initViews() {
        phoneInput = findViewById(R.id.phoneInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        loginProgress = findViewById(R.id.loginProgress);
        rememberAccountCheckbox = findViewById(R.id.rememberAccountCheckbox);
        serviceAgreementText = findViewById(R.id.serviceAgreementText);
        privacyPolicyText = findViewById(R.id.privacyPolicyText);
    }

    private void setupListeners() {
        loginButton.setOnClickListener(v -> handleLogin());
        registerButton.setOnClickListener(v -> switchToRegister());
        serviceAgreementText.setOnClickListener(v -> showServiceAgreementDialog());
        privacyPolicyText.setOnClickListener(v -> showPrivacyPolicyDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 每次回到登录页时都重新加载"记住账号"
        loadRememberedAccount();
    }

    private void loadRememberedAccount() {
        new Thread(() -> {
            SharedPreferences prefs = SecurePrefsHelper.getLoginPrefs(LoginActivity.this);
            final boolean remember = prefs != null && prefs.getBoolean(KEY_REMEMBER_ACCOUNT, false);
            final String lastAccount = prefs != null ? prefs.getString(KEY_LAST_ACCOUNT, "") : "";
            LogUtil.d("LoginActivity", "加载记住的账号 remember=" + remember);
            mainHandler.post(() -> {
                if (remember && lastAccount != null && !lastAccount.isEmpty()) {
                    phoneInput.setText(lastAccount);
                    if (rememberAccountCheckbox != null) rememberAccountCheckbox.setChecked(true);
                } else {
                    phoneInput.setText("");
                    if (rememberAccountCheckbox != null) rememberAccountCheckbox.setChecked(false);
                }
            });
        }).start();
    }

    private void handleLogin() {
        String account = phoneInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        
        // 验证输入
        if (account.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请输入账号和密码", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 显示loading状态
        showLoading(true);
        
        // 检查复选框状态
        boolean isRememberChecked = rememberAccountCheckbox != null && rememberAccountCheckbox.isChecked();
        LogUtil.d("LoginActivity", "发送登录请求 remember=" + isRememberChecked);
        
        ServiceManager.getUserService().login(new LoginRequest(account, password), new ApiCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult data) {
                LogUtil.i("login", "登录成功");
                if (data == null) {
                    LogUtil.e("login", "LoginResult 为 null，后端可能返回了错误的数据格式");
                    Toast.makeText(LoginActivity.this, "登录失败：服务器返回数据异常", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                    return;
                }
                ConfigUtils.imToken = data.getIm_token();
                ConfigUtils.appToken = data.getAuthorization();
                ConfigUtils.myName = data.getNickname();
                ConfigUtils.myAvatarUrl = data.getAvatar();
                ConfigUtils.currentUserId = data.getUser_id();
                
                final boolean shouldRemember = rememberAccountCheckbox != null && rememberAccountCheckbox.isChecked();
                LogUtil.d("LoginActivity", "登录成功，准备保存账号 shouldRemember=" + shouldRemember);
                new Thread(() -> {
                    saveToken(data.getAuthorization(), data.getIm_token(), data.getExpires_in());
                    saveAccount(account, shouldRemember);
                    mainHandler.post(() -> {
                        JIMChatCore.getInstance().connect(ConfigUtils.imToken);
                        showLoading(false);
                        switchToConversationList();
                    });
                }).start();
            }

            @Override
            public void onError(int code, String message) {
                // 隐藏loading状态
                showLoading(false);
                LogUtil.e("login", "登录失败 code=" + code);
                String tip = (message == null || message.isEmpty())
                        ? "登录失败，请稍后重试"
                        : "登录失败：" + message;
                Toast.makeText(LoginActivity.this, tip, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveToken(String token, String imToken, long expiresIn) {
        SharedPreferences prefs = SecurePrefsHelper.getLoginPrefs(this);
        if (prefs == null) return;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_APP_TOKEN, token);
        editor.putString(KEY_IM_TOKEN, imToken);
        
        // 使用后端返回的过期时间，如果没有则使用默认值
        long expireTime;
        if (expiresIn > 0) {
            // 后端返回的是秒数，需要转换为毫秒
            expireTime = System.currentTimeMillis() + (expiresIn * 1000);
            LogUtil.d("LoginActivity", "使用后端返回的过期时间");
        } else {
            // 后端没有返回过期时间，使用默认值
            expireTime = System.currentTimeMillis() + DEFAULT_TOKEN_VALIDITY_DURATION;
            LogUtil.d("LoginActivity", "使用默认过期时间");
        }
        
        editor.putLong(KEY_EXPIRE_TIME, expireTime);
        editor.apply();
    }

    private void saveAccount(String account, boolean remember) {
        SharedPreferences prefs = SecurePrefsHelper.getLoginPrefs(this);
        if (prefs == null) return;
        SharedPreferences.Editor editor = prefs.edit();
        
        LogUtil.d("LoginActivity", "保存账号 remember=" + remember);
        
        if (remember) {
            editor.putBoolean(KEY_REMEMBER_ACCOUNT, true);
            editor.putString(KEY_LAST_ACCOUNT, account);
            LogUtil.d("LoginActivity", "已保存账号到SharedPreferences");
        } else {
            editor.putBoolean(KEY_REMEMBER_ACCOUNT, false);
            editor.remove(KEY_LAST_ACCOUNT);
            LogUtil.d("LoginActivity", "已清除SharedPreferences中的账号");
        }
        editor.apply();
    }

    private void showLoading(boolean show) {
        if (show) {
            loginButton.setText("登录中...");
            loginButton.setEnabled(false);
            loginProgress.setVisibility(View.VISIBLE);
        } else {
            loginButton.setText("开启连接");
            loginButton.setEnabled(true);
            loginProgress.setVisibility(View.GONE);
        }
    }

    // 显示服务协议对话框
    private void showServiceAgreementDialog() {
        new AlertDialog.Builder(this)
                .setTitle("服务协议")
                .setMessage("本应用为即时通信服务产品，用于在您与好友/群组之间收发消息、发起音视频通话及分享内容。\n\n" +
                        "在使用本应用过程中，我们会根据实现这些基础功能所必需的范围处理您的账号信息（如昵称、头像）、消息内容、设备与网络信息等，用于账号登录、消息路由与故障排查，不会将您的个人信息出售或用于与本服务无关的目的。\n\n" +
                        "详细条款请以最新发布的《服务协议》为准。")
                .setPositiveButton("我已知晓", null)
                .show();
    }

    // 显示隐私政策对话框
    private void showPrivacyPolicyDialog() {
        new AlertDialog.Builder(this)
                .setTitle("隐私政策")
                .setMessage("我们仅在实现即时通讯、音视频通话和位置分享等功能所必需的范围内收集和使用您的信息。\n\n" +
                        "例如，相机/麦克风权限仅在您发送图片、语音或发起通话时使用；\n" +
                        "定位权限仅在您进入\"发送位置/位置选择\"页面并主动使用位置功能时按需申请，不在应用启动时自动申请，也不会在后台持续定位；\n" +
                        "通知权限用于向您推送新消息提醒。您可以随时在系统设置中关闭相关权限。\n\n" +
                        "更完整的说明可在\"我的 - 隐私协议\"中查看。")
                .setPositiveButton("我已知晓", null)
                .show();
    }

    // 跳转到注册页面
    private void switchToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    // 跳转到会话列表页面
    private void switchToConversationList() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}