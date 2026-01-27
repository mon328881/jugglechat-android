package com.juggle.im.android.chat.component;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.juggle.im.android.R;
import com.juggle.im.android.server.beans.GroupBean;
import com.juggle.im.android.utils.AvatarUtils;

import java.util.ArrayList;
import java.util.List;

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.GroupViewHolder> {
    private List<GroupBean> groups = new ArrayList<>();
    private OnGroupClickListener onGroupClickListener;

    public interface OnGroupClickListener {
        void onGroupClick(GroupBean group);
    }

    public void setOnGroupClickListener(OnGroupClickListener listener) {
        this.onGroupClickListener = listener;
    }

    public void setGroups(List<GroupBean> groups) {
        this.groups = groups != null ? groups : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        GroupBean group = groups.get(position);
        holder.bind(group);
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public class GroupViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivGroupAvatar;
        private TextView tvGroupName;
        private TextView tvGroupMemberCount;
        private TextView tvCreatorTag;  // 创建者标签

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGroupAvatar = itemView.findViewById(R.id.iv_group_avatar);
            tvGroupName = itemView.findViewById(R.id.tv_group_name);
            tvGroupMemberCount = itemView.findViewById(R.id.tv_group_member_count);
            tvCreatorTag = itemView.findViewById(R.id.tv_creator_tag);  // 获取创建者标签

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onGroupClickListener != null) {
                    onGroupClickListener.onGroupClick(groups.get(position));
                }
            });
        }

        public void bind(GroupBean group) {
            tvGroupName.setText(group.getGroup_name());
            // GroupBean 中没有成员数信息，暂时显示群组ID
            tvGroupMemberCount.setText("群组ID: " + group.getGroup_id());
            AvatarUtils.loadAvatar(ivGroupAvatar, group.getGroup_portrait(), group.getGroup_name());
            
            // 显示创建者标签
            if (tvCreatorTag != null) {
                if (group.isIs_creator()) {
                    tvCreatorTag.setVisibility(View.VISIBLE);
                    tvCreatorTag.setText("(我创建)");
                } else {
                    tvCreatorTag.setVisibility(View.GONE);
                }
            }
        }
    }
}
