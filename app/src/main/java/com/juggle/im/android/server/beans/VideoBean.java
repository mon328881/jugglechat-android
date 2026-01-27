package com.juggle.im.android.server.beans;

/**
 * 视频Bean
 * url：播放地址
 * snapshot_url：封面图
 */
public class VideoBean {
    private String url;
    private String snapshot_url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSnapshot_url() {
        return snapshot_url;
    }

    public void setSnapshot_url(String snapshot_url) {
        this.snapshot_url = snapshot_url;
    }
}
