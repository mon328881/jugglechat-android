package com.juggle.im.android.model

/**
 * 收藏消息数据模型
 * 用于存储用户收藏的消息信息
 */
data class CollectedMessage(
    // 收藏记录的唯一标识
    val collectionId: String,
    // 原始消息的ID
    val messageId: String,
    // 消息内容
    val content: String,
    // 消息发送者ID
    val senderId: String,
    // 消息发送者名称
    val senderName: String,
    // 消息类型（TEXT, IMAGE, VIDEO等）
    val messageType: String,
    // 原始消息的时间戳
    val messageTimestamp: Long,
    // 收藏时间戳
    val collectionTimestamp: Long,
    // 消息所属的对话ID
    val conversationId: String,
    // 消息摘要（用于列表显示）
    val messageSummary: String = ""
) {
    companion object {
        // 消息类型常量
        const val TYPE_TEXT = "TEXT"
        const val TYPE_IMAGE = "IMAGE"
        const val TYPE_VIDEO = "VIDEO"
        const val TYPE_AUDIO = "AUDIO"
        const val TYPE_FILE = "FILE"
    }
}
