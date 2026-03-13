package com.juggle.im.android.server.beans;

import java.util.List;

/**
 * 社区信息Bean
 */
public class CommunityInfoBean {
    private List<String> tags;

    public CommunityInfoBean() {
    }

    public CommunityInfoBean(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
