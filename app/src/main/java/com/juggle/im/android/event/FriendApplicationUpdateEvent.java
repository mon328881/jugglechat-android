package com.juggle.im.android.event;

/**
 *  好友申请更新事件，用于在处理好友申请后更新通讯录徽章等 UI。
 */
public class FriendApplicationUpdateEvent {
    private final int pendingCount;

    public FriendApplicationUpdateEvent(int pendingCount) {
        this.pendingCount = pendingCount;
    }

    public int getPendingCount() {
        return pendingCount;
    }
}
