package com.juggle.im.android.server.beans;

import com.google.gson.annotations.SerializedName;

/**
 * 登录请求Bean
 */
public class LoginRequest {
    @SerializedName("account")
    private String account;
    @SerializedName("phone")
    private String phone;
    @SerializedName("email")
    private String email;
    @SerializedName("password")
    private String password;
    @SerializedName("code")
    private String code;

    public LoginRequest(String accountOrPhoneOrEmail, String password) {
        // 当前应用仅支持账号+密码登录
        // 如果包含@则按email处理，否则按account处理
        if (accountOrPhoneOrEmail != null && accountOrPhoneOrEmail.contains("@")) {
            this.email = accountOrPhoneOrEmail;
        } else {
            this.account = accountOrPhoneOrEmail;
        }
        this.password = password;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
