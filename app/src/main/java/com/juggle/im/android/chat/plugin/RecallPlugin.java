package com.juggle.im.android.chat.plugin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.juggle.im.android.R;

/**
 * 一键撤回插件
 */
public class RecallPlugin extends MorePlugin {
    public static final String ID = "recall";
    public static final int REQ = 12010;

    public RecallPlugin(Callback callback) {
        super(callback);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public int getIconRes() {
        return R.drawable.ic_recall;
    }

    @Override
    public String getLabel(Context ctx) {
        return ctx.getString(R.string.quick_recall);
    }

    @Override
    public String getAction() {
        return ID;
    }

    @Override
    public String[] getRequiredPermissions() {
        return new String[]{};
    }

    @Override
    public void onClick(Activity activity) {
        // TODO: 实现一键撤回功能
        // 目前只是占位，不执行任何操作
    }

    @Override
    public void setHostActivity(Activity activity) {

    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return false;
    }
}
