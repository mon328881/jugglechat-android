package com.juggle.im.android.server.beans;

import java.util.List;

/**
 * 好友列表数据包装类
 */
public class FriendsListData {
    private List<FriendBean> items;

    public List<FriendBean> getItems() {
        return items;
    }

    public void setItems(List<FriendBean> items) {
        this.items = items;
    }
}
