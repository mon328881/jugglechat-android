package com.juggle.im.android.utils

import android.content.Context
import android.content.SharedPreferences
import com.juggle.im.android.model.CollectedMessage
import java.util.UUID

/**
 * 消息收藏管理器
 * 负责管理消息的收藏、取消收藏和查询操作
 */
class CollectionManager(context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREF_NAME = "message_collections"
        private const val KEY_COLLECTIONS = "collections"
        private const val KEY_COLLECTION_IDS = "collection_ids"
        
        @Volatile
        private var instance: CollectionManager? = null
        
        fun getInstance(context: Context): CollectionManager {
            return instance ?: synchronized(this) {
                instance ?: CollectionManager(context).also { instance = it }
            }
        }
    }
    
    /**
     * 收藏消息
     * @param message 要收藏的消息
     * @return 收藏是否成功
     */
    fun collectMessage(message: CollectedMessage): Boolean {
        return try {
            val collectionId = message.collectionId.ifEmpty { UUID.randomUUID().toString() }
            val updatedMessage = message.copy(collectionId = collectionId)
            
            // 获取现有的收藏ID列表
            val collectionIds = getCollectionIds().toMutableSet()
            collectionIds.add(collectionId)
            
            // 保存收藏ID列表
            sharedPreferences.edit().apply {
                putStringSet(KEY_COLLECTION_IDS, collectionIds)
                putString(
                    "collection_$collectionId",
                    serializeMessage(updatedMessage)
                )
                apply()
            }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 取消收藏消息
     * @param collectionId 收藏记录的ID
     * @return 取消收藏是否成功
     */
    fun uncollectMessage(collectionId: String): Boolean {
        return try {
            val collectionIds = getCollectionIds().toMutableSet()
            collectionIds.remove(collectionId)
            
            sharedPreferences.edit().apply {
                putStringSet(KEY_COLLECTION_IDS, collectionIds)
                remove("collection_$collectionId")
                apply()
            }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 获取所有收藏的消息
     * @return 收藏消息列表，按收藏时间倒序排列
     */
    fun getAllCollections(): List<CollectedMessage> {
        return try {
            val collectionIds = getCollectionIds()
            collectionIds.mapNotNull { id ->
                val json = sharedPreferences.getString("collection_$id", null)
                json?.let { deserializeMessage(it) }
            }.sortedByDescending { it.collectionTimestamp }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * 搜索收藏的消息
     * @param keyword 搜索关键词
     * @return 匹配的收藏消息列表
     */
    fun searchCollections(keyword: String): List<CollectedMessage> {
        return try {
            val allCollections = getAllCollections()
            allCollections.filter { message ->
                message.content.contains(keyword, ignoreCase = true) ||
                message.senderName.contains(keyword, ignoreCase = true) ||
                message.messageSummary.contains(keyword, ignoreCase = true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * 按消息类型筛选收藏
     * @param messageType 消息类型
     * @return 指定类型的收藏消息列表
     */
    fun filterCollectionsByType(messageType: String): List<CollectedMessage> {
        return try {
            getAllCollections().filter { it.messageType == messageType }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * 按时间范围筛选收藏
     * @param startTime 开始时间戳
     * @param endTime 结束时间戳
     * @return 在指定时间范围内的收藏消息列表
     */
    fun filterCollectionsByTimeRange(startTime: Long, endTime: Long): List<CollectedMessage> {
        return try {
            getAllCollections().filter { 
                it.collectionTimestamp in startTime..endTime
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * 检查消息是否已被收藏
     * @param messageId 消息ID
     * @return 消息是否已被收藏
     */
    fun isMessageCollected(messageId: String): Boolean {
        return try {
            getAllCollections().any { it.messageId == messageId }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 获取消息的收藏ID
     * @param messageId 消息ID
     * @return 收藏ID，如果未收藏则返回null
     */
    fun getCollectionIdByMessageId(messageId: String): String? {
        return try {
            getAllCollections().find { it.messageId == messageId }?.collectionId
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 获取收藏数量
     * @return 收藏消息的总数
     */
    fun getCollectionCount(): Int {
        return try {
            getCollectionIds().size
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }
    
    /**
     * 清空所有收藏
     * @return 清空是否成功
     */
    fun clearAllCollections(): Boolean {
        return try {
            val collectionIds = getCollectionIds()
            sharedPreferences.edit().apply {
                collectionIds.forEach { id ->
                    remove("collection_$id")
                }
                remove(KEY_COLLECTION_IDS)
                apply()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    // 私有辅助方法
    
    private fun getCollectionIds(): Set<String> {
        return sharedPreferences.getStringSet(KEY_COLLECTION_IDS, emptySet()) ?: emptySet()
    }
    
    private fun serializeMessage(message: CollectedMessage): String {
        return buildString {
            append(message.collectionId).append("|")
            append(message.messageId).append("|")
            append(message.content).append("|")
            append(message.senderId).append("|")
            append(message.senderName).append("|")
            append(message.messageType).append("|")
            append(message.messageTimestamp).append("|")
            append(message.collectionTimestamp).append("|")
            append(message.conversationId).append("|")
            append(message.messageSummary)
        }
    }
    
    private fun deserializeMessage(data: String): CollectedMessage? {
        return try {
            val parts = data.split("|")
            if (parts.size < 9) return null
            
            CollectedMessage(
                collectionId = parts[0],
                messageId = parts[1],
                content = parts[2],
                senderId = parts[3],
                senderName = parts[4],
                messageType = parts[5],
                messageTimestamp = parts[6].toLong(),
                collectionTimestamp = parts[7].toLong(),
                conversationId = parts[8],
                messageSummary = if (parts.size > 9) parts[9] else ""
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
