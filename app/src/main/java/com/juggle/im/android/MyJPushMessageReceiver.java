package com.juggle.im.android;

import android.content.Context;
import com.juggle.im.android.utils.LogUtil;

import cn.jpush.android.api.CustomMessage;
import cn.jpush.android.api.JPushMessage;
import cn.jpush.android.service.JPushMessageReceiver;

/**
 * 极光推送自定义消息接收器
 * 必须在 AndroidManifest 中注册，否则 JPush SDK 会报 missing receiver。
 */
public class MyJPushMessageReceiver extends JPushMessageReceiver {

    private static final String TAG = "MyJPushReceiver";

    @Override
    public void onMessage(Context context, CustomMessage customMessage) {
        LogUtil.d(TAG, "onMessage received");
        super.onMessage(context, customMessage);
    }

    @Override
    public void onAliasOperatorResult(Context context, JPushMessage jPushMessage) {
        LogUtil.d(TAG, "onAliasOperatorResult");
        super.onAliasOperatorResult(context, jPushMessage);
    }
}

