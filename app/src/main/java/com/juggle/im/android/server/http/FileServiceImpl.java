package com.juggle.im.android.server.http;

import android.text.TextUtils;
import android.util.Log;

import com.juggle.im.android.server.beans.FileCredResp;
import com.juggle.im.android.server.beans.PlayUrlResp;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 文件上传实现类
 * 使用/jim/file_cred + 预签名URL的通用文件上传
 * 主要支持Minio/S3这类返回pre_sign_resp.url的存储引擎
 */
public class FileServiceImpl extends BaseService implements FileService {

    // 文件类型定义
    private static final int FILE_TYPE_IMAGE = 1;
    private static final int FILE_TYPE_VIDEO = 3;

    // 对象存储类型定义
    private static final int OSS_TYPE_QINIU = 1;
    private static final int OSS_TYPE_S3 = 2;
    private static final int OSS_TYPE_MINIO = 3;
    private static final int OSS_TYPE_OSS = 4;

    public FileServiceImpl(OkHttpClient client, String baseUrl) {
        super(client, baseUrl);
    }

    @Override
    public void uploadFile(int fileType, String localPath, String ext, ApiCallback<String> callback) {
        if (TextUtils.isEmpty(localPath)) {
            if (callback != null) {
                callback.onError(-1, "本地路径为空");
            }
            return;
        }
        File file = new File(localPath);
        if (!file.exists() || !file.isFile()) {
            if (callback != null) {
                callback.onError(-1, "文件不存在: " + localPath);
            }
            return;
        }

        // 1. 先向业务服务申请上传凭证
        Map<String, Object> req = new HashMap<>();
        req.put("file_type", fileType);
        req.put("ext", ext);

        enqueueJson("/jim/file_cred", req, FileCredResp.class, new ApiCallback<FileCredResp>() {
            @Override
            public void onSuccess(FileCredResp cred) {
                if (cred == null) {
                    if (callback != null) {
                        callback.onError(-1, "凭证为空");
                    }
                    return;
                }
                int ossType = cred.getOss_type();
                if (ossType == OSS_TYPE_MINIO || ossType == OSS_TYPE_S3) {
                    FileCredResp.PreSignResp pre = cred.getPre_sign_resp();
                    if (pre == null || TextUtils.isEmpty(pre.getUrl())) {
                        if (callback != null) {
                            callback.onError(-1, "预签名URL为空");
                        }
                        return;
                    }
                    final String putUrl = pre.getUrl();
                    final String downloadUrl = pre.getDownload_url();

                    // 先用 PUT 预签名 URL 上传到对象存储，成功后再将 downloadUrl 作为文件访问地址返回
                    uploadWithPreSignedUrl(putUrl, file, new ApiCallback<Void>() {
                        @Override
                        public void onSuccess(Void ignore) {
                            if (callback == null) return;

                            String finalUrl;
                            if (!TextUtils.isEmpty(downloadUrl)) {
                                // 优先使用后端为 GET 准备的 DownloadUrl，避免客户端自行拼接导致 403/404
                                finalUrl = downloadUrl;
                            } else {
                                // 兜底：去掉 PUT URL 的 query 作为短地址（与老版本兼容）
                                String tmp = putUrl;
                                int idx = tmp.indexOf("?");
                                if (idx > 0) {
                                    tmp = tmp.substring(0, idx);
                                }
                                finalUrl = tmp;
                            }
                            callback.onSuccess(finalUrl);
                        }

                        @Override
                        public void onError(int code, String message) {
                            if (callback != null) {
                                callback.onError(code, message);
                            }
                        }
                    });
                } else {
                    if (callback != null) {
                        callback.onError(-1, "不支持的存储类型: " + ossType);
                    }
                }
            }

            @Override
            public void onError(int code, String message) {
                if (callback != null) {
                    callback.onError(code, message);
                }
            }
        });
    }

    /**
     * 使用预签名URL上传文件，仅负责 PUT 上传本身，不返回文件访问地址
     */
    private void uploadWithPreSignedUrl(String url, File file, ApiCallback<Void> callback) {
        Runnable r = () -> {
            try {
                RequestBody body = RequestBody.create(MediaType.parse("application/octet-stream"), file);
                Request request = new Request.Builder()
                        .url(url)
                        .put(body)
                        .build();
                Response resp = getClient().newCall(request).execute();
                if (!resp.isSuccessful()) {
                    int code = resp.code();
                    if (callback != null) {
                        callback.onError(code, "上传失败: " + code);
                    }
                    return;
                }
                if (callback != null) {
                    callback.onSuccess(null);
                }
            } catch (IOException e) {
                Log.e("FileService", "上传错误", e);
                if (callback != null) {
                    callback.onError(-1, e.getMessage());
                }
            }
        };
        new Thread(r, "FileService-upload").start();
    }

    @Override
    public void getPlayUrl(String storedUrl, ApiCallback<String> callback) {
        if (TextUtils.isEmpty(storedUrl)) {
            if (callback != null) callback.onError(-1, "url 为空");
            return;
        }
        Map<String, String> req = new HashMap<>();
        req.put("url", storedUrl);
        enqueueJson("/jim/file/play_url", req, PlayUrlResp.class, new ApiCallback<PlayUrlResp>() {
            @Override
            public void onSuccess(PlayUrlResp data) {
                if (callback != null && data != null && data.getUrl() != null) {
                    postSuccess(callback, data.getUrl());
                } else if (callback != null) {
                    callback.onError(-1, "响应无播放地址");
                }
            }

            @Override
            public void onError(int code, String message) {
                if (callback != null) callback.onError(code, message);
            }
        });
    }
}
