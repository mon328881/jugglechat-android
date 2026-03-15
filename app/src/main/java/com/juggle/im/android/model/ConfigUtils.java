package com.juggle.im.android.model;

/**
 * 应用配置工具类。
 * 服务地址、密钥等由 Application 从 BuildConfig 注入（BuildConfig 来自 local.properties 或默认值）。
 */
public class ConfigUtils {
    // 应用密钥（由 Application 从 BuildConfig.APP_KEY 注入）
    public static String appKey;
    
    // 业务服务（jugglechat-server）的HTTP地址
    public static String appServerUrl;
    // IM 核心服务 WebSocket 地址
    public static String imServer;
    
    /** 音视频ID（由 Application 从 BuildConfig.ZEGO_ID 注入） */
    public static Integer zegoId;

    /**
     * 应用Token
     */
    public static String appToken = null;

    /**
     * IM Token
     */
    public static String imToken = null;

    /**
     * 我的头像URL
     */
    public static String myAvatarUrl = null;
    
    /**
     * 我的昵称
     */
    public static String myName = null;

    /**
     * 当前登录用户的user_id
     * 登录时从接口写入，登出清空
     * 用于getCurrentUserId()尚未就绪时的兜底
     */
    public static String currentUserId = null;

    /** 非APP扫码时的下载页地址（由 Application 从 BuildConfig 注入） */
    public static String appDownloadPageUrl;

    /**
     * 极光推送registrationId
     * 由Application初始化时填充
     */
    public static String jpushRegistrationId = null;
}
