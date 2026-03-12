package com.juggle.im.android.event;

/**
 * 简单的本地事件，仅携带会话 ID，用于在 UI 层主动移除会话（例如退出群聊后）。
 */
public class ConversationIdDeletedEvent {
    private final String conversationId;

    public ConversationIdDeletedEvent(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getConversationId() {
        return conversationId;
    }
}
