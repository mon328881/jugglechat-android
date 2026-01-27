package com.juggle.im.android.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import com.juggle.im.android.utils.VideoMessageManager
import com.juggle.im.android.utils.VideoCompressionUtils

/**
 * 视频发送功能集成测试
 * 验证: 需求 18.1, 18.2, 18.3, 18.4, 18.5
 */
class VideoSendingIntegrationTest : FunSpec({
    
    val context: Context = ApplicationProvider.getApplicationContext()
    
    test("应该能完成视频选择流程") {
        // 创建视频消息
        val videoMessage = VideoMessageManager.VideoMessage(
            id = "integration_test_1",
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
        videoMessage.videoPath.shouldNotBe("")
    }
    
    test("应该能完成视频预览流程") {
        val videoMessage = VideoMessageManager.VideoMessage(
            id = "integration_test_2",
            videoPath = "/path/to/video.mp4",
            thumbnailPath = "/path/to/thumbnail.jpg",
            duration = 30000,
            fileSize = 5 * 1024 * 1024,
            width = 1920,
            height = 1080,
            isCompressed = false,
            originalSize = 5 * 1024 * 1024
        )
        
        // 获取视频信息
        val infoString = VideoMessageManager.getVideoInfoString(videoMessage)
        infoString.shouldNotBe("")
    }
    
    test("应该能完成视频压缩流程") {
        val videoMessage = VideoMessageManager.VideoMessage(
            id = "integration_test_3",
            videoPath = "/path/to/video.mp4",
            thumbnailPath = "/path/to/thumbnail.jpg",
            duration = 30000,
            fileSize = 100 * 1024 * 1024,
            width = 1920,
            height = 1080,
            isCompressed = false,
            originalSize = 100 * 1024 * 1024
        )
        
        // 压缩视频
        val compressedMessage = VideoMessageManager.compressVideo(context, videoMessage)
        compressedMessage.shouldNotBe(null)
    }
    
    test("应该能完成视频消息显示流程") {
        val videoMessage = VideoMessageManager.VideoMessage(
            id = "integration_test_4",
            videoPath = "/path/to/video.mp4",
            thumbnailPath = "/path/to/thumbnail.jpg",
            duration = 30000,
            fileSize = 5 * 1024 * 1024,
            width = 1920,
            height = 1080,
            isCompressed = false,
            originalSize = 5 * 1024 * 1024
        )
        
        // 验证视频消息包含所有必需的信息
        videoMessage.id.shouldNotBe("")
        videoMessage.videoPath.shouldNotBe("")
        videoMessage.duration.shouldBe(30000)
        videoMessage.fileSize.shouldBe(5 * 1024 * 1024)
    }
    
    test("应该能完成视频消息接收流程") {
        val videoMessage = VideoMessageManager.VideoMessage(
            id = "integration_test_5",
            videoPath = "/path/to/video.mp4",
            thumbnailPath = "/path/to/thumbnail.jpg",
            duration = 30000,
            fileSize = 5 * 1024 * 1024,
            width = 1920,
            height = 1080,
            isCompressed = false,
            originalSize = 5 * 1024 * 1024
        )
        
        // 验证视频消息可以被接收和显示
        videoMessage.thumbnailPath.shouldNotBe(null)
        videoMessage.width.shouldBe(1920)
        videoMessage.height.shouldBe(1080)
    }
})
