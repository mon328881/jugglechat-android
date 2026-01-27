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
import com.juggle.im.android.server.beans.FriendApplicationBean;
import com.juggle.im.android.server.beans.FriendApplicationsData;
import com.juggle.im.android.server.http.ApiCallback;
import com.juggle.im.android.server.http.ServiceManager;
import com.juggle.im.android.utils.AvatarUtils;

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

        loadApplications();
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
                holder.tvDescription.setText("你申请了");
            } else {
                holder.tvDescription.setText("申请添加你");
            }

            // 根据状态码设置状态文本
            // 0: 申请中, 1: 已同意, 2: 已拒绝, 3: 已过期
            String statusText;
            switch (app.getStatus()) {
                case 1:
                    statusText = "已添加";
                    break;
                case 2:
                    statusText = "已拒绝";
                    break;
                case 3:
                    statusText = "已过期";
                    break;
                case 0:
                default:
                    statusText = "申请中";
                    break;
            }
            holder.tvStatus.setText(statusText);
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

            ViewHolder(@NonNull View v) {
                super(v);
                ivAvatar = v.findViewById(R.id.iv_avatar);
                tvNickname = v.findViewById(R.id.tv_nickname);
                tvDescription = v.findViewById(R.id.tv_description);
                tvStatus = v.findViewById(R.id.tv_status);
            }
        }
    }
}
