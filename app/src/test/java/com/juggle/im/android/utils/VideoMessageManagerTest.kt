package com.juggle.im.android.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * 视频消息管理器单元测试
 * 验证: 需求 18.1, 18.2, 18.3, 18.4, 18.5
 */
class VideoMessageManagerTest : FunSpec({
    
    val context: Context = ApplicationProvider.getApplicationContext()
    
    test("应该能创建视频消息") {
        val videoMessage = VideoMessageManager.VideoMessage(
            id = "test_1",
            videoPath = "/path/to/video.mp4",
            thumbnailPath = "/path/to/thumbnail.jpg",
            duration = 30000,
            fileSize = 5 * 1024 * 1024,
            width = 1920,
            height = 1080,
            isCompressed = false,
            originalSize = 5 * 1024 * 1024
        )
        
        videoMessage.shouldNotBe(null)
        videoMessage.id.shouldBe("test_1")
    }
    
    test("应该能获取视频信息字符串") {
        val videoMessage = VideoMessageManager.VideoMessage(
            id = "test_2",
            videoPath = "/path/to/video.mp4",
            thumbnailPath = "/path/to/thumbnail.jpg",
            duration = 60000,
            fileSize = 10 * 1024 * 1024,
            width = 1920,
            height = 1080,
            isCompressed = false,
            originalSize = 10 * 1024 * 1024
        )
        
        val infoString = VideoMessageManager.getVideoInfoString(videoMessage)
        infoString.shouldNotBe("")
    }
    
    test("应该能清理缓存") {
        // 清理缓存不应该抛出异常
        VideoMessageManager.clearCache(context)
    }
    
    test("应该能删除视频文件") {
        val videoMessage = VideoMessageManager.VideoMessage(
            id = "test_3",
            videoPath = "/path/to/video.mp4",
            thumbnailPath = "/path/to/thumbnail.jpg",
            duration = 30000,
            fileSize = 5 * 1024 * 1024,
            width = 1920,
            height = 1080,
            isCompressed = false,
            originalSize = 5 * 1024 * 1024
        )
        
        // 删除视频文件不应该抛出异常
        VideoMessageManager.deleteVideo(videoMessage)
    }
    
    test("视频消息应该支持复制") {
        val videoMessage = VideoMessageManager.VideoMessage(
            id = "test_4",
            videoPath = "/path/to/video.mp4",
            thumbnailPath = "/path/to/thumbnail.jpg",
            duration = 30000,
            fileSize = 5 * 1024 * 1024,
            width = 1920,
            height = 1080,
            isCompressed = false,
            originalSize = 5 * 1024 * 1024
        )
        
        val copiedMessage = videoMessage.copy(isCompressed = true)
        
        copiedMessage.isCompressed.shouldBe(true)
        copiedMessage.id.shouldBe(videoMessage.id)
    }
    
    test("应该能压缩视频消息") {
        val videoMessage = VideoMessageManager.VideoMessage(
            id = "test_5",
            videoPath = "/path/to/video.mp4",
            thumbnailPath = "/path/to/thumbnail.jpg",
            duration = 30000,
            fileSize = 100 * 1024 * 1024,
            width = 1920,
            height = 1080,
            isCompressed = false,
            originalSize = 100 * 1024 * 1024
        )
        
        // 压缩视频不应该抛出异常
        val compressedMessage = VideoMessageManager.compressVideo(context, videoMessage)
        compressedMessage.shouldNotBe(null)
    }
})
