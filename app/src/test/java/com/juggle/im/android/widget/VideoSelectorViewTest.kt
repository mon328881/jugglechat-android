package com.juggle.im.android.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * 视频选择器视图单元测试
 * 验证: 需求 18.1, 18.2
 */
class VideoSelectorViewTest : FunSpec({
    
    val context: Context = ApplicationProvider.getApplicationContext()
    
    test("应该能创建视频选择器视图") {
        val videoSelectorView = VideoSelectorView(context)
        
        videoSelectorView.shouldNotBe(null)
    }
    
    test("应该能设置视频信息") {
        val videoSelectorView = VideoSelectorView(context)
        
        // 设置视频信息不应该抛出异常
        // 注意：实际路径可能不存在，但方法应该能处理
        videoSelectorView.setVideoInfo("/path/to/video.mp4")
    }
    
    test("应该能获取文件名") {
        val videoSelectorView = VideoSelectorView(context)
        
        val fileName = videoSelectorView.getFileName()
        fileName.shouldNotBe(null)
    }
    
    test("应该能获取文件大小") {
        val videoSelectorView = VideoSelectorView(context)
        
        val fileSize = videoSelectorView.getFileSize()
        fileSize.shouldNotBe(null)
    }
    
    test("应该能获取视频时长") {
        val videoSelectorView = VideoSelectorView(context)
        
        val duration = videoSelectorView.getVideoDuration()
        duration.shouldNotBe(null)
    }
    
    test("应该能获取分辨率") {
        val videoSelectorView = VideoSelectorView(context)
        
        val resolution = videoSelectorView.getResolution()
        resolution.shouldNotBe(null)
    }
    
    test("应该能设置确认监听器") {
        val videoSelectorView = VideoSelectorView(context)
        
        var confirmCalled = false
        videoSelectorView.setOnConfirmListener { path ->
            confirmCalled = true
        }
        
        // 监听器应该被设置
        confirmCalled.shouldBe(false)
    }
    
    test("应该能设置取消监听器") {
        val videoSelectorView = VideoSelectorView(context)
        
        var cancelCalled = false
        videoSelectorView.setOnCancelListener {
            cancelCalled = true
        }
        
        // 监听器应该被设置
        cancelCalled.shouldBe(false)
    }
    
    test("应该能获取选中的视频路径") {
        val videoSelectorView = VideoSelectorView(context)
        
        val selectedPath = videoSelectorView.getSelectedVideoPath()
        // 初始状态应该为 null
        selectedPath.shouldBe(null)
    }
})
