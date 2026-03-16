package com.juggle.im.android.chat.message;

import com.juggle.im.model.messages.TextMessage;

/**
 * 系统/广播通知消息封装，contentType 固定为 "jg:notice"。
 * 服务端控制台发送的系统/广播通知消息使用 msg_type = "jg:notice"，
 * 这里同样复用 TextMessage 的实现，仅调整内容类型，便于前端识别与渲染。
 */
public class SystemNoticeMessage extends TextMessage {
    public static final String CONTENT_TYPE = "jg:notice";

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }
}

