package com.juggle.im.android.chat;

import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.juggle.im.JIM;
import com.juggle.im.android.R;
import com.juggle.im.android.chat.component.FriendsListAdapter;
import com.juggle.im.android.event.FriendApplicationRefreshRequestEvent;
import com.juggle.im.android.event.FriendApplicationUpdateEvent;
import com.juggle.im.android.chat.component.UserListAdapter;
import com.juggle.im.android.server.http.ApiCallback;
import com.juggle.im.android.server.http.ServiceManager;
import com.juggle.im.android.server.beans.FriendsListData;
import com.juggle.im.android.server.beans.FriendBean;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.ConversationInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment {
    /**
     * 好友申请获取方式
     *
     * 查询一下来两个会话是否有未读消息，如果有显示有好友请求
     * 进入好友请求列表，清理未读数
     */
    private static String FRIEND_APPLY = "friend_apply";

    /**
     * 这个是朋友圈的点赞、评论提醒
     */
    private static String MOMENT_NTF = "post_ntf";

    private RecyclerView recyclerView;
    private FriendsListAdapter adapter;
    private UserListAdapter selectAdapter; // 用于选择模式
    private SelectionListener selectionListener;
    private String selectionMode = UserListAdapter.LIST_MODE_NORMAL;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friends_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.rv_friends_list);
        
        // 根据模式选择不同的Adapter
        if (selectionMode.equals(UserListAdapter.LIST_MODE_SELECT_MEMBER)) {
            // 选择模式，使用UserListAdapter
            selectAdapter = new UserListAdapter();
            selectAdapter.setMode(selectionMode);
            selectAdapter.setSelectionChangedListener((item, selected) -> {
                if (selectionListener != null)
                    selectionListener.onMemberSelected(item, selected);
            });
            recyclerView.setAdapter(selectAdapter);
        } else {
            // 普通模式，使用FriendsListAdapter
            adapter = new FriendsListAdapter();
            recyclerView.setAdapter(adapter);
        }
        
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Set up click listeners for header items
        View groupsItem = view.findViewById(R.id.groups_item);
        View newFriendsItem = view.findViewById(R.id.new_friends_item);

        // 群组选项
        groupsItem.setOnClickListener(v -> {
            // 跳转到群组列表页面
            android.content.Intent intent = new android.content.Intent(requireContext(),
                    com.juggle.im.android.chat.GroupListActivity.class);
            startActivity(intent);
        });

        // 新朋友选项
        newFriendsItem.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(requireContext(),
                    com.juggle.im.android.app.FriendApplicationsActivity.class);
            startActivity(intent);
        });

        loadFriends();
        checkNewFriend(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        View v = getView();
        if (v != null) checkNewFriend(v);
        // 从新朋友页同意/拒绝返回后刷新联系人列表，使新通过的好友立即显示
        loadFriends();
        // 切换到通讯录/新朋友时请求刷新红点（HTTP 兜底，不依赖 IM 推送）
        EventBus.getDefault().post(new FriendApplicationRefreshRequestEvent());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFriendApplicationUpdate(FriendApplicationUpdateEvent event) {
        View v = getView();
        if (v != null) {
            updateNewFriendTipVisibility(v, event.getPendingCount());
        }
    }

    private void updateNewFriendTipVisibility(View root, int pendingCount) {
        View tip = root.findViewById(R.id.new_friend_tip);
        if (tip != null) {
            tip.setVisibility(pendingCount > 0 ? View.VISIBLE : View.GONE);
        }
    }

    public void setSelectionMode(String mode) {
        this.selectionMode = mode;
    }

    public void setSelectionListener(SelectionListener l) {
        this.selectionListener = l;
    }

    public void uncheckUser(String userId) {
        if (selectAdapter != null)
            selectAdapter.uncheckUser(userId);
    }

    public interface SelectionListener {
        void onMemberSelected(UserListAdapter.UserInfoObj member, boolean selected);
    }

    private void loadFriends() {
        ServiceManager.getUserService().getFriendsList(1, 50, null, new ApiCallback<FriendsListData>() {
            @Override
            public void onSuccess(FriendsListData data) {
                List<FriendBean> items = data != null ? data.getItems() : null;
                
                if (selectionMode.equals(UserListAdapter.LIST_MODE_SELECT_MEMBER)) {
                    // 选择模式，使用UserListAdapter
                    List<UserListAdapter.UserInfoObj> memberList = new ArrayList<>();
                    for (FriendBean member : items) {
                        UserListAdapter.UserInfoObj userInfoObj = new UserListAdapter.UserInfoObj(false);
                        userInfoObj.setUserId(member.getUser_id());
                        userInfoObj.setName(member.getNickname());
                        userInfoObj.setAvatar(member.getAvatar());
                        memberList.add(userInfoObj);
                    }
                    if (selectAdapter != null) {
                        selectAdapter.setItems(memberList);
                    }
                } else {
                    // 普通模式，使用FriendsListAdapter
                    List<FriendsListAdapter.FriendItem> friendList = new ArrayList<>();
                    for (FriendBean member : items) {
                        FriendsListAdapter.FriendItem friendItem = new FriendsListAdapter.FriendItem();
                        friendItem.userId = member.getUser_id();
                        friendItem.name = member.getNickname();
                        friendItem.avatar = member.getAvatar();
                        friendList.add(friendItem);
                    }
                    if (adapter != null) {
                        adapter.setFriends(friendList);
                    }
                }

                // 同步刷新消息列表中对应私聊会话的头像和昵称
                if (getActivity() instanceof com.juggle.im.android.app.MainActivity && items != null) {
                    com.juggle.im.android.app.MainActivity act = (com.juggle.im.android.app.MainActivity) getActivity();
                    for (FriendBean member : items) {
                        if (member == null) continue;
                        act.updateConversationUserDisplay(
                                member.getUser_id(),
                                member.getNickname(),
                                member.getAvatar());
                    }
                }
            }

            @Override
            public void onError(int code, String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(
                            () -> Toast.makeText(getActivity(), "加载好友失败: " + message, Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void checkNewFriend(View view) {
        Conversation conversation = new Conversation(Conversation.ConversationType.SYSTEM, FRIEND_APPLY);
        ConversationInfo info = JIM.getInstance().getConversationManager().getConversationInfo(conversation);
        if (info != null && info.getUnreadCount() > 0) {
            view.findViewById(R.id.new_friend_tip).setVisibility(VISIBLE);
        } else {
            view.findViewById(R.id.new_friend_tip).setVisibility(View.GONE);
        }
    }
}
