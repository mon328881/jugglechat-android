package com.juggle.im.android.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.juggle.im.android.utils.LogUtil;
import com.juggle.im.JIM;
import com.juggle.im.JIMConst;
import com.juggle.im.android.chat.message.FriendNotifyMessage;
import com.juggle.im.android.chat.message.GroupNotifyMessage;
import com.juggle.im.android.event.ConnectStatusEvent;
import com.juggle.im.android.event.FriendApplicationUpdateEvent;
import com.juggle.im.android.event.MessageReadUpdatedEvent;
import com.juggle.im.android.event.MessageTopEvent;
import com.juggle.im.android.event.MessageUpdatedEvent;
import com.juggle.im.android.event.UnreadMessageCountEvent;
import com.juggle.im.android.model.ConfigUtils;
import com.juggle.im.interfaces.IConnectionManager;
import com.juggle.im.interfaces.IConversationManager;
import com.juggle.im.interfaces.IMessageManager;
import com.juggle.im.internal.logger.JLogConfig;
import com.juggle.im.internal.logger.JLogLevel;
import com.juggle.im.push.PushConfig;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.ConversationInfo;

import java.util.List;
import java.util.Map;

import org.greenrobot.eventbus.EventBus;
import com.juggle.im.android.event.ConversationUpdatedEvent;
import com.juggle.im.model.GetMessageOptions;
import com.juggle.im.model.GroupMessageReadInfo;
import com.juggle.im.model.Message;
import com.juggle.im.model.MessageReaction;
import com.juggle.im.model.UserInfo;

/**
 * IM核心类，负责IM功能的初始化和连接管理
 * 采用单例模式确保全局唯一实例
 */
public class JIMChatCore {
    private static final String tag = "JIMCore";
    private static volatile JIMChatCore instance;
    private static final Object lock = new Object();

    /**
     * 主消息列表中的“系统通知”虚拟会话 ID。
     * 约束：
     * - 不要以 "system" / "broadcast" 开头，避免命中 MainActivity/JIMChatCore 的隐藏会话过滤规则
     * - 不要包含 ":"，避免被当作系统/广播类隐藏会话
     */
    public static final String SYS_NOTICE_CONV_ID = "sys_notice";
    
    private JIMChatCore() {
        // 私有构造函数，防止外部实例化
    }
    
