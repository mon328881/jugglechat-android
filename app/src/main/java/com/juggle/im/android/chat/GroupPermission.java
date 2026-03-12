package com.juggle.im.android.chat;

/**
 * 群组权限常量定义
 */
public class GroupPermission {
    // 权限值
    public static final int ALL_MEMBERS = 7;           // 全部成员
    public static final int OWNER_AND_ADMIN = 3;       // 群主和管理员
    public static final int OWNER_ONLY = 1;            // 仅群主

    // 权限类型
    public static final String KEY_ADD_MEMBER = "group_add_member_right";
    public static final String KEY_TOP_MSG = "group_top_msg_right";
    public static final String KEY_MENTION_ALL = "group_mention_all_right";
    public static final String KEY_EDIT_MSG = "group_edit_msg_right";
    public static final String KEY_SEND_MSG = "group_send_msg_right";
    public static final String KEY_SET_MSG_LIFE = "group_set_msg_life_right";
    public static final String KEY_HIDE_GRP_MSG = "hide_grp_msg";

    /**
     * 将权限值转换为显示文本
     */
    public static String getPermissionText(int value) {
        switch (value) {
            case ALL_MEMBERS:
                return "全部成员";
            case OWNER_AND_ADMIN:
                return "群主和管理员";
            case OWNER_ONLY:
                return "仅群主";
            default:
                return "全部成员";
        }
    }

    /**
     * 获取所有权限选项
     */
    public static String[] getPermissionOptions() {
        return new String[]{
                "全部成员",
                "群主和管理员",
                "仅群主"
        };
    }

    /**
     * 根据选项索引获取权限值
     */
    public static int getPermissionValue(int index) {
        switch (index) {
            case 0:
                return ALL_MEMBERS;
            case 1:
                return OWNER_AND_ADMIN;
            case 2:
                return OWNER_ONLY;
            default:
                return ALL_MEMBERS;
        }
    }

    /**
     * 根据权限值获取选项索引
     */
    public static int getPermissionIndex(int value) {
        switch (value) {
            case ALL_MEMBERS:
                return 0;
            case OWNER_AND_ADMIN:
                return 1;
            case OWNER_ONLY:
                return 2;
            default:
                return 0;
        }
    }
}
