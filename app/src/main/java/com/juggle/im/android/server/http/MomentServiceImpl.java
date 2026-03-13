package com.juggle.im.android.server.http;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.juggle.im.android.server.beans.CommunityTagsResponse;
import com.juggle.im.android.server.beans.ListResult;
import com.juggle.im.android.server.beans.PostBean;
import com.juggle.im.android.server.beans.PostsListData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 朋友圈服务实现类
 * 使用OkHttp执行网络请求
 */
public class MomentServiceImpl extends BaseService implements MomentService {
    private String baseUrl;
    
    public MomentServiceImpl(OkHttpClient client, String baseUrl) {
        super(client, baseUrl);
        this.baseUrl = baseUrl;
    }

    @Override
    public void getPosts(Long start, Integer limit, Integer order, ApiCallback<PostsListData> callback) {
        int l = (limit == null || limit < 1) ? 20 : limit;
        if (l > 50) l = 50;
        StringBuilder sb = new StringBuilder("/jim/posts/list?");
        sb.append("limit=").append(l);
        if (start != null && start > 0) sb.append("&start=").append(start);
        if (order != null) sb.append("&order=").append(order);
        enqueueGet(sb.toString(), PostsListData.class, callback);
    }

    @Override
    public void getPost(String postId, ApiCallback<PostBean> callback) {
        StringBuilder sb = new StringBuilder("/jim/posts/info?post_id=");
        sb.append(postId);
        enqueueGet(sb.toString(), PostBean.class, callback);
    }

    @Override
    public void addComment(String postId, String parentCommentId, String parentUserId, String text, ApiCallback<Void> callback) {
        String url = "/jim/posts/comments/add";
        JsonObject body = new JsonObject();
        body.addProperty("post_id", postId);
        body.addProperty("parent_comment_id", parentCommentId);
        body.addProperty("parent_user_id", parentUserId);
        body.addProperty("text", text);
        enqueueJson(url, body, Void.class, callback);
    }

    @Override
    public void addPost(PostBean content, ApiCallback<Void> callback) {
        String url = "/jim/posts/add";
        enqueueJson(url, content, Void.class, callback);
    }

    @Override
    public void addReaction(String postId, String key, String value, ApiCallback<Void> callback) {
        String url = "/jim/posts/reactions/add";
        JsonObject body = new JsonObject();
        body.addProperty("post_id", postId);
        body.addProperty("key", key);
        body.addProperty("value", value);
        enqueueJson(url, body, Void.class, callback);
    }

    @Override
    public void removeReaction(String postId, String key, ApiCallback<Void> callback) {
        String url = "/jim/posts/reactions/del";
        JsonObject body = new JsonObject();
        body.addProperty("post_id", postId);
        body.addProperty("key", key);
        enqueueJson(url, body, Void.class, callback);
    }

    @Override
    public void deleteComment(List<String> commentIds, ApiCallback<Void> callback) {
        String url = "/jim/posts/comments/del";
        HashMap<String, Object> params = new HashMap<>();
        params.put("comment_ids", commentIds);
        enqueueJson(url, params, Void.class, callback);
    }

    @Override
    public void deletePost(List<String> postIds, ApiCallback<Void> callback) {
        String url = "/jim/posts/del";
        JsonArray ids = new JsonArray();
        for (String id : postIds) {
            ids.add(id);
        }
        JsonObject body = new JsonObject();
        body.add("post_ids", ids);
        enqueueJson(url, body, Void.class, callback);
    }

    @Override
    public void getCommunityTags(ApiCallback<List<String>> callback) {
        String url = "/jim/community/tags";
        Runnable r = () -> {
            try {
                String fullUrl = baseUrl + url;
                Request request = new Request.Builder().url(fullUrl).get().build();
                Response resp = getClient().newCall(request).execute();
                if (!resp.isSuccessful()) {
                    postError(callback, -1, "网络错误: " + resp.code());
                    return;
                }
                String respBody = resp.body() != null ? resp.body().string() : null;
                if (respBody == null) {
                    postError(callback, -1, "响应为空");
                    return;
                }
                
                // 解析响应 JSON
                Gson gson = new Gson();
                JsonObject jo = gson.fromJson(respBody, JsonObject.class);
                
                // 检查响应状态
                if (jo.has("code")) {
                    int code = jo.get("code").getAsInt();
                    if (code != 0) {
                        String msg = jo.has("msg") ? jo.get("msg").getAsString() : "未知错误";
                        postError(callback, code, msg);
                        return;
                    }
                }
                
                // 解析数据字段
                if (jo.has("data")) {
                    JsonArray dataArray = null;
                    if (jo.get("data").isJsonArray()) {
                        dataArray = jo.getAsJsonArray("data");
                    }
                    
                    if (dataArray != null) {
                        List<String> tags = new ArrayList<>();
                        for (int i = 0; i < dataArray.size(); i++) {
                            tags.add(dataArray.get(i).getAsString());
                        }
                        postSuccess(callback, tags);
                    } else {
                        postError(callback, -1, "数据格式错误");
                    }
                } else {
                    postError(callback, -1, "响应中缺少data字段");
                }
            } catch (Exception e) {
                postError(callback, -1, "解析错误: " + e.getMessage());
            }
        };
        new Thread(r, "BaseService-network").start();
    }
}
