package com.juggle.im.android.server.beans;

import java.util.List;

/**
 * 群组列表数据包装类
 */
public class GroupListData {
    private List<GroupBean> items;

    public List<GroupBean> getItems() {
        return items;
    }

    public void setItems(List<GroupBean> items) {
        this.items = items;
    }
}
