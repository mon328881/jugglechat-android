package com.juggle.im.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 记录需要在会话列表中隐藏的会话（例如用户主动退出的群聊）。
 * 通过 SharedPreferences 持久化，确保应用重启后依然生效。
 */
public final class HiddenConversationStore {
    private static final String PREF_NAME = "hidden_conversations";
    private static final String KEY_IDS = "hidden_ids";

    private HiddenConversationStore() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void addHidden(Context context, String conversationId) {
        if (context == null || conversationId == null) return;
        SharedPreferences sp = prefs(context);
        Set<String> oldSet = sp.getStringSet(KEY_IDS, Collections.emptySet());
        // 需要拷贝一份再修改，避免直接修改原始引用
        Set<String> newSet = new HashSet<>(oldSet);
        newSet.add(conversationId);
        sp.edit().putStringSet(KEY_IDS, newSet).apply();
    }

    public static boolean isHidden(Context context, String conversationId) {
        if (context == null || conversationId == null) return false;
        SharedPreferences sp = prefs(context);
        Set<String> set = sp.getStringSet(KEY_IDS, Collections.emptySet());
        return set != null && set.contains(conversationId);
    }

    public static void removeHidden(Context context, String conversationId) {
        if (context == null || conversationId == null) return;
        SharedPreferences sp = prefs(context);
        Set<String> oldSet = sp.getStringSet(KEY_IDS, Collections.emptySet());
        if (oldSet == null || !oldSet.contains(conversationId)) return;
        Set<String> newSet = new HashSet<>(oldSet);
        newSet.remove(conversationId);
        sp.edit().putStringSet(KEY_IDS, newSet).apply();
    }
}
