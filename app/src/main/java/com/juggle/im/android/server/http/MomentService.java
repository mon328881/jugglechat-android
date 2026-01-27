package com.juggle.im.android.server.http;

import com.juggle.im.android.server.beans.PostBean;
import com.juggle.im.android.server.beans.PostsListData;

import java.util.List;

/**
 * 朋友圈相关API接口
 */
public interface MomentService {
    /**
     * 获取朋友圈列表
     *
     * @param start 可选的起始时间戳（毫秒，13位数字）
     * @param limit 可选的分页大小（默认20，最大50）
     * @param order 可选的排序方式：0表示降序，1表示升序
     */
    void getPosts(Long start, Integer limit, Integer order, ApiCallback<PostsListData> callback);

    /**
     * 获取单个朋友圈
     */
    void getPost(String postId, ApiCallback<PostBean> callback);

    /**
     * 添加评论
     *
     * @param postId          朋友圈ID
     * @param parentCommentId 父评论ID
     * @param parentUserId    父用户ID
     * @param text            评论内容
     */
    void addComment(String postId, String parentCommentId, String parentUserId, String text, ApiCallback<Void> callback);

    /**
     * 发布朋友圈
     */
    void addPost(PostBean content, ApiCallback<Void> callback);

    /**
     * 添加反应（点赞等）
     *
     * @param postId 朋友圈ID
     * @param key    反应键
     * @param value  反应值
     */
    void addReaction(String postId, String key, String value, ApiCallback<Void> callback);

    /**
     * 删除反应（取消点赞等）
     *
     * @param postId 朋友圈ID
     * @param key    反应键
     */
    void removeReaction(String postId, String key, ApiCallback<Void> callback);

    /**
     * 删除朋友圈
     */
    void deletePost(List<String> postIds, ApiCallback<Void> callback);

    /**
     * 删除评论
     */
    void deleteComment(List<String> commentIds, ApiCallback<Void> callback);
}
