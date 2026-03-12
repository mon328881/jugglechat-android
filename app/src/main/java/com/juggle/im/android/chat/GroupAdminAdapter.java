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
 * 群管理员列表适配器
 */
public class GroupAdminAdapter extends BaseAdapter {
    private Context context;
    private List<GroupMemberBean> admins;
    private OnAdminRemoveListener removeListener;

    public interface OnAdminRemoveListener {
        void onRemove(GroupMemberBean member);
    }

    public GroupAdminAdapter(Context context, List<GroupMemberBean> admins, OnAdminRemoveListener removeListener) {
        this.context = context;
        this.admins = admins;
        this.removeListener = removeListener;
    }

    @Override
    public int getCount() {
        return admins != null ? admins.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return admins != null ? admins.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_group_admin, parent, false);
            holder = new ViewHolder();
            holder.ivAvatar = convertView.findViewById(R.id.iv_avatar);
            holder.tvNickname = convertView.findViewById(R.id.tv_nickname);
            holder.btnRemove = convertView.findViewById(R.id.btn_remove);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        GroupMemberBean admin = admins.get(position);
        if (admin != null) {
            AvatarUtils.loadAvatar(holder.ivAvatar, admin.getAvatar(), admin.getNickname());
            holder.tvNickname.setText(admin.getNickname());
            holder.btnRemove.setOnClickListener(v -> {
                if (removeListener != null) {
                    removeListener.onRemove(admin);
                }
            });
        }

        return convertView;
    }

    private static class ViewHolder {
        ImageView ivAvatar;
        TextView tvNickname;
        ImageView btnRemove;
    }
}
