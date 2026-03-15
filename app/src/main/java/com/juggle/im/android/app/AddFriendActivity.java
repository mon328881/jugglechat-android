package com.juggle.im.android.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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
import com.juggle.im.android.server.beans.FriendBean;
import com.juggle.im.android.server.beans.FriendsListData;
import com.juggle.im.android.server.http.ApiCallback;
import com.juggle.im.android.server.http.ServiceManager;
import com.juggle.im.android.utils.AvatarUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddFriendActivity extends AppCompatActivity {
    private static final String PREF_NAME_PENDING_FRIEND = "moment_detail_prefs";
    private static final String PREF_KEY_PENDING_IDS = "pending_friend_request_ids";

    private EditText edtSearch;
    private TextView btnCancel;
    private RecyclerView rvResults;
    private ProgressBar progressBar;
    private SearchAdapter adapter;
    /** 当前用户的好友 user_id 集合，用于搜索结果显示「已添加」 */
    private final Set<String> friendIds = new HashSet<>();
    /** 已发送好友申请、待对方通过的 user_id 集合，用于显示「申请中」 */
    private final Set<String> pendingIds = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        edtSearch = findViewById(R.id.edt_search);
        btnCancel = findViewById(R.id.btn_cancel);
        rvResults = findViewById(R.id.rv_results);
        progressBar = findViewById(R.id.progress_bar);
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        adapter = new SearchAdapter(new ArrayList<>());
        adapter.setFriendIds(friendIds);
        adapter.setPendingIds(pendingIds);
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        rvResults.setAdapter(adapter);

        // 加载好友列表，用于搜索结果中区分「已添加」
        loadFriendIds();
        // 加载已发送申请的用户 ID（与动态详情页共用存储）
        pendingIds.addAll(getPendingFriendRequestIds(this));
        // 与服务器同步：已拒绝/已过期的申请从本地移除，避免一直显示「申请中」
        syncPendingFriendRequestsFromServer(this, () -> {
            pendingIds.clear();
            pendingIds.addAll(getPendingFriendRequestIds(AddFriendActivity.this));
            if (adapter != null) adapter.notifyDataSetChanged();
        });

        edtSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showKeyboard(edtSearch);
            }
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String t = s == null ? "" : s.toString();
                btnCancel.setVisibility(TextUtils.isEmpty(t) ? View.GONE : View.VISIBLE);
                // show a temporary search item at top
                adapter.setSearchPreview(t);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnCancel.setOnClickListener(v -> {
            edtSearch.setText("");
            edtSearch.clearFocus();
            hideKeyboard();
        });

        adapter.setOnSearchPreviewClick(keyword -> {
            // remove preview and perform search
            adapter.setSearchPreview(null);
            edtSearch.clearFocus();
            hideKeyboard();
            doSearch(keyword);
        });

        adapter.setOnItemClick(user -> {
            // 申请添加好友
            ServiceManager.getUserService().applyFriend(user.getUser_id(), new ApiCallback<FriendApplicationBean>() {
                @Override
                public void onSuccess(FriendApplicationBean data) {
                    String userId = user.getUser_id();
                    addPendingFriendRequest(AddFriendActivity.this, userId);
                    pendingIds.add(userId);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(AddFriendActivity.this, "申请已发送", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(int code, String message) {
                    Toast.makeText(AddFriendActivity.this, "失败: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private static SharedPreferences getPendingFriendPrefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREF_NAME_PENDING_FRIEND, Context.MODE_PRIVATE);
    }

    private static Set<String> getPendingFriendRequestIds(Context context) {
        Set<String> set = getPendingFriendPrefs(context).getStringSet(PREF_KEY_PENDING_IDS, null);
        return set != null ? new HashSet<>(set) : new HashSet<>();
    }

    private static void addPendingFriendRequest(Context context, String userId) {
        if (userId == null) return;
        SharedPreferences prefs = getPendingFriendPrefs(context);
        Set<String> set = new HashSet<>(prefs.getStringSet(PREF_KEY_PENDING_IDS, new HashSet<>()));
        set.add(userId);
        prefs.edit().putStringSet(PREF_KEY_PENDING_IDS, set).apply();
    }

    /**
     * 与服务器同步「我发出的」好友申请状态，将已拒绝(2)、已过期(3)从本地待通过列表中移除，
     * 这样添加联系人页和动态详情页会正确显示「添加好友」而非一直「申请中」。
     * @param context 上下文
     * @param onDone 同步完成回调（主线程）
     */
    public static void syncPendingFriendRequestsFromServer(Context context, Runnable onDone) {
        ServiceManager.getUserService().getFriendApplications(0, 100, new ApiCallback<FriendApplicationsData>() {
            @Override
            public void onSuccess(FriendApplicationsData data) {
                List<FriendApplicationBean> items = data != null ? data.getItems() : null;
                if (items == null || items.isEmpty()) {
                    if (onDone != null) runOnMain(onDone, context);
                    return;
                }
                Set<String> toRemove = new HashSet<>();
                for (FriendApplicationBean app : items) {
                    // 仅处理「我发起的」申请：已同意(1)、已拒绝(2)、已过期(3) 都从待通过列表移除
                    if (app.isSponsor() && app.getStatus() != 0) {
                        if (app.getUserInfo() != null && app.getUserInfo().getUser_id() != null) {
                            toRemove.add(app.getUserInfo().getUser_id());
                        }
                    }
                }
                if (!toRemove.isEmpty()) {
                    SharedPreferences prefs = getPendingFriendPrefs(context);
                    Set<String> set = new HashSet<>(prefs.getStringSet(PREF_KEY_PENDING_IDS, new HashSet<>()));
                    set.removeAll(toRemove);
                    prefs.edit().putStringSet(PREF_KEY_PENDING_IDS, set).apply();
                }
                if (onDone != null) runOnMain(onDone, context);
            }

            @Override
            public void onError(int code, String message) {
                if (onDone != null) runOnMain(onDone, context);
            }
        });
    }

    private static void runOnMain(Runnable r, Context context) {
        if (r == null || context == null) return;
        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        handler.post(r);
    }

    private void loadFriendIds() {
        ServiceManager.getUserService().getFriendsList(1, 50, null, new ApiCallback<FriendsListData>() {
            @Override
            public void onSuccess(FriendsListData data) {
                if (data != null && data.getItems() != null) {
                    friendIds.clear();
                    for (FriendBean f : data.getItems()) {
                        if (f != null && f.getUser_id() != null) friendIds.add(f.getUser_id());
                    }
                    if (adapter != null) adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(int code, String message) {}
        });
    }

    private void doSearch(String keyword) {
        if (TextUtils.isEmpty(keyword)) return;
        progressBar.setVisibility(View.VISIBLE);
        ServiceManager.getUserService().searchUsers(keyword, new ApiCallback<FriendsListData>() {
            @Override
            public void onSuccess(FriendsListData data) {
                progressBar.setVisibility(View.GONE);
                List<FriendBean> items = data != null ? data.getItems() : null;
                adapter.setItems(items != null ? items : new ArrayList<>());
                if (items == null || items.isEmpty()) {
                    Toast.makeText(AddFriendActivity.this, "该用户不存在", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(int code, String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddFriendActivity.this, "搜索失败: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showKeyboard(View v) {
        v.post(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View v = getCurrentFocus();
        if (v == null) v = new View(this);
        if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    // simple adapter
    static class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_PREVIEW = 0;
        private static final int TYPE_ITEM = 1;
        private String preview;
        private List<FriendBean> items;
        private Set<String> friendIds;
        private Set<String> pendingIds;
        private OnSearchPreviewClick previewClick;
        private OnItemClick itemClick;

        interface OnSearchPreviewClick { void onClick(String keyword); }
        interface OnItemClick { void onClick(FriendBean user); }

        SearchAdapter(List<FriendBean> items) { this.items = items; }

        void setSearchPreview(String p) { this.preview = p; notifyDataSetChanged(); }
        void setItems(List<FriendBean> newItems) { this.items = newItems; notifyDataSetChanged(); }
        void setFriendIds(Set<String> ids) { this.friendIds = ids; }
        void setPendingIds(Set<String> ids) { this.pendingIds = ids; }
        void setOnSearchPreviewClick(OnSearchPreviewClick l) { this.previewClick = l; }
        void setOnItemClick(OnItemClick l) { this.itemClick = l; }

        @Override public int getItemViewType(int position) {
            if (preview != null && !preview.isEmpty()) {
                return position == 0 ? TYPE_PREVIEW : TYPE_ITEM;
            }
            return TYPE_ITEM;
        }

        @Override public int getItemCount() {
            int base = items == null ? 0 : items.size();
            return (preview != null && !preview.isEmpty()) ? base + 1 : base;
        }

        @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_PREVIEW) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_preview, parent, false);
                return new PreviewHolder(v);
            }
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
            return new ItemHolder(v);
        }

        @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == TYPE_PREVIEW) {
                PreviewHolder h = (PreviewHolder) holder;
                h.tv.setText("搜索: " + preview);
                h.itemView.setOnClickListener(v -> {
                    if (previewClick != null) previewClick.onClick(preview);
                });
                return;
            }
            int idx = (preview != null && !preview.isEmpty()) ? position - 1 : position;
            FriendBean user = items.get(idx);
            ItemHolder h = (ItemHolder) holder;
            h.tvName.setText(user.getNickname());
            AvatarUtils.loadAvatar(h.ivAvatar, user.getAvatar(), user.getNickname());
            String uid = user.getUser_id();
            boolean isFriend = friendIds != null && friendIds.contains(uid);
            boolean isPending = pendingIds != null && pendingIds.contains(uid);
            if (isFriend) {
                h.tvFriendAdd.setText(R.string.friend_app_status_added);
                h.tvFriendAdd.setEnabled(false);
                h.tvFriendAdd.setAlpha(0.7f);
                h.tvFriendAdd.setOnClickListener(null);
            } else if (isPending) {
                h.tvFriendAdd.setText(R.string.friend_app_status_applying);
                h.tvFriendAdd.setEnabled(false);
                h.tvFriendAdd.setAlpha(0.7f);
                h.tvFriendAdd.setOnClickListener(null);
            } else {
                h.tvFriendAdd.setText(R.string.add_friend);
                h.tvFriendAdd.setEnabled(true);
                h.tvFriendAdd.setAlpha(1f);
                h.tvFriendAdd.setOnClickListener(v -> { if (itemClick != null) itemClick.onClick(user); });
            }
        }

        static class PreviewHolder extends RecyclerView.ViewHolder {
            TextView tv;
            PreviewHolder(@NonNull View v) { super(v); tv = v.findViewById(R.id.tv_preview); }
        }

        static class ItemHolder extends RecyclerView.ViewHolder {
            ImageView ivAvatar; TextView tvName, tvFriendAdd;
            ItemHolder(@NonNull View v) { super(v);
                ivAvatar = v.findViewById(R.id.iv_avatar);
                tvName = v.findViewById(R.id.tv_name);
                tvFriendAdd = v.findViewById(R.id.friend_add);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从其他页面返回时同步服务器状态，对方拒绝后这里会变为「添加好友」
        syncPendingFriendRequestsFromServer(this, () -> {
            pendingIds.clear();
            pendingIds.addAll(getPendingFriendRequestIds(AddFriendActivity.this));
            if (adapter != null) adapter.notifyDataSetChanged();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideKeyboard();
    }
}
