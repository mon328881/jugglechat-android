package com.juggle.im.android.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * 视频消息显示属性测试
 * 验证: 需求 18.4
 * 属性 77: 视频消息显示
 */
class VideoMessageDisplayPropertyTest : FunSpec({
    
    val context: Context = ApplicationProvider.getApplicationContext()
    
    test("媒体消息视图应该支持视频类型") {
        val mediaMessageView = MediaMessageView(context)
        
        // 设置为视频类型
        mediaMessageView.setMediaType(MediaMessageView.MediaType.VIDEO)
        
        // 验证类型设置成功
        mediaMessageView.getMediaType().shouldBe(MediaMessageView.MediaType.VIDEO)
    }
    
    test("视频消息应该显示播放按钮") {
        val mediaMessageView = MediaMessageView(context)
        
        // 设置为视频类型
        mediaMessageView.setMediaType(MediaMessageView.MediaType.VIDEO)
        
        // 验证播放按钮可见
        mediaMessageView.isPlayButtonVisible().shouldBe(true)
    }
    
    test("视频消息应该显示时长信息") {
        val mediaMessageView = MediaMessageView(context)
        
        // 设置为视频类型
        mediaMessageView.setMediaType(MediaMessageView.MediaType.VIDEO)
        
        // 设置时长
        mediaMessageView.setVideoDuration("00:30")
        
        // 验证时长文本可见
        mediaMessageView.isDurationTextVisible().shouldBe(true)
    }
    
    test("图片消息不应该显示播放按钮") {
        val mediaMessageView = MediaMessageView(context)
        
        // 设置为图片类型
        mediaMessageView.setMediaType(MediaMessageView.MediaType.IMAGE)
        
        // 验证播放按钮不可见
        mediaMessageView.isPlayButtonVisible().shouldBe(false)
    }
    
    test("图片消息不应该显示时长信息") {
        val mediaMessageView = MediaMessageView(context)
        
        // 设置为图片类型
        mediaMessageView.setMediaType(MediaMessageView.MediaType.IMAGE)
        
        // 验证时长文本不可见
        mediaMessageView.isDurationTextVisible().shouldBe(false)
    }
    
    test("视频消息应该支持点击监听") {
        val mediaMessageView = MediaMessageView(context)
        
        var clickCalled = false
        mediaMessageView.setOnMediaClickListener {
            clickCalled = true
        }
        
        // 点击媒体
        mediaMessageView.clickMedia()
        
        // 验证点击事件被触发
        clickCalled.shouldBe(true)
    }
    
    test("视频消息应该能设置缩略图") {
        checkAll(Arb.string(minSize = 1, maxSize = 100)) { testString ->
            val mediaMessageView = MediaMessageView(context)
            
            // 设置缩略图
            mediaMessageView.setThumbnail(android.R.drawable.ic_media_play)
            
            // 验证缩略图已设置
            mediaMessageView.getThumbnailDrawable().shouldNotBe(null)
        }
    }
    
    test("视频消息应该能设置时长") {
        checkAll(Arb.string(minSize = 1, maxSize = 20)) { duration ->
            val mediaMessageView = MediaMessageView(context)
            
            // 设置时长
            mediaMessageView.setVideoDuration(duration)
            
            // 验证时长已设置
            mediaMessageView.getVideoDuration().shouldBe(duration)
        }
    }
    
    test("视频消息应该支持多次点击") {
        val mediaMessageView = MediaMessageView(context)
        
        var clickCount = 0
        mediaMessageView.setOnMediaClickListener {
            clickCount++
        }
        
        // 多次点击
        repeat(5) {
            mediaMessageView.clickMedia()
        }
        
        // 验证点击次数
        clickCount.shouldBe(5)
    }
})
