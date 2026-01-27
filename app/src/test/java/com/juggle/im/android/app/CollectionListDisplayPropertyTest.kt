package com.juggle.im.android.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.juggle.im.android.model.CollectedMessage
import com.juggle.im.android.utils.CollectionManager
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import java.util.UUID

/**
 * 属性 72: 收藏列表显示
 * 验证: 需求 17.3, 17.5
 * 
 * 应用应该显示所有收藏的消息，按收藏时间排序，支持搜索和筛选
 */
class CollectionListDisplayPropertyTest : FunSpec({
    
    private lateinit var context: Context
    private lateinit var collectionManager: CollectionManager
    
    beforeTest {
        context = ApplicationProvider.getApplicationContext()
        collectionManager = CollectionManager.getInstance(context)
        collectionManager.clearAllCollections()
    }
    
    afterTest {
        collectionManager.clearAllCollections()
    }
    
    test("属性 72: 收藏列表显示 - 应显示所有收藏的消息") {
        checkAll(
            Arb.string(minSize = 1, maxSize = 100),
            Arb.string(minSize = 1, maxSize = 100),
            Arb.string(minSize = 1, maxSize = 100)
        ) { content1, content2, content3 ->
            val messages = listOf(
                CollectedMessage(
                    collectionId = UUID.randomUUID().toString(),
                    messageId = UUID.randomUUID().toString(),
                    content = content1,
                    senderId = "sender1",
                    senderName = "User1",
                    messageType = CollectedMessage.TYPE_TEXT,
                    messageTimestamp = System.currentTimeMillis(),
                    collectionTimestamp = System.currentTimeMillis(),
                    conversationId = "conv1",
                    messageSummary = content1.take(50)
                ),
                CollectedMessage(
                    collectionId = UUID.randomUUID().toString(),
                    messageId = UUID.randomUUID().toString(),
                    content = content2,
                    senderId = "sender2",
                    senderName = "User2",
                    messageType = CollectedMessage.TYPE_IMAGE,
                    messageTimestamp = System.currentTimeMillis(),
                    collectionTimestamp = System.currentTimeMillis(),
                    conversationId = "conv2",
                    messageSummary = content2.take(50)
                ),
                CollectedMessage(
                    collectionId = UUID.randomUUID().toString(),
                    messageId = UUID.randomUUID().toString(),
                    content = content3,
                    senderId = "sender3",
                    senderName = "User3",
                    messageType = CollectedMessage.TYPE_VIDEO,
                    messageTimestamp = System.currentTimeMillis(),
                    collectionTimestamp = System.currentTimeMillis(),
                    conversationId = "conv3",
                    messageSummary = content3.take(50)
                )
            )
            
            // 收藏所有消息
            messages.forEach { collectionManager.collectMessage(it) }
            
            // 验证所有消息都能被显示
            val collections = collectionManager.getAllCollections()
            collections.size shouldBe 3
            messages.forEach { message ->
                collections.any { it.messageId == message.messageId } shouldBe true
            }
        }
    }
    
    test("属性 72: 收藏列表显示 - 应按收藏时间倒序排列") {
        checkAll(
            Arb.string(minSize = 1, maxSize = 100),
            Arb.string(minSize = 1, maxSize = 100)
        ) { content1, content2 ->
            val now = System.currentTimeMillis()
            val message1 = CollectedMessage(
                collectionId = UUID.randomUUID().toString(),
                messageId = UUID.randomUUID().toString(),
                content = content1,
                senderId = "sender1",
                senderName = "User1",
                messageType = CollectedMessage.TYPE_TEXT,
                messageTimestamp = now - 5000,
                collectionTimestamp = now - 5000,
                conversationId = "conv1",
                messageSummary = content1.take(50)
            )
            
            val message2 = CollectedMessage(
                collectionId = UUID.randomUUID().toString(),
                messageId = UUID.randomUUID().toString(),
                content = content2,
                senderId = "sender2",
                senderName = "User2",
                messageType = CollectedMessage.TYPE_TEXT,
                messageTimestamp = now,
                collectionTimestamp = now,
                conversationId = "conv2",
                messageSummary = content2.take(50)
            )
            
            collectionManager.collectMessage(message1)
            collectionManager.collectMessage(message2)
            
            // 验证按时间倒序排列
            val collections = collectionManager.getAllCollections()
            collections[0].collectionTimestamp >= collections[1].collectionTimestamp shouldBe true
        }
    }
    
    test("属性 72: 收藏列表显示 - 应支持按关键词搜索") {
        checkAll(Arb.string(minSize = 1, maxSize = 50)) { keyword ->
            val message1 = CollectedMessage(
                collectionId = UUID.randomUUID().toString(),
                messageId = UUID.randomUUID().toString(),
                content = "这是一条包含${keyword}的消息",
                senderId = "sender1",
                senderName = "User1",
                messageType = CollectedMessage.TYPE_TEXT,
                messageTimestamp = System.currentTimeMillis(),
                collectionTimestamp = System.currentTimeMillis(),
                conversationId = "conv1",
                messageSummary = "这是一条包含${keyword}的消息"
            )
            
            val message2 = CollectedMessage(
                collectionId = UUID.randomUUID().toString(),
                messageId = UUID.randomUUID().toString(),
                content = "这是另一条消息",
                senderId = "sender2",
                senderName = "User2",
                messageType = CollectedMessage.TYPE_TEXT,
                messageTimestamp = System.currentTimeMillis(),
                collectionTimestamp = System.currentTimeMillis(),
                conversationId = "conv2",
                messageSummary = "这是另一条消息"
            )
            
            collectionManager.collectMessage(message1)
            collectionManager.collectMessage(message2)
            
            // 验证搜索功能
            val results = collectionManager.searchCollections(keyword)
            results.size shouldBe 1
            results[0].messageId shouldBe message1.messageId
        }
    }
    
    test("属性 72: 收藏列表显示 - 应支持按消息类型筛选") {
        checkAll(Arb.string(minSize = 1, maxSize = 100)) { content ->
            val textMessage = CollectedMessage(
                collectionId = UUID.randomUUID().toString(),
                messageId = UUID.randomUUID().toString(),
                content = content,
                senderId = "sender1",
                senderName = "User1",
                messageType = CollectedMessage.TYPE_TEXT,
                messageTimestamp = System.currentTimeMillis(),
                collectionTimestamp = System.currentTimeMillis(),
                conversationId = "conv1",
                messageSummary = content.take(50)
            )
            
            val imageMessage = CollectedMessage(
                collectionId = UUID.randomUUID().toString(),
                messageId = UUID.randomUUID().toString(),
                content = content,
                senderId = "sender2",
                senderName = "User2",
                messageType = CollectedMessage.TYPE_IMAGE,
                messageTimestamp = System.currentTimeMillis(),
                collectionTimestamp = System.currentTimeMillis(),
                conversationId = "conv2",
                messageSummary = content.take(50)
            )
            
            collectionManager.collectMessage(textMessage)
            collectionManager.collectMessage(imageMessage)
            
            // 验证按类型筛选
            val textMessages = collectionManager.filterCollectionsByType(CollectedMessage.TYPE_TEXT)
            textMessages.size shouldBe 1
            textMessages[0].messageType shouldBe CollectedMessage.TYPE_TEXT
            
            val imageMessages = collectionManager.filterCollectionsByType(CollectedMessage.TYPE_IMAGE)
            imageMessages.size shouldBe 1
            imageMessages[0].messageType shouldBe CollectedMessage.TYPE_IMAGE
        }
    }
    
    test("属性 72: 收藏列表显示 - 应支持按时间范围筛选") {
        checkAll(Arb.string(minSize = 1, maxSize = 100)) { content ->
            val now = System.currentTimeMillis()
            val oldMessage = CollectedMessage(
                collectionId = UUID.randomUUID().toString(),
                messageId = UUID.randomUUID().toString(),
                content = content,
                senderId = "sender1",
                senderName = "User1",
                messageType = CollectedMessage.TYPE_TEXT,
                messageTimestamp = now - 86400000, // 1天前
                collectionTimestamp = now - 86400000,
                conversationId = "conv1",
                messageSummary = content.take(50)
            )
            
            val newMessage = CollectedMessage(
                collectionId = UUID.randomUUID().toString(),
                messageId = UUID.randomUUID().toString(),
                content = content,
                senderId = "sender2",
                senderName = "User2",
                messageType = CollectedMessage.TYPE_TEXT,
                messageTimestamp = now,
                collectionTimestamp = now,
                conversationId = "conv2",
                messageSummary = content.take(50)
            )
            
            collectionManager.collectMessage(oldMessage)
            collectionManager.collectMessage(newMessage)
            
            // 验证按时间范围筛选
            val recentMessages = collectionManager.filterCollectionsByTimeRange(
                now - 3600000, // 1小时前
                now + 3600000  // 1小时后
            )
            recentMessages.size shouldBe 1
            recentMessages[0].messageId shouldBe newMessage.messageId
        }
    }
})
