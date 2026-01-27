package com.juggle.im.android.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.juggle.im.android.model.CollectedMessage
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.util.UUID

/**
 * 收藏管理器单元测试
 */
class CollectionManagerTest {
    
    private lateinit var context: Context
    private lateinit var collectionManager: CollectionManager
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        collectionManager = CollectionManager.getInstance(context)
        collectionManager.clearAllCollections()
    }
    
    @Test
    fun testCollectMessage() {
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
        
        val result = collectionManager.collectMessage(message)
        assertTrue(result)
        assertTrue(collectionManager.isMessageCollected(message.messageId))
    }
    
    @Test
    fun testUncollectMessage() {
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
        
        collectionManager.collectMessage(message)
        assertTrue(collectionManager.isMessageCollected(message.messageId))
        
        val result = collectionManager.uncollectMessage(message.collectionId)
        assertTrue(result)
        assertFalse(collectionManager.isMessageCollected(message.messageId))
    }
    
    @Test
    fun testGetAllCollections() {
        val message1 = CollectedMessage(
            collectionId = UUID.randomUUID().toString(),
            messageId = UUID.randomUUID().toString(),
            content = "消息1",
            senderId = "sender1",
            senderName = "用户1",
            messageType = CollectedMessage.TYPE_TEXT,
            messageTimestamp = System.currentTimeMillis(),
            collectionTimestamp = System.currentTimeMillis(),
            conversationId = "conv1",
            messageSummary = "消息1"
        )
        
        val message2 = CollectedMessage(
            collectionId = UUID.randomUUID().toString(),
            messageId = UUID.randomUUID().toString(),
            content = "消息2",
            senderId = "sender2",
            senderName = "用户2",
            messageType = CollectedMessage.TYPE_IMAGE,
            messageTimestamp = System.currentTimeMillis(),
            collectionTimestamp = System.currentTimeMillis(),
            conversationId = "conv2",
            messageSummary = "消息2"
        )
        
        collectionManager.collectMessage(message1)
        collectionManager.collectMessage(message2)
        
        val collections = collectionManager.getAllCollections()
        assertEquals(2, collections.size)
    }
    
    @Test
    fun testSearchCollections() {
        val message1 = CollectedMessage(
            collectionId = UUID.randomUUID().toString(),
            messageId = UUID.randomUUID().toString(),
            content = "这是一条包含关键词的消息",
            senderId = "sender1",
            senderName = "用户1",
            messageType = CollectedMessage.TYPE_TEXT,
            messageTimestamp = System.currentTimeMillis(),
            collectionTimestamp = System.currentTimeMillis(),
            conversationId = "conv1",
            messageSummary = "这是一条包含关键词的消息"
        )
        
        val message2 = CollectedMessage(
            collectionId = UUID.randomUUID().toString(),
            messageId = UUID.randomUUID().toString(),
            content = "这是另一条消息",
            senderId = "sender2",
            senderName = "用户2",
            messageType = CollectedMessage.TYPE_TEXT,
            messageTimestamp = System.currentTimeMillis(),
            collectionTimestamp = System.currentTimeMillis(),
            conversationId = "conv2",
            messageSummary = "这是另一条消息"
        )
        
        collectionManager.collectMessage(message1)
        collectionManager.collectMessage(message2)
        
        val results = collectionManager.searchCollections("关键词")
        assertEquals(1, results.size)
        assertEquals(message1.messageId, results[0].messageId)
    }
    
    @Test
    fun testFilterCollectionsByType() {
        val textMessage = CollectedMessage(
            collectionId = UUID.randomUUID().toString(),
            messageId = UUID.randomUUID().toString(),
            content = "文本消息",
            senderId = "sender1",
            senderName = "用户1",
            messageType = CollectedMessage.TYPE_TEXT,
            messageTimestamp = System.currentTimeMillis(),
            collectionTimestamp = System.currentTimeMillis(),
            conversationId = "conv1",
            messageSummary = "文本消息"
        )
        
        val imageMessage = CollectedMessage(
            collectionId = UUID.randomUUID().toString(),
            messageId = UUID.randomUUID().toString(),
            content = "图片消息",
            senderId = "sender2",
            senderName = "用户2",
            messageType = CollectedMessage.TYPE_IMAGE,
            messageTimestamp = System.currentTimeMillis(),
            collectionTimestamp = System.currentTimeMillis(),
            conversationId = "conv2",
            messageSummary = "图片消息"
        )
        
        collectionManager.collectMessage(textMessage)
        collectionManager.collectMessage(imageMessage)
        
        val textMessages = collectionManager.filterCollectionsByType(CollectedMessage.TYPE_TEXT)
        assertEquals(1, textMessages.size)
        assertEquals(CollectedMessage.TYPE_TEXT, textMessages[0].messageType)
        
        val imageMessages = collectionManager.filterCollectionsByType(CollectedMessage.TYPE_IMAGE)
        assertEquals(1, imageMessages.size)
        assertEquals(CollectedMessage.TYPE_IMAGE, imageMessages[0].messageType)
    }
    
    @Test
    fun testGetCollectionCount() {
        assertEquals(0, collectionManager.getCollectionCount())
        
        val message1 = CollectedMessage(
            collectionId = UUID.randomUUID().toString(),
            messageId = UUID.randomUUID().toString(),
            content = "消息1",
            senderId = "sender1",
            senderName = "用户1",
            messageType = CollectedMessage.TYPE_TEXT,
            messageTimestamp = System.currentTimeMillis(),
            collectionTimestamp = System.currentTimeMillis(),
            conversationId = "conv1",
            messageSummary = "消息1"
        )
        
        collectionManager.collectMessage(message1)
        assertEquals(1, collectionManager.getCollectionCount())
    }
    
    @Test
    fun testClearAllCollections() {
        val message1 = CollectedMessage(
            collectionId = UUID.randomUUID().toString(),
            messageId = UUID.randomUUID().toString(),
            content = "消息1",
            senderId = "sender1",
            senderName = "用户1",
            messageType = CollectedMessage.TYPE_TEXT,
            messageTimestamp = System.currentTimeMillis(),
            collectionTimestamp = System.currentTimeMillis(),
            conversationId = "conv1",
            messageSummary = "消息1"
        )
        
        collectionManager.collectMessage(message1)
        assertEquals(1, collectionManager.getCollectionCount())
        
        val result = collectionManager.clearAllCollections()
        assertTrue(result)
        assertEquals(0, collectionManager.getCollectionCount())
    }
}
