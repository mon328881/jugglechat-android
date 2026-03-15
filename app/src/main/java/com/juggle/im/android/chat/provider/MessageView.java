package com.juggle.im.android.chat.provider;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.juggle.im.JIM;
import com.juggle.im.android.R;
import com.juggle.im.android.chat.utils.MessageUtils;
import com.juggle.im.android.model.ConfigUtils;
import com.juggle.im.android.model.UiMessage;
import com.juggle.im.android.utils.AvatarUtils;
import com.juggle.im.model.Message;
import com.juggle.im.model.UserInfo;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.TextUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Generic base for all message content views used by the adapter.
 * T is the UI wrapper type (UiMessage), K is the underlying SDK message content type.
 */
public abstract class MessageView<T extends UiMessage, K> extends RecyclerView.ViewHolder {
    public MessageView(ViewGroup container, int resId) {
        super(LayoutInflater.from(container.getContext()).inflate(resId, container, false));
    }

    public MessageView(@NonNull ViewGroup itemView) {
        super(itemView);
    }

    public abstract void bindItem(T message, K content, boolean isGroup);

    /**
     * Bind UI wrapper to the view.
     *
     * @param message  UiMessage wrapper
     * @param content  SDK message content
     * @param isGroup  whether the conversation is a group
     * @param itemView
     */
    final public void bind(T message, K content, boolean isGroup, View itemView) {
        ImageView ivAvatar = itemView.findViewById(R.id.image_avatar);
        if (ivAvatar != null) {
            UserInfo sendUser = JIM.getInstance().getUserInfoManager().getUserInfo(message.getSenderId());
            if (sendUser != null) {
                // 优先使用 IM 返回的用户资料；对于自己发送的消息，头像 URL 以 ConfigUtils.myAvatarUrl 为准，确保资料页更新后群聊/单聊头像立即生效
                String name = sendUser.getUserName();
                message.setSenderName(name);
                String portraitUrl = sendUser.getPortrait();
                if (message.getDirection() == com.juggle.im.model.Message.MessageDirection.SEND
                        && !TextUtils.isEmpty(ConfigUtils.myAvatarUrl)) {
                    portraitUrl = ConfigUtils.myAvatarUrl;
                }
                AvatarUtils.loadAvatar(ivAvatar, portraitUrl, name);
                TextView txSender = itemView.findViewById(R.id.text_sender_name);
                if (txSender != null) {
                    if (isGroup && message.getDirection() != com.juggle.im.model.Message.MessageDirection.SEND) {
                        txSender.setVisibility(VISIBLE);
                        String displayName = sendUser.getUserName();
                        String roleText = "";
                        
                        // 获取发送者的角色
                        int role = message.getSenderRole();
                        if (role == 1) {
                            roleText = " (群主)";
                        } else if (role == 2) {
                            roleText = " (管理员)";
                        }
                        
                        // 使用 SpannableString 为角色文本设置绿色
                        if (!roleText.isEmpty()) {
                            SpannableString spannable = new SpannableString(displayName + roleText);
                            spannable.setSpan(
                                    new ForegroundColorSpan(Color.parseColor("#4CAF50")),
                                    displayName.length(),
                                    spannable.length(),
                                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                            );
                            txSender.setText(spannable);
                        } else {
                            txSender.setText(displayName);
                        }
                    } else {
                        txSender.setVisibility(GONE);
                    }
                }
            } else {
                // 兜底：IM 本地暂时没有该用户资料时，使用本地缓存的昵称 / senderId 生成首字母头像
                String nameFallback;
                String urlFallback = null;
                if (message.getDirection() == com.juggle.im.model.Message.MessageDirection.SEND) {
                    // 自己发送的消息：优先使用 Login 时缓存的昵称和头像
                    nameFallback = !TextUtils.isEmpty(ConfigUtils.myName)
                            ? ConfigUtils.myName
                            : message.getSenderId();
                    urlFallback = ConfigUtils.myAvatarUrl;
                } else {
                    // 对端用户：至少用 senderId 做一个首字母头像，避免一直是系统默认图标
                    nameFallback = !TextUtils.isEmpty(message.getSenderName())
                            ? message.getSenderName()
                            : message.getSenderId();
                }
                AvatarUtils.loadAvatar(ivAvatar, urlFallback, nameFallback);

                TextView txSender = itemView.findViewById(R.id.text_sender_name);
                if (txSender != null) {
                    if (isGroup && message.getDirection() != com.juggle.im.model.Message.MessageDirection.SEND) {
                        txSender.setVisibility(VISIBLE);
                        txSender.setText(nameFallback);
                    } else {
                        txSender.setVisibility(GONE);
                    }
                }
            }
        }
        TextView vMsgTime = itemView.findViewById(R.id.msg_sent_time);
        if (message.getDirection() == com.juggle.im.model.Message.MessageDirection.SEND) {
            ProgressBar progressBar = itemView.findViewById(R.id.msg_send_status);
            ImageView errorView = itemView.findViewById(R.id.send_error);
            ViewGroup msgStatusContainer = itemView.findViewById(R.id.msg_status_container);
            if (progressBar != null) {
                if (message.getMessage().getState().getValue() == Message.MessageState.SENDING.getValue()
                        || message.getMessage().getState().getValue() == Message.MessageState.UPLOADING.getValue()) {
                    progressBar.setVisibility(VISIBLE);
                } else {
                    progressBar.setVisibility(GONE);
                }
            }
            if (errorView != null) {
                if (message.getMessage().getState().getValue() == Message.MessageState.FAIL.getValue()) {
                    errorView.setVisibility(VISIBLE);
                } else {
                    errorView.setVisibility(GONE);
                }
            }
            if (msgStatusContainer != null) {
                msgStatusContainer.setVisibility(VISIBLE);
                ImageView ivStatus = msgStatusContainer.findViewById(R.id.msg_read_status);
                // 已读
                if (message.getMessage().isHasRead()) {
                    ivStatus.setVisibility(VISIBLE);
                    ivStatus.setImageResource(R.drawable.ic_msg_read);
                }
                // 已发送
                else if (message.getMessage().getState().getValue() == Message.MessageState.SENT.getValue()) {
                    ivStatus.setVisibility(VISIBLE);
                    ivStatus.setImageResource(R.drawable.ic_msg_sent);
                }
                // 发送中或者失败
                else {
                    ivStatus.setVisibility(GONE);
                }
            }
        }
        if (vMsgTime != null) {
            String spanTimeTxt = message.getMessage().isEdit() ? "（已修改）" : "";
            if (message.getDirection() == Message.MessageDirection.SEND) {
                spanTimeTxt += MessageUtils.formatTimestamp(message.getMessage().getTimestamp());
            } else {
                spanTimeTxt = MessageUtils.formatTimestamp(message.getMessage().getTimestamp()) + spanTimeTxt;
            }
            vMsgTime.setText(spanTimeTxt);
        }
        this.bindItem(message, content, isGroup);
    }
}
