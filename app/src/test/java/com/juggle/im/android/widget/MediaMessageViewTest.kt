package com.juggle.im.android.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.Assert.*

/**
 * MediaMessageView 单元测试
 * 测试媒体消息缩略图显示功能
 */
@RunWith(RobolectricTestRunner::class)
class MediaMessageViewTest {
    
    private lateinit var context: Context
    private lateinit var mediaMessageView: MediaMessageView
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        mediaMessageView = MediaMessageView(context)
    }
    
    @Test
    fun testInitialMediaType() {
        // 验证初始媒体类型为图片
        assertEquals(MediaMessageView.MediaType.IMAGE, mediaMessageView.getMediaType())
    }
    
    @Test
    fun testSetMediaTypeImage() {
        // 测试设置媒体类型为图片
        mediaMessageView.setMediaType(MediaMessageView.MediaType.IMAGE)
        
        assertEquals(MediaMessageView.MediaType.IMAGE, mediaMessageView.getMediaType())
    }
    
    @Test
    fun testSetMediaTypeVideo() {
        // 测试设置媒体类型为视频
        mediaMessageView.setMediaType(MediaMessageView.MediaType.VIDEO)
        
        assertEquals(MediaMessageView.MediaType.VIDEO, mediaMessageView.getMediaType())
    }
    
    @Test
    fun testSetThumbnail() {
        // 测试设置缩略图
        mediaMessageView.setThumbnail(android.R.drawable.ic_dialog_info)
        
        // 验证缩略图已设置
        assertNotNull(mediaMessageView.getThumbnailDrawable())
    }
    
    @Test
    fun testSetVideoDuration() {
        // 测试设置视频时长
        mediaMessageView.setMediaType(MediaMessageView.MediaType.VIDEO)
        mediaMessageView.setVideoDuration("2:30")
        
        assertEquals("2:30", mediaMessageView.getVideoDuration())
    }
    
    @Test
    fun testOnMediaClickListener() {
        // 测试媒体点击监听器
        var mediaClicked = false
        mediaMessageView.setOnMediaClickListener {
            mediaClicked = true
        }
        
        mediaMessageView.clickMedia()
        
        assertTrue(mediaClicked)
    }
    
    @Test
    fun testImageMediaTypeHidesPlayButton() {
        // 测试图片类型隐藏播放按钮
        mediaMessageView.setMediaType(MediaMessageView.MediaType.IMAGE)
        
        assertFalse(mediaMessageView.isPlayButtonVisible())
        assertFalse(mediaMessageView.isDurationTextVisible())
    }
    
    @Test
    fun testVideoMediaTypeShowsPlayButton() {
        // 测试视频类型显示播放按钮
        mediaMessageView.setMediaType(MediaMessageView.MediaType.VIDEO)
        
        assertTrue(mediaMessageView.isPlayButtonVisible())
        assertTrue(mediaMessageView.isDurationTextVisible())
    }
    
    @Test
    fun testSwitchMediaType() {
        // 测试切换媒体类型
        mediaMessageView.setMediaType(MediaMessageView.MediaType.IMAGE)
        assertFalse(mediaMessageView.isPlayButtonVisible())
        
        mediaMessageView.setMediaType(MediaMessageView.MediaType.VIDEO)
        assertTrue(mediaMessageView.isPlayButtonVisible())
        
        mediaMessageView.setMediaType(MediaMessageView.MediaType.IMAGE)
        assertFalse(mediaMessageView.isPlayButtonVisible())
    }
}
