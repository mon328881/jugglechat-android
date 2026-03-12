package com.juggle.im.android.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.juggle.im.android.R;
import com.juggle.im.android.server.beans.GroupMemberBean;
import com.juggle.im.android.utils.AvatarUtils;

import java.util.List;

/**
 * 群成员列表适配器
 */
public class GroupMemberAdapter extends BaseAdapter {
    private Context context;
    private List<GroupMemberBean> members;
    private OnMemberSelectedListener selectedListener;

    public interface OnMemberSelectedListener {
        void onSelected(GroupMemberBean member);
    }

    public GroupMemberAdapter(Context context, List<GroupMemberBean> members, OnMemberSelectedListener selectedListener) {
        this.context = context;
        this.members = members;
        this.selectedListener = selectedListener;
    }

    @Override
    public int getCount() {
        return members != null ? members.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return members != null ? members.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_group_member, parent, false);
            holder = new ViewHolder();
            holder.ivAvatar = convertView.findViewById(R.id.iv_avatar);
            holder.tvNickname = convertView.findViewById(R.id.tv_nickname);
            holder.tvRole = convertView.findViewById(R.id.tv_role);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        GroupMemberBean member = members.get(position);
        if (member != null) {
            AvatarUtils.loadAvatar(holder.ivAvatar, member.getAvatar(), member.getNickname());
            holder.tvNickname.setText(member.getNickname());
            holder.tvRole.setText(GroupRole.getRoleName(member.getRole()));
            
            convertView.setOnClickListener(v -> {
                if (selectedListener != null) {
                    selectedListener.onSelected(member);
                }
            });
        }

        return convertView;
    }

    private static class ViewHolder {
        ImageView ivAvatar;
        TextView tvNickname;
        TextView tvRole;
    }
}
