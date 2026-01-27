package com.juggle.im.android.server.http;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.juggle.im.android.model.ConfigUtils;
import com.juggle.im.android.server.beans.HttpResult;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * 服务管理器：统一管理所有HTTP服务
 */
public class ServiceManager {
    public static final MediaType MEDIA_TYPE_JSON =
            MediaType.parse("application/json;charset=UTF-8");
    private static final UserService userService;
    private static final MomentService momentService;
    private static final FileService fileService;

    static {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .sslSocketFactory(Objects.requireNonNull(SSLHelper.getTrustAllSSLSocketFactory()), SSLHelper.getTrustAllManager())
                .addInterceptor(new Interceptor() {
                    @NonNull
                    @Override
                    public Response intercept(@NonNull Chain chain) throws IOException {
                        okhttp3.Request request = chain.request();
                        if (!TextUtils.isEmpty(ConfigUtils.appToken)) {
                            request = request.newBuilder().addHeader("authorization", ConfigUtils.appToken).build();
                        }
                        request = request.newBuilder().addHeader("appkey", ConfigUtils.appKey).build();
                        return chain.proceed(request);
                    }
                })
                .hostnameVerifier((hostname, session) -> true)
                .build();
        userService = new UserServiceImpl(okHttpClient, ConfigUtils.appServerUrl);
        momentService = new MomentServiceImpl(okHttpClient, ConfigUtils.appServerUrl);
        fileService = new FileServiceImpl(okHttpClient, ConfigUtils.appServerUrl);
    }

    public static UserService getUserService() {
        return userService;
    }

    public static MomentService getMomentService() {
        return momentService;
    }

    public static FileService getFileService() {
        return fileService;
    }
}
