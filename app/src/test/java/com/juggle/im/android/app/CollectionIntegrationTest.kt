package com.juggle.im.android.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.juggle.im.android.model.CollectedMessage
import com.juggle.im.android.utils.CollectionManager
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.util.UUID

/**
 * 消息收藏功能集成测试
 * 验证收藏、取消收藏、搜索、筛选等完整流程
 */
class CollectionIntegrationTest {
    
    private lateinit var context: Context
    private lateinit var collectionManager: CollectionManager
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        collectionManager = CollectionManager.getInstance(context)
        collectionManager.clearAllCollections()
    }
    
    @Test
    fun testCompleteCollectionWorkflow() {
        // 1. 创建多条消息
        val messages = listOf(
            CollectedMessage(
                collectionId = UUID.randomUUID().toString(),
                messageId = UUID.randomUUID().toString(),
                content = "这是一条重要的文本消息",
                senderId = "sender1",
                senderName = "张三",
                messageType = CollectedMessage.TYPE_TEXT,
                messageTimestamp = System.currentTimeMillis() - 5000,
                collectionTimestamp = System.currentTimeMillis() - 5000,
                conversationId = "conv1",
                messageSummary = "这是一条重要的文本消息"
            ),
            CollectedMessage(
                collectionId = UUID.randomUUID().toString(),
                messageId = UUID.randomUUID().toString(),
                content = "这是一条图片消息",
                senderId = "sender2",
                senderName = "李四",
                messageType = CollectedMessage.TYPE_IMAGE,
                messageTimestamp = System.currentTimeMillis() - 3000,
                collectionTimestamp = System.currentTimeMillis() - 3000,
                conversationId = "conv2",
                messageSummary = "这是一条图片消息"
            ),
            CollectedMessage(
                collectionId = UUID.randomUUID().toString(),
                messageId = UUID.randomUUID().toString(),
                content = "这是一条视频消息",
                senderId = "sender3",
                senderName = "王五",
                messageType = CollectedMessage.TYPE_VIDEO,
                messageTimestamp = System.currentTimeMillis(),
                collectionTimestamp = System.currentTimeMillis(),
                conversationId = "conv3",
                messageSummary = "这是一条视频消息"
            )
        )
        
        // 2. 收藏所有消息
        messages.forEach { message ->
            val result = collectionManager.collectMessage(message)
            assertTrue("消息应该被成功收藏", result)
        }
        
        // 3. 验证收藏数量
        assertEquals("应该有3条收藏消息", 3, collectionManager.getCollectionCount())
        
        // 4. 验证所有消息都已被收藏
        messages.forEach { message ->
            assertTrue("消息应该被标记为已收藏", collectionManager.isMessageCollected(message.messageId))
        }
        
        // 5. 验证收藏列表按时间倒序排列
        val allCollections = collectionManager.getAllCollections()
        assertEquals("应该有3条收藏消息", 3, allCollections.size)
        assertTrue("最新的消息应该在前面", allCollections[0].collectionTimestamp >= allCollections[1].collectionTimestamp)
        
        // 6. 测试搜索功能
        val searchResults = collectionManager.searchCollections("重要")
        assertEquals("搜索结果应该有1条", 1, searchResults.size)
        assertEquals("搜索结果应该是文本消息", messages[0].messageId, searchResults[0].messageId)
        
        // 7. 测试按类型筛选
        val textMessages = collectionManager.filterCollectionsByType(CollectedMessage.TYPE_TEXT)
        assertEquals("应该有1条文本消息", 1, textMessages.size)
        
        val imageMessages = collectionManager.filterCollectionsByType(CollectedMessage.TYPE_IMAGE)
        assertEquals("应该有1条图片消息", 1, imageMessages.size)
        
        val videoMessages = collectionManager.filterCollectionsByType(CollectedMessage.TYPE_VIDEO)
        assertEquals("应该有1条视频消息", 1, videoMessages.size)
        
        // 8. 测试取消收藏
        val firstMessage = messages[0]
        val collectionId = collectionManager.getCollectionIdByMessageId(firstMessage.messageId)
        assertNotNull("应该能获取到收藏ID", collectionId)
        
        val uncollectResult = collectionManager.uncollectMessage(collectionId!!)
        assertTrue("取消收藏应该成功", uncollectResult)
        
        // 9. 验证取消收藏后的状态
        assertFalse("消息应该不再被标记为已收藏", collectionManager.isMessageCollected(firstMessage.messageId))
        assertEquals("收藏数量应该减少到2", 2, collectionManager.getCollectionCount())
        
        // 10. 验证其他消息仍在收藏中
        assertTrue("其他消息应该仍被收藏", collectionManager.isMessageCollected(messages[1].messageId))
        assertTrue("其他消息应该仍被收藏", collectionManager.isMessageCollected(messages[2].messageId))
        
        // 11. 测试清空所有收藏
        val clearResult = collectionManager.clearAllCollections()
        assertTrue("清空收藏应该成功", clearResult)
        assertEquals("收藏数量应该为0", 0, collectionManager.getCollectionCount())
        
        // 12. 验证所有消息都不再被收藏
        messages.forEach { message ->
            assertFalse("消息应该不再被收藏", collectionManager.isMessageCollected(message.messageId))
        }
    }
    
    @Test
    fun testTimeRangeFiltering() {
        val now = System.currentTimeMillis()
        
        // 创建不同时间的消息
        val oldMessage = CollectedMessage(
            collectionId = UUID.randomUUID().toString(),
            messageId = UUID.randomUUID().toString(),
            content = "旧消息",
            senderId = "sender1",
            senderName = "用户1",
            messageType = CollectedMessage.TYPE_TEXT,
            messageTimestamp = now - 86400000, // 1天前
            collectionTimestamp = now - 86400000,
            conversationId = "conv1",
            messageSummary = "旧消息"
        )
        
        val recentMessage = CollectedMessage(
            collectionId = UUID.randomUUID().toString(),
            messageId = UUID.randomUUID().toString(),
            content = "最近的消息",
            senderId = "sender2",
            senderName = "用户2",
            messageType = CollectedMessage.TYPE_TEXT,
            messageTimestamp = now,
            collectionTimestamp = now,
            conversationId = "conv2",
            messageSummary = "最近的消息"
        )
        
        collectionManager.collectMessage(oldMessage)
        collectionManager.collectMessage(recentMessage)
        
        // 筛选最近1小时内的消息
        val recentMessages = collectionManager.filterCollectionsByTimeRange(
            now - 3600000, // 1小时前
            now + 3600000  // 1小时后
        )
        
        assertEquals("应该只有1条最近的消息", 1, recentMessages.size)
        assertEquals("应该是最近的消息", recentMessage.messageId, recentMessages[0].messageId)
    }
    
    @Test
    fun testRecollectAfterUncollect() {
        val message = CollectedMessage(
            collectionId = UUID.randomUUID().toString(),
            messageId = UUID.randomUUID().toString(),
            content = "测试消息",
            senderId = "sender1",
            senderName = "用户1",
            messageType = CollectedMessage.TYPE_TEXT,
            messageTimestamp = System.currentTimeMillis(),
            collectionTimestamp = System.currentTimeMillis(),
            conversationId = "conv1",
            messageSummary = "测试消息"
        )
        
        // 收藏
        collectionManager.collectMessage(message)
        assertTrue("消息应该被收藏", collectionManager.isMessageCollected(message.messageId))
        
        // 取消收藏
        val collectionId = collectionManager.getCollectionIdByMessageId(message.messageId)
        collectionManager.uncollectMessage(collectionId!!)
        assertFalse("消息应该不再被收藏", collectionManager.isMessageCollected(message.messageId))
        
        // 重新收藏
        val newMessage = message.copy(collectionId = UUID.randomUUID().toString())
        collectionManager.collectMessage(newMessage)
        assertTrue("消息应该被重新收藏", collectionManager.isMessageCollected(message.messageId))
    }
}
