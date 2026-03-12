package com.juggle.im.android.server.beans;

import com.google.gson.annotations.SerializedName;

/**
 * 群组管理Bean
 */
public class GroupManagementBean {
    @SerializedName("group_mute")
    private int groupMute;
    @SerializedName("max_admin_count")
    private int maxAdminCount;
    @SerializedName("admin_count")
    private int adminCount;
    @SerializedName("group_verify_type")
    private int groupVerifyType;
    @SerializedName("group_his_msg_visible")
    private int historyMessageVisible;
    
    // 权限字段
    @SerializedName("group_add_member_right")
    private Integer groupAddMemberRight;
    @SerializedName("group_top_msg_right")
    private Integer groupTopMsgRight;
    @SerializedName("group_mention_all_right")
    private Integer groupMentionAllRight;
    @SerializedName("group_edit_msg_right")
    private Integer groupEditMsgRight;
    @SerializedName("group_send_msg_right")
    private Integer groupSendMsgRight;
    @SerializedName("group_set_msg_life_right")
    private Integer groupSetMsgLifeRight;

    public int getGroupMute() {
        return groupMute;
    }

    public void setGroupMute(int groupMute) {
        this.groupMute = groupMute;
    }

    public int getMaxAdminCount() {
        return maxAdminCount;
    }

    public void setMaxAdminCount(int maxAdminCount) {
        this.maxAdminCount = maxAdminCount;
    }

    public int getAdminCount() {
        return adminCount;
    }

    public void setAdminCount(int adminCount) {
        this.adminCount = adminCount;
    }

    public int getGroupVerifyType() {
        return groupVerifyType;
    }

    public void setGroupVerifyType(int groupVerifyType) {
        this.groupVerifyType = groupVerifyType;
    }

    public int getHistoryMessageVisible() {
        return historyMessageVisible;
    }

    public void setHistoryMessageVisible(int historyMessageVisible) {
        this.historyMessageVisible = historyMessageVisible;
    }

    public Integer getGroupAddMemberRight() {
        return groupAddMemberRight != null ? groupAddMemberRight : 7;
    }

    public void setGroupAddMemberRight(Integer groupAddMemberRight) {
        this.groupAddMemberRight = groupAddMemberRight;
    }

    public Integer getGroupTopMsgRight() {
        return groupTopMsgRight != null ? groupTopMsgRight : 7;
    }

    public void setGroupTopMsgRight(Integer groupTopMsgRight) {
        this.groupTopMsgRight = groupTopMsgRight;
    }

    public Integer getGroupMentionAllRight() {
        return groupMentionAllRight != null ? groupMentionAllRight : 7;
    }

    public void setGroupMentionAllRight(Integer groupMentionAllRight) {
        this.groupMentionAllRight = groupMentionAllRight;
    }

    public Integer getGroupEditMsgRight() {
        return groupEditMsgRight != null ? groupEditMsgRight : 3;
    }

    public void setGroupEditMsgRight(Integer groupEditMsgRight) {
        this.groupEditMsgRight = groupEditMsgRight;
    }

    public Integer getGroupSendMsgRight() {
        return groupSendMsgRight != null ? groupSendMsgRight : 7;
    }

    public void setGroupSendMsgRight(Integer groupSendMsgRight) {
        this.groupSendMsgRight = groupSendMsgRight;
    }

    public Integer getGroupSetMsgLifeRight() {
        return groupSetMsgLifeRight != null ? groupSetMsgLifeRight : 7;
    }

    public void setGroupSetMsgLifeRight(Integer groupSetMsgLifeRight) {
        this.groupSetMsgLifeRight = groupSetMsgLifeRight;
    }
}
