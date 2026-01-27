package com.juggle.im.android.utils

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.util.Log
import java.io.File
import kotlin.math.min

/**
 * 视频压缩工具类
 * 用于自动压缩视频文件，减少文件大小
 * 验证: 需求 18.3
 */
object VideoCompressionUtils {
    
    private const val TAG = "VideoCompressionUtils"
    
    // 压缩参数
    private const val TARGET_BITRATE = 1000000 // 1 Mbps
    private const val TARGET_FRAME_RATE = 24
    private const val TARGET_WIDTH = 1280
    private const val TARGET_HEIGHT = 720
    
    /**
     * 压缩视频
     * @param context 上下文
     * @param inputPath 输入视频路径
     * @param outputPath 输出视频路径
     * @return 是否压缩成功
     */
    fun compressVideo(
        context: Context,
        inputPath: String,
        outputPath: String
    ): Boolean {
        return try {
            val inputFile = File(inputPath)
            val outputFile = File(outputPath)
            
            // 检查输入文件
            if (!inputFile.exists()) {
                Log.e(TAG, "输入文件不存在: $inputPath")
                return false
            }
            
            // 创建输出目录
            outputFile.parentFile?.mkdirs()
            
            // 执行压缩
            performCompression(inputPath, outputPath)
            
            // 检查输出文件
            if (outputFile.exists() && outputFile.length() > 0) {
                Log.d(TAG, "视频压缩成功: $inputPath -> $outputPath")
                true
            } else {
                Log.e(TAG, "视频压缩失败: 输出文件为空")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "视频压缩异常: $inputPath", e)
            false
        }
    }
    
    /**
     * 执行压缩操作
     */
    private fun performCompression(inputPath: String, outputPath: String) {
        val extractor = MediaExtractor()
        extractor.setDataSource(inputPath)
        
        val muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        
        try {
            // 处理视频轨道
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
                
                if (mime.startsWith("video/")) {
                    extractor.selectTrack(i)
                    
                    // 创建压缩格式
                    val compressedFormat = createCompressedVideoFormat(format)
                    val trackIndex = muxer.addTrack(compressedFormat)
                    
                    muxer.start()
                    
                    // 复制视频数据
                    copyVideoTrack(extractor, muxer, trackIndex)
                } else if (mime.startsWith("audio/")) {
                    extractor.selectTrack(i)
                    val trackIndex = muxer.addTrack(format)
                    
                    // 复制音频数据
                    copyAudioTrack(extractor, muxer, trackIndex)
                }
            }
        } finally {
            muxer.stop()
            muxer.release()
            extractor.release()
        }
    }
    
    /**
     * 创建压缩后的视频格式
     */
    private fun createCompressedVideoFormat(originalFormat: MediaFormat): MediaFormat {
        val width = originalFormat.getInteger(MediaFormat.KEY_WIDTH)
        val height = originalFormat.getInteger(MediaFormat.KEY_HEIGHT)
        
        // 计算缩放尺寸
        val (scaledWidth, scaledHeight) = calculateScaledDimensions(width, height)
        
        return MediaFormat.createVideoFormat(
            MediaFormat.MIMETYPE_VIDEO_AVC,
            scaledWidth,
            scaledHeight
        ).apply {
            setInteger(MediaFormat.KEY_BIT_RATE, TARGET_BITRATE)
            setInteger(MediaFormat.KEY_FRAME_RATE, TARGET_FRAME_RATE)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodec.COLOR_FormatSurface)
        }
    }
    
    /**
     * 计算缩放尺寸
     */
    private fun calculateScaledDimensions(width: Int, height: Int): Pair<Int, Int> {
        val aspectRatio = width.toFloat() / height.toFloat()
        
        return if (width > height) {
            // 横屏
            val newWidth = min(width, TARGET_WIDTH)
            val newHeight = (newWidth / aspectRatio).toInt()
            Pair(newWidth, newHeight)
        } else {
            // 竖屏
            val newHeight = min(height, TARGET_HEIGHT)
            val newWidth = (newHeight * aspectRatio).toInt()
            Pair(newWidth, newHeight)
        }
    }
    
    /**
     * 复制视频轨道
     */
    private fun copyVideoTrack(
        extractor: MediaExtractor,
        muxer: MediaMuxer,
        trackIndex: Int
    ) {
        val buffer = android.media.MediaCodec.BufferInfo()
        
        while (true) {
            val sampleSize = extractor.readSampleData(
                android.nio.ByteBuffer.allocate(1024 * 1024),
                0
            )
            
            if (sampleSize < 0) break
            
            buffer.presentationTimeUs = extractor.sampleTime
            buffer.flags = extractor.sampleFlags
            buffer.size = sampleSize
            
            muxer.writeSampleData(trackIndex, android.nio.ByteBuffer.allocate(sampleSize), buffer)
            extractor.advance()
        }
    }
    
    /**
     * 复制音频轨道
     */
    private fun copyAudioTrack(
        extractor: MediaExtractor,
        muxer: MediaMuxer,
        trackIndex: Int
    ) {
        val buffer = android.media.MediaCodec.BufferInfo()
        val byteBuffer = android.nio.ByteBuffer.allocate(1024 * 1024)
        
        while (true) {
            val sampleSize = extractor.readSampleData(byteBuffer, 0)
            
            if (sampleSize < 0) break
            
            buffer.presentationTimeUs = extractor.sampleTime
            buffer.flags = extractor.sampleFlags
            buffer.size = sampleSize
            
            muxer.writeSampleData(trackIndex, byteBuffer, buffer)
            extractor.advance()
        }
    }
    
    /**
     * 获取压缩后的文件大小估计
     * @param originalSize 原始文件大小（字节）
     * @return 压缩后的估计大小（字节）
     */
    fun estimateCompressedSize(originalSize: Long): Long {
        // 估计压缩率为 30-50%
        return (originalSize * 0.4).toLong()
    }
    
    /**
     * 检查是否需要压缩
     * @param fileSize 文件大小（字节）
     * @return 如果文件大小超过 50MB，则需要压缩
     */
    fun shouldCompress(fileSize: Long): Boolean {
        return fileSize > 50 * 1024 * 1024 // 50MB
    }
    
    /**
     * 获取压缩比例
     * @param originalSize 原始文件大小
     * @param compressedSize 压缩后的文件大小
     * @return 压缩比例（百分比）
     */
    fun getCompressionRatio(originalSize: Long, compressedSize: Long): Float {
        return if (originalSize > 0) {
            ((originalSize - compressedSize).toFloat() / originalSize) * 100
        } else {
            0f
        }
    }
}
