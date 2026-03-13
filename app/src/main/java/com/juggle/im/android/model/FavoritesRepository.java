package com.juggle.im.android.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 收藏数据存储（SharedPreferences + JSON）
 */
public class FavoritesRepository {
    private static final String PREFS_NAME = "favorites";
    private static final String KEY_LIST = "list";

    private final SharedPreferences prefs;
    private final Context context;

    public FavoritesRepository(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void add(FavoriteItem item) {
        List<FavoriteItem> list = getAll();
        if (item.getId() == null || item.getId().isEmpty()) {
            item.setId(UUID.randomUUID().toString());
        }
        item.setTimestamp(System.currentTimeMillis());
        list.add(0, item);
        save(list);
    }

    public void remove(String id) {
        List<FavoriteItem> list = getAll();
        for (int i = 0; i < list.size(); i++) {
            if (id.equals(list.get(i).getId())) {
                FavoriteItem removed = list.remove(i);
                if (removed.getLocalPath() != null) {
                    File f = new File(removed.getLocalPath());
                    if (f.exists()) f.delete();
                }
                save(list);
                return;
            }
        }
    }

    public List<FavoriteItem> getAll() {
        String json = prefs.getString(KEY_LIST, "[]");
        List<FavoriteItem> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                FavoriteItem item = new FavoriteItem();
                item.setId(o.optString("id"));
                item.setType(o.optString("type"));
                item.setContent(o.optString("content"));
                item.setLocalPath(o.optString("localPath"));
                item.setUrl(o.optString("url"));
                item.setThumbnailUrl(o.optString("thumbnailUrl"));
                item.setName(o.optString("name"));
                item.setSize(o.optLong("size", 0));
                item.setTimestamp(o.optLong("timestamp", 0));
                list.add(item);
            }
        } catch (Exception e) {
            Log.e("FavoritesRepository", "parse error", e);
        }
        return list;
    }

    private void save(List<FavoriteItem> list) {
        try {
            JSONArray arr = new JSONArray();
            for (FavoriteItem item : list) {
                JSONObject o = new JSONObject();
                o.put("id", item.getId());
                o.put("type", item.getType());
                o.put("content", item.getContent());
                o.put("localPath", item.getLocalPath() != null ? item.getLocalPath() : "");
                o.put("url", item.getUrl() != null ? item.getUrl() : "");
                o.put("thumbnailUrl", item.getThumbnailUrl() != null ? item.getThumbnailUrl() : "");
                o.put("name", item.getName() != null ? item.getName() : "");
                o.put("size", item.getSize());
                o.put("timestamp", item.getTimestamp());
                arr.put(o);
            }
            prefs.edit().putString(KEY_LIST, arr.toString()).apply();
        } catch (Exception e) {
            Log.e("FavoritesRepository", "save error", e);
        }
    }

    public FavoriteItem getById(String id) {
        for (FavoriteItem item : getAll()) {
            if (id.equals(item.getId())) return item;
        }
        return null;
    }
}
