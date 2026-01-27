package com.juggle.im.android.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 视频消息管理器
 * 用于管理视频消息的选择、压缩、发送和接收
 * 验证: 需求 18.1, 18.2, 18.3, 18.4, 18.5
 */
object VideoMessageManager {
    
    private const val TAG = "VideoMessageManager"
    private const val VIDEO_CACHE_DIR = "video_cache"
    private const val COMPRESSED_VIDEO_DIR = "compressed_videos"
    
    /**
     * 视频消息数据类
     */
    data class VideoMessage(
        val id: String,
        val videoPath: String,
        val thumbnailPath: String?,
        val duration: Long,
        val fileSize: Long,
        val width: Int,
        val height: Int,
        val isCompressed: Boolean,
        val originalSize: Long,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * 选择视频
     * @param context 上下文
     * @param videoUri 视频 URI
     * @return 视频消息对象，如果选择失败返回 null
     */
    fun selectVideo(context: Context, videoUri: Uri): VideoMessage? {
        return try {
            val videoPath = copyVideoToCache(context, videoUri)
            if (videoPath == null) {
                Log.e(TAG, "复制视频到缓存失败")
                return null
            }
            
            // 获取视频信息
            val duration = VideoUtils.getVideoDurationFromUri(context, videoUri)
            val fileSize = VideoUtils.getVideoFileSize(videoPath)
            val resolution = VideoUtils.getVideoResolution(videoPath)
            
            // 生成缩略图
            val thumbnailPath = generateThumbnail(context, videoPath)
            
            VideoMessage(
                id = generateVideoId(),
                videoPath = videoPath,
                thumbnailPath = thumbnailPath,
                duration = duration,
                fileSize = fileSize,
                width = resolution?.first ?: 0,
                height = resolution?.second ?: 0,
                isCompressed = false,
                originalSize = fileSize
            )
        } catch (e: Exception) {
            Log.e(TAG, "选择视频失败", e)
            null
        }
    }
    
    /**
     * 压缩视频
     * @param context 上下文
     * @param videoMessage 视频消息
     * @return 压缩后的视频消息，如果压缩失败返回原消息
     */
    fun compressVideo(context: Context, videoMessage: VideoMessage): VideoMessage {
        return try {
            // 检查是否需要压缩
            if (!VideoCompressionUtils.shouldCompress(videoMessage.fileSize)) {
                Log.d(TAG, "文件大小不需要压缩")
                return videoMessage
            }
            
            // 创建压缩输出路径
            val compressedPath = getCompressedVideoPath(context, videoMessage.id)
            
            // 执行压缩
            val success = VideoCompressionUtils.compressVideo(
                context,
                videoMessage.videoPath,
                compressedPath
            )
            
            if (success) {
                val compressedSize = File(compressedPath).length()
                val ratio = VideoCompressionUtils.getCompressionRatio(
                    videoMessage.fileSize,
                    compressedSize
                )
                Log.d(TAG, "视频压缩成功，压缩率: $ratio%")
                
                videoMessage.copy(
                    videoPath = compressedPath,
                    fileSize = compressedSize,
                    isCompressed = true
                )
            } else {
                Log.w(TAG, "视频压缩失败，使用原文件")
                videoMessage
            }
        } catch (e: Exception) {
            Log.e(TAG, "压缩视频异常", e)
            videoMessage
        }
    }
    
    /**
     * 复制视频到缓存目录
     */
    private fun copyVideoToCache(context: Context, videoUri: Uri): String? {
        return try {
            val cacheDir = File(context.cacheDir, VIDEO_CACHE_DIR)
            cacheDir.mkdirs()
            
            val fileName = "video_${System.currentTimeMillis()}.mp4"
            val outputFile = File(cacheDir, fileName)
            
            context.contentResolver.openInputStream(videoUri)?.use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            outputFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "复制视频到缓存失败", e)
            null
        }
    }
    
    /**
     * 生成视频缩略图
     */
    private fun generateThumbnail(context: Context, videoPath: String): String? {
        return try {
            val thumbnail = VideoUtils.getVideoThumbnail(videoPath)
            if (thumbnail != null) {
                val cacheDir = File(context.cacheDir, VIDEO_CACHE_DIR)
                cacheDir.mkdirs()
                
                val fileName = "thumbnail_${System.currentTimeMillis()}.jpg"
                val thumbnailFile = File(cacheDir, fileName)
                
                thumbnailFile.outputStream().use { output ->
                    thumbnail.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, output)
                }
                
                thumbnailFile.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "生成缩略图失败", e)
            null
        }
    }
    
    /**
     * 获取压缩视频路径
     */
    private fun getCompressedVideoPath(context: Context, videoId: String): String {
        val compressedDir = File(context.cacheDir, COMPRESSED_VIDEO_DIR)
        compressedDir.mkdirs()
        return File(compressedDir, "compressed_$videoId.mp4").absolutePath
    }
    
    /**
     * 生成视频 ID
     */
    private fun generateVideoId(): String {
        return "video_${System.currentTimeMillis()}_${(Math.random() * 10000).toInt()}"
    }
    
    /**
     * 清理缓存
     */
    fun clearCache(context: Context) {
        try {
            val cacheDir = File(context.cacheDir, VIDEO_CACHE_DIR)
            if (cacheDir.exists()) {
                cacheDir.deleteRecursively()
            }
            
            val compressedDir = File(context.cacheDir, COMPRESSED_VIDEO_DIR)
            if (compressedDir.exists()) {
                compressedDir.deleteRecursively()
            }
            
            Log.d(TAG, "缓存清理成功")
        } catch (e: Exception) {
            Log.e(TAG, "清理缓存失败", e)
        }
    }
    
    /**
     * 删除视频文件
     */
    fun deleteVideo(videoMessage: VideoMessage) {
        try {
            File(videoMessage.videoPath).delete()
            videoMessage.thumbnailPath?.let { File(it).delete() }
            Log.d(TAG, "视频文件删除成功")
        } catch (e: Exception) {
            Log.e(TAG, "删除视频文件失败", e)
        }
    }
    
    /**
     * 获取视频信息字符串
     */
    fun getVideoInfoString(videoMessage: VideoMessage): String {
        val duration = VideoUtils.formatDuration(videoMessage.duration)
        val size = formatFileSize(videoMessage.fileSize)
        val resolution = "${videoMessage.width}x${videoMessage.height}"
        
        return "时长: $duration | 大小: $size | 分辨率: $resolution"
    }
    
    /**
     * 格式化文件大小
     */
    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
}
