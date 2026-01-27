package com.juggle.im.android.utils

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import java.io.File

/**
 * 视频处理工具类
 * 用于加载视频缩略图、获取视频信息等
 */
object VideoUtils {

    private const val TAG = "VideoUtils"

    /**
     * 获取视频缩略图
     * @param videoPath 视频文件路径
     * @return 缩略图 Bitmap，如果获取失败返回 null
     */
    fun getVideoThumbnail(videoPath: String): Bitmap? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoPath)
            val bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST)
            retriever.release()
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "获取视频缩略图失败: $videoPath", e)
            null
        }
    }

    /**
     * 获取视频缩略图（从 URI）
     * @param context 上下文
     * @param videoUri 视频 URI
     * @return 缩略图 Bitmap，如果获取失败返回 null
     */
    fun getVideoThumbnailFromUri(context: Context, videoUri: Uri): Bitmap? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, videoUri)
            val bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST)
            retriever.release()
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "获取视频缩略图失败: $videoUri", e)
            null
        }
    }

    /**
     * 加载视频缩略图到 ImageView
     * @param context 上下文
     * @param imageView 目标 ImageView
     * @param videoPath 视频文件路径
     * @param placeholderResId 占位符资源 ID
     */
    fun loadVideoThumbnail(
        context: Context,
        imageView: ImageView,
        videoPath: String,
        placeholderResId: Int = android.R.drawable.ic_media_play
    ) {
        val thumbnail = getVideoThumbnail(videoPath)
        if (thumbnail != null) {
            imageView.setImageBitmap(thumbnail)
        } else {
            imageView.setImageResource(placeholderResId)
        }
    }

    /**
     * 获取视频时长
     * @param videoPath 视频文件路径
     * @return 视频时长（毫秒），如果获取失败返回 0
     */
    fun getVideoDuration(videoPath: String): Long {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoPath)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            duration?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            Log.e(TAG, "获取视频时长失败: $videoPath", e)
            0L
        }
    }

    /**
     * 获取视频时长（从 URI）
     * @param context 上下文
     * @param videoUri 视频 URI
     * @return 视频时长（毫秒），如果获取失败返回 0
     */
    fun getVideoDurationFromUri(context: Context, videoUri: Uri): Long {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, videoUri)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            duration?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            Log.e(TAG, "获取视频时长失败: $videoUri", e)
            0L
        }
    }

    /**
     * 格式化视频时长
     * @param durationMs 时长（毫秒）
     * @return 格式化后的时长字符串（HH:MM:SS 或 MM:SS）
     */
    fun formatDuration(durationMs: Long): String {
        val totalSeconds = durationMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    /**
     * 获取视频分辨率
     * @param videoPath 视频文件路径
     * @return 视频分辨率 (宽, 高)，如果获取失败返回 null
     */
    fun getVideoResolution(videoPath: String): Pair<Int, Int>? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoPath)
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            retriever.release()
            if (width > 0 && height > 0) Pair(width, height) else null
        } catch (e: Exception) {
            Log.e(TAG, "获取视频分辨率失败: $videoPath", e)
            null
        }
    }

    /**
     * 获取视频文件大小
     * @param videoPath 视频文件路径
     * @return 文件大小（字节），如果获取失败返回 0
     */
    fun getVideoFileSize(videoPath: String): Long {
        return try {
            val file = File(videoPath)
            if (file.exists()) file.length() else 0L
        } catch (e: Exception) {
            Log.e(TAG, "获取视频文件大小失败: $videoPath", e)
            0L
        }
    }

    /**
     * 获取视频比特率
     * @param videoPath 视频文件路径
     * @return 比特率（bps），如果获取失败返回 0
     */
    fun getVideoBitrate(videoPath: String): Long {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoPath)
            val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toLongOrNull() ?: 0L
            retriever.release()
            bitrate
        } catch (e: Exception) {
            Log.e(TAG, "获取视频比特率失败: $videoPath", e)
            0L
        }
    }

    /**
     * 检查视频是否有效
     * @param videoPath 视频文件路径
     * @return 如果视频有效返回 true，否则返回 false
     */
    fun isValidVideo(videoPath: String): Boolean {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoPath)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            retriever.release()
            duration > 0
        } catch (e: Exception) {
            Log.e(TAG, "视频验证失败: $videoPath", e)
            false
        }
    }
}
