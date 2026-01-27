package com.juggle.im.android.chat

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.juggle.im.android.widget.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.Assert.*

/**
 * 第2阶段检查点测试
 * 验证登录、导航、聊天列表和聊天界面功能正确
 */
@RunWith(RobolectricTestRunner::class)
class Phase2CheckpointTest {
    
    private lateinit var context: Context
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }
    
    @Test
    fun testChatBubbleViewInitialization() {
        // 验证聊天气泡组件初始化
        val chatBubbleView = ChatBubbleView(context)
        assertNotNull(chatBubbleView)
        assertEquals(ChatBubbleView.MessageDirection.SENT, chatBubbleView.getMessageDirection())
        assertEquals(ChatBubbleView.MessageStatus.SENDING, chatBubbleView.getMessageStatus())
    }
    
    @Test
    fun testChatBubbleViewMessageDirection() {
        // 测试聊天气泡消息方向
        val chatBubbleView = ChatBubbleView(context)
        
        chatBubbleView.setMessageDirection(ChatBubbleView.MessageDirection.SENT)
        assertEquals(ChatBubbleView.MessageDirection.SENT, chatBubbleView.getMessageDirection())
        
        chatBubbleView.setMessageDirection(ChatBubbleView.MessageDirection.RECEIVED)
        assertEquals(ChatBubbleView.MessageDirection.RECEIVED, chatBubbleView.getMessageDirection())
    }
    
    @Test
    fun testChatBubbleViewMessageStatus() {
        // 测试聊天气泡消息状态
        val chatBubbleView = ChatBubbleView(context)
        
        chatBubbleView.setMessageStatus(ChatBubbleView.MessageStatus.SENDING)
        assertEquals(ChatBubbleView.MessageStatus.SENDING, chatBubbleView.getMessageStatus())
        
        chatBubbleView.setMessageStatus(ChatBubbleView.MessageStatus.SENT)
        assertEquals(ChatBubbleView.MessageStatus.SENT, chatBubbleView.getMessageStatus())
        
        chatBubbleView.setMessageStatus(ChatBubbleView.MessageStatus.READ)
        assertEquals(ChatBubbleView.MessageStatus.READ, chatBubbleView.getMessageStatus())
    }
    
    @Test
    fun testMessageInputViewFunctionality() {
        // 测试消息输入框功能
        val messageInputView = MessageInputView(context)
        
        messageInputView.setMessage("测试消息")
        assertEquals("测试消息", messageInputView.getInputText())
        
        messageInputView.clearInput()
        assertEquals("", messageInputView.getInputText())
    }
    
    @Test
    fun testMessageContextMenuFunctionality() {
        // 测试消息长按菜单功能
        val messageContextMenu = MessageContextMenu(context)
        
        val copyItem = MessageContextMenu.MenuItem("copy", "复制")
        val deleteItem = MessageContextMenu.MenuItem("delete", "删除")
        
        messageContextMenu.addMenuItem(copyItem)
        messageContextMenu.addMenuItem(deleteItem)
        
        val items = messageContextMenu.getMenuItems()
        assertEquals(2, items.size)
        
        messageContextMenu.clearMenuItems()
        assertEquals(0, messageContextMenu.getMenuItems().size)
    }
    
    @Test
    fun testTypingStatusViewFunctionality() {
        // 测试输入状态显示功能
        val typingStatusView = TypingStatusView(context)
        
        assertFalse(typingStatusView.isShowingTypingStatus())
        
        typingStatusView.showTypingStatus()
        assertTrue(typingStatusView.isShowingTypingStatus())
        
        typingStatusView.hideTypingStatus()
        assertFalse(typingStatusView.isShowingTypingStatus())
    }
    
    @Test
    fun testMediaMessageViewFunctionality() {
        // 测试媒体消息显示功能
        val mediaMessageView = MediaMessageView(context)
        
        assertEquals(MediaMessageView.MediaType.IMAGE, mediaMessageView.getMediaType())
        
        mediaMessageView.setMediaType(MediaMessageView.MediaType.VIDEO)
        assertEquals(MediaMessageView.MediaType.VIDEO, mediaMessageView.getMediaType())
        
        mediaMessageView.setVideoDuration("2:30")
        assertEquals("2:30", mediaMessageView.getVideoDuration())
    }
    
    @Test
    fun testChatListItemViewFunctionality() {
        // 测试聊天列表项功能
        val chatListItemView = ChatListItemView(context)
        
        chatListItemView.setNickname("张三")
        assertEquals("张三", chatListItemView.getNickname())
        
        chatListItemView.setMessagePreview("你好")
        assertEquals("你好", chatListItemView.getMessagePreview())
        
        chatListItemView.setTimestamp("14:30")
        assertEquals("14:30", chatListItemView.getTimestamp())
    }
    
    @Test
    fun testMaterialBottomNavigationFunctionality() {
        // 测试底部导航功能
        val bottomNavigation = MaterialBottomNavigation(context)
        
        // 添加导航项
        bottomNavigation.addNavItem(MaterialBottomNavigation.NavItem(1, android.R.drawable.ic_dialog_info, "聊天"))
        bottomNavigation.addNavItem(MaterialBottomNavigation.NavItem(2, android.R.drawable.ic_dialog_info, "朋友"))
        bottomNavigation.addNavItem(MaterialBottomNavigation.NavItem(3, android.R.drawable.ic_dialog_info, "发现"))
        bottomNavigation.addNavItem(MaterialBottomNavigation.NavItem(4, android.R.drawable.ic_dialog_info, "群组"))
        bottomNavigation.addNavItem(MaterialBottomNavigation.NavItem(5, android.R.drawable.ic_dialog_info, "个人资料"))
        
        // 验证导航项数量
        assertEquals(5, bottomNavigation.getItemCount())
        
        // 验证导航项标签
        val labels = bottomNavigation.getItemLabels()
        assertTrue(labels.contains("聊天"))
        assertTrue(labels.contains("朋友"))
        assertTrue(labels.contains("发现"))
        assertTrue(labels.contains("群组"))
        assertTrue(labels.contains("个人资料"))
    }
    
    @Test
    fun testPhase2ComponentsIntegration() {
        // 集成测试：验证所有第2阶段组件可以正常创建和使用
        val chatBubbleView = ChatBubbleView(context)
        val messageInputView = MessageInputView(context)
        val messageContextMenu = MessageContextMenu(context)
        val typingStatusView = TypingStatusView(context)
        val mediaMessageView = MediaMessageView(context)
        val chatListItemView = ChatListItemView(context)
        val bottomNavigation = MaterialBottomNavigation(context)
        
        // 验证所有组件都已成功创建
        assertNotNull(chatBubbleView)
        assertNotNull(messageInputView)
        assertNotNull(messageContextMenu)
        assertNotNull(typingStatusView)
        assertNotNull(mediaMessageView)
        assertNotNull(chatListItemView)
        assertNotNull(bottomNavigation)
    }
}
