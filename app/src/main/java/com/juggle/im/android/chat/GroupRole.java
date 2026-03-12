package com.juggle.im.android.chat;

/**
 * 群组成员角色定义
 */
public class GroupRole {
    public static final int MEMBER = 0;      // 普通成员
    public static final int OWNER = 1;       // 群主
    public static final int ADMIN = 2;       // 管理员
    public static final int NOT_MEMBER = 3;  // 非成员

    /**
     * 判断是否是群主
     */
    public static boolean isOwner(int role) {
        return role == OWNER;
    }

    /**
     * 判断是否是管理员或群主
     */
    public static boolean isOwnerOrAdmin(int role) {
        return role == OWNER || role == ADMIN;
    }

    /**
     * 获取角色名称
     */
    public static String getRoleName(int role) {
        switch (role) {
            case OWNER:
                return "群主";
            case ADMIN:
                return "管理员";
            case MEMBER:
                return "成员";
            case NOT_MEMBER:
                return "非成员";
            default:
                return "未知";
        }
    }
}
