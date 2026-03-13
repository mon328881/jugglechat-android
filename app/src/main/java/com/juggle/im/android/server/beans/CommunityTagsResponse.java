package com.juggle.im.android.server.beans;

import java.util.List;

/**
 * 社区标签响应
 */
public class CommunityTagsResponse {
    private List<String> data;

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }
}
