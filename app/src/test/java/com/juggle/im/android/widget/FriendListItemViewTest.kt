package com.juggle.im.android.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.Assert.*

/**
 * FriendListItemView 单元测试
 * 测试朋友列表项完整性
 */
@RunWith(RobolectricTestRunner::class)
class FriendListItemViewTest {
    
    private lateinit var context: Context
    private lateinit var friendListItemView: FriendListItemView
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        friendListItemView = FriendListItemView(context)
    }
    
    @Test
    fun testSetAndGetNickname() {
        // 测试设置和获取昵称
        val nickname = "张三"
        friendListItemView.setNickname(nickname)
        
        assertEquals(nickname, friendListItemView.getNickname())
    }
    
    @Test
    fun testSetAndGetStatus() {
        // 测试设置和获取状态
        val status = "在线"
        friendListItemView.setStatus(status)
        
        assertEquals(status, friendListItemView.getStatus())
    }
    
    @Test
    fun testOnlineIndicatorVisibility() {
        // 测试在线指示器可见性
        assertFalse(friendListItemView.isOnlineIndicatorVisible())
    }
    
    @Test
    fun testBindDataWithOnlineFriend() {
        // 测试绑定在线朋友数据
        val friend = FriendListItemView.FriendItem(
            id = "friend_001",
            nickname = "李四",
            avatar = "https://example.com/avatar.jpg",
            isOnline = true,
            lastActiveTime = "刚刚"
        )
        
        friendListItemView.bindData(friend)
        
        assertEquals("李四", friendListItemView.getNickname())
        assertTrue(friendListItemView.isOnlineIndicatorVisible())
    }
    
    @Test
    fun testBindDataWithOfflineFriend() {
        // 测试绑定离线朋友数据
        val friend = FriendListItemView.FriendItem(
            id = "friend_002",
            nickname = "王五",
            avatar = "https://example.com/avatar.jpg",
            isOnline = false,
            lastActiveTime = "2小时前"
        )
        
        friendListItemView.bindData(friend)
        
        assertEquals("王五", friendListItemView.getNickname())
        assertFalse(friendListItemView.isOnlineIndicatorVisible())
    }
    
    @Test
    fun testOnItemClickListener() {
        // 测试点击监听器
        var clicked = false
        friendListItemView.setOnItemClickListener {
            clicked = true
        }
        
        friendListItemView.performClick()
        
        assertTrue(clicked)
    }
    
    @Test
    fun testMultipleFriendBindings() {
        // 测试多次绑定不同朋友数据
        val friend1 = FriendListItemView.FriendItem(
            id = "friend_001",
            nickname = "朋友1",
            avatar = "https://example.com/avatar1.jpg",
            isOnline = true
        )
        
        val friend2 = FriendListItemView.FriendItem(
            id = "friend_002",
            nickname = "朋友2",
            avatar = "https://example.com/avatar2.jpg",
            isOnline = false
        )
        
        friendListItemView.bindData(friend1)
        assertEquals("朋友1", friendListItemView.getNickname())
        
        friendListItemView.bindData(friend2)
        assertEquals("朋友2", friendListItemView.getNickname())
    }
}
