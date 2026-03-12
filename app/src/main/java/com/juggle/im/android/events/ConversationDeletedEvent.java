package com.juggle.im.android.events;

/**
 * Event posted when a conversation is deleted (e.g., user quits group or group is dissolved)
 */
public class ConversationDeletedEvent {
    private String conversationId;
    private boolean isGroup;

    public ConversationDeletedEvent(String conversationId, boolean isGroup) {
        this.conversationId = conversationId;
        this.isGroup = isGroup;
    }

    public String getConversationId() {
        return conversationId;
    }

    public boolean isGroup() {
        return isGroup;
    }
}
