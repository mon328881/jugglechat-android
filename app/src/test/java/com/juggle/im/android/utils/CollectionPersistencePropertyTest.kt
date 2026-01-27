package com.juggle.im.android.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.juggle.im.android.model.CollectedMessage
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import java.util.UUID

/**
 * 属性 71: 收藏消息持久化
 * 验证: 需求 17.2
 * 
 * 收藏的消息应该被保存到本地数据库，用户可以随时查看
 */
class CollectionPersistencePropertyTest : FunSpec({
    
    private lateinit var context: Context
    private lateinit var collectionManager: CollectionManager
    
    beforeTest {
        context = ApplicationProvider.getApplicationContext()
        collectionManager = CollectionManager.getInstance(context)
        // 清空之前的测试数据
        collectionManager.clearAllCollections()
    }
    
    afterTest {
        collectionManager.clearAllCollections()
    }
    
    test("属性 71: 收藏消息持久化 - 收藏的消息应被保存") {
        checkAll(
            Arb.string(minSize = 1, maxSize = 100),
            Arb.string(minSize = 1, maxSize = 50),
            Arb.long(min = 1000000000000L, max = System.currentTimeMillis())
        ) { content, senderName, timestamp ->
            val message = CollectedMessage(
                collectionId = UUID.randomUUID().toString(),
                messageId = UUID.randomUUID().toString(),
                content = content,
                senderId = "sender_${UUID.randomUUID()}",
                senderName = senderName,
                messageType = CollectedMessage.TYPE_TEXT,
                messageTimestamp = timestamp,
                collectionTimestamp = System.currentTimeMillis(),
                conversationId = "conv_${UUID.randomUUID()}",
                messageSummary = content.take(50)
            )
            
            // 收藏消息
            val result = collectionManager.collectMessage(message)
            result shouldBe true
            
            // 验证消息已被保存
            val collections = collectionManager.getAllCollections()
            collections.any { it.messageId == message.messageId } shouldBe true
        }
    }
    
    test("属性 71: 收藏消息持久化 - 收藏的消息应能被检索") {
        checkAll(
            Arb.string(minSize = 1, maxSize = 100),
            Arb.string(minSize = 1, maxSize = 50)
        ) { content, senderName ->
            val messageId = UUID.randomUUID().toString()
            val message = CollectedMessage(
                collectionId = UUID.randomUUID().toString(),
                messageId = messageId,
                content = content,
                senderId = "sender_${UUID.randomUUID()}",
                senderName = senderName,
                messageType = CollectedMessage.TYPE_TEXT,
                messageTimestamp = System.currentTimeMillis(),
                collectionTimestamp = System.currentTimeMillis(),
                conversationId = "conv_${UUID.randomUUID()}",
                messageSummary = content.take(50)
            )
            
            // 收藏消息
            collectionManager.collectMessage(message)
            
            // 验证消息可被检索
            val isCollected = collectionManager.isMessageCollected(messageId)
            isCollected shouldBe true
        }
    }
    
    test("属性 71: 收藏消息持久化 - 多条消息应能同时被保存") {
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
            
            // 验证所有消息都被保存
            val collections = collectionManager.getAllCollections()
            collections.size shouldBe 3
            messages.forEach { message ->
                collections.any { it.messageId == message.messageId } shouldBe true
            }
        }
    }
    
    test("属性 71: 收藏消息持久化 - 收藏消息应按时间排序") {
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
                messageTimestamp = now - 1000,
                collectionTimestamp = now - 1000,
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
            
            // 先收藏第一条消息，再收藏第二条消息
            collectionManager.collectMessage(message1)
            collectionManager.collectMessage(message2)
            
            // 验证消息按时间倒序排列（最新的在前）
            val collections = collectionManager.getAllCollections()
            collections.size shouldBe 2
            collections[0].collectionTimestamp shouldBe now
            collections[1].collectionTimestamp shouldBe (now - 1000)
        }
    }
})
