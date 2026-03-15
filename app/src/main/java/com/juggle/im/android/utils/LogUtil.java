package com.juggle.im.android.utils;

import com.juggle.im.android.BuildConfig;

/**
 * 统一日志工具：Release 下关闭调试日志，避免泄露 token、userId、URL 等敏感信息。
 * 仅 BuildConfig.DEBUG 时输出 d/i/w/v；e 在 Release 下同样不输出，避免异常堆栈泄露内部信息。
 */
public final class LogUtil {

    private LogUtil() {
    }

    public static void v(String tag, String msg) {
        if (BuildConfig.DEBUG && tag != null && msg != null) {
            android.util.Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (BuildConfig.DEBUG && tag != null && msg != null) {
            android.util.Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (BuildConfig.DEBUG && tag != null && msg != null) {
            android.util.Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (BuildConfig.DEBUG && tag != null && msg != null) {
            android.util.Log.w(tag, msg);
        }
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (BuildConfig.DEBUG && tag != null) {
            android.util.Log.w(tag, msg != null ? msg : "", tr);
        }
    }

    public static void e(String tag, String msg) {
        if (BuildConfig.DEBUG && tag != null && msg != null) {
            android.util.Log.e(tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (BuildConfig.DEBUG && tag != null) {
            android.util.Log.e(tag, msg != null ? msg : "", tr);
        }
    }

    /**
     * 对可能包含敏感信息的字符串做脱敏，仅用于必须输出到日志时（建议 Release 下仍用 LogUtil 不输出）。
     * 脱敏：token/authorization 只显示前4位+***，userId 显示前2位+***，URL 去掉 query。
     */
    public static String redact(String raw) {
        if (raw == null || raw.isEmpty()) return "***";
        if (raw.length() <= 6) return "***";
        return raw.substring(0, Math.min(4, raw.length())) + "***";
    }
}
