package com.juggle.im.android.server.http;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.juggle.im.android.server.beans.HttpResult;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * HTTP服务基类
 */
public abstract class BaseService {
    private Gson gson = new com.google.gson.GsonBuilder()
            .serializeNulls()
            .create();
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private OkHttpClient client;
    private String baseUrl;

    public BaseService(OkHttpClient client, String baseUrl) {
        this.client = client;
        this.baseUrl = baseUrl;
    }

    /**
     * 获取OkHttpClient，供子类在需要时直接使用
     */
    protected OkHttpClient getClient() {
        return client;
    }

    /**
     * 发送JSON POST请求
     */
    protected <T> void enqueueJson(String path, Object bodyObj, Class<T> dataClass, ApiCallback<T> callback) {
        Runnable r = () -> {
            try {
                String url = baseUrl + path;
                String json = gson.toJson(bodyObj == null ? new Object() : bodyObj);
                RequestBody body = RequestBody.create(ServiceManager.MEDIA_TYPE_JSON, json);
                Request request = new Request.Builder().url(url).post(body).build();
                Response resp = client.newCall(request).execute();
                if (!resp.isSuccessful()) {
                    postError(callback, -1, "网络错误: " + resp.code());
                    return;
                }
                String respBody = resp.body() != null ? resp.body().string() : null;
                if (respBody == null) {
                    postError(callback, -1, "响应为空");
                    return;
                }
                HttpResult<T> result = parseHttpResult(respBody, dataClass);
                if (result == null) {
                    postError(callback, -1, "解析错误");
                    return;
                }
                if (result.isSuccess()) {
                    postSuccess(callback, result.getData());
                } else {
                    postError(callback, result.getCode(), result.getMsg());
                }
            } catch (IOException e) {
                postError(callback, -1, e.getMessage());
            }
        };
        new Thread(r, "BaseService-network").start();
    }

    /**
     * 发送GET请求
     */
    protected <T> void enqueueGet(String path, Class<T> dataClass, ApiCallback<T> callback) {
        Runnable r = () -> {
            try {
                String url = baseUrl + path;
                Request request = new Request.Builder().url(url).get().build();
                Response resp = client.newCall(request).execute();
                if (!resp.isSuccessful()) {
                    postError(callback, -1, "网络错误: " + resp.code());
                    return;
                }
                String respBody = resp.body() != null ? resp.body().string() : null;
                if (respBody == null) {
                    postError(callback, -1, "响应为空");
                    return;
                }
                HttpResult<T> result = parseHttpResult(respBody, dataClass);
                if (result == null) {
                    postError(callback, -1, "解析错误");
                    return;
                }
                if (result.isSuccess()) {
                    postSuccess(callback, result.getData());
                } else {
                    postError(callback, result.getCode(), result.getMsg());
                }
            } catch (IOException e) {
                postError(callback, -1, e.getMessage());
            }
        };
        new Thread(r, "BaseService-network").start();
    }

    /**
     * 解析HTTP响应结果
     */
    protected <T> HttpResult<T> parseHttpResult(String json, Class<T> dataClass) {
        try {
            Log.d("BaseService", "解析响应 JSON: " + json);
            HttpResult raw = gson.fromJson(json, HttpResult.class);
            com.google.gson.JsonObject jo = gson.fromJson(json, com.google.gson.JsonObject.class);
            if (jo.has("data") && !jo.get("data").isJsonNull()) {
                try {
                    T data = gson.fromJson(jo.get("data"), dataClass);
                    raw.setData(data);
                    Log.d("BaseService", "成功解析数据: " + dataClass.getSimpleName());
                } catch (JsonSyntaxException e) {
                    Log.e("BaseService", "数据解析失败: " + e.getMessage());
                    return null;
                }
            } else {
                Log.w("BaseService", "响应中没有 data 字段或 data 为 null");
            }
            return raw;
        } catch (JsonSyntaxException e) {
            Log.e("BaseService", "JSON 解析失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 在主线程上发送成功回调
     */
    protected <T> void postSuccess(ApiCallback<T> callback, T data) {
        if (callback == null) return;
        if (Looper.myLooper() == Looper.getMainLooper()) {
            callback.onSuccess(data);
        } else {
            mainHandler.post(() -> callback.onSuccess(data));
        }
    }

    /**
     * 在主线程上发送错误回调
     */
    protected void postError(ApiCallback<?> callback, int code, String msg) {
        Log.e("BaseService", "错误: " + code + " - " + msg);
        if (callback == null) return;
        if (Looper.myLooper() == Looper.getMainLooper()) {
            callback.onError(code, msg);
        } else {
            mainHandler.post(() -> callback.onError(code, msg));
        }
    }
}
