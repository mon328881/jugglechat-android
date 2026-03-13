package com.juggle.im.android.model;

import java.io.Serializable;

/**
 * 收藏项：文本、图片、文件
 */
public class FavoriteItem implements Serializable {
    public static final String TYPE_TEXT = "text";
    public static final String TYPE_IMAGE = "image";
    public static final String TYPE_FILE = "file";

    private String id;
    private String type;
    private String content;      // 文本内容，或预览摘要
    private String localPath;    // 图片/文件的本地路径
    private String url;          // 服务器上的 URL（优先使用）
    private String thumbnailUrl; // 缩略图 URL（仅图片）
    private String name;         // 文件名称（仅文件类型）
    private long size;           // 文件大小（仅文件类型）
    private long timestamp;

    public FavoriteItem() {
    }

    public FavoriteItem(String id, String type, String content, String localPath, String name, long size, long timestamp) {
        this.id = id;
        this.type = type;
        this.content = content;
        this.localPath = localPath;
        this.name = name;
        this.size = size;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getLocalPath() { return localPath; }
    public void setLocalPath(String localPath) { this.localPath = localPath; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
