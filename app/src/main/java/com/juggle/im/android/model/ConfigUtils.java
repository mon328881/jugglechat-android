package com.juggle.im.android.model;

/**
 * 应用配置工具类
 */
public class ConfigUtils {
    // 应用密钥
    public static String appKey = "JJJB0Dh1PtDgZx5X";
    
    // 业务服务（jugglechat-server）的HTTP地址
    // 用于用户、好友、朋友圈等业务接口
    public static String appServerUrl = "http://192.168.123.214:8070";
    // IM 核心服务（im-server-master）的 WebSocket 地址，用于长连接收发消息（端口与 im-server defaultPort/connectManager.wsPort 一致，默认 9003）
    public static String imServer = "ws://192.168.123.214:9003";
    
    /**
     * 音视频ID
     * 需要申请即构音视频ID
     */
    public static Integer zegoId = 1881186044;

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

    /**
     * 非APP扫码时的下载页地址
     * 用于二维码分享页「打开下载页」及提示
     */
    public static String appDownloadPageUrl = "https://im-anhui.onego.top/";

    /**
     * 极光推送registrationId
     * 由Application初始化时填充
     */
    public static String jpushRegistrationId = null;
}
