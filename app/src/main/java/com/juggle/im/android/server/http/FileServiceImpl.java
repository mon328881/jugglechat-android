package com.juggle.im.android.server.http;

import android.text.TextUtils;
import android.util.Log;

import com.juggle.im.android.server.beans.FileCredResp;

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
                    uploadWithPreSignedUrl(pre.getUrl(), file, callback);
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
     * 使用预签名URL上传文件
     */
    private void uploadWithPreSignedUrl(String url, File file, ApiCallback<String> callback) {
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
                // 对于Minio/S3，预签名URL中带有查询参数
                // 实际访问时一般使用不带query的路径作为文件URL
                String finalUrl = url;
                int idx = finalUrl.indexOf("?");
                if (idx > 0) {
                    finalUrl = finalUrl.substring(0, idx);
                }
                if (callback != null) {
                    callback.onSuccess(finalUrl);
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
}
