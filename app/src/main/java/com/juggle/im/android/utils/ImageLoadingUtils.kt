package com.juggle.im.android.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.juggle.im.android.R

/**
 * 图片加载工具类
 * 使用 Glide 库加载和缓存图片
 */
object ImageLoadingUtils {

    /**
     * 加载圆形头像
     * @param context 上下文
     * @param imageView 目标 ImageView
     * @param imageUrl 图片 URL
     * @param placeholderResId 占位符资源 ID
     */
    fun loadCircularAvatar(
        context: Context,
        imageView: ImageView,
        imageUrl: String?,
        placeholderResId: Int = R.drawable.ic_default_avatar
    ) {
        if (imageUrl.isNullOrEmpty()) {
            imageView.setImageResource(placeholderResId)
            return
        }

        val requestOptions = RequestOptions()
            .transform(CircleCrop())
            .placeholder(placeholderResId)
            .error(placeholderResId)
            .diskCacheStrategy(DiskCacheStrategy.ALL)

        Glide.with(context)
            .load(imageUrl)
            .apply(requestOptions)
            .into(imageView)
    }

    /**
     * 加载圆形头像（带默认占位符）
     * @param context 上下文
     * @param imageView 目标 ImageView
     * @param imageUrl 图片 URL
     */
    fun loadCircularAvatarWithDefault(
        context: Context,
        imageView: ImageView,
        imageUrl: String?
    ) {
        loadCircularAvatar(context, imageView, imageUrl, R.drawable.ic_default_avatar)
    }

    /**
     * 加载图片缩略图
     * @param context 上下文
     * @param imageView 目标 ImageView
     * @param imageUrl 图片 URL
     * @param placeholderResId 占位符资源 ID
     */
    fun loadThumbnail(
        context: Context,
        imageView: ImageView,
        imageUrl: String?,
        placeholderResId: Int = R.drawable.ic_image_placeholder
    ) {
        if (imageUrl.isNullOrEmpty()) {
            imageView.setImageResource(placeholderResId)
            return
        }

        val requestOptions = RequestOptions()
            .placeholder(placeholderResId)
            .error(placeholderResId)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .override(300, 300)

        Glide.with(context)
            .load(imageUrl)
            .apply(requestOptions)
            .into(imageView)
    }

    /**
     * 加载图片（不做任何变换）
     * @param context 上下文
     * @param imageView 目标 ImageView
     * @param imageUrl 图片 URL
     * @param placeholderResId 占位符资源 ID
     */
    fun loadImage(
        context: Context,
        imageView: ImageView,
        imageUrl: String?,
        placeholderResId: Int = R.drawable.ic_image_placeholder
    ) {
        if (imageUrl.isNullOrEmpty()) {
            imageView.setImageResource(placeholderResId)
            return
        }

        val requestOptions = RequestOptions()
            .placeholder(placeholderResId)
            .error(placeholderResId)
            .diskCacheStrategy(DiskCacheStrategy.ALL)

        Glide.with(context)
            .load(imageUrl)
            .apply(requestOptions)
            .into(imageView)
    }

    /**
     * 加载图片为 Bitmap（异步）
     * @param context 上下文
     * @param imageUrl 图片 URL
     * @param onBitmapLoaded 加载完成回调
     * @param onLoadFailed 加载失败回调
     */
    fun loadImageAsBitmap(
        context: Context,
        imageUrl: String?,
        onBitmapLoaded: (Bitmap) -> Unit,
        onLoadFailed: () -> Unit = {}
    ) {
        if (imageUrl.isNullOrEmpty()) {
            onLoadFailed()
            return
        }

        Glide.with(context)
            .asBitmap()
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    onBitmapLoaded(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    onLoadFailed()
                }
            })
    }

    /**
     * 清除所有缓存
     * @param context 上下文
     */
    fun clearAllCache(context: Context) {
        Glide.get(context).clearMemory()
        Thread {
            Glide.get(context).clearDiskCache()
        }.start()
    }

    /**
     * 清除内存缓存
     * @param context 上下文
     */
    fun clearMemoryCache(context: Context) {
        Glide.get(context).clearMemory()
    }

    /**
     * 预加载图片
     * @param context 上下文
     * @param imageUrl 图片 URL
     */
    fun preloadImage(context: Context, imageUrl: String?) {
        if (imageUrl.isNullOrEmpty()) return

        Glide.with(context)
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .preload()
    }
}