    /**
     * 获取IMCore的单例实例
     * @return IMCore实例
     */
    public static JIMChatCore getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new JIMChatCore();
                }
            }
        }
        return instance;
    }
    
    /**
     * 初始化IM
     * @param context Android上下文
     * @param serverList 服务器列表
     * @param appKey 应用密钥
     */
    public void init(Context context, List<String> serverList, String appKey) {
        if (context == null || serverList == null || appKey == null) {
            throw new IllegalArgumentException("Invalid arguments");
        }
        /**
         * 需要申请对应自己音视频 ID，这个仅仅用来测试
         */
        JIM.getInstance().getCallManager().initZegoEngine(ConfigUtils.zegoId, context);
        JIM.getInstance().setServerUrls(serverList);
        JIM.InitConfig.Builder builder = new JIM.InitConfig.Builder();

        // 日志配置
        JLogConfig.Builder logBuilder = new JLogConfig.Builder(context);
        logBuilder.setLogConsoleLevel(JLogLevel.JLogLevelVerbose);
        builder.setJLogConfig(new JLogConfig(logBuilder));

        // 极光推送配置：启用 JIM 的 JPush 支持
        PushConfig pushConfig = new PushConfig.Builder()
                .setJgConfig()
                .build();
        builder.setPushConfig(pushConfig);

        JIM.getInstance().getMessageManager().registerContentType(FriendNotifyMessage.class);
        JIM.getInstance().getMessageManager().registerContentType(GroupNotifyMessage.class);
        // 注册系统/广播自定义消息类型（jg:text & jg:notice）
        JIM.getInstance().getMessageManager().registerContentType(com.juggle.im.android.chat.message.SystemTextMessage.class);
        JIM.getInstance().getMessageManager().registerContentType(com.juggle.im.android.chat.message.SystemNoticeMessage.class);
        JIM.getInstance().init(context, appKey, builder.build());
        initListener();
        JIM.getInstance().getConnectionManager().addConnectionStatusListener("conn", new IConnectionManager.IConnectionStatusListener() {
            @Override
            public void onStatusChange(JIMConst.ConnectionStatus connectionStatus, int i, String s) {
                LogUtil.i(tag, "connection status change: " + connectionStatus);
                EventBus.getDefault().post(new ConnectStatusEvent(connectionStatus, i, s));
            }

            @Override
            public void onDbOpen() {
                LogUtil.i(tag, "db open");
                // 在子线程执行 DB 读取，避免主线程 StrictMode DiskReadViolation
                new Thread(() -> syncConversationListOnBackground()).start();
            }

            @Override
            public void onDbClose() {
                LogUtil.i(tag, "db close");
            }
        });
    }

    /**
     * 链接 IM
     * @param token 访问令牌
     */
    public void connect(String token) {
        JIM.getInstance().getConnectionManager().connect(token);
    }

    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());

    /**
     * 在子线程同步会话列表并 post 事件到主线程，供 onDbOpen 调用，避免主线程读 DB。
     */
    private void syncConversationListOnBackground() {
        long cursor = -1;
        for (;;) {
            List<ConversationInfo> conversationInfoList = JIM.getInstance().getConversationManager().getConversationInfoList(20, cursor, JIMConst.PullDirection.NEWER);
            if (conversationInfoList == null || conversationInfoList.isEmpty()) {
                LogUtil.i(tag, "empty conversation");
                break;
            }
            LogUtil.d(tag, "fetch conversation size: " + conversationInfoList.size());
            final List<ConversationInfo> batch = conversationInfoList;
            sMainHandler.post(() -> EventBus.getDefault().post(new ConversationUpdatedEvent(batch)));
            ConversationInfo last = conversationInfoList.get(conversationInfoList.size() - 1);
            cursor = last.getSortTime();
            if (conversationInfoList.size() < 20) {
                LogUtil.i(tag, "fetch conversation end");
                break;
            }
        }
        final int unreadCount = getChatUnreadCountExcludingSystem();
        final int friendApplyBadge = getFriendApplyBadgeCount();
        sMainHandler.post(() -> {
            EventBus.getDefault().post(new UnreadMessageCountEvent(unreadCount));
            EventBus.getDefault().post(new FriendApplicationUpdateEvent(friendApplyBadge));
        });
    }

    /**
     * 同步会话列表（可能在主线程调用，如手动刷新时）
     * 应用初次启动，先进行会话列表同步、更新
     */
    public void syncConversationList() {
        long cursor = -1;
        for(;;) {
            List<ConversationInfo> conversationInfoList = JIM.getInstance().getConversationManager().getConversationInfoList(20, cursor, JIMConst.PullDirection.NEWER);
            if (conversationInfoList == null || conversationInfoList.isEmpty()) {
                LogUtil.i(tag, "empty conversation");
                break;
            }
            LogUtil.d(tag, "fetch conversation size: " + conversationInfoList.size());
            EventBus.getDefault().post(new ConversationUpdatedEvent(conversationInfoList));
            ConversationInfo last = conversationInfoList.get(conversationInfoList.size() - 1);
            cursor = last.getSortTime();
            if (conversationInfoList.size() < 20) {
                LogUtil.i(tag, "fetch conversation end");
                break;
            }
        }
        int c = getChatUnreadCountExcludingSystem();
        EventBus.getDefault().post(new UnreadMessageCountEvent(c));
        postFriendApplyBadge();
    }

    /**
     * 加载更多会话（分页）
     * @param cursor 上次加载的最后一条会话的sortTime，第一次传-1
     * @param pageSize 每页数量
     * @return 加载的会话数量
     */
    public int loadMoreConversations(long cursor, int pageSize) {
        List<ConversationInfo> conversationInfoList = JIM.getInstance().getConversationManager()
                .getConversationInfoList(pageSize, cursor, JIMConst.PullDirection.OLDER);
        if (conversationInfoList == null || conversationInfoList.isEmpty()) {
            LogUtil.i(tag, "no more conversations to load");
            return 0;
        }
        LogUtil.d(tag, "load more conversations size: " + conversationInfoList.size());

        // Post conversation update event for this page
        EventBus.getDefault().post(new ConversationUpdatedEvent(conversationInfoList));

        return conversationInfoList.size();
    }

    private void initListener() {

        JIM.getInstance().getConversationManager().addListener("conversationList", new IConversationManager.IConversationListener() {
            @Override
            public void onConversationInfoAdd(List<ConversationInfo> list) {
                LogUtil.i(tag, "onConversationInfoAdd: " + list.size());
                EventBus.getDefault().post(new ConversationUpdatedEvent(list));
                maybePostFriendApplyBadge(list);
            }

            @Override
            public void onConversationInfoUpdate(List<ConversationInfo> list) {
                LogUtil.i(tag, "onConversationInfoUpdate: " + list.size());
                EventBus.getDefault().post(new ConversationUpdatedEvent(list));
                maybePostFriendApplyBadge(list);
            }

            @Override
            public void onConversationInfoDelete(List<ConversationInfo> list) {

            }

            @Override
            public void onTotalUnreadMessageCountUpdate(int i) {
                int c = getChatUnreadCountExcludingSystem();
                EventBus.getDefault().post(new UnreadMessageCountEvent(c));
            }
        });
        JIM.getInstance().getMessageManager().addListener("msg", new IMessageManager.IMessageListener() {
            @Override
            public void onMessageReceive(Message message) {
                // LogUtil.d(tag, "onMessageReceive: " + message.toString());
                // JIMChatCore.initListener 中的 onMessageReceive 里，打印一下
                LogUtil.d(tag, "onMessageReceive: convId=" 
                        + (message.getConversation() != null ? message.getConversation().getConversationId() : "null")
                        + ", channelType=" + message.getConversation().getConversationType()
                        + ", msgType=" + message.getContent().getContentType());
                EventBus.getDefault().post(new MessageUpdatedEvent(message));
                // 好友申请会话收到新消息时立即刷新通讯录/新朋友红点（会话列表回调可能晚于消息回调）
                if (message != null && message.getConversation() != null
                        && message.getConversation().getConversationId() != null
                        && message.getConversation().getConversationId().startsWith("friend_apply")) {
                    postFriendApplyBadge();
                }
            }

            @Override
            public void onMessageRecall(Message message) {
                LogUtil.d(tag, "onMessageRecall: " + message.toString());
            }

            @Override
            public void onMessageDelete(Conversation conversation, List<Long> list) {

            }

            @Override
            public void onMessageClear(Conversation conversation, long l, String s) {

            }

            @Override
            public void onMessageUpdate(Message message) {
                LogUtil.d(tag, "onMessageUpdate: " + message.toString());

            }

            @Override
            public void onMessageReactionAdd(Conversation conversation, MessageReaction messageReaction) {
                LogUtil.d(tag, "onMessageReactionAdd: " + messageReaction.toString());
            }

            @Override
            public void onMessageReactionRemove(Conversation conversation, MessageReaction messageReaction) {
                LogUtil.d(tag, "onMessageReactionRemove: " + messageReaction.toString());
            }

            @Override
            public void onMessageSetTop(Message message, UserInfo userInfo, boolean b) {
                LogUtil.d(tag, "onMessageSetTop: " + message.toString());
                EventBus.getDefault().post(new MessageTopEvent(message, userInfo, b));
            }
        });

        JIM.getInstance().getMessageManager().addReadReceiptListener("readReceipt", new IMessageManager.IMessageReadReceiptListener() {
            @Override
            public void onMessagesRead(Conversation conversation, List<String> list) {
                EventBus.getDefault().post(new MessageReadUpdatedEvent(conversation, list));
            }

            @Override
            public void onGroupMessagesRead(Conversation conversation, Map<String, GroupMessageReadInfo> map) {

            }
        });
    }

    public static boolean isSysNoticeConversationId(String convId) {
        return SYS_NOTICE_CONV_ID.equals(convId);
    }

    private static boolean isExcludedSystemConversationFromSysNotice(String convId) {
        if (convId == null || convId.isEmpty()) return true;
        // 好友申请/动态通知等系统会话走各自的红点逻辑，不纳入“系统通知”聚合
        return convId.toLowerCase().startsWith("friend_apply")
                || convId.toLowerCase().startsWith("post_ntf");
    }

    /**
     * 清空“系统通知”虚拟会话的未读数。
     * 注意：sys_notice 是 UI 层虚拟会话 ID，SDK/服务端未必存在该会话，直接 clearUnreadCount 可能失败（例如 21003）。
     * 这里通过清空所有真实 SYSTEM 会话（排除 friend_apply/post_ntf）来达到“系统通知已读”的效果。
     */
    public void clearSysNoticeUnread() {
        IConversationManager cm = JIM.getInstance().getConversationManager();
        long cursor = -1L;
        for (;;) {
            List<ConversationInfo> list = cm.getConversationInfoList(50, cursor, JIMConst.PullDirection.NEWER);
            if (list == null || list.isEmpty()) break;
            for (ConversationInfo info : list) {
                if (info == null || info.getConversation() == null) continue;
                Conversation c = info.getConversation();
                if (c.getConversationType() != Conversation.ConversationType.SYSTEM) continue;
                String convId = c.getConversationId();
                if (isExcludedSystemConversationFromSysNotice(convId)) continue;
                try {
                    cm.clearUnreadCount(c, null);
                } catch (Throwable ignored) {
                }
            }
            ConversationInfo last = list.get(list.size() - 1);
            cursor = last.getSortTime();
            if (list.size() < 50) break;
        }
    }

    /**
     * 判断是否为系统/广播类会话的 conversationId，与 MainActivity 过滤规则一致。
     */
    private static boolean isSystemOrBroadcastConversationId(String convId) {
        if (convId == null || convId.isEmpty()) return false;
        // 系统通知虚拟会话需要常驻主消息列表，不能被当作隐藏会话过滤
        if (isSysNoticeConversationId(convId)) return false;
        if (convId.contains(":")) return true;
        String lower = convId.toLowerCase();
        return lower.startsWith("friend_apply") || lower.startsWith("post_ntf")
                || lower.startsWith("broadcast") || lower.startsWith("system");
    }

    /**
     * 计算消息 Tab 未读数：仅统计会展示的普通会话，排除系统/好友申请等隐藏会话。
     */
    private int getChatUnreadCountExcludingSystem() {
        IConversationManager cm = JIM.getInstance().getConversationManager();
        int total = 0;
        long cursor = -1L;
        for (;;) {
            List<ConversationInfo> list = cm.getConversationInfoList(50, cursor, JIMConst.PullDirection.NEWER);
            if (list == null || list.isEmpty()) break;
            for (ConversationInfo info : list) {
                if (info == null || info.getConversation() == null) continue;
                Conversation c = info.getConversation();
                String convId = c.getConversationId();
                Conversation.ConversationType type = c.getConversationType();
                // 消息 Tab 未读数：SYSTEM 会话聚合到“系统通知”，因此要把真实 SYSTEM 未读计入（排除 friend_apply/post_ntf）
                if (type == Conversation.ConversationType.SYSTEM) {
                    if (isExcludedSystemConversationFromSysNotice(convId)) {
                        continue;
                    }
                    total += info.getUnreadCount();
                    continue;
                }
                if (type == Conversation.ConversationType.PRIVATE && convId != null && isSystemOrBroadcastConversationId(convId)) {
                    continue;
                }
                total += info.getUnreadCount();
            }
            ConversationInfo last = list.get(list.size() - 1);
            cursor = last.getSortTime();
            if (list.size() < 50) break;
        }
        return Math.max(total, 0);
    }

    /**
     * 根据好友申请会话未读数发送 FriendApplicationUpdateEvent，用于通讯录 Tab 与新朋友红点。
     */
    private void postFriendApplyBadge() {
        int pending = getFriendApplyBadgeCount();
        EventBus.getDefault().post(new FriendApplicationUpdateEvent(pending));
    }

    /**
     * 在子线程可调用的好友申请未读数（读 DB），供 syncConversationListOnBackground 使用。
     */
    private int getFriendApplyBadgeCount() {
        IConversationManager cm = JIM.getInstance().getConversationManager();
        Conversation sysConv = new Conversation(Conversation.ConversationType.SYSTEM, "friend_apply");
        Conversation privateConv = new Conversation(Conversation.ConversationType.PRIVATE, "friend_apply");
        ConversationInfo infoSys = cm.getConversationInfo(sysConv);
        ConversationInfo infoPrv = cm.getConversationInfo(privateConv);
        int sysCount = infoSys != null ? infoSys.getUnreadCount() : 0;
        int prvCount = infoPrv != null ? infoPrv.getUnreadCount() : 0;
        return Math.max(sysCount, prvCount);
    }

    /**
     * 会话变更时若包含好友申请会话，则更新通讯录/新朋友红点。
     * 服务端可能推成 PRIVATE 会话（conversationId=friend_apply），需同时匹配 SYSTEM 与 PRIVATE。
     */
    private void maybePostFriendApplyBadge(List<ConversationInfo> list) {
        if (list == null || list.isEmpty()) return;
        for (ConversationInfo info : list) {
            if (info == null || info.getConversation() == null) continue;
            Conversation c = info.getConversation();
            String convId = c.getConversationId();
            if (convId != null && convId.startsWith("friend_apply")
                    && (c.getConversationType() == Conversation.ConversationType.SYSTEM
                    || c.getConversationType() == Conversation.ConversationType.PRIVATE)) {
                EventBus.getDefault().post(new FriendApplicationUpdateEvent(info.getUnreadCount()));
                return;
            }
        }
    }

    /**
     * 根据会话 ID 获取会话的消息列表
     * @param conversationId 会话 ID
     * @param count 获取消息数量
     * @param msgTime 消息时间戳 第一次获取传 0
     */
    public void getMessages(String conversationId, Conversation.ConversationType conversationType, int count, long msgTime, IMessageManager.IGetMessagesCallbackV3 callback) {
        GetMessageOptions options = new GetMessageOptions();
        options.setCount(count);
        options.setStartTime(msgTime);
        Conversation conversation = new Conversation(conversationType, conversationId);
        JIM.getInstance().getMessageManager().getMessages(conversation, JIMConst.PullDirection.OLDER, options, new IMessageManager.IGetMessagesCallbackV3() {
            @Override
            public void onGetMessages(List<Message> messages, long timestamp, boolean hasMore, int code) {
                LogUtil.d(tag, "messageList count: " + messages.size());
                callback.onGetMessages(messages, timestamp, hasMore, code);
            }
        });
    }
}
