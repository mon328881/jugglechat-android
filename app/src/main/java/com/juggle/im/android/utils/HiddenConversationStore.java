package com.juggle.im.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 记录需要在会话列表中隐藏的会话（例如用户主动退出的群聊）。
 * 通过 SharedPreferences 持久化；内存缓存避免主线程读盘触发 StrictMode。
 */
public final class HiddenConversationStore {
    private static final String PREF_NAME = "hidden_conversations";
    private static final String KEY_IDS = "hidden_ids";
    private static volatile Set<String> sHiddenIdsCache;
    private static final Object sLock = new Object();
    private static final AtomicBoolean sLoadStarted = new AtomicBoolean(false);

    private HiddenConversationStore() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 在子线程预加载缓存，避免主线程首次 isHidden 时读盘。建议在 Application 或 MainActivity 中调用。
     */
    public static void loadCacheAsync(final Context context) {
        if (context == null || sLoadStarted.getAndSet(true)) return;
        new Thread(() -> {
            synchronized (sLock) {
                if (sHiddenIdsCache == null) {
                    Set<String> set = prefs(context).getStringSet(KEY_IDS, Collections.emptySet());
                    sHiddenIdsCache = set != null ? new HashSet<>(set) : new HashSet<>();
                }
            }
        }).start();
    }

    /** 仅读内存缓存，不触发磁盘。若缓存未加载则返回 false（未隐藏）。 */
    public static boolean isHidden(Context context, String conversationId) {
        if (context == null || conversationId == null) return false;
        Set<String> cache = sHiddenIdsCache;
        if (cache == null) return false;
        return cache.contains(conversationId);
    }

    public static void addHidden(Context context, String conversationId) {
        if (context == null || conversationId == null) return;
        new Thread(() -> {
            SharedPreferences sp = prefs(context);
            Set<String> oldSet = sp.getStringSet(KEY_IDS, Collections.emptySet());
            Set<String> newSet = new HashSet<>(oldSet != null ? oldSet : Collections.emptySet());
            newSet.add(conversationId);
            sp.edit().putStringSet(KEY_IDS, newSet).apply();
            synchronized (sLock) {
                if (sHiddenIdsCache != null) sHiddenIdsCache.add(conversationId);
                else sHiddenIdsCache = new HashSet<>(newSet);
            }
        }).start();
    }

    public static void removeHidden(Context context, String conversationId) {
        if (context == null || conversationId == null) return;
        new Thread(() -> {
            SharedPreferences sp = prefs(context);
            Set<String> oldSet = sp.getStringSet(KEY_IDS, Collections.emptySet());
            if (oldSet == null || !oldSet.contains(conversationId)) return;
            Set<String> newSet = new HashSet<>(oldSet);
            newSet.remove(conversationId);
            sp.edit().putStringSet(KEY_IDS, newSet).apply();
            synchronized (sLock) {
                if (sHiddenIdsCache != null) sHiddenIdsCache.remove(conversationId);
            }
        }).start();
    }
}
