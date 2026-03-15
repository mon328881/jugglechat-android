package com.juggle.im.android.chat;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import static com.juggle.im.android.chat.MessageListFragment.ARG_MENTION;
import static com.juggle.im.android.chat.SelectMemberActivity.DISABLE_MEMBERS;
import static com.juggle.im.android.chat.SelectMemberActivity.GROUP_ID;
import static com.juggle.im.android.chat.SelectMemberActivity.SELECTED_MEMBERS;
import static com.juggle.im.android.chat.SelectMemberActivity.SELECTED_MEMBERS_NAME;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.juggle.im.android.utils.LogUtil;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.juggle.im.JIM;
import com.juggle.im.android.R;

import com.juggle.im.android.chat.call.BaseCallActivity;
import com.juggle.im.android.chat.mention.MentionManager;
import com.juggle.im.android.chat.mention.MentionModel;
import com.juggle.im.android.chat.plugin.CameraPlugin;
import com.juggle.im.android.chat.plugin.FilePlugin;
import com.juggle.im.android.chat.plugin.ImagePlugin;
import com.juggle.im.android.chat.utils.FileUtils;
import com.juggle.im.android.chat.utils.MessageUtils;

import java.io.File;
import com.juggle.im.android.chat.view.ChatInputActionBar;
import com.juggle.im.android.event.MessageReadUpdatedEvent;
import com.juggle.im.android.event.MessageTopEvent;
import com.juggle.im.android.event.MessageUpdatedEvent;
import com.juggle.im.android.events.GroupNameUpdatedEvent;
import com.juggle.im.android.model.FavoriteItem;
import com.juggle.im.android.model.UiMessage;
import com.juggle.im.interfaces.IMessageManager;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.MergeMessagePreviewUnit;
import com.juggle.im.model.Message;
import com.juggle.im.model.MessageMentionInfo;
import com.juggle.im.model.MessageOptions;
import com.juggle.im.model.PushData;
import com.juggle.im.model.UserInfo;
import com.juggle.im.model.messages.FileMessage;
import com.juggle.im.model.messages.ImageMessage;
import com.juggle.im.model.messages.MergeMessage;
import com.juggle.im.model.messages.TextMessage;
import com.juggle.im.model.messages.VoiceMessage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ConversationActivity extends AppCompatActivity {
    public static final String EXTRA_CONVERSATION_ID = "extra_conversation_id";
    public static final String EXTRA_IS_GROUP = "extra_is_group";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_IS_TOP = "extra_is_top";
    public static final String EXTRA_IS_MUTE = "extra_is_mute";
    public static final String EXTRA_UNREAD_COUNT = "extra_unread_count";
    public static final int REQ_FORWARD = 2001;
    public static final int REQ_MENTION = 2002;
    public static final int REQ_LOCATION_PICKER = 2005;
    private boolean isGroup;
    private String conversationId;
    private Conversation conversation;
    private int lastHeight = 0;

    public static Intent intentFor(Context ctx,
            String conversationId,
            String title,
            boolean isGroup,
            boolean isTop,
            boolean isMute) {
        Intent i = new Intent(ctx, ConversationActivity.class);
        i.putExtra(EXTRA_CONVERSATION_ID, conversationId);
        i.putExtra(EXTRA_IS_GROUP, isGroup);
        i.putExtra(EXTRA_IS_TOP, isTop);
        i.putExtra(EXTRA_IS_MUTE, isMute);
        i.putExtra(EXTRA_TITLE, title);
        return i;
    }

    // Overload to include a human-readable title for the conversation
    // (UiConversation.name)
    public static Intent intentFor(Context ctx, String conversationId, boolean isGroup, String title) {
        Intent i = intentFor(ctx, conversationId, title, isGroup, false, false);
        i.putExtra(EXTRA_TITLE, title);
        return i;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        EventBus.getDefault().register(this);

        // setup toolbar title and back button
        TextView tvTitle = findViewById(R.id.tv_title);
        ImageView ivBack = findViewById(R.id.iv_back);
        ImageView ivSettings = findViewById(R.id.iv_settings);
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        if (tvTitle != null && title != null) {
            tvTitle.setText(title);
        }
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }

        if (ivSettings != null) {
            ivSettings.setOnClickListener(v -> {
                // launch settings activity with conversation id and isGroup
                String cid = getIntent().getStringExtra(EXTRA_CONVERSATION_ID);
                boolean grp = getIntent().getBooleanExtra(EXTRA_IS_GROUP, false);
                String t = getIntent().getStringExtra(EXTRA_TITLE);
                boolean isTop = getIntent().getBooleanExtra(EXTRA_IS_TOP, false);
                boolean isMute = getIntent().getBooleanExtra(EXTRA_IS_MUTE, false);
                Intent it = ConversationSettingsActivity.intentFor(this, cid, t, grp, isTop, isMute);
                startActivity(it);
            });
        }

        if (savedInstanceState == null) {
            conversationId = getIntent().getStringExtra(EXTRA_CONVERSATION_ID);
            isGroup = getIntent().getBooleanExtra(EXTRA_IS_GROUP, false);
            boolean isMention = getIntent().getBooleanExtra(ARG_MENTION, false);
            int unreadCount = getIntent().getIntExtra(EXTRA_UNREAD_COUNT, 0);
            conversation = new Conversation(
                    isGroup ? Conversation.ConversationType.GROUP : Conversation.ConversationType.PRIVATE,
                    conversationId);
            
            // 如果是群组，检查当前用户是否是群组成员
            if (isGroup) {
                com.juggle.im.model.GroupInfo groupInfo = JIM.getInstance().getUserInfoManager().getGroupInfo(conversationId);
                if (groupInfo == null) {
                    // 群组不存在或用户不是成员
                    Toast.makeText(this, "你不是该群组的成员", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            }
            
            MessageListFragment frag = MessageListFragment.newInstance(conversationId, isGroup, unreadCount, isMention);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_messages_container, frag)
                    .commit();
        }

        // wire up input bar
        ChatInputActionBar inputBar = findViewById(R.id.input_bar);
        if (inputBar != null) {
            inputBar.setListener(new ChatInputActionBar.Listener() {
                public void onSend(String text, String msgId, List<MentionModel> mentionModelList, int sendType) {
                    TextMessage msg = new TextMessage(text);
                    MessageOptions options = new MessageOptions();
                    PushData pushData = new PushData();
                    pushData.setContent(text);
                    options.setPushData(pushData);
                    if (mentionModelList != null && !mentionModelList.isEmpty()) {
                        MessageMentionInfo mentionInfo = getMessageMentionInfo(mentionModelList);
                        options.setMentionInfo(mentionInfo);
                    }
                    if (sendType == R.id.tag_edit_msg) {
                        editTextMessage(msgId, msg, options, conversation);
                    } else {
                        options.setReferredMessageId(msgId);
                        sendTextMessage(msg, options, conversation);
                    }
                }

                @Override
                public void onMentionTrigger(MentionManager mentionManager) {
                    Intent it = new Intent(ConversationActivity.this, SelectMemberActivity.class);
                    it.putExtra(GROUP_ID, conversationId);
                    startActivityForResult(it, REQ_MENTION);
                }

                @Override
                public void onRequestVoice() {
                }

                @Override
                public void onStartVoiceRecord() {
                    LogUtil.i("TAG", "onStartVoiceRecord");
                }

                @Override
                public void onFinishRecord(String voiceUrl, long duration) {
                    LogUtil.i("TAG", "onFinishVoiceRecord: voiceUrl=" + voiceUrl + ", duration=" + duration);
                    
                    // Validate voice file path
                    if (voiceUrl == null || voiceUrl.isEmpty()) {
                        LogUtil.e("TAG", "ERROR: voiceUrl is null or empty");
                        Toast.makeText(ConversationActivity.this, "语音文件路径无效", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    File voiceFile = new File(voiceUrl);
                    if (!voiceFile.exists()) {
                        LogUtil.e("TAG", "ERROR: Voice file does not exist at: " + voiceUrl);
                        Toast.makeText(ConversationActivity.this, "语音文件不存在", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    long fileSize = voiceFile.length();
                    if (fileSize == 0) {
                        LogUtil.e("TAG", "ERROR: Voice file is empty at: " + voiceUrl);
                        Toast.makeText(ConversationActivity.this, "语音文件为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    LogUtil.i("TAG", "Voice file validated: size=" + fileSize + " bytes");
                    
                    VoiceMessage voice = new VoiceMessage();
                    voice.setLocalPath(voiceUrl);
                    voice.setDuration((int) duration);
                    sendVoiceMessage(voice, conversation);
                }

                @Override
                public void onCancelVoiceRecord() {
                    LogUtil.i("TAG", "onCancelVoiceRecord");
                }

                @Override
                public void onMoreAction(String pluginId, String action, Object data) {
                    handlePluginResult(pluginId, action, data);
                }

                @Override
                public void onPanelVisibilityChanged(boolean visible) {
                    scrollMessageListIfNeed(visible);
                }

                @Override
                public void onKeyboardVisibilityChanged(boolean visible) {
                    if (visible) {
                        // when keyboard shows, ensure messages are scrolled to bottom so input isn't
                        // obscured
                        MessageListFragment frag = (MessageListFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.fragment_messages_container);
                        if (frag != null)
                            frag.scrollToBottomIfNeeded();
                    }
                }

                @Override
                public void onKeyboardCreated(int h) {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                }
            });
        }

        // 消息置顶
        JIM.getInstance().getMessageManager().getTopMessage(conversation, new IMessageManager.IGetTopMessageCallback() {
            @Override
            public void onSuccess(Message message, UserInfo userInfo, long l) {
                handleTopMessage(message, userInfo);
            }

            @Override
            public void onError(int i) {
                LogUtil.i("TAG", "getTopMessage error: " + i);
            }
        });
        Window window = getWindow();
        window.setNavigationBarColor(getColor(R.color.input_bg_light));
    }

    @NonNull
    private static MessageMentionInfo getMessageMentionInfo(List<MentionModel> mentionModelList) {
        MessageMentionInfo mentionInfo = new MessageMentionInfo();
        List<UserInfo> messageMentionInfoList = new ArrayList<>();
        for (MentionModel mentionModel : mentionModelList) {
            UserInfo userInfo = new UserInfo();
            userInfo.setUserId(mentionModel.getUserId());
            userInfo.setUserName(mentionModel.getDisplayName());
            messageMentionInfoList.add(userInfo);
        }
        mentionInfo.setType(MessageMentionInfo.MentionType.SOMEONE);
        mentionInfo.setTargetUsers(messageMentionInfoList);
        return mentionInfo;
    }

    private void handleTopMessage(Message message, UserInfo userInfo) {
        View vPin = findViewById(R.id.layout_pin_message);
        TextView tvContent = vPin.findViewById(R.id.pin_message_content);
        tvContent.setText(
                userInfo.getUserName() + "：" + MessageUtils.getMessageSummary(ConversationActivity.this, message));
        View del = findViewById(R.id.button_del_pin);
        del.setOnClickListener(v -> {
            JIM.getInstance().getMessageManager().setTop(message.getMessageId(), conversation, false, null);
            vPin.setVisibility(GONE);
        });
        vPin.setVisibility(VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.d("ConversationActivity", "onActivityResult: requestCode=" + requestCode + 
                ", resultCode=" + resultCode + ", data=" + (data != null ? "not null" : "null"));
        
        ChatInputActionBar inputBar = findViewById(R.id.input_bar);
        boolean handled = false;
        if (inputBar != null) {
            handled = inputBar.onActivityResult(requestCode, resultCode, data);
        }
        // If not handled by plugins, you may still want to handle other requestCodes
        // here.
        if (!handled && requestCode == REQ_FORWARD && resultCode == RESULT_OK && data != null) {
            String targetConvId = data.getStringExtra(ForwardConversationListActivity.EXTRA_CONVERSATION_ID);
            String targetName = data.getStringExtra(ForwardConversationListActivity.EXTRA_CONVERSATION_NAME);
            boolean targetIsGroup = data.getBooleanExtra(ForwardConversationListActivity.EXTRA_IS_GROUP, false);
            String mode = data.getStringExtra(ForwardConversationListActivity.EXTRA_FORWARD_MODE);
            // obtain the message list fragment to get selected messages
            MessageListFragment frag = (MessageListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_messages_container);
            if (frag != null) {
                List<UiMessage> selected = frag.getSelectedMessagesForForward();
                Conversation targetConv = new Conversation(
                        targetIsGroup ? Conversation.ConversationType.GROUP : Conversation.ConversationType.PRIVATE,
                        targetConvId);
                if (mode != null && mode.equals("merge")) {
                    // perform merge forward
                    sendMergeMessage(selected, targetConv, targetName);
                } else {
                    // single-forward: forward text messages only (others skipped for now)
                    for (UiMessage um : selected) {
                        if (um == null || um.getMessage() == null)
                            continue;
                        Message m = um.getMessage();
                        if (m.getContent() instanceof TextMessage) {
                            sendTextMessage(new TextMessage(((TextMessage) m.getContent()).getContent()), null,
                                    targetConv);
                        } else if (m.getContent() instanceof ImageMessage) {
                            sendImageMessage((ImageMessage) m.getContent(), null, targetConv);
                        } else if (m.getContent() instanceof VoiceMessage) {
                            sendVoiceMessage((VoiceMessage) m.getContent(), targetConv);
                        } else if (m.getContent() instanceof FileMessage) {
                            sendFileMessage((FileMessage) m.getContent(), targetConv);
                        }
                    }
                }
                // clear selection state in fragment after forwarding
                frag.clearSelectionAfterForward();
            }
        } else if (requestCode == REQ_MENTION) {
            MessageListFragment frag = (MessageListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_messages_container);
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> newIds = data.getStringArrayListExtra(SELECTED_MEMBERS);
                ArrayList<String> newNames = data.getStringArrayListExtra(SELECTED_MEMBERS_NAME);
                frag.insertMention(newIds, newNames);
            } else {
                frag.showKeyboardIfNeed();
            }
        } else if (requestCode == REQ_LOCATION_PICKER && resultCode == RESULT_OK && data != null) {
            double lat = data.getDoubleExtra(LocationPickerActivity.EXTRA_LAT, 0);
            double lng = data.getDoubleExtra(LocationPickerActivity.EXTRA_LNG, 0);
            String address = data.getStringExtra(LocationPickerActivity.EXTRA_ADDRESS);
            if (address == null) address = "";
            
            // 发送为文本消息，格式 [LOCATION]lat,lng|address
            String content = com.juggle.im.android.chat.utils.LocationMessageHelper.buildContent(lat, lng, address);
            LogUtil.d("LocationMessage", "Sending location: " + content + ", conversation: " + (conversation != null ? conversation.getConversationId() : "null"));
            TextMessage msg = new TextMessage(content);
            sendTextMessage(msg, null, conversation);
            
            // 自动滚动到底部
            MessageListFragment frag = (MessageListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_messages_container);
            if (frag != null && frag.getView() != null) {
                frag.getView().postDelayed(() -> frag.scrollToBottomIfNeeded(), 350);
            }
        }
        // 移除了通话相关的 ActivityResult 处理
    }

    private void scrollMessageListIfNeed(boolean panelVisible) {
        // find the fragment and notify it when a panel opens so it can scroll to bottom
        // if needed
        MessageListFragment frag = (MessageListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_messages_container);
        if (frag != null) {
            if (panelVisible) {
                frag.scrollToBottomIfNeeded();
            } else {
                // nothing special for hide currently
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void MessageTopEvent(MessageTopEvent event) {
        if (!event.getMessage().getConversation().getConversationId().equals(conversationId)) {
            return;
        }
        if (event.isTop()) {
            handleTopMessage(event.getMessage(), event.getUserInfo());
        } else {
            findViewById(R.id.layout_pin_message).setVisibility(GONE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void MessageUpdatedEvent(MessageUpdatedEvent event) {
        // New single message received via EventBus -> append to the end (newest)
        if (!event.getMessage().getConversation().getConversationId().equals(conversationId)) {
            return;
        }
        Message m = event.getMessage();
        MessageListFragment frag = (MessageListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_messages_container);
        if (frag != null) {
            frag.onNewMessage(m);
        }
        // tag message read
        Conversation conversation = new Conversation(
                isGroup ? Conversation.ConversationType.GROUP : Conversation.ConversationType.PRIVATE,
                conversationId);
        JIM.getInstance().getConversationManager().clearUnreadCount(conversation, null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void MessageReadUpdatedEvent(MessageReadUpdatedEvent event) {
        if (!event.getConversation().getConversationId().equals(conversationId)) {
            return;
        }
        MessageListFragment frag = (MessageListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_messages_container);
        if (frag != null) {
            List<Message> messages = JIM.getInstance().getMessageManager()
                    .getMessagesByMessageIds(event.getMessageIds());
            frag.onUpdateMessage(messages);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void GroupNameUpdatedEvent(GroupNameUpdatedEvent event) {
        // Update title if this is the group being viewed
        if (isGroup && event.getGroupId().equals(conversationId)) {
            TextView tvTitle = findViewById(R.id.tv_title);
            if (tvTitle != null) {
                tvTitle.setText(event.getNewGroupName());
            }
        }
    }

    private void handlePluginResult(String pluginId, String action, Object data) {
        if (pluginId.equals(ImagePlugin.ID)) {
            for (String url : (ArrayList<String>) data) {
                ImageMessage image = new ImageMessage();
                image.setHeight(600);
                image.setWidth(800);
                String fileUrl = FileUtils.convertContentUriToFile(this, url);
                image.setLocalPath(fileUrl);
                image.setThumbnailLocalPath(fileUrl);
                sendImageMessage(image, null, conversation);
            }
        } else if (pluginId.equals(CameraPlugin.ID)) {
            ImageMessage image = new ImageMessage();
            image.setHeight(600);
            image.setWidth(800);
            String fileUrl = FileUtils.convertContentUriToFile(this, data.toString());
            image.setLocalPath(fileUrl);
            image.setThumbnailLocalPath(fileUrl);
            sendImageMessage(image, null, conversation);
        } else if (pluginId.equals("location")) {
            Intent it = new Intent(this, LocationPickerActivity.class);
            startActivityForResult(it, REQ_LOCATION_PICKER);
        } else if (pluginId.equals("contact")) {

        } else if (pluginId.equals(FilePlugin.ID)) {
            // 通过 Uri 读取原始文件名（包含正确的视频/文件后缀）
            Uri uri = null;
            if (data instanceof Uri) {
                uri = (Uri) data;
            } else if (data != null) {
                uri = Uri.parse(data.toString());
            }
            if (uri == null) {
                LogUtil.w("ConversationActivity", "FilePlugin data is null, ignore.");
                return;
            }

            String originalName = getDisplayNameFromUri(this, uri);

            // 为临时文件选择合适的后缀，保证本地路径也带正确扩展名（如 .mp4）
            String ext = "";
            if (!TextUtils.isEmpty(originalName)) {
                int dotIndex = originalName.lastIndexOf(".");
                if (dotIndex > 0 && dotIndex < originalName.length() - 1) {
                    ext = originalName.substring(dotIndex); // 包含点，例如 ".mp4"
                }
            }
            String suffix = TextUtils.isEmpty(ext) ? "temp_file" : ext;

            String fileUrl = FileUtils.convertContentUriToFile(this, uri.toString(), suffix);
            FileMessage fileMessage = new FileMessage();
            File f = new File(fileUrl);
            fileMessage.setLocalPath(fileUrl);

            // 使用原始文件名作为消息展示名称，必要时做截断，但始终保留后缀
            String finalName = !TextUtils.isEmpty(originalName) ? originalName : f.getName();
            String baseName = finalName;
            String displayExt = "";
            int dotIndex = finalName.lastIndexOf(".");
            if (dotIndex > 0 && dotIndex < finalName.length() - 1) {
                baseName = finalName.substring(0, dotIndex);
                displayExt = finalName.substring(dotIndex); // 包含点，例如 ".mp4"
            }
            // 仅截断主文件名部分，避免丢失扩展名
            if (baseName.length() > 30) {
                baseName = baseName.substring(0, 30);
            }
            finalName = TextUtils.isEmpty(displayExt) ? baseName : (baseName + displayExt);

            fileMessage.setName(finalName);
            long size = f.length();
            fileMessage.setSize(size);
            sendFileMessage(fileMessage, conversation);
        } else if (pluginId.equals("favorite")) {
            @SuppressWarnings("unchecked")
            ArrayList<FavoriteItem> selected = (ArrayList<FavoriteItem>) data;
            if (selected == null || selected.isEmpty()) return;
            for (FavoriteItem item : selected) {
                if (FavoriteItem.TYPE_TEXT.equals(item.getType())) {
                    TextMessage tm = new TextMessage(item.getContent() != null ? item.getContent() : "");
                    sendTextMessage(tm, null, conversation);
                } else if (FavoriteItem.TYPE_IMAGE.equals(item.getType())) {
                    ImageMessage image = new ImageMessage();
                    image.setHeight(600);
                    image.setWidth(800);
                    
                    // 优先使用服务器 URL，其次使用本地路径
                    String url = item.getUrl();
                    String localPath = item.getLocalPath();
                    
                    if (url != null && !url.isEmpty()) {
                        // 使用服务器 URL
                        image.setUrl(url);
                        image.setThumbnailUrl(item.getThumbnailUrl());
                    } else if (localPath != null && !localPath.isEmpty()) {
                        // 使用本地路径
                        File imgFile = new File(localPath);
                        if (imgFile.exists()) {
                            image.setLocalPath(localPath);
                            image.setThumbnailLocalPath(localPath);
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                    
                    sendImageMessage(image, null, conversation);
                } else if (FavoriteItem.TYPE_FILE.equals(item.getType())) {
                    FileMessage fileMessage = new FileMessage();
                    
                    // 优先使用服务器 URL，其次使用本地路径
                    String url = item.getUrl();
                    String localPath = item.getLocalPath();
                    
                    if (url != null && !url.isEmpty()) {
                        // 使用服务器 URL
                        fileMessage.setUrl(url);
                    } else if (localPath != null && !localPath.isEmpty()) {
                        // 使用本地路径
                        File fileFile = new File(localPath);
                        if (fileFile.exists()) {
                            fileMessage.setLocalPath(localPath);
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                    
                    fileMessage.setName(item.getName());
                    fileMessage.setSize(item.getSize());
                    sendFileMessage(fileMessage, conversation);
                }
            }
        }
        // 移除了通话插件的处理逻辑
    }

    private void editTextMessage(String msgId, TextMessage msg, MessageOptions options, Conversation conversation) {
        JIM.getInstance().getMessageManager().updateMessage(msgId, msg, conversation,
                new IMessageManager.IMessageCallback() {
                    @Override
                    public void onSuccess(Message message) {
                        MessageListFragment frag = (MessageListFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.fragment_messages_container);
                        if (frag != null) {
                            frag.onUpdateMessage(Arrays.asList(message));
                        }
                    }

                    @Override
                    public void onError(int i) {
                        LogUtil.d("MessageListFragment", "update message failed: " + i);
                    }
                });
    }

    private void sendTextMessage(TextMessage text, MessageOptions options, Conversation conversation) {
        IMessageManager.ISendMessageCallback callback = new IMessageManager.ISendMessageCallback() {
            @Override
            public void onSuccess(Message message) {
                MessageListFragment frag = (MessageListFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_messages_container);
                if (frag != null) {
                    frag.onUpdateMessage(Arrays.asList(message));
                }
            }

            @Override
            public void onError(Message message, int errorCode) {
                LogUtil.i("TAG", "send message error: " + errorCode);
                MessageListFragment frag = (MessageListFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_messages_container);
                if (frag != null) {
                    frag.onUpdateMessage(Arrays.asList(message));
                }
            }
        };
        Message message = JIM.getInstance().getMessageManager().sendMessage(text, conversation, options, callback);
        MessageListFragment frag = (MessageListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_messages_container);
        if (frag != null) {
            frag.onNewMessage(message);
        }
    }

    private void sendImageMessage(ImageMessage image, MessageOptions options, Conversation conversation) {
        final MessageListFragment frag = (MessageListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_messages_container);
        IMessageManager.ISendMediaMessageCallback callback = new IMessageManager.ISendMediaMessageCallback() {
            @Override
            public void onProgress(int progress, Message message) {
                LogUtil.i("sendImageMessage", "onProgress: " + progress);
            }

            @Override
            public void onSuccess(Message message) {
                LogUtil.i("sendImageMessage", "send message success");
                frag.onUpdateMessage(Arrays.asList(message));
            }

            @Override
            public void onError(Message message, int errorCode) {
                LogUtil.i("sendImageMessage", "send message error: " + errorCode);
                frag.onUpdateMessage(Arrays.asList(message));
            }

            @Override
            public void onCancel(Message message) {
                LogUtil.i("sendImageMessage", "onCancel");
            }
        };
        Message message = JIM.getInstance().getMessageManager().sendMediaMessage(image, conversation, callback);
        LogUtil.i("TAG", "sendImageMessage msgId= " + message.getMessageId());
        if (frag != null) {
            frag.onNewMessage(message);
        }
    }

    public void sendFileMessage(FileMessage fileMessage, Conversation conversation) {
        MessageListFragment frag = (MessageListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_messages_container);
        IMessageManager.ISendMediaMessageCallback callback = new IMessageManager.ISendMediaMessageCallback() {
            @Override
            public void onProgress(int progress, Message message) {
                LogUtil.i("TAG", "onProgress");
            }

            @Override
            public void onSuccess(Message message) {
                LogUtil.i("TAG", "send message success");
                if (message != null && message.getContent() instanceof FileMessage) {
                    FileMessage fm = (FileMessage) message.getContent();
                    String url = fm.getUrl();
                    LogUtil.i("ConversationFile", "会话页发送视频/文件成功");
                }
                frag.onUpdateMessage(Arrays.asList(message));

            }

            @Override
            public void onError(Message message, int errorCode) {
                LogUtil.i("TAG", "send message error: " + errorCode);
                frag.onUpdateMessage(Arrays.asList(message));

            }

            @Override
            public void onCancel(Message message) {
                LogUtil.i("TAG", "onCancel");
            }
        };

        Message message = JIM.getInstance().getMessageManager().sendMediaMessage(fileMessage, conversation, callback);
        LogUtil.i("TAG", "after send, clientMsgNo is " + message.getClientMsgNo());
        frag.onNewMessage(message);
    }

    private void sendVoiceMessage(VoiceMessage voice, Conversation conversation) {
        LogUtil.i("TAG", "sendVoiceMessage: localPath=" + voice.getLocalPath() + ", duration=" + voice.getDuration());
        
        MessageListFragment frag = (MessageListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_messages_container);
        
        if (frag == null) {
            LogUtil.e("TAG", "ERROR: MessageListFragment is null");
            return;
        }
        
        IMessageManager.ISendMediaMessageCallback callback = new IMessageManager.ISendMediaMessageCallback() {
            @Override
            public void onProgress(int progress, Message message) {
                LogUtil.i("TAG", "onProgress: " + progress + "%");
            }

            @Override
            public void onSuccess(Message message) {
                LogUtil.i("TAG", "send message success: msgId=" + message.getMessageId());
                frag.onUpdateMessage(Arrays.asList(message));
            }

            @Override
            public void onError(Message message, int errorCode) {
                LogUtil.e("TAG", "send message error: errorCode=" + errorCode + ", msgId=" + message.getMessageId());
                frag.onUpdateMessage(Arrays.asList(message));
            }

            @Override
            public void onCancel(Message message) {
                LogUtil.i("TAG", "send message cancelled: msgId=" + message.getMessageId());
            }
        };
        
        try {
            Message message = JIM.getInstance().getMessageManager().sendMediaMessage(voice, conversation, callback);
            LogUtil.i("TAG", "after send, clientMsgNo=" + message.getClientMsgNo() + ", msgId=" + message.getMessageId());
            frag.onNewMessage(message);
        } catch (Exception e) {
            LogUtil.e("TAG", "sendMediaMessage exception", e);
            Toast.makeText(this, "发送语音失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void sendMergeMessage(List<UiMessage> forwardMsg, Conversation targetConv, String targetName) {
        List<MergeMessagePreviewUnit> previewList = new ArrayList<>();
        List<String> msgIds = new ArrayList<>();
        for (int i = 0; i < forwardMsg.size(); i++) {
            MergeMessagePreviewUnit unit = new MergeMessagePreviewUnit();
            unit.setPreviewContent(MessageUtils.getMessageSummary(this, forwardMsg.get(i).getMessage()));
            UserInfo userInfo = new UserInfo();
            userInfo.setUserId(forwardMsg.get(i).getSenderId());
            userInfo.setUserName(forwardMsg.get(i).getSenderName());
            unit.setSender(userInfo);
            previewList.add(unit);
            msgIds.add(forwardMsg.get(i).getMessageId());
        }
        MergeMessage merge = new MergeMessage(targetName, conversation, msgIds, previewList);
        MessageListFragment frag = (MessageListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_messages_container);
        Message m = JIM.getInstance().getMessageManager().sendMessage(merge, targetConv,
                new IMessageManager.ISendMessageCallback() {
                    @Override
                    public void onSuccess(Message message) {
                        frag.onUpdateMessage(Arrays.asList(message));
                    }

                    @Override
                    public void onError(Message message, int errorCode) {
                        LogUtil.i("TAG", "send message error: " + errorCode);
                        frag.onUpdateMessage(Arrays.asList(message));
                    }
                });
        frag.onNewMessage(m);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ChatInputActionBar inputBar = findViewById(R.id.input_bar);
        if (inputBar != null) {
            inputBar.onPluginRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hide keyboard properly by accessing the ChatInputActionBar component
        ChatInputActionBar inputBar = findViewById(R.id.input_bar);
        if (inputBar != null) {
            inputBar.hideKeyboard();
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void finish() {
        // Hide the keyboard before finishing the activity
        ChatInputActionBar inputBar = findViewById(R.id.input_bar);
        if (inputBar != null) {
            inputBar.hideKeyboard();
        }
        super.finish();
    }

    /**
     * 从 content Uri 中读取展示用文件名（DISPLAY_NAME），用于保留真实后缀（如 .mp4）
     */
    private String getDisplayNameFromUri(Context context, Uri uri) {
        String result = null;
        if (uri == null) return null;

        if ("content".equals(uri.getScheme())) {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            } catch (Exception e) {
                LogUtil.w("ConversationActivity", "query display name error", e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        if (TextUtils.isEmpty(result)) {
            String path = uri.getPath();
            if (!TextUtils.isEmpty(path)) {
                int cut = path.lastIndexOf('/');
                if (cut != -1) {
                    result = path.substring(cut + 1);
                }
            }
        }

        return result;
    }
}
