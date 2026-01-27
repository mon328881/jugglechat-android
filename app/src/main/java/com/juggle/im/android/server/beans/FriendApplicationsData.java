package com.juggle.im.android.server.beans;

import java.util.List;

/**
 * 好友申请列表数据包装类
 */
public class FriendApplicationsData {
    private List<FriendApplicationBean> items;

    public List<FriendApplicationBean> getItems() {
        return items;
    }

    public void setItems(List<FriendApplicationBean> items) {
        this.items = items;
    }
}
