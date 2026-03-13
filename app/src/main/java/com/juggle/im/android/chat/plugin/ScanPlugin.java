package com.juggle.im.android.chat.plugin;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import com.juggle.im.android.R;
import com.juggle.im.android.chat.ScanActivity;

/**
 * 扫一扫插件
 */
public class ScanPlugin extends MorePlugin {
    public static final String ID = "scan";
    public static final int REQ = 12011;

    public ScanPlugin(Callback callback) {
        super(callback);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public int getIconRes() {
        return R.drawable.ic_scan;
    }

    @Override
    public String getLabel(Context ctx) {
        return ctx.getString(R.string.scan);
    }

    @Override
    public String getAction() {
        return ID;
    }

    @Override
    public String[] getRequiredPermissions() {
        return new String[]{Manifest.permission.CAMERA};
    }

    @Override
    public void onClick(Activity activity) {
        Activity act = activity != null ? activity : host;
        if (act == null) return;
        
        // 检查权限
        if (ContextCompat.checkSelfPermission(act, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            callback.requestPermissions(getRequiredPermissions(), REQ, getId());
            return;
        }
        
        // 启动扫描Activity
        callback.registerForActivityResult(REQ, this);
        Intent intent = new Intent(act, ScanActivity.class);
        act.startActivityForResult(intent, REQ);
    }

    @Override
    public void setHostActivity(Activity activity) {
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQ) return false;
        if (resultCode != Activity.RESULT_OK) return true;
        if (data == null) return true;
        
        String scanResult = data.getStringExtra("scan_result");
        if (scanResult != null && callback != null) {
            callback.onPluginAction(getId(), getAction(), scanResult);
        }
        return true;
    }
}
