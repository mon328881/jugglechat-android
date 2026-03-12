package com.juggle.im.android.chat.component;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.juggle.im.JIM;
import com.juggle.im.android.R;
import com.juggle.im.android.chat.ConversationActivity;
import com.juggle.im.android.utils.AvatarUtils;
import com.juggle.im.model.Conversation;

import java.util.ArrayList;
import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.VH> {
    
    public static class UserInfoObj {
        public String userId;
        public String name;
        public String avatar;
        public boolean disabled;
        public int role;  // 0=成员, 1=群主, 2=管理员
        public boolean isCurrentUser;

        public UserInfoObj(boolean disabled) {
            this.disabled = disabled;
            this.role = 0;
            this.isCurrentUser = false;
        }

        public UserInfoObj() {
            this.role = 0;
            this.isCurrentUser = false;
        }

        public String getUserId() {
            return userId;
        }

        public String getName() {
            return name;
        }

        public String getAvatar() {
            return avatar;
        }

        public int getRole() {
            return role;
        }

        public boolean isCurrentUser() {
            return isCurrentUser;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public void setRole(int role) {
            this.role = role;
        }

        public void setIsCurrentUser(boolean isCurrentUser) {
            this.isCurrentUser = isCurrentUser;
        }
    }

    public final static String LIST_MODE_SELECT_MEMBER = "select_mem";
    public final static String LIST_MODE_ADD_FRIENDS = "add_friends";
    public final static String LIST_MODE_NORMAL = "normal";

    private List<UserInfoObj> items = new ArrayList<>();
    private String mode = LIST_MODE_NORMAL;
    private java.util.Map<String, Boolean> selectedMap = new java.util.HashMap<>();
    private OnSelectionChanged selectionChanged;

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<UserInfoObj> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setSelectionChangedListener(OnSelectionChanged l) {
        this.selectionChanged = l;
    }

    public void uncheckUser(String userId) {
        selectedMap.remove(userId);
        notifyDataSetChanged();
    }

    public interface OnSelectionChanged {
        void onSelectionChanged(UserInfoObj item, boolean selected);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        UserInfoObj f = items.get(position);
        
        // 设置昵称和角色
        String displayName = f.getName() != null ? f.getName() : f.getUserId();
        String roleText = "";
        if (f.isCurrentUser()) {
            roleText = " (我)";
        } else if (f.getRole() == 1) {
            roleText = " (群主)";
        } else if (f.getRole() == 2) {
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
            holder.tv.setText(spannable);
        } else {
            holder.tv.setText(displayName);
        }
        
        ImageView checkbox = holder.itemView.findViewById(R.id.iv_checkbox);
        AvatarUtils.loadAvatar(holder.iv, f.getAvatar(), f.getName());
        if (mode.equals(LIST_MODE_SELECT_MEMBER)) {
            checkbox.setVisibility(View.VISIBLE);
            if (!f.disabled) {
                holder.itemView.setOnClickListener(v -> {
                    boolean cur = selectedMap.containsKey(f.getUserId());
                    if (cur) selectedMap.remove(f.getUserId());
                    else selectedMap.put(f.getUserId(), true);
                    notifyItemChanged(position);
                    if (selectionChanged != null) selectionChanged.onSelectionChanged(f, !cur);
                });
                if (selectedMap.containsKey(f.getUserId())) {
                    checkbox.setImageResource(R.drawable.ic_checkbox_selected);
                } else {
                    checkbox.setImageResource(R.drawable.ic_checkbox_unselect);
                }
            } else {
                checkbox.setImageResource(R.drawable.ic_checkbox_disabled);
            }
        } else if (mode.equals(LIST_MODE_ADD_FRIENDS)) {
            checkbox.setVisibility(View.VISIBLE);
            checkbox.setImageResource(R.drawable.ic_add);
        }
        else {
            checkbox.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(v -> {
                // 如果是自己，不做任何操作
                if (f.isCurrentUser()) {
                    return;
                }
                // 否则发起对话
                Conversation convo = new Conversation(Conversation.ConversationType.PRIVATE, f.getUserId());
                JIM.getInstance().getConversationManager().clearUnreadCount(convo, null);
                Intent intent = ConversationActivity.intentFor(v.getContext(), f.getUserId(), false, f.getName());
                v.getContext().startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView iv;
        TextView tv;

        VH(@NonNull View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.iv_avatar);
            tv = itemView.findViewById(R.id.tv_nickname);
        }
    }
}
