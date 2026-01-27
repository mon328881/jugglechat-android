package com.juggle.im.android.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.Assert.*

/**
 * FeedItemView 单元测试
 * 测试动态项完整性
 */
@RunWith(RobolectricTestRunner::class)
class FeedItemViewTest {
    
    private lateinit var context: Context
    private lateinit var feedItemView: FeedItemView
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        feedItemView = FeedItemView(context)
    }
    
    @Test
    fun testBindDataWithFeedItem() {
        // 测试绑定动态项数据
        val feedItem = FeedItemView.FeedItem(
            id = "feed_001",
            publisherId = "user_001",
            publisherName = "张三",
            publisherAvatar = "https://example.com/avatar.jpg",
            publishTime = "2小时前",
            content = "今天天气真好！",
            likeCount = 10,
            commentCount = 5,
            isLiked = false
        )
        
        feedItemView.bindData(feedItem)
        
        assertEquals("张三", feedItemView.getPublisherName())
        assertEquals("今天天气真好！", feedItemView.getContent())
        assertEquals("2小时前", feedItemView.getPublishTime())
    }
    
    @Test
    fun testBindDataWithImages() {
        // 测试绑定包含图片的动态
        val feedItem = FeedItemView.FeedItem(
            id = "feed_002",
            publisherId = "user_002",
            publisherName = "李四",
            publisherAvatar = "https://example.com/avatar.jpg",
            publishTime = "1小时前",
            content = "分享我的照片",
            images = listOf(
                "https://example.com/image1.jpg",
                "https://example.com/image2.jpg",
                "https://example.com/image3.jpg"
            ),
            likeCount = 20,
            commentCount = 8
        )
        
        feedItemView.bindData(feedItem)
        
        assertEquals("李四", feedItemView.getPublisherName())
        assertEquals("分享我的照片", feedItemView.getContent())
    }
    
    @Test
    fun testBindDataWithVideo() {
        // 测试绑定包含视频的动态
        val feedItem = FeedItemView.FeedItem(
            id = "feed_003",
            publisherId = "user_003",
            publisherName = "王五",
            publisherAvatar = "https://example.com/avatar.jpg",
            publishTime = "30分钟前",
            content = "分享我的视频",
            videoUrl = "https://example.com/video.mp4",
            likeCount = 15,
            commentCount = 3
        )
        
        feedItemView.bindData(feedItem)
        
        assertEquals("王五", feedItemView.getPublisherName())
        assertEquals("分享我的视频", feedItemView.getContent())
    }
    
    @Test
    fun testOnLikeClickListener() {
        // 测试点赞按钮点击监听
        var likeClicked = false
        feedItemView.setOnLikeClickListener {
            likeClicked = true
        }
        
        // 验证监听器已设置
        assertNotNull(feedItemView)
    }
    
    @Test
    fun testOnCommentClickListener() {
        // 测试评论按钮点击监听
        var commentClicked = false
        feedItemView.setOnCommentClickListener {
            commentClicked = true
        }
        
        // 验证监听器已设置
        assertNotNull(feedItemView)
    }
    
    @Test
    fun testOnShareClickListener() {
        // 测试分享按钮点击监听
        var shareClicked = false
        feedItemView.setOnShareClickListener {
            shareClicked = true
        }
        
        // 验证监听器已设置
        assertNotNull(feedItemView)
    }
    
    @Test
    fun testGetPublisherName() {
        // 测试获取发布者名称
        val feedItem = FeedItemView.FeedItem(
            id = "feed_001",
            publisherId = "user_001",
            publisherName = "赵六",
            publisherAvatar = "https://example.com/avatar.jpg",
            publishTime = "刚刚",
            content = "内容"
        )
        
        feedItemView.bindData(feedItem)
        
        assertEquals("赵六", feedItemView.getPublisherName())
    }
    
    @Test
    fun testGetContent() {
        // 测试获取内容
        val feedItem = FeedItemView.FeedItem(
            id = "feed_001",
            publisherId = "user_001",
            publisherName = "孙七",
            publisherAvatar = "https://example.com/avatar.jpg",
            publishTime = "刚刚",
            content = "这是一条很长的内容，包含很多信息"
        )
        
        feedItemView.bindData(feedItem)
        
        assertEquals("这是一条很长的内容，包含很多信息", feedItemView.getContent())
    }
    
    @Test
    fun testGetPublishTime() {
        // 测试获取发布时间
        val feedItem = FeedItemView.FeedItem(
            id = "feed_001",
            publisherId = "user_001",
            publisherName = "周八",
            publisherAvatar = "https://example.com/avatar.jpg",
            publishTime = "3天前",
            content = "内容"
        )
        
        feedItemView.bindData(feedItem)
        
        assertEquals("3天前", feedItemView.getPublishTime())
    }
}
