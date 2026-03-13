package com.juggle.im.android.chat.provider;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.juggle.im.android.R;
import com.juggle.im.android.chat.LocationPreviewActivity;
import com.juggle.im.android.chat.utils.LocationMessageHelper;
import com.juggle.im.android.model.UiMessage;
import com.juggle.im.model.Message;
import com.juggle.im.model.messages.TextMessage;

/**
 * Text message content view. Also renders [LOCATION] messages as a location card; click opens map / navigate.
 */
public class TextMessageView extends MessageView<UiMessage, TextMessage> {
    public TextMessageView(@NonNull ViewGroup root) {
        super(root, R.layout.content_text);
    }

    @Override
    public void bindItem(UiMessage m, TextMessage t, boolean isGroup) {
        try {
            String content = t.getContent();
            TextView tvContent = this.itemView.findViewById(R.id.text_message_content);
            View locationCard = this.itemView.findViewById(R.id.location_card);

            if (locationCard == null) {
                // 如果 location_card 不存在，直接显示文本
                if (tvContent != null) {
                    tvContent.setVisibility(View.VISIBLE);
                    tvContent.setText(content);
                }
                return;
            }

            if (LocationMessageHelper.isLocationContent(content)) {
                double[] latLng = LocationMessageHelper.parseLatLng(content);
                String address = LocationMessageHelper.parseAddress(content);

                if (address == null) address = "";

                tvContent.setVisibility(View.GONE);
                locationCard.setVisibility(View.VISIBLE);

                TextView tvAddress = locationCard.findViewById(R.id.location_address);

                if (tvAddress != null) {
                    if (address.isEmpty() && latLng != null)
                        address = String.format("%.6f, %.6f", latLng[0], latLng[1]);
                    tvAddress.setText(address);
                }

                // 位置卡片本身是可点击的（打开地图预览）。在 Android 事件分发中，长按不会自动冒泡到父 View，
                // 所以这里将长按转发给外层 itemView 的 long click（由消息列表统一弹出操作菜单）。
                locationCard.setLongClickable(true);
                locationCard.setOnLongClickListener(v -> {
                    // 获取 itemView 的父容器（RecyclerView 中的 item 容器）
                    View parent = (View) itemView.getParent();
                    if (parent != null) {
                        return parent.performLongClick();
                    }
                    return itemView.performLongClick();
                });

                if (latLng != null) {
                    final double lat = latLng[0];
                    final double lng = latLng[1];
                    final String addr = address;
                    locationCard.setTag(new double[]{lat, lng});
                    locationCard.setOnClickListener(v -> {
                        Intent intent = new Intent(v.getContext(), LocationPreviewActivity.class);
                        intent.putExtra(LocationPreviewActivity.EXTRA_LAT, lat);
                        intent.putExtra(LocationPreviewActivity.EXTRA_LNG, lng);
                        intent.putExtra(LocationPreviewActivity.EXTRA_ADDRESS, addr);

                        v.getContext().startActivity(intent);
                    });
                } else {
                    locationCard.setOnClickListener(null);
                }
            } else {
                tvContent.setVisibility(View.VISIBLE);
                locationCard.setVisibility(View.GONE);

                tvContent.setText(content);
                locationCard.setOnClickListener(null);
                locationCard.setOnLongClickListener(null);
            }
        } catch (Exception e) {
            android.util.Log.e("TextMessageView", "Error binding text message", e);
            TextView tvContent = this.itemView.findViewById(R.id.text_message_content);
            if (tvContent != null && t != null) {
                tvContent.setVisibility(View.VISIBLE);
                tvContent.setText(t.getContent());
            }
        }
    }
}
