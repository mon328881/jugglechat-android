package com.juggle.im.android.server.beans;

/**
 * 反应项Bean
 */
public class ReactionItem {
    // 注意：UserInfoBean在同一包中，无需额外导入
    private String value;
    private UserInfoBean user_info;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public UserInfoBean getUser_info() {
        return user_info;
    }

    public void setUser_info(UserInfoBean user_info) {
        this.user_info = user_info;
    }
}
