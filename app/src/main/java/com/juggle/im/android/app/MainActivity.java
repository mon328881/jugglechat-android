package com.juggle.im.android.app;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import com.juggle.im.android.utils.LogUtil;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.juggle.im.JIM;
import com.juggle.im.JIMConst;
import com.juggle.im.android.R;
import com.juggle.im.android.chat.ConversationListFragment;
import com.juggle.im.android.chat.FriendsFragment;
import com.juggle.im.android.chat.DiscoverFragment;
import com.juggle.im.android.chat.MyProfileFragment;
import com.juggle.im.android.chat.call.MultiCallActivity;
import com.juggle.im.android.chat.call.SingleCallActivity;
import com.juggle.im.android.core.JIMChatCore;
import com.juggle.im.android.event.ConnectStatusEvent;
import com.juggle.im.android.event.ConversationUpdatedEvent;
import com.juggle.im.android.event.FriendApplicationRefreshRequestEvent;
import com.juggle.im.android.event.FriendApplicationUpdateEvent;
import com.juggle.im.android.event.MessageReadUpdatedEvent;
import com.juggle.im.android.event.UnreadMessageCountEvent;
import com.juggle.im.android.model.ConfigUtils;
import com.juggle.im.android.model.UiConversation;
import com.juggle.im.android.server.beans.FriendApplicationBean;
import com.juggle.im.android.server.beans.FriendApplicationsData;
import com.juggle.im.android.server.beans.UserInfoBean;
import com.juggle.im.android.server.http.ApiCallback;
import com.juggle.im.android.server.http.ServiceManager;
import com.juggle.im.android.utils.HiddenConversationStore;
import com.juggle.im.android.utils.NetworkStateManager;
import com.juggle.im.android.utils.SecurePrefsHelper;
import com.juggle.im.call.CallConst;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.ConversationInfo;
import com.juggle.im.model.GroupInfo;
import com.juggle.im.model.UserInfo;
import com.qiniu.android.utils.StringUtils;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    // 自动重连配置
    private static final int MAX_RETRY_COUNT = 3;
    private static final int RETRY_DELAY_MS = 5000;
    /** 先断开再重连时，等待 SDK 状态机切到 Idle 的延时，避免 "connection already exist" */
    private static final int RECONNECT_AFTER_DISCONNECT_MS = 400;

    private ConversationListFragment conversationListFragment;
    private FriendsFragment friendsFragment; // kept for places that still use it
    private DiscoverFragment discoverFragment;
    private MyProfileFragment myProfileFragment;
    private BottomNavView bottomNav;
    private ImageView btnMore, btnSearch;
    
    // 重连状态管理
    private int currentRetryCount = 0;
    private boolean isReconnecting = false;
    private final Handler reconnectHandler = new Handler(Looper.getMainLooper());
    
    // 网络状态管理
    private NetworkStateManager networkStateManager;

    private final Runnable reconnectRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isReconnecting) {
                return;
            }
            // 仅在重试次数用尽时才显示失败提示，其余重连过程不展示文案
            if (currentRetryCount >= MAX_RETRY_COUNT) {
                isReconnecting = false;
                View v = findViewById(R.id.connect_status);
                TextView vStatus = findViewById(R.id.connect_text_view);
                View btnReconnect = findViewById(R.id.btn_reconnect);
                if (v != null) {
                    v.setVisibility(VISIBLE);
                }
                if (vStatus != null) {
                    vStatus.setText("连接失败，请检查网络");
                }
                if (btnReconnect != null) {
                    btnReconnect.setVisibility(VISIBLE);
                }
                return;
            }

            currentRetryCount++;

            if (ConfigUtils.imToken != null && !ConfigUtils.imToken.isEmpty()) {
                LogUtil.i("MainActivity", "try reconnect, attempt " + currentRetryCount);
                // 先断开再连：SDK 在 ConnConnectedState 时若收到同 token 的 connect 会直接忽略（connection already exist），
                // 导致实际已断网但状态机仍认为已连接，重连无效。先 disconnect 让状态机回到 Idle，再延迟 connect。
                JIM.getInstance().getConnectionManager().disconnect(false);
                reconnectHandler.postDelayed(() -> {
                    if (ConfigUtils.imToken != null && !ConfigUtils.imToken.isEmpty()) {
                        JIMChatCore.getInstance().connect(ConfigUtils.imToken);
                    }
                }, RECONNECT_AFTER_DISCONNECT_MS);
            }

            reconnectHandler.postDelayed(this, RETRY_DELAY_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentRetryCount = 0;
        JIMChatCore.getInstance().connect(ConfigUtils.imToken);

        setContentView(R.layout.activity_main);
        com.juggle.im.android.utils.HiddenConversationStore.loadCacheAsync(this);

        Window window = getWindow();
        window.setNavigationBarColor(getColor(R.color.white));

        // add conversation fragment as default
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tx = fm.beginTransaction();
        conversationListFragment = new ConversationListFragment();
        tx.add(R.id.content_container, conversationListFragment, "conversations");
        tx.commitAllowingStateLoss();

        bottomNav = findViewById(R.id.footer_nav);
        
        // 注册网络状态监听
        networkStateManager = new NetworkStateManager(this, new NetworkStateManager.Callback() {
            @Override
            public void onNetworkAvailable() {
                LogUtil.i("MainActivity", "网络已连接，尝试重连");
                if (ConfigUtils.imToken != null && !ConfigUtils.imToken.isEmpty()) {
                    currentRetryCount = 0;
                    isReconnecting = true;
                    reconnectHandler.removeCallbacks(reconnectRunnable);
                    reconnectHandler.post(reconnectRunnable);
                }
            }
            
            @Override
            public void onNetworkLost() {
                LogUtil.i("MainActivity", "网络已断开");
                isReconnecting = false;
                reconnectHandler.removeCallbacks(reconnectRunnable);
            }
        });
        networkStateManager.register();
        
        // 设置手动重连按钮
        View btnReconnect = findViewById(R.id.btn_reconnect);
        if (btnReconnect != null) {
            btnReconnect.setOnClickListener(v -> {
                currentRetryCount = 0;
                isReconnecting = true;
                reconnectHandler.removeCallbacks(reconnectRunnable);
                reconnectHandler.post(reconnectRunnable);
            });
        }
        
        // 设置用户信息
        setupUserInfo();
        
        if (bottomNav != null) {
            bottomNav.setOnTabClickListener(index -> onTabSelected(index));
            bottomNav.setSelectedTab(0);
        }

        // add button: show popup menu (Add friend, Create group)
        btnMore = findViewById(R.id.btn_more);
        if (btnMore != null) {
            btnMore.setOnClickListener(v -> {
                // 获取当前选中的标签页
                int currentTab = bottomNav != null ? bottomNav.getSelectedTab() : 0;
                
                if (currentTab == 1) {
                    // 联系人页面，直接跳转到添加好友页面
                    startActivity(new android.content.Intent(MainActivity.this, AddFriendActivity.class));
                } else {
                    // 聊天页面，显示弹出菜单
                    android.widget.PopupMenu popup = new android.widget.PopupMenu(MainActivity.this, v);
                    popup.getMenuInflater().inflate(R.menu.menu_add, popup.getMenu());
                    popup.setOnMenuItemClickListener(item -> {
                        int id = item.getItemId();
                        if (id == R.id.menu_add_friend) {
                            startActivity(new android.content.Intent(MainActivity.this, AddFriendActivity.class));
                            return true;
                        } else if (id == R.id.menu_create_group) {
                            startActivity(new android.content.Intent(MainActivity.this, CreateGroupActivity.class));
                            return true;
                        } else if (id == R.id.menu_scan) {
                            startActivity(new android.content.Intent(MainActivity.this, com.juggle.im.android.chat.ScanActivity.class));
                            return true;
                        }
                        return false;
                    });
                    popup.show();
                }
            });
        }
        btnSearch = findViewById(R.id.btn_search);
        btnSearch.setOnClickListener( v -> {
            startActivity(new Intent(MainActivity.this, AddFriendActivity.class));
        });

        EventBus.getDefault().register(this);

        // 延迟拉取好友申请数量并刷新红点（服务端可能未通过 IM friend_apply 会话推送，用 HTTP 兜底）
        reconnectHandler.postDelayed(this::refreshFriendApplicationBadgeFromServer, 800);

        JIM.getInstance().getCallManager().addReceiveListener("CallReceive", iCallSession -> {
            LogUtil.d("MainActivity", "receive call: " + iCallSession.getCallId());
            int members = iCallSession.getMembers().size();
            Intent it = members == 2
                    ? new Intent(this, SingleCallActivity.class)
                    : new Intent(this, MultiCallActivity.class);
            it.putExtra("inviter", iCallSession.getInviter());
            it.putExtra("is_video_call", iCallSession.getMediaType() == CallConst.CallMediaType.VIDEO);
            List<String> ids = iCallSession.getMembers().stream()
                    .map(member -> member.getUserInfo().getUserId())
                    .collect(Collectors.toList());
            it.putStringArrayListExtra("targetUserIds", (ArrayList<String>)ids);
            it.putExtra("direction", "incoming");
            it.putExtra("callId", iCallSession.getCallId());
            String extra = iCallSession.getExtra();
            if (!StringUtils.isBlank(extra)) {
                try {
                    JSONObject jsonObject = new JSONObject(extra);
                    String conversationId = jsonObject.getString("conversationId");
                    it.putExtra("conversationId", conversationId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            startActivity(it);
        });
    }


    private void onTabSelected(int index) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tx = fm.beginTransaction();
        LinearLayout userInfoContainer = findViewById(R.id.user_info_container);
        TextView pageTitle = findViewById(R.id.page_title);
        
        switch (index) {
            case 0:
                // 消息列表页面 - 显示用户信息，隐藏页面标题
                if (conversationListFragment == null) {
                    conversationListFragment = new ConversationListFragment();
                    tx.add(R.id.content_container, conversationListFragment, "conversations");
                }
                if (friendsFragment != null) tx.hide(friendsFragment);
                if (discoverFragment != null) tx.hide(discoverFragment);
                if (myProfileFragment != null) tx.hide(myProfileFragment);
                tx.show(conversationListFragment);
                btnMore.setVisibility(VISIBLE);
                btnSearch.setVisibility(VISIBLE);
                if (userInfoContainer != null) userInfoContainer.setVisibility(VISIBLE);
                if (pageTitle != null) pageTitle.setVisibility(GONE);

                break;
            case 2:
                // 发现页面 - 隐藏用户信息，显示页面标题
                if (discoverFragment == null) {
                    discoverFragment = new DiscoverFragment();
                    tx.add(R.id.content_container, discoverFragment, "discover");
                }
                if (conversationListFragment != null) tx.hide(conversationListFragment);
                if (friendsFragment != null) tx.hide(friendsFragment);
                if (myProfileFragment != null) tx.hide(myProfileFragment);
                tx.show(discoverFragment);
                btnMore.setVisibility(GONE);
                btnSearch.setVisibility(GONE);
                if (userInfoContainer != null) userInfoContainer.setVisibility(GONE);
                if (pageTitle != null) {
                    pageTitle.setText("发现");
                    pageTitle.setVisibility(VISIBLE);
                }

                break;
            case 1:
                // 联系人页面 - 隐藏用户信息，显示页面标题
                if (friendsFragment == null) {
                    friendsFragment = new FriendsFragment();
                    tx.add(R.id.content_container, friendsFragment, "friends");
                }
                if (conversationListFragment != null) tx.hide(conversationListFragment);
                if (discoverFragment != null) tx.hide(discoverFragment);
                if (myProfileFragment != null) tx.hide(myProfileFragment);
                tx.show(friendsFragment);
                btnMore.setVisibility(VISIBLE);
                btnSearch.setVisibility(GONE);
                if (userInfoContainer != null) userInfoContainer.setVisibility(GONE);
                if (pageTitle != null) {
                    pageTitle.setText("联系人");
                    pageTitle.setVisibility(VISIBLE);
                }
                break;
            case 3:
                // 个人资料页面 - 隐藏用户信息，显示页面标题
                if (myProfileFragment == null) {
                    myProfileFragment = new MyProfileFragment();
                    tx.add(R.id.content_container, myProfileFragment, "profile");
                }
                if (conversationListFragment != null) tx.hide(conversationListFragment);
                if (friendsFragment != null) tx.hide(friendsFragment);
                if (discoverFragment != null) tx.hide(discoverFragment);
                tx.show(myProfileFragment);
                btnMore.setVisibility(GONE);
                btnSearch.setVisibility(GONE);
                if (userInfoContainer != null) userInfoContainer.setVisibility(GONE);
                if (pageTitle != null) {
                    pageTitle.setText("我的");
                    pageTitle.setVisibility(VISIBLE);
                }

                break;
            default:
                // other tabs not implemented yet
                break;
        }
        tx.commitAllowingStateLoss();
        BottomNavView bottomNav = findViewById(R.id.footer_nav);
        if (bottomNav != null) bottomNav.setSelectedTab(index);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectStatusChanged(ConnectStatusEvent event) {
        LogUtil.i("MainActivity", event.getConnectionStatus().toString() + "," + event.getCode());
        View v = findViewById(R.id.connect_status);
        View btnReconnect = findViewById(R.id.btn_reconnect);
        TextView vStatus = findViewById(R.id.connect_text_view);
        JIMConst.ConnectionStatus status = event.getConnectionStatus();

        int code = event.getCode();

        // === 1. 明确的账号/Token问题：不再自动重连，清除登录状态并跳转到登录页 ===
        // 11004 TOKEN_ILLEGAL
        // 11005 TOKEN_AUTHFAIL
        // 11006 TOKEN_EXPIRED
        // 11012 LOGOUT
        if (code == 11004 || code == 11005 || code == 11006 || code == 11012) {
            isReconnecting = false;
            currentRetryCount = 0;
            reconnectHandler.removeCallbacks(reconnectRunnable);
            
            // 清除登录状态
            clearLoginState();
            
            // 显示提示信息
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "登录已失效，请重新登录", Toast.LENGTH_LONG).show();
                
                // 跳转到登录页面
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
            return;
        }

        // === 2. 用户被封禁：清除登录状态并跳转到登录页 ===
        if (code == 11010) {
            isReconnecting = false;
            currentRetryCount = 0;
            reconnectHandler.removeCallbacks(reconnectRunnable);
            
            // 清除登录状态
            clearLoginState();
            
            // 显示提示信息
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "账户已被封禁，请联系管理员", Toast.LENGTH_LONG).show();
                
                // 跳转到登录页面
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
            return;
        }

        // === 3. 被踢下线：清除登录状态并跳转到登录页 ===
        if (code == 11011) {
            isReconnecting = false;
            currentRetryCount = 0;
            reconnectHandler.removeCallbacks(reconnectRunnable);
            
            // 清除登录状态
            clearLoginState();
            
            // 显示提示信息
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "账户在其他设备登录", Toast.LENGTH_LONG).show();
                
                // 跳转到登录页面
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
            return;
        }

        // === 4. 已连线成功：隐藏提示并重置状态 ===
        if (status == JIMConst.ConnectionStatus.CONNECTED) {
            if (v != null) {
                v.setVisibility(GONE);
            }
            currentRetryCount = 0;
            isReconnecting = false;
            reconnectHandler.removeCallbacks(reconnectRunnable);
            if (btnReconnect != null) {
                btnReconnect.setVisibility(GONE);
            }
            // 更新用户状态指示器为在线（绿点）
            View statusIndicator = findViewById(R.id.status_indicator);
            TextView tvStatus = findViewById(R.id.tv_status);
            updateConnectionStatus(statusIndicator, tvStatus, status);
            return;
        }

        // === 5. 仅在明确失败/断开时尝试自动重连 ===
        if (status == JIMConst.ConnectionStatus.FAILURE
                || status == JIMConst.ConnectionStatus.DISCONNECTED) {
            // 还没开始重连且未达最大次数：启动自动重连循环（重连过程不显示失败文案）
            if (!isReconnecting && currentRetryCount < MAX_RETRY_COUNT) {
                isReconnecting = true;
                reconnectHandler.removeCallbacks(reconnectRunnable);
                reconnectHandler.post(reconnectRunnable);
            } else if (!isReconnecting && currentRetryCount >= MAX_RETRY_COUNT) {
                // 自动重连已经结束：保持失败提示和手动按钮
                if (v != null) {
                    v.setVisibility(VISIBLE);
                }
                if (vStatus != null) {
                    vStatus.setText("连接失败，请检查网络");
                }
                if (btnReconnect != null) {
                    btnReconnect.setVisibility(VISIBLE);
                }
            }
        }
        // 其它状态（如 CONNECTING 等）不重置重试计数、不停止自动重连，
        // 也不强制改变文案，避免顶部提示频繁闪烁。
        
        // 更新用户状态指示器
        View statusIndicator = findViewById(R.id.status_indicator);
        TextView tvStatus = findViewById(R.id.tv_status);
        updateConnectionStatus(statusIndicator, tvStatus, status);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void MessageReadUpdatedEvent(MessageReadUpdatedEvent event) {

    }

    /**
     * 判断是否为系统/广播类会话的 conversationId（如添加好友、朋友圈通知等）。
     * 这类会话在消息列表中隐藏，仅在通讯录「新朋友」等入口展示。
     */
    private static boolean isSystemOrBroadcastConversationId(String convId) {
        if (convId == null || convId.isEmpty()) return false;
        // 系统通知虚拟会话需要常驻主消息列表，不能被当作隐藏会话过滤
        if (com.juggle.im.android.core.JIMChatCore.isSysNoticeConversationId(convId)) return false;
        if (convId.contains(":")) return true;
        String lower = convId.toLowerCase();
        return lower.startsWith("friend_apply") || lower.startsWith("post_ntf")
                || lower.startsWith("broadcast") || lower.startsWith("system");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConversationUpdated(ConversationUpdatedEvent event) {
        LogUtil.i("MainActivity", "onConversationUpdated, batch size=" + (event.getConversationInfoList() != null ? event.getConversationInfoList().size() : 0));
        List<ConversationInfo> infoList = event.getConversationInfoList();
        if (infoList == null || infoList.isEmpty()) return;
        final android.content.Context appContext = getApplicationContext();
        // isHidden / getGroupInfo / getUserInfo 会触发放盘或 DB，放到子线程，避免 StrictMode
        new Thread(() -> {
            Map<String, UiConversation> byId = new LinkedHashMap<>();
            Set<String> privateIdsWithoutUserInfo = new HashSet<>();
            for (ConversationInfo info : infoList) {
                Conversation conversation = info.getConversation();
                if (conversation == null) continue;
                String rawConvId = conversation.getConversationId();
                Conversation.ConversationType type = conversation.getConversationType();
                // 将所有 SYSTEM 类型的会话统一映射到 sys_notice 虚拟会话，用于在主消息列表聚合展示系统通知
                String convId = rawConvId;
                if (type == Conversation.ConversationType.SYSTEM) {
                    convId = com.juggle.im.android.core.JIMChatCore.SYS_NOTICE_CONV_ID;
                }
                LogUtil.d("MainActivity", "[conv] id=" + convId
                        + ", type=" + type
                        + ", unread=" + info.getUnreadCount()
                        + ", isTop=" + info.isTop()
                        + ", sortTime=" + info.getSortTime()
                        + ", isSysNotice=" + com.juggle.im.android.core.JIMChatCore.isSysNoticeConversationId(convId));
                // 系统通知虚拟会话常驻，不受本地隐藏列表影响
                if (!com.juggle.im.android.core.JIMChatCore.isSysNoticeConversationId(convId)
                        && HiddenConversationStore.isHidden(appContext, convId)) {
                    LogUtil.d("MainActivity", "[conv] filtered by HiddenConversationStore id=" + convId);
                    continue;
                }
                // SYSTEM 类型会话默认不在主消息列表展示，但“系统通知”虚拟会话（sys_notice）是特例，需常驻主列表
                if (type == Conversation.ConversationType.SYSTEM
                        && !com.juggle.im.android.core.JIMChatCore.isSysNoticeConversationId(convId)) {
                    LogUtil.d("MainActivity", "[conv] filtered: SYSTEM type id=" + convId);
                    continue;
                }
                if (type == Conversation.ConversationType.PRIVATE && convId != null && isSystemOrBroadcastConversationId(convId)) {
                    LogUtil.d("MainActivity", "[conv] filtered: private system/broadcast virtual id=" + convId);
                    continue;
                }
                UiConversation ui = UiConversation.fromConversationInfo(info);
                // 若为 SYSTEM 类型会话，则在 UI 层统一归并到 sys_notice 虚拟会话 ID
                if (type == Conversation.ConversationType.SYSTEM) {
                    ui.setId(com.juggle.im.android.core.JIMChatCore.SYS_NOTICE_CONV_ID);
                }
                // 系统通知虚拟会话：强制置顶、固定展示名称，且不可取消置顶
                if (com.juggle.im.android.core.JIMChatCore.isSysNoticeConversationId(convId)) {
                    try {
                        if (!info.isTop()) {
                            JIM.getInstance().getConversationManager().setTop(conversation, true, null);
                            info.setTop(true);
                        }
                    } catch (Exception ignored) {
                        // ignore: UI 仍按当前 info 状态展示
                    }
                    ui.setName("系统通知");
                    ui.setLastMessageUserName("系统");
                    // 使用本地资源图标作为头像（避免依赖网络/用户资料）
                    ui.setAvatar("res://notice");
                    LogUtil.i("MainActivity", "[conv] sys_notice applied: id=" + convId
                            + ", sortTime=" + ui.getSortTime());
                }
                if (type == Conversation.ConversationType.GROUP) {
                    GroupInfo groupInfo = JIM.getInstance().getUserInfoManager().getGroupInfo(ui.getConversationInfo().getConversation().getConversationId());
                    if (groupInfo != null) {
                        ui.setName(groupInfo.getGroupName());
                        ui.setAvatar(groupInfo.getPortrait());
                    }
                    UserInfo userInfo = ui.getLastMessage() != null
                            ? JIM.getInstance().getUserInfoManager().getUserInfo(ui.getLastMessage().getSenderUserId())
                            : null;
                    if (userInfo != null) ui.setLastMessageUserName(userInfo.getUserName());
                } else if (type == Conversation.ConversationType.PRIVATE) {
                    UserInfo userInfo = JIM.getInstance().getUserInfoManager().getUserInfo(ui.getConversationInfo().getConversation().getConversationId());
                    if (userInfo != null) {
                        ui.setName(userInfo.getUserName());
                        ui.setAvatar(userInfo.getPortrait());
                        ui.setLastMessageUserName(userInfo.getUserName());
                    } else {
                        privateIdsWithoutUserInfo.add(convId);
                    }
                }
                UiConversation existing = byId.get(convId);
                if (existing == null || ui.getSortTime() > existing.getSortTime()) byId.put(convId, ui);
            }
            List<UiConversation> uiList = new ArrayList<>(byId.values());
            uiList.sort((a, b) -> {
                if (a.isTop() != b.isTop()) return a.isTop() ? -1 : 1;
                return Long.compare(b.getSortTime(), a.getSortTime());
            });
            final List<UiConversation> finalList = uiList;
            // 打印排序后的会话列表概况
            StringBuilder sb = new StringBuilder();
            sb.append("[conv] final list size=").append(finalList.size()).append('\n');
            for (int i = 0; i < finalList.size(); i++) {
                UiConversation ui = finalList.get(i);
                String id = ui.getId();
                sb.append("  #").append(i)
                        .append(" id=").append(id)
                        .append(", name=").append(ui.getName())
                        .append(", isTop=").append(ui.isTop())
                        .append(", sortTime=").append(ui.getSortTime())
                        .append(", unread=").append(ui.getUnreadCount())
                        .append(", isSysNotice=").append(com.juggle.im.android.core.JIMChatCore.isSysNoticeConversationId(id))
                        .append('\n');
            }
            LogUtil.i("MainActivity", sb.toString());
            final Set<String> finalIdsWithoutUserInfo = new HashSet<>(privateIdsWithoutUserInfo);
            runOnUiThread(() -> {
                if (isFinishing()) return;
                ConversationListFragment frag = (ConversationListFragment) getSupportFragmentManager().findFragmentByTag("conversations");
                if (frag != null) {
                    frag.upsertConversations(finalList);
                    for (String userId : finalIdsWithoutUserInfo) fetchAndUpdateConversationDisplay(frag, userId);
                }
            });
        }).start();
    }

    /**
     * 私聊会话在本地无用户资料时，从业务服务器拉取昵称/头像并更新该条会话的展示。
     */
    private void fetchAndUpdateConversationDisplay(ConversationListFragment frag, String userId) {
        if (frag == null || userId == null) return;
        ServiceManager.getUserService().getUserInfo(userId, new ApiCallback<UserInfoBean>() {
            @Override
            public void onSuccess(UserInfoBean data) {
                if (data == null) return;
                runOnUiThread(() -> {
                    String name = data.getNickname();
                    String avatar = data.getAvatar();
                    if (name != null || avatar != null) {
                        frag.updateConversationDisplayInfo(userId,
                                name != null ? name : userId,
                                avatar);
                    }
                });
            }

            @Override
            public void onError(int code, String message) {
                // 忽略，列表继续显示 ID
            }
        });
    }

    /**
     * 由通讯录/其他页面在拿到最新好友资料（昵称、头像）时调用，
     * 主动刷新消息列表中对应私聊会话的展示信息，避免头像/昵称不同步。
     */
    public void updateConversationUserDisplay(String userId, String name, String avatar) {
        if (userId == null) return;
        ConversationListFragment frag = (ConversationListFragment) getSupportFragmentManager()
                .findFragmentByTag("conversations");
        if (frag != null) {
            String finalName = (name != null && !name.isEmpty()) ? name : userId;
            frag.updateConversationDisplayInfo(userId, finalName, avatar);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUnreadMessageCountEvent(UnreadMessageCountEvent event) {
        if (bottomNav != null) bottomNav.updateUnreadCount(event.getTotalCount());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFriendApplicationUpdate(FriendApplicationUpdateEvent event) {
        if (bottomNav != null) {
            bottomNav.updateContactsBadge(event.getPendingCount());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFriendApplicationRefreshRequest(FriendApplicationRefreshRequestEvent event) {
        refreshFriendApplicationBadgeFromServer();
    }

    /**
     * 通过 HTTP 拉取好友申请列表，统计「待处理」数量并刷新通讯录/新朋友红点。
     * 用于服务端未向 IM friend_apply 会话推送消息时的兜底。
     */
    private void refreshFriendApplicationBadgeFromServer() {
        ServiceManager.getUserService().getFriendApplications(0, 100, new ApiCallback<FriendApplicationsData>() {
            @Override
            public void onSuccess(FriendApplicationsData data) {
                int count = 0;
                if (data != null && data.getItems() != null) {
                    for (FriendApplicationBean app : data.getItems()) {
                        // 对方发给我且状态为申请中(0)
                        if (!app.isSponsor() && app.getStatus() == 0) {
                            count++;
                        }
                    }
                }
                final int pending = count;
                runOnUiThread(() -> EventBus.getDefault().post(new FriendApplicationUpdateEvent(pending)));
            }

            @Override
            public void onError(int code, String message) {
                // 失败不更新，保留当前红点状态
            }
        });
    }

    // 设置用户信息显示（公开方法，便于资料页更新头像后主动刷新顶部区域）
    // 注意：getUserInfo 会读 SQLite，放在子线程执行避免 StrictMode DiskReadViolation
    public void setupUserInfo() {
        ImageView ivUserAvatar = findViewById(R.id.iv_user_avatar);
        TextView tvUserName = findViewById(R.id.tv_user_name);
        TextView tvUserId = findViewById(R.id.tv_user_id);
        View statusIndicator = findViewById(R.id.status_indicator);
        TextView tvStatus = findViewById(R.id.tv_status);
        
        if (ivUserAvatar == null || tvUserName == null || tvUserId == null) return;

        String userId = JIM.getInstance().getCurrentUserId();
        if (userId == null || userId.isEmpty()) {
            userId = ConfigUtils.currentUserId;
        }
        final String currentUserId = userId;

        if (currentUserId == null || currentUserId.isEmpty()) {
            tvUserName.setText("用户");
            tvUserId.setText("@未知");
            com.juggle.im.android.utils.AvatarUtils.loadAvatar(ivUserAvatar, ConfigUtils.myAvatarUrl, "用户");
            if (statusIndicator != null && tvStatus != null) {
                ivUserAvatar.postDelayed(() -> updateConnectionStatus(statusIndicator, tvStatus, null), 500);
            }
            return;
        }

        // 在子线程读取本地 DB，避免主线程磁盘读触发 StrictMode
        new Thread(() -> {
            final UserInfo userInfo = JIM.getInstance().getUserInfoManager().getUserInfo(currentUserId);
            runOnUiThread(() -> {
                if (isFinishing()) return;
                applyUserInfoToViews(ivUserAvatar, tvUserName, tvUserId, currentUserId, userInfo);
                if (statusIndicator != null && tvStatus != null) {
                    ivUserAvatar.postDelayed(() -> updateConnectionStatus(statusIndicator, tvStatus, null), 500);
                }
            });
        }).start();
    }

    /** 在主线程执行：根据 UserInfo 或 ConfigUtils 更新顶部用户信息视图 */
    private void applyUserInfoToViews(ImageView ivUserAvatar, TextView tvUserName, TextView tvUserId, String currentUserId, UserInfo userInfo) {
        if (userInfo != null) {
            String displayName = !TextUtils.isEmpty(ConfigUtils.myName) ? ConfigUtils.myName : userInfo.getUserName();
            String displayAvatar = !TextUtils.isEmpty(ConfigUtils.myAvatarUrl) ? ConfigUtils.myAvatarUrl : userInfo.getPortrait();
            tvUserName.setText(displayName);
            tvUserId.setText("@" + userInfo.getUserId());
            com.juggle.im.android.utils.AvatarUtils.loadAvatar(ivUserAvatar, displayAvatar, displayName);
        } else {
            if (ConfigUtils.myName != null && !ConfigUtils.myName.isEmpty()) {
                tvUserName.setText(ConfigUtils.myName);
            } else {
                tvUserName.setText("用户");
            }
            tvUserId.setText("@" + currentUserId);
            String displayName = ConfigUtils.myName != null ? ConfigUtils.myName : "用户";
            com.juggle.im.android.utils.AvatarUtils.loadAvatar(ivUserAvatar, ConfigUtils.myAvatarUrl, displayName);
            ivUserAvatar.postDelayed(() -> retryFetchUserInfo(ivUserAvatar, tvUserName, tvUserId, currentUserId, 0), 1000);
        }
    }
    
    // 重试获取用户信息，最多重试3次，每次间隔1秒（getUserInfo 在子线程执行，避免 StrictMode）
    private void retryFetchUserInfo(ImageView ivUserAvatar, TextView tvUserName, TextView tvUserId, String userId, int retryCount) {
        if (retryCount >= 3) {
            LogUtil.d("MainActivity", "retryFetchUserInfo: 本地重试3次失败，开始从服务器拉取用户信息");
            fetchUserInfoFromServer(ivUserAvatar, tvUserName, tvUserId, userId);
            return;
        }
        new Thread(() -> {
            final UserInfo userInfo = JIM.getInstance().getUserInfoManager().getUserInfo(userId);
            runOnUiThread(() -> {
                if (isFinishing()) return;
                if (userInfo != null) {
                    LogUtil.d("MainActivity", "retryFetchUserInfo: 成功获取用户信息");
                    tvUserName.setText(userInfo.getUserName());
                    tvUserId.setText("@" + userInfo.getUserId());
                    com.juggle.im.android.utils.AvatarUtils.loadAvatar(ivUserAvatar, ConfigUtils.myAvatarUrl, userInfo.getUserName());
                    ConfigUtils.myName = userInfo.getUserName();
                } else {
                    LogUtil.d("MainActivity", "retryFetchUserInfo: 本地未找到用户信息，继续重试 retryCount=" + retryCount);
                    ivUserAvatar.postDelayed(() -> retryFetchUserInfo(ivUserAvatar, tvUserName, tvUserId, userId, retryCount + 1), 1000);
                }
            });
        }).start();
    }
    
    /**
     * 将用户信息写入 SDK 本地缓存（优先 updateUserInfo，否则反射调用 insertUserInfoList），
     * 避免后续 setupUserInfo 时“本地未找到用户信息”反复重试。
     */
    private void saveUserInfoToSdk(UserInfo userInfo) {
        if (userInfo == null) return;
        try {
            Object manager = JIM.getInstance().getUserInfoManager();
            if (manager == null) return;
            // 优先调用 IUserInfoManager.updateUserInfo（需 SDK 包含该接口）
            try {
                java.lang.reflect.Method update = manager.getClass().getMethod("updateUserInfo", UserInfo.class);
                update.invoke(manager, userInfo);
                return;
            } catch (NoSuchMethodException ignored) {
                // 接口未提供 updateUserInfo，尝试内部实现 insertUserInfoList
            }
            java.lang.reflect.Method insert = manager.getClass().getMethod("insertUserInfoList", List.class);
            insert.invoke(manager, Collections.singletonList(userInfo));
        } catch (Exception e) {
            LogUtil.d("MainActivity", "saveUserInfoToSdk: " + (e != null ? e.getMessage() : ""));
        }
    }

    // 从服务器拉取用户信息
    private void fetchUserInfoFromServer(ImageView ivUserAvatar, TextView tvUserName, TextView tvUserId, String userId) {
        LogUtil.d("MainActivity", "fetchUserInfoFromServer: 开始从服务器拉取用户信息");
        ServiceManager.getUserService().getUserInfo(userId, new ApiCallback<UserInfoBean>() {
            @Override
            public void onSuccess(UserInfoBean data) {
                LogUtil.d("MainActivity", "fetchUserInfoFromServer onSuccess: data=" + (data != null ? data.toString() : "null"));
                if (data == null) return;
                runOnUiThread(() -> {
                    String name = data.getNickname();
                    String avatar = data.getAvatar();
                    LogUtil.d("MainActivity", "fetchUserInfoFromServer updating UI: name=" + name + ", avatar=" + avatar);
                    if (name != null && !name.isEmpty()) {
                        tvUserName.setText(name);
                        ConfigUtils.myName = name;
                    }
                    if (avatar != null && !avatar.isEmpty()) {
                        ConfigUtils.myAvatarUrl = avatar;
                    }
                    com.juggle.im.android.utils.AvatarUtils.loadAvatar(ivUserAvatar, ConfigUtils.myAvatarUrl, name != null ? name : "用户");
                    // 回写 SDK 本地缓存，避免后续 setupUserInfo 时“本地未找到用户信息”反复重试
                    UserInfo self = new UserInfo();
                    self.setUserId(userId);
                    self.setUserName(name != null ? name : "");
                    self.setPortrait(avatar != null ? avatar : "");
                    self.setUpdatedTime(System.currentTimeMillis());
                    saveUserInfoToSdk(self);
                });
            }

            @Override
            public void onError(int code, String message) {
                LogUtil.d("MainActivity", "fetchUserInfoFromServer onError: code=" + code);
            }
        });
    }
    
    
    // 更新连接状态显示
    private void updateConnectionStatus(View statusIndicator, TextView tvStatus, Object status) {
        if (statusIndicator == null || tvStatus == null) return;
        
        // 如果没有传入状态，则获取当前连接状态
        String statusStr;
        if (status == null) {
            // 从事件总线或其他方式获取当前连接状态
            // 这里我们使用一个简单的方法：检查是否有连接失败的提示
            View connectStatusView = findViewById(R.id.connect_status);
            if (connectStatusView != null && connectStatusView.getVisibility() == VISIBLE) {
                // 连接失败
                statusStr = "FAILURE";
            } else {
                // 默认为在线
                statusStr = "SUCCESS";
            }
        } else {
            statusStr = status.toString();
        }
        
        // 根据连接状态更新指示器
        if (statusStr.contains("SUCCESS")) {
            // 在线状态 - 绿点
            statusIndicator.setBackground(getDrawable(R.drawable.status_indicator_online));
            tvStatus.setText(R.string.status_online);
        } else if (statusStr.contains("FAILURE") || statusStr.contains("DISCONNECTED")) {
            // 离线状态 - 灰点
            statusIndicator.setBackground(getDrawable(R.drawable.status_indicator_offline));
            tvStatus.setText(R.string.status_offline);
        } else if (statusStr.contains("CONNECTING")) {
            // 网络波动 - 黄点
            statusIndicator.setBackground(getDrawable(R.drawable.status_indicator_unstable));
            tvStatus.setText(R.string.status_unstable);
        } else {
            // 默认在线状态
            statusIndicator.setBackground(getDrawable(R.drawable.status_indicator_online));
            tvStatus.setText(R.string.status_online);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 每次从后台回到前台时用 HTTP 拉取好友申请数量，确保红点正确（不依赖 IM 推送）
        refreshFriendApplicationBadgeFromServer();
        // 同步刷新顶部用户信息区域，确保使用最新昵称和头像（包括在其他设备更新后的情况）
        setupUserInfo();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        reconnectHandler.removeCallbacks(reconnectRunnable);
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        if (networkStateManager != null) {
            networkStateManager.unregister();
        }
    }
    
    /**
     * 清除登录状态
     * 包括清除 token、用户信息等
     */
    private void clearLoginState() {
        ConfigUtils.imToken = null;
        ConfigUtils.appToken = null;
        ConfigUtils.myName = null;
        ConfigUtils.myAvatarUrl = null;
        ConfigUtils.currentUserId = null;

        new Thread(() -> {
            SharedPreferences prefs = SecurePrefsHelper.getLoginPrefs(this);
            if (prefs != null) {
                prefs.edit()
                        .remove(LoginActivity.KEY_APP_TOKEN)
                        .remove(LoginActivity.KEY_IM_TOKEN)
                        .remove(LoginActivity.KEY_EXPIRE_TIME)
                        .apply();
            }
            runOnUiThread(() -> {
                try {
                    JIM.getInstance().getConnectionManager().disconnect(false);
                } catch (Exception e) {
                    LogUtil.e("MainActivity", "断开连接失败", e);
                }
            });
        }).start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 当用户按下返回键时，将应用移至后台而不是关闭
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
