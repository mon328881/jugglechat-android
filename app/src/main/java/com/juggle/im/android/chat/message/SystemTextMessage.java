package com.juggle.im.android.chat.message;

import com.juggle.im.model.messages.TextMessage;

/**
 * 系统/广播文本消息封装，contentType 固定为 "jg:text"。
 * 服务端控制台发送的系统/广播文本消息使用 msg_type = "jg:text"，
 * 这里通过继承 TextMessage 复用展示和存储逻辑，仅覆盖类型字符串。
 */
public class SystemTextMessage extends TextMessage {
    public static final String CONTENT_TYPE = "jg:text";

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }
}

