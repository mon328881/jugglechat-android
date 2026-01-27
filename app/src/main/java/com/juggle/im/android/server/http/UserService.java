package com.juggle.im.android.server.http;

import com.juggle.im.android.server.beans.*;

import java.util.List;

/**
 * 用户相关API操作接口
 * 实现类应该执行网络请求并在UI线程上调用提供的ApiCallback
 */
public interface UserService {
    void getSmsVerificationCode(CodeRequest phone, ApiCallback<Void> callback);

    void login(LoginRequest phone, ApiCallback<LoginResult> callback);

    void register(RegisterRequest request, ApiCallback<LoginResult> callback);

    void updateUserInfo(UserInfoRequest userInfo, ApiCallback<Void> callback);

    void getUserInfo(String userId, ApiCallback<UserInfoBean> callback);

    void getQRCode(ApiCallback<QRCodeBean> callback);

    /**
     * 获取好友列表，支持分页和排序
     * page从1开始，size默认20，最大50
     */
    void getFriendsList(Integer page, Integer size, String orderTag, ApiCallback<FriendsListData> callback);

    /**
     * 按关键词搜索用户
     */
    void searchUsers(String keyword, ApiCallback<FriendsListData> callback);

    void searchFriends(String keyword, int offset, int limit, ApiCallback<FriendsListData> callback);

    void searchMyGroups(String keyword, int limit, ApiCallback<GroupListData> callback);

    /**
     * 申请添加好友
     */
    void applyFriend(String friendId, ApiCallback<FriendApplicationBean> callback);

    /**
     * 创建群组
     */
    void createGroup(Object body, ApiCallback<CreateGroupResult> callback);

    /**
     * 获取群组信息
     */
    void getGroupInfo(String groupId, ApiCallback<GroupDetailBean> callback);

    /**
     * 邀请用户加入群组
     */
    void inviteJoinGroup(String groupId, List<String> userIds, ApiCallback<Void> callback);

    /**
     * 获取好友申请列表
     */
    void getFriendApplications(int start, int count, ApiCallback<FriendApplicationsData> callback);

    /**
     * 确认好友申请（接受或拒绝）
     */
    void confirmFriend(String sponsorId, boolean isAgree, ApiCallback<Void> callback);

    /**
     * 退出群组
     */
    void quitGroup(String groupId, ApiCallback<Void> callback);
}
