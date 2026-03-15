package com.juggle.im.android.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.juggle.im.android.model.ConfigUtils;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 版本检查工具类（从 jugglechat-android-master 同步）
 */
public final class UpdateChecker {

    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private static final OkHttpClient HTTP = buildHttpClient();

    private UpdateChecker() {
    }

    /**
     * 获取本地应用版本名称
     */
    public static String getLocalVersionName(Context context) {
        AppVersion v = getLocalVersion(context);
        return v != null ? v.versionName : null;
    }

    /**
     * 获取本地应用版本号
     */
    public static int getLocalVersionCode(Context context) {
        AppVersion v = getLocalVersion(context);
        return v != null ? v.versionCode : -1;
    }

    /**
     * 检查更新：从 ConfigUtils.appDownloadPageUrl 读取下载页，拉取 page.json 进行版本比对。
     * 使用 WeakReference 持有 Activity，避免异步回调导致泄漏。
     */
    public static void checkForUpdate(@NonNull Activity activity) {
        final WeakReference<Activity> activityRef = new WeakReference<>(activity);
        String base = ConfigUtils.appDownloadPageUrl;
        if (TextUtils.isEmpty(base)) {
            Toast.makeText(activity, "未配置下载页地址", Toast.LENGTH_SHORT).show();
            return;
        }

        String baseNorm = normalizeBaseUrl(base);
        if (TextUtils.isEmpty(baseNorm)) {
            Toast.makeText(activity, "下载页地址不正确", Toast.LENGTH_SHORT).show();
            return;
        }

        String jsonUrl = baseNorm + "/page.json";
        Toast.makeText(activity, "正在检查更新…", Toast.LENGTH_SHORT).show();

        Request req = new Request.Builder().url(jsonUrl).get().build();
        HTTP.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                MAIN.post(() -> {
                    Activity a = activityRef.get();
                    if (!isActivityUsable(a)) return;
                    Toast.makeText(a, "检查更新失败：" + safeMsg(e.getMessage()), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : null;
                MAIN.post(() -> {
                    Activity a = activityRef.get();
                    if (!isActivityUsable(a)) return;
                    if (!response.isSuccessful()) {
                        Toast.makeText(a, "检查更新失败：网络错误 " + response.code(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (TextUtils.isEmpty(body)) {
                        Toast.makeText(a, "检查更新失败：返回为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    UpdateInfo info = parseUpdateInfo(body, baseNorm);
                    if (info == null) {
                        Toast.makeText(a, "检查更新失败：解析失败", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    AppVersion local = getLocalVersion(a);
                    if (local == null) {
                        Toast.makeText(a, "检查更新失败：无法获取当前版本", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (isNewerThanCurrent(local, info)) {
                        showUpdateDialog(a, local, info);
                    } else {
                        Toast.makeText(a, "已是最新版本", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private static boolean isNewerThanCurrent(AppVersion local, UpdateInfo info) {
        int localCode = local.versionCode;
        if (info.versionCode > 0) {
            return info.versionCode > localCode;
        }
        if (!TextUtils.isEmpty(info.versionName)) {
            return compareVersionName(info.versionName, local.versionName) > 0;
        }
        return false;
    }

    private static void showUpdateDialog(@NonNull Activity activity, @NonNull AppVersion local, @NonNull UpdateInfo info) {
        String latestLabel = !TextUtils.isEmpty(info.versionName) ? ("v" + info.versionName) : ("build " + info.versionCode);
        StringBuilder msg = new StringBuilder();
        msg.append("当前版本：v").append(local.versionName != null ? local.versionName : "--").append("\n");
        msg.append("最新版本：").append(latestLabel);
        if (!TextUtils.isEmpty(info.changelog)) {
            msg.append("\n\n更新内容：\n").append(info.changelog.trim());
        }

        AlertDialog.Builder b = new AlertDialog.Builder(activity)
                .setTitle("发现新版本")
                .setMessage(msg.toString())
                .setPositiveButton("前往浏览器下载", (d, which) -> downloadAndInstall(activity, info));

        if (info.force) {
            b.setCancelable(false);
        } else {
            b.setNegativeButton("取消", null);
        }

        AlertDialog dialog = b.show();
        try {
            // 使用系统颜色，避免依赖不存在的项目自定义颜色
            int primary = activity.getResources().getColor(android.R.color.holo_blue_light);
            int gray = activity.getResources().getColor(android.R.color.darker_gray);
            if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(primary);
            }
            if (dialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(gray);
            }
        } catch (Exception ignored) {
        }
    }

    private static void downloadAndInstall(@NonNull Activity activity, @NonNull UpdateInfo info) {
        if (TextUtils.isEmpty(info.apkUrl)) {
            Toast.makeText(activity, "下载地址为空", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(activity, "即将跳转到浏览器下载最新安装包", Toast.LENGTH_SHORT).show();
        openInBrowser(activity, info.apkUrl);
    }

    private static void openInBrowser(@NonNull Activity activity, @NonNull String url) {
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(activity, "下载地址为空", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            activity.startActivity(i);
        } catch (Exception e) {
            Toast.makeText(activity, "无法打开下载链接", Toast.LENGTH_SHORT).show();
        }
    }

    private static UpdateInfo parseUpdateInfo(String json, String baseNorm) {
        try {
            JsonElement root = new JsonParser().parse(json);
            if (root == null || !root.isJsonObject()) return null;
            JsonObject jo = root.getAsJsonObject();
            if (jo.has("data") && jo.get("data").isJsonObject()) {
                jo = jo.getAsJsonObject("data");
            }

            UpdateInfo info = new UpdateInfo();
            info.versionCode = optInt(jo, "versionCode", "version_code", "apkVersionCode", "apk_version_code");
            info.versionName = optString(jo, "versionName", "version_name", "apkVersionName", "apk_version_name");
            info.changelog = optString(jo, "changelog", "changeLog", "desc", "description", "notes", "note");
            info.force = optBoolean(jo, "force", "forceUpdate", "force_update", "mandatory");

            String apk = optString(jo, "apkUrl", "apk_url", "downloadUrl", "download_url", "url", "apk");
            info.apkUrl = normalizeDownloadUrl(baseNorm, apk);
            if (TextUtils.isEmpty(info.apkUrl)) {
                info.apkUrl = baseNorm + "/android.apk";
            }
            return info;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static String normalizeBaseUrl(String base) {
        if (base == null) return null;
        String s = base.trim();
        if (s.isEmpty()) return s;
        while (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    private static String normalizeDownloadUrl(String baseNorm, String maybeUrl) {
        if (TextUtils.isEmpty(baseNorm)) return null;
        if (TextUtils.isEmpty(maybeUrl)) return null;
        String s = maybeUrl.trim();
        if (s.isEmpty()) return null;
        if (s.startsWith("http://") || s.startsWith("https://")) {
            return s;
        }
        while (s.startsWith("/")) {
            s = s.substring(1);
        }
        return baseNorm + "/" + s;
    }

    private static int optInt(JsonObject jo, String... keys) {
        for (String k : keys) {
            if (!jo.has(k) || jo.get(k).isJsonNull()) continue;
            try {
                JsonElement e = jo.get(k);
                if (e.isJsonPrimitive()) {
                    if (e.getAsJsonPrimitive().isNumber()) return e.getAsInt();
                    if (e.getAsJsonPrimitive().isString()) {
                        String s = e.getAsString();
                        if (TextUtils.isEmpty(s)) continue;
                        return Integer.parseInt(s.trim());
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return -1;
    }

    private static boolean optBoolean(JsonObject jo, String... keys) {
        for (String k : keys) {
            if (!jo.has(k) || jo.get(k).isJsonNull()) continue;
            try {
                JsonElement e = jo.get(k);
                if (e.isJsonPrimitive()) {
                    if (e.getAsJsonPrimitive().isBoolean()) return e.getAsBoolean();
                    if (e.getAsJsonPrimitive().isNumber()) return e.getAsInt() != 0;
                    if (e.getAsJsonPrimitive().isString()) {
                        String s = e.getAsString();
                        if (TextUtils.isEmpty(s)) continue;
                        s = s.trim().toLowerCase();
                        return "1".equals(s) || "true".equals(s) || "yes".equals(s);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    private static String optString(JsonObject jo, String... keys) {
        for (String k : keys) {
            if (!jo.has(k) || jo.get(k).isJsonNull()) continue;
            try {
                JsonElement e = jo.get(k);
                if (e.isJsonPrimitive()) {
                    String s = e.getAsString();
                    if (!TextUtils.isEmpty(s)) return s;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static int compareVersionName(String remote, String local) {
        if (TextUtils.isEmpty(remote) || TextUtils.isEmpty(local)) return 0;
        int[] r = parseVersionParts(remote);
        int[] l = parseVersionParts(local);
        int max = Math.max(r.length, l.length);
        for (int i = 0; i < max; i++) {
            int rv = i < r.length ? r[i] : 0;
            int lv = i < l.length ? l[i] : 0;
            if (rv != lv) return rv > lv ? 1 : -1;
        }
        return 0;
    }

    private static int[] parseVersionParts(String v) {
        try {
            String[] parts = v.trim().split("[^0-9]+");
            int count = 0;
            for (String p : parts) {
                if (!TextUtils.isEmpty(p)) count++;
            }
            int[] out = new int[count];
            int idx = 0;
            for (String p : parts) {
                if (TextUtils.isEmpty(p)) continue;
                try {
                    out[idx++] = Integer.parseInt(p);
                } catch (Exception ignored) {
                    out[idx++] = 0;
                }
            }
            return out;
        } catch (Exception ignored) {
            return new int[0];
        }
    }

    private static boolean isActivityUsable(Activity activity) {
        if (activity == null) return false;
        if (activity.isFinishing()) return false;
        try {
            return !activity.isDestroyed();
        } catch (Throwable ignored) {
            return true;
        }
    }

    private static String safeMsg(String s) {
        if (TextUtils.isEmpty(s)) return "网络异常";
        return s;
    }

    private static AppVersion getLocalVersion(Context context) {
        if (context == null) return null;
        try {
            PackageManager pm = context.getPackageManager();
            if (pm == null) return null;
            String pkg = context.getPackageName();
            PackageInfo pi = pm.getPackageInfo(pkg, 0);
            if (pi == null) return null;
            AppVersion v = new AppVersion();
            v.versionName = pi.versionName;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                v.versionCode = (int) pi.getLongVersionCode();
            } else {
                //noinspection deprecation
                v.versionCode = pi.versionCode;
            }
            return v;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static OkHttpClient buildHttpClient() {
        try {
            return new OkHttpClient.Builder()
                    .build();
        } catch (Throwable ignored) {
            return new OkHttpClient();
        }
    }

    private static final class UpdateInfo {
        int versionCode = -1;
        String versionName;
        String apkUrl;
        String changelog;
        boolean force;
    }

    private static final class AppVersion {
        int versionCode;
        String versionName;
    }
}
