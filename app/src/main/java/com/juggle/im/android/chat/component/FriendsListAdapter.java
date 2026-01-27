package com.juggle.im.android.chat.component;

import android.content.Intent;
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

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FriendsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_FRIEND = 1;

    public static class FriendItem {
        public String userId;
        public String name;
        public String avatar;
        public String firstLetter; // 首字母
        public boolean isHeader; // 是否是分类头

        public FriendItem(boolean isHeader) {
            this.isHeader = isHeader;
        }

        public FriendItem() {
            this.isHeader = false;
        }
    }

    private List<FriendItem> items = new ArrayList<>();

    public void setFriends(List<FriendItem> friends) {
        items.clear();
        if (friends == null || friends.isEmpty()) {
            notifyDataSetChanged();
            return;
        }

        // 为每个好友计算首字母
        for (FriendItem friend : friends) {
            friend.firstLetter = getFirstLetter(friend.name);
        }

        // 按首字母排序
        Collections.sort(friends, new Comparator<FriendItem>() {
            @Override
            public int compare(FriendItem o1, FriendItem o2) {
                return o1.firstLetter.compareTo(o2.firstLetter);
            }
        });

        // 添加分类头
        String currentLetter = "";
        for (FriendItem friend : friends) {
            if (!friend.firstLetter.equals(currentLetter)) {
                currentLetter = friend.firstLetter;
                FriendItem header = new FriendItem(true);
                header.firstLetter = currentLetter;
                items.add(header);
            }
            items.add(friend);
        }

        notifyDataSetChanged();
    }

    // 获取首字母
    private String getFirstLetter(String name) {
        if (name == null || name.isEmpty()) {
            return "#";
        }

        char firstChar = name.charAt(0);
        
        // 如果是英文字母
        if ((firstChar >= 'a' && firstChar <= 'z') || (firstChar >= 'A' && firstChar <= 'Z')) {
            return String.valueOf(firstChar).toUpperCase();
        }

        // 如果是中文，转换为拼音
        try {
            HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
            format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
            format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
            format.setVCharType(HanyuPinyinVCharType.WITH_V);
            
            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(firstChar, format);
            if (pinyinArray != null && pinyinArray.length > 0) {
                return String.valueOf(pinyinArray[0].charAt(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "#";
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).isHeader ? TYPE_HEADER : TYPE_FRIEND;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_friend_header, parent, false);
            return new HeaderVH(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_friend, parent, false);
            return new FriendVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FriendItem item = items.get(position);
        
        if (holder instanceof HeaderVH) {
            ((HeaderVH) holder).tvHeader.setText(item.firstLetter);
        } else if (holder instanceof FriendVH) {
            FriendVH friendHolder = (FriendVH) holder;
            friendHolder.tvName.setText(item.name != null ? item.name : item.userId);
            AvatarUtils.loadAvatar(friendHolder.ivAvatar, item.avatar, item.name);
            
            // 隐藏复选框
            friendHolder.ivCheckbox.setVisibility(View.GONE);
            
            // 点击跳转到聊天页面
            friendHolder.itemView.setOnClickListener(v -> {
                Conversation convo = new Conversation(Conversation.ConversationType.PRIVATE, item.userId);
                JIM.getInstance().getConversationManager().clearUnreadCount(convo, null);
                Intent intent = ConversationActivity.intentFor(v.getContext(), item.userId, false, item.name);
                v.getContext().startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView tvHeader;

        HeaderVH(@NonNull View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tv_header);
        }
    }

    static class FriendVH extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName;
        ImageView ivCheckbox;

        FriendVH(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvName = itemView.findViewById(R.id.tv_nickname);
            ivCheckbox = itemView.findViewById(R.id.iv_checkbox);
        }
    }
}
