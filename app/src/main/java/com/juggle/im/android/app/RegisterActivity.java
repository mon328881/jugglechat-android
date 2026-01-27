package com.juggle.im.android.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.juggle.im.android.R;
import com.juggle.im.android.server.beans.LoginResult;
import com.juggle.im.android.server.beans.RegisterRequest;
import com.juggle.im.android.server.http.ApiCallback;
import com.juggle.im.android.server.http.ServiceManager;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private EditText inputField;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private Button registerButton;
    private Button backToLoginButton;

    // 正则表达式模式
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("^[A-Za-z0-9_]{5,20}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupListeners();
    }

    private void initViews() {
        inputField = findViewById(R.id.inputField);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        registerButton = findViewById(R.id.registerButton);
        backToLoginButton = findViewById(R.id.backToLoginButton);
    }

    private void setupListeners() {
        registerButton.setOnClickListener(v -> handleRegister());
        backToLoginButton.setOnClickListener(v -> finish());
    }

    /**
     * 判断输入的类型：手机号、邮箱或账号
     */
    private String getInputType(String input) {
        if (PHONE_PATTERN.matcher(input).matches()) {
            return "phone";
        } else if (EMAIL_PATTERN.matcher(input).matches()) {
            return "email";
        } else if (ACCOUNT_PATTERN.matcher(input).matches()) {
            return "account";
        }
        return null;
    }

    private void handleRegister() {
        String input = inputField.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // 验证输入
        if (TextUtils.isEmpty(input)) {
            Toast.makeText(this, "请输入手机号、邮箱或账号", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请设置密码", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "请确认密码", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "密码长度至少为 6 个字符", Toast.LENGTH_SHORT).show();
            return;
        }

        // 判断输入类型
        String inputType = getInputType(input);
        if (inputType == null) {
            Toast.makeText(this, "请输入有效的手机号、邮箱或账号（账号需为5-20个字母数字）", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建注册请求
        RegisterRequest request = new RegisterRequest();
        request.setPassword(password);

        if ("phone".equals(inputType)) {
            request.setPhone(input);
        } else if ("email".equals(inputType)) {
            request.setEmail(input);
        } else if ("account".equals(inputType)) {
            request.setAccount(input);
        }

        // 显示加载状态
        setLoadingState(true);

        // 发送注册请求
        ServiceManager.getUserService().register(request, new ApiCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult data) {
                setLoadingState(false);
                Toast.makeText(RegisterActivity.this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(int code, String message) {
                setLoadingState(false);
                Toast.makeText(RegisterActivity.this, "注册失败: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoadingState(boolean isLoading) {
        registerButton.setEnabled(!isLoading);
        inputField.setEnabled(!isLoading);
        passwordInput.setEnabled(!isLoading);
        confirmPasswordInput.setEnabled(!isLoading);
        backToLoginButton.setEnabled(!isLoading);

        if (isLoading) {
            registerButton.setAlpha(0.6f);
            registerButton.setText("注册中...");
        } else {
            registerButton.setAlpha(1.0f);
            registerButton.setText("立即注册");
        }
    }
}
