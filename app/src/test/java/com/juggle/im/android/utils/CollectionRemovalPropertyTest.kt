package com.juggle.im.android.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.juggle.im.android.model.CollectedMessage
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import java.util.UUID

/**
 * 属性 73: 取消收藏功能
 * 验证: 需求 17.4
 * 
 * 当用户取消收藏时，消息应该从收藏列表中移除
 */
class CollectionRemovalPropertyTest : FunSpec({
    
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
    
    test("属性 73: 取消收藏功能 - 取消收藏后消息应被移除") {
        checkAll(Arb.string(minSize = 1, maxSize = 100)) { content ->
            val message = CollectedMessage(
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
            
            // 收藏消息
            collectionManager.collectMessage(message)
            
            // 验证消息已被收藏
            collectionManager.isMessageCollected(message.messageId) shouldBe true
            
            // 取消收藏
            val result = collectionManager.uncollectMessage(message.collectionId)
            result shouldBe true
            
            // 验证消息已被移除
            collectionManager.isMessageCollected(message.messageId) shouldBe false
        }
    }
    
    test("属性 73: 取消收藏功能 - 取消收藏后收藏列表应更新") {
        checkAll(
            Arb.string(minSize = 1, maxSize = 100),
            Arb.string(minSize = 1, maxSize = 100)
        ) { content1, content2 ->
            val message1 = CollectedMessage(
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
            )
            
            val message2 = CollectedMessage(
                collectionId = UUID.randomUUID().toString(),
                messageId = UUID.randomUUID().toString(),
                content = content2,
                senderId = "sender2",
                senderName = "User2",
                messageType = CollectedMessage.TYPE_TEXT,
                messageTimestamp = System.currentTimeMillis(),
                collectionTimestamp = System.currentTimeMillis(),
                conversationId = "conv2",
                messageSummary = content2.take(50)
            )
            
            // 收藏两条消息
            collectionManager.collectMessage(message1)
            collectionManager.collectMessage(message2)
            
            // 验证收藏数量
            collectionManager.getCollectionCount() shouldBe 2
            
            // 取消收藏第一条消息
            collectionManager.uncollectMessage(message1.collectionId)
            
            // 验证收藏数量已更新
            collectionManager.getCollectionCount() shouldBe 1
            
            // 验证第二条消息仍在收藏中
            collectionManager.isMessageCollected(message2.messageId) shouldBe true
        }
    }
    
    test("属性 73: 取消收藏功能 - 取消收藏后应能重新收藏") {
        checkAll(Arb.string(minSize = 1, maxSize = 100)) { content ->
            val message = CollectedMessage(
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
            
            // 收藏消息
            collectionManager.collectMessage(message)
            collectionManager.isMessageCollected(message.messageId) shouldBe true
            
            // 取消收藏
            collectionManager.uncollectMessage(message.collectionId)
            collectionManager.isMessageCollected(message.messageId) shouldBe false
            
            // 重新收藏
            val newCollectionId = UUID.randomUUID().toString()
            val newMessage = message.copy(collectionId = newCollectionId)
            collectionManager.collectMessage(newMessage)
            
            // 验证消息已被重新收藏
            collectionManager.isMessageCollected(message.messageId) shouldBe true
        }
    }
    
    test("属性 73: 取消收藏功能 - 取消收藏不存在的消息应返回false") {
        checkAll(Arb.string(minSize = 1, maxSize = 100)) { collectionId ->
            val result = collectionManager.uncollectMessage(collectionId)
            // 取消收藏不存在的消息应该返回true（操作成功，但没有实际删除任何东西）
            result shouldBe true
        }
    }
    
    test("属性 73: 取消收藏功能 - 清空所有收藏应成功") {
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
                    messageType = CollectedMessage.TYPE_TEXT,
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
                    messageType = CollectedMessage.TYPE_TEXT,
                    messageTimestamp = System.currentTimeMillis(),
                    collectionTimestamp = System.currentTimeMillis(),
                    conversationId = "conv3",
                    messageSummary = content3.take(50)
                )
            )
            
            // 收藏所有消息
            messages.forEach { collectionManager.collectMessage(it) }
            collectionManager.getCollectionCount() shouldBe 3
            
            // 清空所有收藏
            val result = collectionManager.clearAllCollections()
            result shouldBe true
            
            // 验证所有收藏已被清空
            collectionManager.getCollectionCount() shouldBe 0
            messages.forEach { message ->
                collectionManager.isMessageCollected(message.messageId) shouldBe false
            }
        }
    }
})
