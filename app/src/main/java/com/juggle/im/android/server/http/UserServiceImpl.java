package com.juggle.im.android.server.http;

import com.google.gson.Gson;
import com.juggle.im.android.server.beans.*;
import com.juggle.im.android.utils.SHA1Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;

/**
 * 用户服务实现类
 * 使用OkHttp执行网络请求，在后台线程同步执行，在主线程分发回调
 */
public class UserServiceImpl extends BaseService implements UserService {
    public UserServiceImpl(OkHttpClient client, String baseUrl) {
        super(client, baseUrl);
    }

    @Override
    public void getSmsVerificationCode(CodeRequest request, ApiCallback<Void> callback) {
        enqueueJson("/jim/sms/send", request, Void.class, callback);
    }

    @Override
    public void login(LoginRequest request, ApiCallback<LoginResult> callback) {
        // 对密码进行SHA1加密
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            request.setPassword(SHA1Utils.sha1(request.getPassword()));
        }
        enqueueJson("/jim/login", request, LoginResult.class, callback);
    }

    @Override
    public void register(RegisterRequest request, ApiCallback<LoginResult> callback) {
        // 对密码进行SHA1加密
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            request.setPassword(SHA1Utils.sha1(request.getPassword()));
        }
        enqueueJson("/jim/register", request, LoginResult.class, callback);
    }

    @Override
    public void updateUserInfo(UserInfoRequest userInfo, ApiCallback<Void> callback) {
        enqueueJson("/jim/users/update", userInfo, Void.class, callback);
    }

    @Override
    public void getUserInfo(String userId, ApiCallback<UserInfoBean> callback) {
        enqueueGet("/jim/users/info?user_id=" + userId, UserInfoBean.class, callback);
    }

    @Override
    public void getQRCode(ApiCallback<QRCodeBean> callback) {
        enqueueGet("/jim/users/qrcode", QRCodeBean.class, callback);
    }

    @Override
    public void getFriendsList(Integer page, Integer size, String orderTag, ApiCallback<FriendsListData> callback) {
        int p = (page == null || page < 1) ? 1 : page;
        int s = (size == null || size < 1) ? 20 : size;
        if (s > 50) s = 50;
        StringBuilder sb = new StringBuilder("/jim/friends/list?");
        sb.append("page=").append(p).append("&size=").append(s);
        if (orderTag != null && !orderTag.isEmpty()) {
            sb.append("&order_tag=").append(orderTag);
        }
        enqueueGet(sb.toString(), FriendsListData.class, callback);
    }

    @Override
    public void searchUsers(String keyword, ApiCallback<FriendsListData> callback) {
        Map<String, String> body = new HashMap<>();
        body.put("keyword", keyword == null ? "" : keyword);
        enqueueJson("/jim/users/search", body, FriendsListData.class, callback);
    }

    @Override
    public void searchFriends(String keyword, int offset, int limit, ApiCallback<FriendsListData> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("key", keyword == null ? "" : keyword);
        body.put("limit", limit);
        enqueueJson("/jim/friends/search", body, FriendsListData.class, callback);
    }

    @Override
    public void searchMyGroups(String keyword, int limit, ApiCallback<GroupListData> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("keyword", keyword == null ? "" : keyword);
        body.put("limit", limit);
        enqueueJson("/jim/groups/mygroups/search", body, GroupListData.class, callback);
    }

    @Override
    public void applyFriend(String friendId, ApiCallback<FriendApplicationBean> callback) {
        Map<String, String> body = new HashMap<>();
        body.put("friend_id", friendId == null ? "" : friendId);
        enqueueJson("/jim/friends/apply", body, FriendApplicationBean.class, callback);
    }

    @Override
    public void createGroup(Object body, ApiCallback<CreateGroupResult> callback) {
        enqueueJson("/jim/groups/add", body, CreateGroupResult.class, callback);
    }

    @Override
    public void getGroupInfo(String groupId, ApiCallback<GroupDetailBean> callback) {
        StringBuilder sb = new StringBuilder("/jim/groups/info?group_id=").append(groupId);
        enqueueGet(sb.toString(), GroupDetailBean.class, callback);
    }

    @Override
    public void inviteJoinGroup(String groupId, List<String> userIds, ApiCallback<Void> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("group_id", groupId);
        body.put("member_ids", userIds);
        enqueueJson("/jim/groups/invite", body, Void.class, callback);
    }

    @Override
    public void getFriendApplications(int start, int count, ApiCallback<FriendApplicationsData> callback) {
        StringBuilder sb = new StringBuilder("/jim/friends/applications?");
        sb.append("start=").append(start).append("&count=").append(count);
        enqueueGet(sb.toString(), FriendApplicationsData.class, callback);
    }

    @Override
    public void confirmFriend(String sponsorId, boolean isAgree, ApiCallback<Void> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("sponsor_id", sponsorId);
        body.put("is_agree", isAgree);
        enqueueJson("/jim/friends/confirm", body, Void.class, callback);
    }

    @Override
    public void quitGroup(String groupId, ApiCallback<Void> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("group_id", groupId);
        enqueueJson("/jim/groups/quit", body, Void.class, callback);
    }

    @Override
    public void updateGroupName(String groupId, String groupName, ApiCallback<Void> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("group_id", groupId);
        body.put("group_name", groupName);
        enqueueJson("/jim/groups/update", body, Void.class, callback);
    }

    @Override
    public void dissolveGroup(String groupId, ApiCallback<Void> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("group_id", groupId);
        enqueueJson("/jim/groups/dissolve", body, Void.class, callback);
    }

    @Override
    public void setGroupSettings(String groupId, Map<String, Object> settings, ApiCallback<Void> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("group_id", groupId);
        
        // 将settings中的键值对直接添加到body中
        if (settings != null) {
            body.putAll(settings);
        }
        
        enqueueJson("/jim/groups/management/set", body, Void.class, callback);
    }

    @Override
    public void setGroupHistoryMessageVisible(String groupId, boolean visible, ApiCallback<Void> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("group_id", groupId);
        body.put("visible", visible ? 1 : 0);
        enqueueJson("/jim/groups/management/sethismsgvisible", body, Void.class, callback);
    }

    @Override
    public void transferGroupOwner(String groupId, String newOwnerId, ApiCallback<Void> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("group_id", groupId);
        body.put("owner_id", newOwnerId);
        enqueueJson("/jim/groups/management/chgowner", body, Void.class, callback);
    }

    @Override
    public void addGroupAdministrators(String groupId, List<String> adminIds, ApiCallback<Void> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("group_id", groupId);
        body.put("admin_ids", adminIds);
        enqueueJson("/jim/groups/management/administrators/add", body, Void.class, callback);
    }

    @Override
    public void delGroupAdministrators(String groupId, List<String> adminIds, ApiCallback<Void> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("group_id", groupId);
        body.put("admin_ids", adminIds);
        enqueueJson("/jim/groups/management/administrators/del", body, Void.class, callback);
    }

    @Override
    public void queryGroupAdministrators(String groupId, ApiCallback<List<GroupMemberBean>> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("group_id", groupId);
        enqueueJson("/jim/groups/management/administrators/list", body, AdminsListWrapper.class, new ApiCallback<AdminsListWrapper>() {
            @Override
            public void onSuccess(AdminsListWrapper data) {
                if (data != null && data.getAdmins() != null) {
                    callback.onSuccess(data.getAdmins());
                } else {
                    callback.onSuccess(new ArrayList<>());
                }
            }

            @Override
            public void onError(int code, String message) {
                callback.onError(code, message);
            }
        });
    }
    
    /**
     * 管理员列表包装类
     */
    private static class AdminsListWrapper {
        private List<GroupMemberBean> admins;
        
        public List<GroupMemberBean> getAdmins() {
            return admins;
        }
        
        public void setAdmins(List<GroupMemberBean> admins) {
            this.admins = admins;
        }
    }
}
