package com.juggle.im.android.server.beans;

/**
 * 群组Bean
 */
public class GroupBean {
    private String group_id;
    private String group_name;
    private String group_portrait;
    private String creator_id;  // 群组创建者ID
    private boolean is_creator; // 是否是创建者

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public String getGroup_portrait() {
        return group_portrait;
    }

    public void setGroup_portrait(String group_portrait) {
        this.group_portrait = group_portrait;
    }

    public String getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(String creator_id) {
        this.creator_id = creator_id;
    }

    public boolean isIs_creator() {
        return is_creator;
    }

    public void setIs_creator(boolean is_creator) {
        this.is_creator = is_creator;
    }
}
