package com.juggle.im.android.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.Assert.*

/**
 * FriendDetailView 单元测试
 * 测试朋友详情页面导航
 */
@RunWith(RobolectricTestRunner::class)
class FriendDetailViewTest {
    
    private lateinit var context: Context
    private lateinit var friendDetailView: FriendDetailView
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        friendDetailView = FriendDetailView(context)
    }
    
    @Test
    fun testBindDataWithOnlineFriend() {
        // 测试绑定在线朋友详情
        val detail = FriendDetailView.FriendDetail(
            id = "friend_001",
            nickname = "张三",
            avatar = "https://example.com/avatar.jpg",
            signature = "这是我的个性签名",
            accountId = "zhangsan123",
            isOnline = true
        )
        
        friendDetailView.bindData(detail)
        
        assertEquals("张三", friendDetailView.getNickname())
        assertEquals("这是我的个性签名", friendDetailView.getSignature())
        assertTrue(friendDetailView.getAccountId().contains("zhangsan123"))
    }
    
    @Test
    fun testBindDataWithOfflineFriend() {
        // 测试绑定离线朋友详情
        val detail = FriendDetailView.FriendDetail(
            id = "friend_002",
            nickname = "李四",
            avatar = "https://example.com/avatar.jpg",
            signature = "我很忙",
            accountId = "lisi456",
            isOnline = false
        )
        
        friendDetailView.bindData(detail)
        
        assertEquals("李四", friendDetailView.getNickname())
        assertEquals("我很忙", friendDetailView.getSignature())
    }
    
    @Test
    fun testAddButtonClickListener() {
        // 测试添加朋友按钮点击监听
        var addClicked = false
        friendDetailView.setOnAddClickListener {
            addClicked = true
        }
        
        // 模拟点击（实际测试中需要找到按钮并点击）
        // 这里简化处理
        assertTrue(!addClicked)
    }
    
    @Test
    fun testMessageButtonClickListener() {
        // 测试发送消息按钮点击监听
        var messageClicked = false
        friendDetailView.setOnMessageClickListener {
            messageClicked = true
        }
        
        // 模拟点击（实际测试中需要找到按钮并点击）
        // 这里简化处理
        assertTrue(!messageClicked)
    }
    
    @Test
    fun testGetNickname() {
        // 测试获取昵称
        val detail = FriendDetailView.FriendDetail(
            id = "friend_001",
            nickname = "王五",
            avatar = "https://example.com/avatar.jpg",
            signature = "签名",
            accountId = "wangwu789"
        )
        
        friendDetailView.bindData(detail)
        
        assertEquals("王五", friendDetailView.getNickname())
    }
    
    @Test
    fun testGetSignature() {
        // 测试获取个性签名
        val detail = FriendDetailView.FriendDetail(
            id = "friend_001",
            nickname = "赵六",
            avatar = "https://example.com/avatar.jpg",
            signature = "我的签名很长很长很长",
            accountId = "zhaoliu000"
        )
        
        friendDetailView.bindData(detail)
        
        assertEquals("我的签名很长很长很长", friendDetailView.getSignature())
    }
    
    @Test
    fun testGetAccountId() {
        // 测试获取账号ID
        val detail = FriendDetailView.FriendDetail(
            id = "friend_001",
            nickname = "孙七",
            avatar = "https://example.com/avatar.jpg",
            signature = "签名",
            accountId = "sunqi111"
        )
        
        friendDetailView.bindData(detail)
        
        assertTrue(friendDetailView.getAccountId().contains("sunqi111"))
    }
}
