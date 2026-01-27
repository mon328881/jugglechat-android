package com.juggle.im.android.server.http;

/**
 * 文件上传相关HTTP接口封装
 * 
 * 内部流程：
 * 1. 向/jim/file_cred申请上传凭证
 * 2. 使用预签名URL把本地文件上传到对象存储
 * 3. 返回可用于展示/播放的文件URL
 */
public interface FileService {

    /**
     * 上传文件到后端配置的对象存储
     *
     * @param fileType  文件类型：1表示图片，3表示视频
     * @param localPath 本地文件绝对路径
     * @param ext       文件后缀（不带点），例如"jpg"、"mp4"
     * @param callback  回调：成功返回可访问的URL
     */
    void uploadFile(int fileType, String localPath, String ext, ApiCallback<String> callback);
}
