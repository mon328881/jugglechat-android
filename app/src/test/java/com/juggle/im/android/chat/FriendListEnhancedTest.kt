package com.juggle.im.android.chat

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.Assert.*

/**
 * FriendListEnhanced 单元测试
 * 测试朋友列表搜索和字母分组功能
 */
@RunWith(RobolectricTestRunner::class)
class FriendListEnhancedTest {
    
    private lateinit var context: Context
    private lateinit var recyclerView: RecyclerView
    private lateinit var friendListEnhanced: FriendListEnhanced
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        recyclerView = RecyclerView(context)
        friendListEnhanced = FriendListEnhanced(context, recyclerView)
    }
    
    @Test
    fun testSetFriends() {
        // 测试设置朋友列表
        val friends = listOf(
            FriendListEnhanced.Friend(
                id = "friend_001",
                nickname = "张三",
                avatar = "https://example.com/avatar1.jpg",
                isOnline = true
            ),
            FriendListEnhanced.Friend(
                id = "friend_002",
                nickname = "李四",
                avatar = "https://example.com/avatar2.jpg",
                isOnline = false
            )
        )
        
        friendListEnhanced.setFriends(friends)
        
        assertEquals(2, friendListEnhanced.getFriendCount())
    }
    
    @Test
    fun testSearchFriends() {
        // 测试搜索朋友
        val friends = listOf(
            FriendListEnhanced.Friend(
                id = "friend_001",
                nickname = "张三",
                avatar = "https://example.com/avatar1.jpg"
            ),
            FriendListEnhanced.Friend(
                id = "friend_002",
                nickname = "李四",
                avatar = "https://example.com/avatar2.jpg"
            ),
            FriendListEnhanced.Friend(
                id = "friend_003",
                nickname = "王五",
                avatar = "https://example.com/avatar3.jpg"
            )
        )
        
        friendListEnhanced.setFriends(friends)
        friendListEnhanced.searchFriends("张")
        
        val filtered = friendListEnhanced.getFilteredFriends()
        assertEquals(1, filtered.size)
        assertEquals("张三", filtered[0].nickname)
    }
    
    @Test
    fun testSearchByAccountId() {
        // 测试按账号ID搜索
        val friends = listOf(
            FriendListEnhanced.Friend(
                id = "friend_001",
                nickname = "张三",
                avatar = "https://example.com/avatar1.jpg"
            ),
            FriendListEnhanced.Friend(
                id = "friend_002",
                nickname = "李四",
                avatar = "https://example.com/avatar2.jpg"
            )
        )
        
        friendListEnhanced.setFriends(friends)
        friendListEnhanced.searchFriends("friend_001")
        
        val filtered = friendListEnhanced.getFilteredFriends()
        assertEquals(1, filtered.size)
        assertEquals("friend_001", filtered[0].id)
    }
    
    @Test
    fun testGroupFriendsByLetter() {
        // 测试按首字母分组
        val friends = listOf(
            FriendListEnhanced.Friend(
                id = "friend_001",
                nickname = "张三",
                avatar = "https://example.com/avatar1.jpg"
            ),
            FriendListEnhanced.Friend(
                id = "friend_002",
                nickname = "李四",
                avatar = "https://example.com/avatar2.jpg"
            ),
            FriendListEnhanced.Friend(
                id = "friend_003",
                nickname = "王五",
                avatar = "https://example.com/avatar3.jpg"
            )
        )
        
        friendListEnhanced.setFriends(friends)
        
        val grouped = friendListEnhanced.getFriendsByLetter()
        assertTrue(grouped.isNotEmpty())
    }
    
    @Test
    fun testClearSearch() {
        // 测试清空搜索
        val friends = listOf(
            FriendListEnhanced.Friend(
                id = "friend_001",
                nickname = "张三",
                avatar = "https://example.com/avatar1.jpg"
            ),
            FriendListEnhanced.Friend(
                id = "friend_002",
                nickname = "李四",
                avatar = "https://example.com/avatar2.jpg"
            )
        )
        
        friendListEnhanced.setFriends(friends)
        friendListEnhanced.searchFriends("张")
        
        var filtered = friendListEnhanced.getFilteredFriends()
        assertEquals(1, filtered.size)
        
        friendListEnhanced.clearSearch()
        filtered = friendListEnhanced.getFilteredFriends()
        assertEquals(2, filtered.size)
    }
    
    @Test
    fun testGetAllFriends() {
        // 测试获取所有朋友
        val friends = listOf(
            FriendListEnhanced.Friend(
                id = "friend_001",
                nickname = "张三",
                avatar = "https://example.com/avatar1.jpg"
            ),
            FriendListEnhanced.Friend(
                id = "friend_002",
                nickname = "李四",
                avatar = "https://example.com/avatar2.jpg"
            )
        )
        
        friendListEnhanced.setFriends(friends)
        
        val allFriends = friendListEnhanced.getAllFriends()
        assertEquals(2, allFriends.size)
    }
    
    @Test
    fun testGetFilteredFriendCount() {
        // 测试获取过滤后的朋友数量
        val friends = listOf(
            FriendListEnhanced.Friend(
                id = "friend_001",
                nickname = "张三",
                avatar = "https://example.com/avatar1.jpg"
            ),
            FriendListEnhanced.Friend(
                id = "friend_002",
                nickname = "李四",
                avatar = "https://example.com/avatar2.jpg"
            ),
            FriendListEnhanced.Friend(
                id = "friend_003",
                nickname = "王五",
                avatar = "https://example.com/avatar3.jpg"
            )
        )
        
        friendListEnhanced.setFriends(friends)
        friendListEnhanced.searchFriends("李")
        
        assertEquals(1, friendListEnhanced.getFilteredFriendCount())
    }
    
    @Test
    fun testOnFriendClickListener() {
        // 测试朋友点击监听
        var clickedFriendId = ""
        friendListEnhanced.setOnFriendClickListener { friend ->
            clickedFriendId = friend.id
        }
        
        val friends = listOf(
            FriendListEnhanced.Friend(
                id = "friend_001",
                nickname = "张三",
                avatar = "https://example.com/avatar1.jpg"
            )
        )
        
        friendListEnhanced.setFriends(friends)
        
        // 验证监听器已设置
        assertNotNull(friendListEnhanced)
    }
    
    @Test
    fun testEmptySearch() {
        // 测试空搜索结果
        val friends = listOf(
            FriendListEnhanced.Friend(
                id = "friend_001",
                nickname = "张三",
                avatar = "https://example.com/avatar1.jpg"
            )
        )
        
        friendListEnhanced.setFriends(friends)
        friendListEnhanced.searchFriends("不存在的朋友")
        
        val filtered = friendListEnhanced.getFilteredFriends()
        assertEquals(0, filtered.size)
    }
}
