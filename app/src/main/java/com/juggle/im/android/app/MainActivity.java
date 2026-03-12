package com.juggle.im.android.app;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import com.juggle.im.android.chat.SearchActivity;
import com.juggle.im.android.chat.call.MultiCallActivity;
import com.juggle.im.android.chat.call.SingleCallActivity;
import com.juggle.im.android.core.JIMChatCore;
import com.juggle.im.android.event.ConnectStatusEvent;
import com.juggle.im.android.event.ConversationUpdatedEvent;
import com.juggle.im.android.event.MessageReadUpdatedEvent;
import com.juggle.im.android.event.UnreadMessageCountEvent;
import com.juggle.im.android.model.ConfigUtils;
import com.juggle.im.android.model.UiConversation;
import com.juggle.im.android.utils.NetworkStateManager;
import com.juggle.im.call.CallConst;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.ConversationInfo;
import com.juggle.im.model.GroupInfo;
import com.juggle.im.model.UserInfo;
import com.qiniu.android.utils.StringUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    // 自动重连配置
    private static final int MAX_RETRY_COUNT = 3;
    private static final int RETRY_DELAY_MS = 5000;

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
                Log.i("MainActivity", "try reconnect, attempt " + currentRetryCount);
                JIMChatCore.getInstance().connect(ConfigUtils.imToken);
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
                Log.i("MainActivity", "网络已连接，尝试重连");
                if (ConfigUtils.imToken != null && !ConfigUtils.imToken.isEmpty()) {
                    currentRetryCount = 0;
                    isReconnecting = true;
                    reconnectHandler.removeCallbacks(reconnectRunnable);
                    reconnectHandler.post(reconnectRunnable);
                }
            }
            
            @Override
            public void onNetworkLost() {
                Log.i("MainActivity", "网络已断开");
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
                        }
                        return false;
                    });
                    popup.show();
                }
            });
        }
        btnSearch = findViewById(R.id.btn_search);
        btnSearch.setOnClickListener( v -> {
            startActivity(new Intent(MainActivity.this, SearchActivity.class));
        });

        EventBus.getDefault().register(this);

        JIM.getInstance().getCallManager().addReceiveListener("CallReceive", iCallSession -> {
            Log.d("MainActivity", "receive call: " + iCallSession.getCallId());
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
        Log.i("MainActivity", event.getConnectionStatus().toString() + "," + event.getCode());
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConversationUpdated(ConversationUpdatedEvent event) {
        Log.i("MainActivity", "onConversationUpdated");
        List<ConversationInfo> infoList = event.getConversationInfoList();
        if (infoList == null || infoList.isEmpty()) return;

        List<UiConversation> uiList = new ArrayList<>();
        for (ConversationInfo info : infoList) {
            UiConversation ui = UiConversation.fromConversationInfo(info);
            //不显示系统消息
            if (info.getConversation().getConversationType().equals(Conversation.ConversationType.SYSTEM)) {
                continue;
            }
            if (info.getConversation().getConversationType().equals(Conversation.ConversationType.GROUP)) {
                GroupInfo groupInfo = JIM.getInstance().getUserInfoManager().getGroupInfo(ui.getConversationInfo().getConversation().getConversationId());
                if (groupInfo != null) {
                    ui.setName(groupInfo.getGroupName());
                    ui.setAvatar(groupInfo.getPortrait());
                }
                UserInfo userInfo = ui.getLastMessage() != null
                        ? JIM.getInstance().getUserInfoManager().getUserInfo(ui.getLastMessage().getSenderUserId())
                        : null;
                if (userInfo != null) {
                    ui.setLastMessageUserName(userInfo.getUserName());
                }
            } else if (info.getConversation().getConversationType().equals(Conversation.ConversationType.PRIVATE)) {
                UserInfo userInfo = JIM.getInstance().getUserInfoManager().getUserInfo(ui.getConversationInfo().getConversation().getConversationId());
                if (userInfo != null) {
                    ui.setName(userInfo.getUserName());
                    ui.setAvatar(userInfo.getPortrait());
                    ui.setLastMessageUserName(userInfo.getUserName());
                }
            }
            uiList.add(ui);
        }
        ConversationListFragment frag = (ConversationListFragment) getSupportFragmentManager().findFragmentByTag("conversations");
        if (frag != null) {
            runOnUiThread(() -> frag.upsertConversations(uiList));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUnreadMessageCountEvent(UnreadMessageCountEvent event) {
        bottomNav.updateUnreadCount(event.getTotalCount());
    }

    // 设置用户信息显示
    private void setupUserInfo() {
        ImageView ivUserAvatar = findViewById(R.id.iv_user_avatar);
        TextView tvUserName = findViewById(R.id.tv_user_name);
        TextView tvUserId = findViewById(R.id.tv_user_id);
        View statusIndicator = findViewById(R.id.status_indicator);
        TextView tvStatus = findViewById(R.id.tv_status);
        
        if (ivUserAvatar != null && tvUserName != null && tvUserId != null) {
            // 从JIM SDK获取当前用户ID
            final String userId = JIM.getInstance().getCurrentUserId();
            String finalUserId = userId;
            if (finalUserId == null || finalUserId.isEmpty()) {
                // 如果JIM SDK还没有初始化，使用ConfigUtils中的缓存
                finalUserId = ConfigUtils.currentUserId;
            }
            
            final String currentUserId = finalUserId;
            
            if (currentUserId != null && !currentUserId.isEmpty()) {
                // 获取当前登录用户信息
                UserInfo userInfo = JIM.getInstance().getUserInfoManager().getUserInfo(currentUserId);
                
                if (userInfo != null) {
                    // 设置用户昵称
                    tvUserName.setText(userInfo.getUserName());
                    // 设置用户ID
                    tvUserId.setText("@" + userInfo.getUserId());
                    // 设置用户头像
                    com.juggle.im.android.utils.AvatarUtils.loadAvatar(ivUserAvatar, ConfigUtils.myAvatarUrl, userInfo.getUserName());
                } else {
                    // 如果用户信息不存在，先使用ConfigUtils中的缓存数据
                    if (ConfigUtils.myName != null && !ConfigUtils.myName.isEmpty()) {
                        tvUserName.setText(ConfigUtils.myName);
                    } else {
                        tvUserName.setText("用户");
                    }
                    
                    tvUserId.setText("@" + currentUserId);
                    
                    // 使用首字母生成头像
                    String displayName = ConfigUtils.myName != null ? ConfigUtils.myName : "用户";
                    com.juggle.im.android.utils.AvatarUtils.loadAvatar(ivUserAvatar, ConfigUtils.myAvatarUrl, displayName);
                    
                    // 延迟重试获取用户信息，因为APP重启后用户信息可能还没有加载
                    ivUserAvatar.postDelayed(() -> {
                        retryFetchUserInfo(ivUserAvatar, tvUserName, tvUserId, currentUserId, 0);
                    }, 1000);
                }
            } else {
                // 用户ID为空，显示默认信息
                tvUserName.setText("用户");
                tvUserId.setText("@未知");
                com.juggle.im.android.utils.AvatarUtils.loadAvatar(ivUserAvatar, ConfigUtils.myAvatarUrl, "用户");
            }
            
            // 延迟更新连接状态，确保连接状态已确定
            ivUserAvatar.postDelayed(() -> {
                if (statusIndicator != null && tvStatus != null) {
                    updateConnectionStatus(statusIndicator, tvStatus, null);
                }
            }, 500);
        }
    }
    
    // 重试获取用户信息，最多重试3次，每次间隔1秒
    private void retryFetchUserInfo(ImageView ivUserAvatar, TextView tvUserName, TextView tvUserId, String userId, int retryCount) {
        if (retryCount >= 3) {
            return;
        }
        
        UserInfo userInfo = JIM.getInstance().getUserInfoManager().getUserInfo(userId);
        if (userInfo != null) {
            // 成功获取用户信息，更新UI
            tvUserName.setText(userInfo.getUserName());
            tvUserId.setText("@" + userInfo.getUserId());
            com.juggle.im.android.utils.AvatarUtils.loadAvatar(ivUserAvatar, ConfigUtils.myAvatarUrl, userInfo.getUserName());
            
            // 更新缓存
            ConfigUtils.myName = userInfo.getUserName();
        } else {
            // 继续重试
            ivUserAvatar.postDelayed(() -> {
                retryFetchUserInfo(ivUserAvatar, tvUserName, tvUserId, userId, retryCount + 1);
            }, 1000);
        }
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
        // 清除内存中的配置
        ConfigUtils.imToken = null;
        ConfigUtils.appToken = null;
        ConfigUtils.myName = null;
        ConfigUtils.myAvatarUrl = null;
        ConfigUtils.currentUserId = null;
        
        // 清除 SharedPreferences 中的 token
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(LoginActivity.KEY_APP_TOKEN);
        editor.remove(LoginActivity.KEY_IM_TOKEN);
        editor.remove(LoginActivity.KEY_EXPIRE_TIME);
        // 注意：不清除 KEY_REMEMBER_ACCOUNT 和 KEY_LAST_ACCOUNT，保留"记住账号"功能
        editor.apply();
        
        // 断开 IM 连接
        try {
            JIM.getInstance().getConnectionManager().disconnect(false);
        } catch (Exception e) {
            Log.e("MainActivity", "断开连接失败", e);
        }
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
