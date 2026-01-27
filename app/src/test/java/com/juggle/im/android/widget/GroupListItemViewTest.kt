package com.juggle.im.android.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.juggle.im.android.theme.ThemeManager
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * 群组列表项视图测试
 * 
 * **验证: 需求 9.1**
 */
@RunWith(RobolectricTestRunner::class)
class GroupListItemViewTest {
    
    private lateinit var context: Context
    private lateinit var groupListItemView: GroupListItemView
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        groupListItemView = GroupListItemView(context)
    }
    
    @Test
    fun testGroupItemDataClass() {
        val groupItem = GroupListItemView.GroupItem(
            id = "group1",
            name = "开发团队",
            avatar = "https://example.com/avatar.jpg",
            memberCount = 5,
            lastMessage = "今天的会议很有收获",
            lastMessageTime = "14:30"
        )
        
        assertEquals("group1", groupItem.id)
        assertEquals("开发团队", groupItem.name)
        assertEquals(5, groupItem.memberCount)
        assertEquals("今天的会议很有收获", groupItem.lastMessage)
        assertEquals("14:30", groupItem.lastMessageTime)
    }
    
    @Test
    fun testBindGroupData() {
        val groupItem = GroupListItemView.GroupItem(
            id = "group1",
            name = "开发团队",
            avatar = "https://example.com/avatar.jpg",
            memberCount = 5,
            lastMessage = "今天的会议很有收获",
            lastMessageTime = "14:30"
        )
        
        groupListItemView.bindData(groupItem)
        
        assertEquals("开发团队", groupListItemView.getGroupName())
        assertEquals("成员数：5", groupListItemView.getMemberCount())
        assertEquals("今天的会议很有收获", groupListItemView.getLastMessage())
        assertEquals("14:30", groupListItemView.getLastMessageTime())
    }
    
    @Test
    fun testSetGroupName() {
        groupListItemView.setGroupName("新的群组名称")
        assertEquals("新的群组名称", groupListItemView.getGroupName())
    }
    
    @Test
    fun testSetMemberCount() {
        groupListItemView.setMemberCount(10)
        assertEquals("成员数：10", groupListItemView.getMemberCount())
    }
    
    @Test
    fun testSetLastMessage() {
        groupListItemView.setLastMessage("最后一条消息")
        assertEquals("最后一条消息", groupListItemView.getLastMessage())
    }
    
    @Test
    fun testSetLastMessageTime() {
        groupListItemView.setLastMessageTime("15:45")
        assertEquals("15:45", groupListItemView.getLastMessageTime())
    }
    
    @Test
    fun testGroupListItemViewNotNull() {
        assertNotNull(groupListItemView)
    }
    
    @Test
    fun testMultipleGroupItems() {
        val groups = listOf(
            GroupListItemView.GroupItem(
                id = "group1",
                name = "开发团队",
                avatar = "https://example.com/avatar1.jpg",
                memberCount = 5,
                lastMessage = "讨论新功能",
                lastMessageTime = "14:30"
            ),
            GroupListItemView.GroupItem(
                id = "group2",
                name = "设计团队",
                avatar = "https://example.com/avatar2.jpg",
                memberCount = 3,
                lastMessage = "分享设计稿",
                lastMessageTime = "15:00"
            ),
            GroupListItemView.GroupItem(
                id = "group3",
                name = "产品团队",
                avatar = "https://example.com/avatar3.jpg",
                memberCount = 4,
                lastMessage = "确认需求",
                lastMessageTime = "15:30"
            )
        )
        
        for (group in groups) {
            groupListItemView.bindData(group)
            assertEquals(group.name, groupListItemView.getGroupName())
            assertEquals("成员数：${group.memberCount}", groupListItemView.getMemberCount())
        }
    }
}
