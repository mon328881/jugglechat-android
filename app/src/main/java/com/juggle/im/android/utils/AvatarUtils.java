package com.juggle.im.android.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.TypedValue;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.juggle.im.android.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 头像工具类：支持加载远程头像或生成首字母头像
 */
public final class AvatarUtils {
    private AvatarUtils() {
    }

    private static final int TAG_URL = 0x7F0A0001;
    private static final int TAG_VIDEO_COVER = 0x7F0A0002;
    private static final ExecutorService videoCoverExecutor = Executors.newFixedThreadPool(2);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * 加载头像：优先使用远程URL，如果URL为空则生成首字母头像
     *
     * @param iv   目标ImageView
     * @param url  远程头像URL（可为空）
     * @param name 用户名或昵称（用于生成首字母头像）
     */
    public static void loadAvatar(ImageView iv, String url, String name) {
        if (iv == null) return;
        Context ctx = iv.getContext();

        // 检查URL是否与当前已加载的相同，避免重复加载导致闪烁
        String currentUrl = (String) iv.getTag(TAG_URL);
        if (!TextUtils.isEmpty(url)) {
            if (url.equals(currentUrl)) {
                return; // URL相同，跳过加载
            }
            iv.setTag(TAG_URL, url);

            Glide.with(iv)
                    .load(url)
                    .centerCrop()
                    .transform(new CircleCrop())
                    .placeholder(R.drawable.ic_avatar_loading)
                    .error(R.drawable.default_avatar)
                    .dontAnimate()
                    .into(iv);
            return;
        }

        // 处理没有URL的情况（生成首字母头像）
        String generatedTag = "generated:" + name;
        if (generatedTag.equals(currentUrl)) {
            return; // 相同的生成头像，跳过
        }
        iv.setTag(TAG_URL, generatedTag);

        String initial = extractInitial(name);
        int sizePx = dpToPx(ctx, 40);
        Bitmap bmp = createInitialsBitmap(sizePx, initial);
        Glide.with(iv).load(bmp).circleCrop().dontAnimate().into(iv);
    }

    /**
     * 加载图片
     */
    public static void loadImage(ImageView iv, String url) {
        Glide.with(iv)
                .load(url)
                .centerCrop()
                .placeholder(R.drawable.default_image)
                .dontAnimate()
                .into(iv);
    }

    /**
     * 加载朋友圈视频封面：优先snapshotUrl，若无则从videoUrl截取首帧作为封面
     */
    public static void loadVideoCover(ImageView iv, String snapshotUrl, String videoUrl) {
        if (iv == null) return;
        if (!TextUtils.isEmpty(snapshotUrl)) {
            iv.setTag(TAG_VIDEO_COVER, null);
            loadImage(iv, snapshotUrl);
            return;
        }
        if (TextUtils.isEmpty(videoUrl)) {
            iv.setImageResource(R.drawable.default_image);
            return;
        }
        iv.setTag(TAG_VIDEO_COVER, videoUrl);
        iv.setImageResource(R.drawable.default_image);
        videoCoverExecutor.execute(() -> {
            Bitmap frame = null;
            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(videoUrl);
                frame = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                retriever.release();
            } catch (Throwable ignored) {
            }
            Bitmap finalFrame = frame;
            mainHandler.post(() -> {
                if (iv == null) return;
                Object tag = iv.getTag(TAG_VIDEO_COVER);
                if (!videoUrl.equals(tag)) return;
                // 若 Context 为 Activity 且已销毁/finishing，不再更新，避免泄漏与异常
                android.content.Context ctx = iv.getContext();
                if (ctx instanceof android.app.Activity) {
                    android.app.Activity act = (android.app.Activity) ctx;
                    if (act.isFinishing() || (android.os.Build.VERSION.SDK_INT >= 17 && act.isDestroyed())) return;
                }
                if (finalFrame != null) {
                    iv.setImageBitmap(finalFrame);
                    iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } else {
                    iv.setImageResource(R.drawable.default_image);
                }
            });
        });
    }

    private static String extractInitial(String name) {
        if (TextUtils.isEmpty(name)) return "";
        name = name.trim();
        if (name.length() == 0) return "";
        int cp = name.codePointAt(0);
        return new String(Character.toChars(cp)).toUpperCase();
    }

    private static Bitmap createInitialsBitmap(int sizePx, String initial) {
        if (sizePx <= 0) sizePx = 64;
        Bitmap bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);

        // 背景渐变色
        int[] colors = colorsForString(initial);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Shader shader = new LinearGradient(0, 0, sizePx, sizePx, colors[0], colors[1], Shader.TileMode.CLAMP);
        paint.setShader(shader);
        RectF r = new RectF(0, 0, sizePx, sizePx);
        c.drawRoundRect(r, sizePx / 2f, sizePx / 2f, paint);

        if (!initial.isEmpty()) {
            Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(0xFFFFFFFF);
            textPaint.setTextSize(sizePx * 0.45f);
            textPaint.setTextAlign(Paint.Align.CENTER);
            Paint.FontMetrics fm = textPaint.getFontMetrics();
            float x = sizePx / 2f;
            float y = sizePx / 2f - (fm.ascent + fm.descent) / 2f;
            c.drawText(initial, x, y, textPaint);
        }

        return bmp;
    }

    private static int[] colorsForString(String s) {
        if (s == null || s.isEmpty()) {
            return new int[]{0xFF888888, 0xFFBBBBBB};
        }
        int h = s.hashCode();
        int r1 = 80 + (Math.abs(h) % 120);
        int g1 = 80 + (Math.abs(h / 31) % 120);
        int b1 = 80 + (Math.abs(h / 17) % 120);

        int r2 = 120 + (Math.abs(h / 13) % 120);
        int g2 = 120 + (Math.abs(h / 7) % 120);
        int b2 = 120 + (Math.abs(h / 3) % 120);

        int c1 = 0xFF000000 | ((r1 & 0xFF) << 16) | ((g1 & 0xFF) << 8) | (b1 & 0xFF);
        int c2 = 0xFF000000 | ((r2 & 0xFF) << 16) | ((g2 & 0xFF) << 8) | (b2 & 0xFF);
        return new int[]{c1, c2};
    }

    private static int dpToPx(Context ctx, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, ctx.getResources().getDisplayMetrics());
    }
}
