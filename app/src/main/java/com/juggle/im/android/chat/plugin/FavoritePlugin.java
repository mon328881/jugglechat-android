package com.juggle.im.android.chat.plugin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.juggle.im.android.R;
import com.juggle.im.android.chat.FavoritesPickerActivity;

/**
 * 我的收藏插件
 */
public class FavoritePlugin extends MorePlugin {
    public static final String ID = "favorite";
    public static final int REQ = 12008;

    public FavoritePlugin(Callback callback) {
        super(callback);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public int getIconRes() {
        return R.drawable.ic_favorite;
    }

    @Override
    public String getLabel(Context ctx) {
        return ctx.getString(R.string.favorite);
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
        Activity act = activity != null ? activity : host;
        if (act == null) return;
        callback.registerForActivityResult(REQ, this);
        Intent intent = new Intent(act, FavoritesPickerActivity.class);
        act.startActivityForResult(intent, REQ);
    }

    @Override
    public void setHostActivity(Activity activity) {
        host = activity;
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQ) return false;
        if (resultCode != Activity.RESULT_OK || data == null) return true;
        java.util.ArrayList<com.juggle.im.android.model.FavoriteItem> list =
                (java.util.ArrayList<com.juggle.im.android.model.FavoriteItem>)
                        data.getSerializableExtra(FavoritesPickerActivity.EXTRA_SELECTED);
        if (list != null && callback != null) {
            callback.onPluginAction(getId(), getAction(), list);
        }
        return true;
    }
}
