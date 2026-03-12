package com.juggle.im.android.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.juggle.im.android.R;
import com.juggle.im.android.server.beans.GroupMemberBean;
import com.juggle.im.android.utils.AvatarUtils;

import java.util.List;

/**
 * 选择管理员适配器
 */
public class SelectAdminAdapter extends BaseAdapter {
    private Context context;
    private List<GroupMemberBean> members;
    private int selectedPosition = -1;

    public SelectAdminAdapter(Context context, List<GroupMemberBean> members) {
        this.context = context;
        this.members = members;
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

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_select_admin, parent, false);
            holder = new ViewHolder();
            holder.radioButton = convertView.findViewById(R.id.rb_select);
            holder.ivAvatar = convertView.findViewById(R.id.iv_avatar);
            holder.tvNickname = convertView.findViewById(R.id.tv_nickname);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        GroupMemberBean member = members.get(position);
        
        // 设置单选框状态
        holder.radioButton.setChecked(position == selectedPosition);
        
        // 设置头像
        AvatarUtils.loadAvatar(holder.ivAvatar, member.getAvatar(), member.getNickname());
        
        // 设置昵称
        holder.tvNickname.setText(member.getNickname());

        return convertView;
    }

    private static class ViewHolder {
        RadioButton radioButton;
        ImageView ivAvatar;
        TextView tvNickname;
    }
}
