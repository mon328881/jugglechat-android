package com.juggle.im.android.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.juggle.im.android.R;
import com.juggle.im.android.event.FriendApplicationUpdateEvent;
import com.juggle.im.android.server.beans.FriendApplicationBean;
import com.juggle.im.android.server.beans.FriendApplicationsData;
import com.juggle.im.android.server.http.ApiCallback;
import com.juggle.im.android.server.http.ServiceManager;
import com.juggle.im.android.utils.AvatarUtils;
import com.juggle.im.JIM;
import com.juggle.im.model.Conversation;
import org.greenrobot.eventbus.EventBus;

import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class FriendApplicationsActivity extends AppCompatActivity {
    private RecyclerView rvApplications;
    private ProgressBar progressBar;
    private ApplicationsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_applications);

        rvApplications = findViewById(R.id.rv_applications);
        progressBar = findViewById(R.id.progress_bar);
        View btnBack = findViewById(R.id.btn_back);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        adapter = new ApplicationsAdapter(new ArrayList<>());
        rvApplications.setLayoutManager(new LinearLayoutManager(this));
        rvApplications.setAdapter(adapter);

        // 进入「新的朋友」页即视为已读，清除好友申请会话未读数，红点消失
        clearFriendApplyUnread();

        loadApplications();
    }

    /**
     * 清除好友申请系统会话的未读数，并通知底部通讯录与「新的朋友」右侧红点刷新。
     * 进入本页或通过/拒绝请求后调用。
     */
    void clearFriendApplyUnread() {
        Conversation friendApply = new Conversation(Conversation.ConversationType.SYSTEM, "friend_apply");
        JIM.getInstance().getConversationManager().clearUnreadCount(friendApply, null);
        EventBus.getDefault().post(new FriendApplicationUpdateEvent(0));
    }

    private void loadApplications() {
        progressBar.setVisibility(View.VISIBLE);
        ServiceManager.getUserService().getFriendApplications(0, 50, new ApiCallback<FriendApplicationsData>() {
            @Override
            public void onSuccess(FriendApplicationsData data) {
                progressBar.setVisibility(View.GONE);
                if (data != null && data.getItems() != null) {
                    adapter.setItems(data.getItems());
                }
            }

            @Override
            public void onError(int code, String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(FriendApplicationsActivity.this, "加载失败: " + message, Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    static class ApplicationsAdapter extends RecyclerView.Adapter<ApplicationsAdapter.ViewHolder> {
        private List<FriendApplicationBean> items;

        ApplicationsAdapter(List<FriendApplicationBean> items) {
            this.items = items;
        }

        void setItems(List<FriendApplicationBean> newItems) {
            this.items = newItems;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_application, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            FriendApplicationBean app = items.get(position);

            // 设置昵称
            if (app.getUserInfo() != null) {
                holder.tvNickname.setText(app.getUserInfo().getNickname());
                AvatarUtils.loadAvatar(holder.ivAvatar, app.getUserInfo().getAvatar(), app.getUserInfo().getNickname());
            }

            // 根据is_sponsor设置描述
            if (app.isSponsor()) {
                holder.tvDescription.setText(R.string.friend_app_you_applied);
            } else {
                holder.tvDescription.setText(R.string.friend_app_applied_to_add_you);
            }

            // 当状态为"申请中"(0)且用户不是发起者时，显示按钮
            if (app.getStatus() == 0 && !app.isSponsor()) {
                holder.llActions.setVisibility(View.VISIBLE);
                holder.tvStatus.setVisibility(View.GONE);
            } else {
                holder.llActions.setVisibility(View.GONE);
                holder.tvStatus.setVisibility(View.VISIBLE);

                // 根据状态码设置状态文本
                // 0: 申请中, 1: 已同意, 2: 已拒绝, 3: 已过期
                int statusRes;
                switch (app.getStatus()) {
                    case 1:
                        statusRes = R.string.friend_app_status_added;
                        break;
                    case 2:
                        statusRes = R.string.friend_app_status_rejected;
                        break;
                    case 3:
                        statusRes = R.string.friend_app_status_expired;
                        break;
                    case 0:
                    default:
                        statusRes = R.string.friend_app_status_applying;
                        break;
                }
                holder.tvStatus.setText(statusRes);
            }

            // 接受按钮点击处理
            holder.btnAccept.setOnClickListener(v -> {
                // 禁用按钮以避免重复点击
                holder.btnAccept.setEnabled(false);
                holder.btnReject.setEnabled(false);

                String sponsorId = app.getUserInfo() != null ? app.getUserInfo().getUser_id() : null;
                ServiceManager.getUserService().confirmFriend(sponsorId, true, new ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        Toast.makeText(holder.itemView.getContext(),
                                R.string.friend_app_operation_success, Toast.LENGTH_SHORT).show();
                        // 更新本地状态并刷新
                        app.setStatus(1);
                        notifyItemChanged(holder.getAdapterPosition());
                        // 通过后清除好友申请未读并刷新红点
                        if (holder.itemView.getContext() instanceof FriendApplicationsActivity) {
                            ((FriendApplicationsActivity) holder.itemView.getContext()).clearFriendApplyUnread();
                        }
                    }

                    @Override
                    public void onError(int code, String message) {
                        Toast.makeText(holder.itemView.getContext(),
                                holder.itemView.getContext().getString(
                                        R.string.friend_app_operation_failed, message),
                                Toast.LENGTH_SHORT).show();
                        holder.btnAccept.setEnabled(true);
                        holder.btnReject.setEnabled(true);
                    }
                });
            });

            // 拒绝按钮点击处理
            holder.btnReject.setOnClickListener(v -> {
                holder.btnAccept.setEnabled(false);
                holder.btnReject.setEnabled(false);

                String sponsorId = app.getUserInfo() != null ? app.getUserInfo().getUser_id() : null;
                ServiceManager.getUserService().confirmFriend(sponsorId, false, new ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        Toast.makeText(holder.itemView.getContext(),
                                R.string.friend_app_operation_success, Toast.LENGTH_SHORT).show();
                        app.setStatus(2);
                        notifyItemChanged(holder.getAdapterPosition());
                        // 拒绝后清除好友申请未读并刷新红点
                        if (holder.itemView.getContext() instanceof FriendApplicationsActivity) {
                            ((FriendApplicationsActivity) holder.itemView.getContext()).clearFriendApplyUnread();
                        }
                    }

                    @Override
                    public void onError(int code, String message) {
                        Toast.makeText(holder.itemView.getContext(),
                                holder.itemView.getContext().getString(
                                        R.string.friend_app_operation_failed, message),
                                Toast.LENGTH_SHORT).show();
                        holder.btnAccept.setEnabled(true);
                        holder.btnReject.setEnabled(true);
                    }
                });
            });
        }

        @Override
        public int getItemCount() {
            return items == null ? 0 : items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivAvatar;
            TextView tvNickname;
            TextView tvDescription;
            TextView tvStatus;
            LinearLayout llActions;
            Button btnAccept;
            Button btnReject;

            ViewHolder(@NonNull View v) {
                super(v);
                ivAvatar = v.findViewById(R.id.iv_avatar);
                tvNickname = v.findViewById(R.id.tv_nickname);
                tvDescription = v.findViewById(R.id.tv_description);
                tvStatus = v.findViewById(R.id.tv_status);
                llActions = v.findViewById(R.id.ll_actions);
                btnAccept = v.findViewById(R.id.btn_accept);
                btnReject = v.findViewById(R.id.btn_reject);
            }
        }
    }
}
