package com.juggle.im.android.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll

/**
 * 视频消息接收显示属性测试
 * 验证: 需求 18.5
 * 属性 78: 视频消息接收显示
 */
class VideoMessageReceivePropertyTest : FunSpec({
    
    val context: Context = ApplicationProvider.getApplicationContext()
    
    test("视频消息管理器应该能创建视频消息") {
        val videoMessage = VideoMessageManager.VideoMessage(
            id = "test_video_1",
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
        videoMessage.id.shouldBe("test_video_1")
    }
    
    test("视频消息应该包含缩略图路径") {
        val videoMessage = VideoMessageManager.VideoMessage(
            id = "test_video_2",
            videoPath = "/path/to/video.mp4",
            thumbnailPath = "/path/to/thumbnail.jpg",
            duration = 30000,
            fileSize = 5 * 1024 * 1024,
            width = 1920,
            height = 1080,
            isCompressed = false,
            originalSize = 5 * 1024 * 1024
        )
        
        videoMessage.thumbnailPath.shouldNotBe(null)
        videoMessage.thumbnailPath.shouldBe("/path/to/thumbnail.jpg")
    }
    
    test("视频消息应该包含时长信息") {
        checkAll(Arb.long(min = 0, max = 3600000)) { duration ->
            val videoMessage = VideoMessageManager.VideoMessage(
                id = "test_video_3",
                videoPath = "/path/to/video.mp4",
                thumbnailPath = "/path/to/thumbnail.jpg",
                duration = duration,
                fileSize = 5 * 1024 * 1024,
                width = 1920,
                height = 1080,
                isCompressed = false,
                originalSize = 5 * 1024 * 1024
            )
            
            videoMessage.duration.shouldBe(duration)
        }
    }
    
    test("视频消息应该包含分辨率信息") {
        val videoMessage = VideoMessageManager.VideoMessage(
            id = "test_video_4",
            videoPath = "/path/to/video.mp4",
            thumbnailPath = "/path/to/thumbnail.jpg",
            duration = 30000,
            fileSize = 5 * 1024 * 1024,
            width = 1920,
            height = 1080,
            isCompressed = false,
            originalSize = 5 * 1024 * 1024
        )
        
        videoMessage.width.shouldBe(1920)
        videoMessage.height.shouldBe(1080)
    }
    
    test("视频消息应该包含文件大小信息") {
        checkAll(Arb.long(min = 1024, max = 500 * 1024 * 1024)) { fileSize ->
            val videoMessage = VideoMessageManager.VideoMessage(
                id = "test_video_5",
                videoPath = "/path/to/video.mp4",
                thumbnailPath = "/path/to/thumbnail.jpg",
                duration = 30000,
                fileSize = fileSize,
                width = 1920,
                height = 1080,
                isCompressed = false,
                originalSize = fileSize
            )
            
            videoMessage.fileSize.shouldBe(fileSize)
        }
    }
    
    test("视频消息应该能生成信息字符串") {
        val videoMessage = VideoMessageManager.VideoMessage(
            id = "test_video_6",
            videoPath = "/path/to/video.mp4",
            thumbnailPath = "/path/to/thumbnail.jpg",
            duration = 30000,
            fileSize = 5 * 1024 * 1024,
            width = 1920,
            height = 1080,
            isCompressed = false,
            originalSize = 5 * 1024 * 1024
        )
        
        val infoString = VideoMessageManager.getVideoInfoString(videoMessage)
        infoString.shouldNotBe("")
        infoString.shouldContain("时长")
        infoString.shouldContain("大小")
        infoString.shouldContain("分辨率")
    }
    
    test("压缩后的视频消息应该标记为已压缩") {
        val videoMessage = VideoMessageManager.VideoMessage(
            id = "test_video_7",
            videoPath = "/path/to/compressed_video.mp4",
            thumbnailPath = "/path/to/thumbnail.jpg",
            duration = 30000,
            fileSize = 2 * 1024 * 1024,
            width = 1280,
            height = 720,
            isCompressed = true,
            originalSize = 5 * 1024 * 1024
        )
        
        videoMessage.isCompressed.shouldBe(true)
        videoMessage.fileSize.shouldBe(2 * 1024 * 1024)
        videoMessage.originalSize.shouldBe(5 * 1024 * 1024)
    }
    
    test("视频消息应该包含时间戳") {
        val videoMessage = VideoMessageManager.VideoMessage(
            id = "test_video_8",
            videoPath = "/path/to/video.mp4",
            thumbnailPath = "/path/to/thumbnail.jpg",
            duration = 30000,
            fileSize = 5 * 1024 * 1024,
            width = 1920,
            height = 1080,
            isCompressed = false,
            originalSize = 5 * 1024 * 1024
        )
        
        videoMessage.timestamp.shouldNotBe(0L)
    }
})
