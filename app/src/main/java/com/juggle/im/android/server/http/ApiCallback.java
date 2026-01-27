package com.juggle.im.android.server.http;

/**
 * API回调接口
 */
public interface ApiCallback<T> {
    /**
     * 成功回调
     *
     * @param data 返回的数据
     */
    void onSuccess(T data);

    /**
     * 错误回调
     *
     * @param code 错误码
     * @param message 错误信息
     */
    void onError(int code, String message);
}
