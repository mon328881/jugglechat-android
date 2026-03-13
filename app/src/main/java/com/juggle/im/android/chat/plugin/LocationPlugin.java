package com.juggle.im.android.chat.plugin;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.juggle.im.android.R;
import com.juggle.im.android.chat.LocationPickerActivity;

/**
 * 地图定位插件
 */
public class LocationPlugin extends MorePlugin {
    public static final String ID = "location";
    public static final int REQ = 12009;

    public LocationPlugin(Callback callback) {
        super(callback);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public int getIconRes() {
        return R.drawable.ic_location;
    }

    @Override
    public String getLabel(Context ctx) {
        return ctx.getString(R.string.location);
    }

    @Override
    public String getAction() {
        return ID;
    }

    @Override
    public String[] getRequiredPermissions() {
        return new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
    }

    @Override
    public void onClick(Activity activity) {
        // 由宿主 Activity 处理实际地图页面和结果回调
        callback.onPluginAction(getId(), getAction(), null);
    }

    @Override
    public void setHostActivity(Activity activity) {
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        // 当前由 ConversationActivity 直接处理 onActivityResult
        return false;
    }
}
