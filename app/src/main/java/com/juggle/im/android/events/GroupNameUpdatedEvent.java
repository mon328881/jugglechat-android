package com.juggle.im.android.events;

/**
 * Event posted when group name is updated
 */
public class GroupNameUpdatedEvent {
    private String groupId;
    private String newGroupName;

    public GroupNameUpdatedEvent(String groupId, String newGroupName) {
        this.groupId = groupId;
        this.newGroupName = newGroupName;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getNewGroupName() {
        return newGroupName;
    }
}
