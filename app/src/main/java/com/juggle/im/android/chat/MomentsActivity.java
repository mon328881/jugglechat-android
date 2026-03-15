package com.juggle.im.android.chat;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import com.juggle.im.android.utils.LogUtil;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.juggle.im.JIM;
import com.juggle.im.android.R;
import com.juggle.im.android.model.ConfigUtils;
import com.juggle.im.android.server.beans.ImageBean;
import com.juggle.im.android.server.beans.PostBean;
import com.juggle.im.android.server.beans.PostsListData;
import com.juggle.im.android.server.beans.TopCommentBean;
import com.juggle.im.android.server.http.ApiCallback;
import com.juggle.im.android.server.http.ServiceManager;
import com.juggle.im.android.utils.AvatarUtils;
import com.juggle.im.android.chat.utils.FileUtils;

import android.widget.GridLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Moments page. Collapsing cover image fills status bar area. When scrolled past cover, title bar shows.
 * Simple RecyclerView feed and a comment input anchored above keyboard.
 */
public class MomentsActivity extends AppCompatActivity {

    private AppBarLayout appBarLayout;
    private com.google.android.material.appbar.CollapsingToolbarLayout collapsingContainer;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View commentBar;
    private EditText editTextField;
    private static Gson gson = new Gson();
    private PostBean selectedPost = null;
    private TopCommentBean selectedTopComment = null;
    private MomentsAdapter adapter;
    private TextView tvName;
    private ImageView ivAvatar;

    // 分页相关变量
    private int currentPage = 0;
    private int pageSize = 20;
    private boolean isLoading = false;
    private boolean hasMore = true;

    // 拍照相关变量
    private static final int REQUEST_CODE_CHOOSE_PHOTO = 1001;
    private static final int REQUEST_CODE_TAKE_PHOTO = 1002;
    private static final int REQUEST_CODE_CREATE_POST = 1003;
    private Uri photoUri;
    private int currentPaddingBottom;

    // 标签切换相关变量
    private String mCurrentPage = "moments"; // "moments" 或 "community"
    // 当前选中的社区标签（null 或空表示不过滤，展示所有社区贴）
    private String mSelectedCommunityTag = null;

    protected static class CommentDetail {
        String content;
        String type; //jm:text

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moments);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(""); // 移除顶部标题
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // 初始化标签切换
        TextView tabMoments = findViewById(R.id.tab_moments);
        TextView tabCommunity = findViewById(R.id.tab_community);

        if (tabMoments != null && tabCommunity != null) {
            // 设置朋友圈标签点击事件
            tabMoments.setOnClickListener(v -> switchToMoments(tabMoments, tabCommunity));

            // 设置社区标签点击事件
            tabCommunity.setOnClickListener(v -> switchToCommunity(tabMoments, tabCommunity));
        }

        appBarLayout = findViewById(R.id.appbar);
        collapsingContainer = findViewById(R.id.collapsing_container);
        recyclerView = findViewById(R.id.rv_moments);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        tvName = findViewById(R.id.tv_name);
        ivAvatar = findViewById(R.id.iv_avatar);
        
        // 从JIM SDK获取当前用户信息，而不是依赖ConfigUtils
        String currentUserId = JIM.getInstance().getCurrentUserId();
        String userName = ConfigUtils.myName;
        String userAvatar = ConfigUtils.myAvatarUrl;
        
        // 如果ConfigUtils中的值为空，尝试从服务器获取用户信息
        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(userAvatar)) {
            ServiceManager.getUserService().getUserInfo(currentUserId, new ApiCallback<com.juggle.im.android.server.beans.UserInfoBean>() {
                @Override
                public void onSuccess(com.juggle.im.android.server.beans.UserInfoBean data) {
                    if (data != null) {
                        String nickname = data.getNickname();
                        String avatar = data.getAvatar();
                        
                        // 更新ConfigUtils缓存
                        if (!TextUtils.isEmpty(nickname)) {
                            ConfigUtils.myName = nickname;
                        }
                        if (!TextUtils.isEmpty(avatar)) {
                            ConfigUtils.myAvatarUrl = avatar;
                        }
                        
                        // 更新UI
                        runOnUiThread(() -> {
                            tvName.setText(!TextUtils.isEmpty(nickname) ? nickname : "我");
                            AvatarUtils.loadAvatar(ivAvatar, avatar, nickname);
                        });
                    }
                }

                @Override
                public void onError(int code, String message) {
                    LogUtil.e("MomentsActivity", "获取用户信息失败: " + message);
                }
            });
        }
        
        // 先显示ConfigUtils中的值，如果为空则显示默认值
        tvName.setText(!TextUtils.isEmpty(userName) ? userName : "我");
        AvatarUtils.loadAvatar(ivAvatar, userAvatar, !TextUtils.isEmpty(userName) ? userName : "我");

        // 默认进入页面为朋友圈模式：使用单列 LinearLayoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemViewCacheSize(20);
        recyclerView.addItemDecoration(new SpacesItemDecoration(dpToPx(this, 4)));
        adapter = new MomentsAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // 修改获取评论输入框相关视图的代码
        commentBar = findViewById(R.id.comment_bar);
        editTextField = findViewById(R.id.edit_comment);

        findViewById(R.id.btn_camera).setOnClickListener(v -> {
            showCameraOptions();
        });
        findViewById(R.id.btn_camera).setOnLongClickListener(l -> {
            Intent it = new Intent(MomentsActivity.this, CreatePostActivity.class);
            it.putExtra("current_page", mCurrentPage);
            startActivityForResult(it, 100);
            return true;
        });

        // hide keyboard and comment when tapping content
        CoordinatorLayout root = findViewById(R.id.root_coordinator);
        root.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                hideCommentInput();
            }
            return false;
        });
        recyclerView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN ||
                    event.getAction() == MotionEvent.ACTION_UP) {
                hideCommentInput();
            }
            return false;
        });

        // show/hide toolbar title based on collapse
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean shown = false;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int total = appBarLayout.getTotalScrollRange();
                if (Math.abs(verticalOffset) >= total - 10) {
                    // Collapsed
                    if (!shown) {
                        shown = true;
                    }
                } else {
                    // Expanded
                    if (shown) {
                        toolbar.setTitle("");
                        shown = false;
                    }
                }
            }
        });

        // 设置下拉刷新监听器
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshMoments();
        });

        // 设置上拉加载更多（兼容 LinearLayoutManager 与 StaggeredGridLayoutManager）
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
                if (lm == null) return;

                int visibleItemCount = lm.getChildCount();
                int totalItemCount = lm.getItemCount();
                int lastVisibleItemPosition = 0;
                int firstVisibleItemPosition = 0;

                if (lm instanceof StaggeredGridLayoutManager) {
                    StaggeredGridLayoutManager sgm = (StaggeredGridLayoutManager) lm;
                    lastVisibleItemPosition = getLastVisibleItemPosition(sgm);
                    firstVisibleItemPosition = getFirstVisibleItemPosition(sgm);
                } else if (lm instanceof LinearLayoutManager) {
                    LinearLayoutManager llm = (LinearLayoutManager) lm;
                    lastVisibleItemPosition = llm.findLastVisibleItemPosition();
                    firstVisibleItemPosition = llm.findFirstVisibleItemPosition();
                } else {
                    return;
                }

                // 判断是否需要加载更多
                if (!isLoading
                        && hasMore
                        && visibleItemCount > 0
                        && lastVisibleItemPosition >= totalItemCount - 1
                        && firstVisibleItemPosition >= 0) {
                    loadMoreMoments();
                }
            }
        });
        currentPaddingBottom = recyclerView.getPaddingBottom();


        adapter.setListener(new Listener() {
            @Override
            public void onComment(int position, PostBean post, TopCommentBean topCommentBean) {
                if (topCommentBean != null && JIM.getInstance().getCurrentUserId().equals(topCommentBean.getUser_info().getUserId())) {
                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MomentsActivity.this);
                    View sheetView = LayoutInflater.from(MomentsActivity.this).inflate(R.layout.dialog_delete_comment, null);
                    bottomSheetDialog.setContentView(sheetView);
                    sheetView.findViewById(R.id.btn_delete).setOnClickListener(v -> {
                        bottomSheetDialog.dismiss();
                        List<String> commentIds = new ArrayList<>();
                        commentIds.add(post.getPost_id()); // Assuming post contains comment_id
                        ServiceManager.getMomentService().deleteComment(commentIds, new ApiCallback<Void>() {
                            @Override
                            public void onSuccess(Void data) {
                                refreshPostItem(post);
                            }

                            @Override
                            public void onError(int code, String message) {
                                runOnUiThread(() -> {
                                    // Handle error
                                    Toast.makeText(MomentsActivity.this, "Failed to delete comment: " + message, Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
                    });

                    sheetView.findViewById(R.id.btn_cancel).setOnClickListener(v -> bottomSheetDialog.dismiss());

                    bottomSheetDialog.show();
                } else {
                    showPostComment(position, post, topCommentBean);
                }
            }

            @Override
            public void onClickImage(int position, PostBean post, String imageUrl) {
                Intent it = new Intent(MomentsActivity.this, ImagePreviewActivity.class);
                ArrayList<String> urls = new ArrayList<>();
                int startIndex = 0;
                List<ImageBean> images = post.getContent().getImages();
                for (int i = 0; i < images.size(); i++) {
                    ImageBean imageBean = images.get(i);
                    urls.add(imageBean.getUrl());
                    if (imageBean.getUrl().equals(imageUrl)) {
                        startIndex = i;
                    }
                }
                it.putStringArrayListExtra(ImagePreviewActivity.EXTRA_IMAGE_URLS, urls);
                it.putExtra(ImagePreviewActivity.EXTRA_IMAGE_INDEX, startIndex);
                startActivity(it);
            }

            @Override
            public void onDeletePost(int position, PostBean post) {
                ServiceManager.getMomentService().deletePost(Arrays.asList(post.getPost_id()), new ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        adapter.notifyItemRemoved(position);
                    }

                    @Override
                    public void onError(int code, String message) {
                        Toast.makeText(MomentsActivity.this, "Failed to delete post: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        findViewById(R.id.btn_send_comment).setOnClickListener(v -> {
            if (selectedPost != null) {
                String commentText = editTextField.getText().toString().trim();
                if (!TextUtils.isEmpty(commentText)) {
                    Map<String, String> comment = new HashMap<>();
                    comment.put("content", commentText);
                    comment.put("type", "jg:text");
                    ServiceManager.getMomentService().addComment(
                            selectedPost.getPost_id(), // postId
                            selectedTopComment != null ? selectedTopComment.getParent_comment_id() : null, // parentCommentId (null for top-level comment)
                            selectedTopComment != null ? selectedTopComment.getUser_info().getUserId() : null, // parentUserId (null for top-level comment)
                            gson.toJson(comment), // text
                            new ApiCallback<Void>() {
                                @Override
                                public void onSuccess(Void data) {
                                    refreshPostItem(selectedPost);
                                    runOnUiThread(() -> {
                                        hideCommentInput();
                                        editTextField.setText(""); // Clear input field
                                    });
                                }

                                @Override
                                public void onError(int code, String message) {
                                    runOnUiThread(() -> {
                                        LogUtil.e("MomentsActivity", "Failed to add comment: " + message);
                                    });
                                }
                            }
                    );
                }
            }
        });

        // 初始加载数据
        swipeRefreshLayout.setRefreshing(true);
        loadMoments();
    }

    private void showCameraOptions() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.dialog_camera_options, null);
        bottomSheetDialog.setContentView(sheetView);

        sheetView.findViewById(R.id.btn_take_photo).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            takePhoto();
        });

        sheetView.findViewById(R.id.btn_choose_from_album).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            chooseFromAlbum();
        });

        sheetView.findViewById(R.id.btn_cancel).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void takePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "请授予相机权限", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            photoUri = FileUtils.createTmpImageFile(this);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(takePictureIntent, REQUEST_CODE_TAKE_PHOTO);
        }
    }

    private void chooseFromAlbum() {
        Intent intent = new Intent(this, AlbumActivity.class);
        startActivityForResult(intent, REQUEST_CODE_CHOOSE_PHOTO);
    }

    private void refreshMoments() {
        currentPage = 0;
        hasMore = true;
        loadMoments();
    }

    private void loadMoreMoments() {
        if (!hasMore) return;
        isLoading = true;
        loadMoments();
    }

    private void refreshPostItem(PostBean postBean) {
        ServiceManager.getMomentService().getPost(postBean.getPost_id(), new ApiCallback<PostBean>() {
            @Override
            public void onSuccess(PostBean data) {
                runOnUiThread(() -> {
                    int pos = adapter.getPositionById(data.getPost_id());
                    if (pos >= 0) {
                        adapter.items.set(pos, data);
                        adapter.notifyItemChanged(pos);
                    }
                });

            }

            @Override
            public void onError(int code, String message) {

            }
        });
    }

    private void likePost(int position, PostBean post) {
        // 检查当前用户是否已点赞
        String currentUserId = JIM.getInstance().getCurrentUserId();
        boolean hasLiked = false;
        if (post.getReactions() != null && post.getReactions().containsKey("like")) {
            for (com.juggle.im.android.server.beans.ReactionItem item : post.getReactions().get("like")) {
                if (item.getUser_info() != null && item.getUser_info().getUserId().equals(currentUserId)) {
                    hasLiked = true;
                    break;
                }
            }
        }
        
        if (hasLiked) {
            // 取消点赞
            ServiceManager.getMomentService().removeReaction(post.getPost_id(), "like", new ApiCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    refreshPostItem(post);
                }

                @Override
                public void onError(int code, String message) {
                    LogUtil.e("MomentsActivity", "Failed to remove reaction: " + message);
                }
            });
        } else {
            // 添加点赞
            ServiceManager.getMomentService().addReaction(post.getPost_id(), "like", "like", new ApiCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    refreshPostItem(post);
                }

                @Override
                public void onError(int code, String message) {
                    LogUtil.e("MomentsActivity", "Failed to add reaction: " + message);
                }
            });
        }
    }

    private void showPostComment(int position, PostBean post, TopCommentBean topCommentBean) {
        if (commentBar.getVisibility() == GONE) {
            selectedPost = post;
            selectedTopComment = topCommentBean;
            showCommentInput(position);
        }
        if (topCommentBean != null) {
            String hint = topCommentBean.getUser_info().getNickname();
            editTextField.setHint("回复 " + hint + ": ");
        }
    }

    private void showCommentInput(int position) {
        commentBar.setVisibility(VISIBLE);
        editTextField.requestFocus();

        // 监听布局变化以处理键盘弹出后的滚动定位（兼容 LinearLayoutManager 与 StaggeredGridLayoutManager）
        View rootView = findViewById(android.R.id.content);
        View.OnLayoutChangeListener layoutChangeListener = new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                // 移除监听器避免重复调用
                v.removeOnLayoutChangeListener(this);

                // 获取布局管理器
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (layoutManager == null) return;

                // 折叠AppBarLayout确保可见性
                appBarLayout.setExpanded(false, true);

                // 获取目标视图
                View targetView = layoutManager.findViewByPosition(position);
                if (targetView == null) {
                    // 如果目标视图不可见，先滚动到目标位置
                    recyclerView.smoothScrollToPosition(position);
                    // 添加延时处理，确保滚动完成后再进行精确调整
                    recyclerView.postDelayed(() -> {
                        View newTargetView = layoutManager.findViewByPosition(position);
                        if (newTargetView != null) {
                            adjustScrollPosition(newTargetView, layoutManager, position);
                        }
                    }, 300);
                    return;
                }

                // 调整滚动位置
                adjustScrollPosition(targetView, layoutManager, position);
            }

            private void adjustScrollPosition(View targetView, RecyclerView.LayoutManager layoutManager, int position) {
                // 计算键盘高度
                int screenHeight = getResources().getDisplayMetrics().heightPixels;
                int rootViewHeight = rootView.getHeight();
                int keyboardHeight = screenHeight - rootViewHeight;

                // 计算目标视图在屏幕中的位置
                int[] location = new int[2];
                targetView.getLocationInWindow(location);
                int targetTop = location[1];
                int targetBottom = targetTop + targetView.getHeight();

                // 计算需要滚动的距离
                int scrollDistance = 0;

                if (keyboardHeight > 0) {
                    // 键盘可见，计算目标视图与键盘顶部的距离
                    int visibleAreaBottom = screenHeight - keyboardHeight;
                    // 考虑EditText的高度，确保输入框不被遮挡
                    int editTextHeight = editTextField.getHeight();
                    int safeAreaBottom = visibleAreaBottom - editTextHeight - dpToPx(MomentsActivity.this, 10);

                    if (targetBottom > safeAreaBottom) {
                        // 目标视图被键盘遮挡，需要向上滚动
                        scrollDistance = targetBottom - safeAreaBottom;
                    }
                } else {
                    // 键盘高度无法确定时使用默认策略
                    int editTextHeight = editTextField.getHeight();
                    // 检查是否在底部
                    int totalItemCount = layoutManager.getItemCount();
                    int lastVisiblePosition;
                    if (layoutManager instanceof StaggeredGridLayoutManager) {
                        lastVisiblePosition = getLastVisibleItemPosition((StaggeredGridLayoutManager) layoutManager);
                    } else if (layoutManager instanceof LinearLayoutManager) {
                        lastVisiblePosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                    } else {
                        lastVisiblePosition = totalItemCount - 1;
                    }
                    boolean isAtBottom = (totalItemCount > 0) && (lastVisiblePosition >= totalItemCount - 1);

                    // 在底部时增加滚动距离确保可见
                    int extraScroll = isAtBottom ? editTextHeight * 3 : editTextHeight * 2;
                    scrollDistance = extraScroll;
                }

                // 执行滚动 TODO 执行无效，已经在最底部
                if (scrollDistance > 0) {
                    recyclerView.smoothScrollBy(0, scrollDistance);
                }
            }
        };

        // 添加布局变化监听器

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            // 添加小延迟确保视图完全布局后再显示键盘
            editTextField.postDelayed(() -> {
                rootView.addOnLayoutChangeListener(layoutChangeListener);
                imm.showSoftInput(editTextField, InputMethodManager.SHOW_IMPLICIT);
            }, 100);
        }
    }

    private void hideCommentInput() {
        if (commentBar.getVisibility() == VISIBLE) {
            editTextField.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(editTextField.getWindowToken(), 0);
            commentBar.setVisibility(View.GONE);
        }
    }

    private void loadMoments() {
        // fetch posts from server and populate adapter
        ServiceManager.getMomentService().getPosts(null, pageSize, currentPage * pageSize, new ApiCallback<PostsListData>() {
            @Override
            public void onSuccess(PostsListData data) {
                runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    isLoading = false;

                    if (data != null && data.getItems() != null) {
                        // Filter posts based on current page (moments vs community)
                        List<PostBean> filteredItems = filterPostsByMode(data.getItems());
                        
                        if (currentPage == 0) {
                            // 下拉刷新，替换所有数据
                            adapter.setItems(filteredItems);
                        } else {
                            // 上拉加载更多，追加数据
                            adapter.addItems(filteredItems);
                        }

                        // 更新分页参数
                        if (data.getItems().size() < pageSize) {
                            hasMore = false; // 没有更多数据了
                        } else {
                            currentPage++;
                        }
                    } else if (currentPage == 0) {
                        // 第一页就没有数据，清空列表
                        adapter.setItems(new ArrayList<>());
                        hasMore = false;
                    }
                });
            }

            @Override
            public void onError(int code, String message) {
                LogUtil.e("MomentsActivity", "Failed to loadMoments: " + message);
                runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    isLoading = false;
                    Toast.makeText(MomentsActivity.this, "加载失败: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Filter posts based on current mode (moments vs community)
     * - moments mode: show only posts WITHOUT community_info (or with null community_info)
     * - community mode: show only posts WITH community_info
     */
    private List<PostBean> filterPostsByMode(List<PostBean> allPosts) {
        List<PostBean> filtered = new ArrayList<>();
        
        for (PostBean post : allPosts) {
            boolean hasCommunityInfo = post.getCommunity_info() != null;

            if ("community".equals(mCurrentPage)) {
                // 社区模式：只展示有 community_info 的帖子，并根据当前选中的标签过滤
                if (hasCommunityInfo) {
                    if (mSelectedCommunityTag == null || mSelectedCommunityTag.isEmpty()) {
                        // 未选中特定标签，展示所有社区帖子
                        filtered.add(post);
                    } else if (post.getCommunity_info().getTags() != null
                            && post.getCommunity_info().getTags().contains(mSelectedCommunityTag)) {
                        // 只展示包含当前标签的帖子
                        filtered.add(post);
                    }
                }
            } else {
                // 朋友圈模式：只展示没有 community_info 的帖子
                if (!hasCommunityInfo) {
                    filtered.add(post);
                }
            }
        }

        return filtered;
    }

    private int dpToPx(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    private int getFirstVisibleItemPosition(StaggeredGridLayoutManager layoutManager) {
        int[] into = layoutManager.findFirstVisibleItemPositions(null);
        int min = Integer.MAX_VALUE;
        if (into != null) {
            for (int value : into) {
                if (value < min) {
                    min = value;
                }
            }
        }
        return min == Integer.MAX_VALUE ? 0 : min;
    }

    private int getLastVisibleItemPosition(StaggeredGridLayoutManager layoutManager) {
        int[] into = layoutManager.findLastVisibleItemPositions(null);
        int max = Integer.MIN_VALUE;
        if (into != null) {
            for (int value : into) {
                if (value > max) {
                    max = value;
                }
            }
        }
        return max == Integer.MIN_VALUE ? 0 : max;
    }

    // 切换到朋友圈模式
    private void switchToMoments(TextView tabMoments, TextView tabCommunity) {
        if ("moments".equals(mCurrentPage)) {
            return; // 已经在朋友圈模式
        }
        mCurrentPage = "moments";

        // 更新标签样式
        tabMoments.setTypeface(null, android.graphics.Typeface.BOLD);
        tabMoments.setAlpha(1.0f);
        tabCommunity.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabCommunity.setAlpha(0.6f);

        // 更新红线指示器
        View underlineMoments = findViewById(R.id.underline_moments);
        View underlineCommunity = findViewById(R.id.underline_community);
        if (underlineMoments != null) {
            underlineMoments.setVisibility(View.VISIBLE);
        }
        if (underlineCommunity != null) {
            underlineCommunity.setVisibility(View.GONE);
        }

        // 隐藏社区标签栏
        HorizontalScrollView communityTagsScroll = findViewById(R.id.community_tags_scroll);
        if (communityTagsScroll != null) {
            communityTagsScroll.setVisibility(View.GONE);
        }

        // 显示顶部大封面区域（朋友圈模式使用 Collapsing）
        if (collapsingContainer != null) {
            collapsingContainer.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams lp = collapsingContainer.getLayoutParams();
            lp.height = dpToPx(this, 250);
            collapsingContainer.setLayoutParams(lp);
        }
        View cover = findViewById(R.id.header_cover_image);
        View headerUser = findViewById(R.id.header_user_container);
        if (cover != null) cover.setVisibility(View.VISIBLE);
        if (headerUser != null) headerUser.setVisibility(View.VISIBLE);

        // 使用单列列表布局
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 刷新数据（加载朋友圈内容）
        refreshMoments();
    }

    // 切换到社区模式
    private void switchToCommunity(TextView tabMoments, TextView tabCommunity) {
        if ("community".equals(mCurrentPage)) {
            return; // 已经在社区模式
        }
        mCurrentPage = "community";

        // 更新标签样式
        tabMoments.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabMoments.setAlpha(0.6f);
        tabCommunity.setTypeface(null, android.graphics.Typeface.BOLD);
        tabCommunity.setAlpha(1.0f);

        // 更新红线指示器
        View underlineMoments = findViewById(R.id.underline_moments);
        View underlineCommunity = findViewById(R.id.underline_community);
        if (underlineMoments != null) {
            underlineMoments.setVisibility(View.GONE);
        }
        if (underlineCommunity != null) {
            underlineCommunity.setVisibility(View.VISIBLE);
        }

        // 显示社区标签栏
        HorizontalScrollView communityTagsScroll = findViewById(R.id.community_tags_scroll);
        if (communityTagsScroll != null) {
            communityTagsScroll.setVisibility(View.VISIBLE);
            generateCommunityTags();
        }

        // 社区模式使用两列瀑布流布局
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        // 收起顶部大封面区域，只保留折叠体系中的 Toolbar + 社区标签栏
        if (collapsingContainer != null) {
            ViewGroup.LayoutParams lp = collapsingContainer.getLayoutParams();
            lp.height = dpToPx(this, 96); // 约等于 Toolbar(48dp) + 标签栏(48dp)
            collapsingContainer.setLayoutParams(lp);
        }
        View cover = findViewById(R.id.header_cover_image);
        View headerUser = findViewById(R.id.header_user_container);
        if (cover != null) cover.setVisibility(View.GONE);
        if (headerUser != null) headerUser.setVisibility(View.GONE);

        // 刷新数据（加载社区内容）
        refreshMoments();
    }

    /**
     * 生成社区标签
     */
    private void generateCommunityTags() {
        LinearLayout tagsContainer = findViewById(R.id.community_tags_container);
        if (tagsContainer == null) return;

        // 清空之前的标签
        tagsContainer.removeAllViews();

        // 从后端获取社区标签列表
        ServiceManager.getMomentService().getCommunityTags(new ApiCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> tagList) {
                runOnUiThread(() -> {
                    if (tagList != null && !tagList.isEmpty()) {
                        // 在服务端返回的标签前面加上一个“全部”标签
                        List<String> allTags = new ArrayList<>();
                        allTags.add("全部");
                        allTags.addAll(tagList);
                        String[] tags = allTags.toArray(new String[0]);
                        createTagViews(tagsContainer, tags);
                    } else {
                        // 如果后端返回空列表，使用默认标签（第一个为“全部”）
                        String[] defaultTags = {"全部", "推荐", "直播", "短剧", "美食", "穿搭", "旅行"};
                        createTagViews(tagsContainer, defaultTags);
                    }
                });
            }

            @Override
            public void onError(int code, String message) {
                LogUtil.e("MomentsActivity", "获取社区标签失败: " + message);
                runOnUiThread(() -> {
                    // 获取失败时使用默认标签（第一个为“全部”）
                    String[] defaultTags = {"全部", "推荐", "直播", "短剧", "美食", "穿搭", "旅行"};
                    createTagViews(tagsContainer, defaultTags);
                });
            }
        });
    }

    /**
     * 创建标签视图
     */
    private void createTagViews(LinearLayout tagsContainer, String[] tags) {
        for (int index = 0; index < tags.length; index++) {
            String tag = tags[index];
            // 创建标签容器
            FrameLayout tagContainer = new FrameLayout(this);
            FrameLayout.LayoutParams containerParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            );
            containerParams.setMargins(12, 0, 12, 0);
            tagContainer.setLayoutParams(containerParams);

            // 创建标签文本
            TextView tagText = new TextView(this);
            tagText.setText(tag);
            tagText.setTextSize(14);
            tagText.setTextColor(Color.WHITE);
            tagText.setAlpha(0.6f);
            tagText.setGravity(Gravity.CENTER);
            FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            );
            textParams.gravity = Gravity.CENTER;
            tagText.setLayoutParams(textParams);

            // 创建下划线
            View underline = new View(this);
            underline.setBackgroundColor(Color.WHITE);
            underline.setAlpha(0.8f);
            FrameLayout.LayoutParams underlineParams = new FrameLayout.LayoutParams(40, 2);
            underlineParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
            underlineParams.bottomMargin = 6;
            underline.setLayoutParams(underlineParams);
            underline.setVisibility(View.GONE);

            // 添加到容器
            tagContainer.addView(tagText);
            tagContainer.addView(underline);

            // 设置点击事件
            final int tagIndex = index;
            tagContainer.setOnClickListener(v -> {
                // 更新所有标签的样式
                for (int i = 0; i < tagsContainer.getChildCount(); i++) {
                    FrameLayout container = (FrameLayout) tagsContainer.getChildAt(i);
                    TextView text = (TextView) container.getChildAt(0);
                    View line = container.getChildAt(1);
                    
                    if (container == tagContainer) {
                        text.setAlpha(1.0f);
                        text.setTypeface(null, android.graphics.Typeface.BOLD);
                        line.setVisibility(View.VISIBLE);
                    } else {
                        text.setAlpha(0.6f);
                        text.setTypeface(null, android.graphics.Typeface.NORMAL);
                        line.setVisibility(View.GONE);
                    }
                }

                // 更新当前选中的社区标签：
                // 约定第一个标签为“推荐/全部”，选中时不过滤，显示所有社区内容
                if (tagIndex == 0) {
                    mSelectedCommunityTag = null;
                } else {
                    mSelectedCommunityTag = tag;
                }

                // 刷新数据
                refreshMoments();
            });

            tagsContainer.addView(tagContainer);
        }

        // 设置第一个标签为选中状态，并默认不过滤（推荐/全部）
        if (tagsContainer.getChildCount() > 0) {
            FrameLayout firstContainer = (FrameLayout) tagsContainer.getChildAt(0);
            TextView firstText = (TextView) firstContainer.getChildAt(0);
            View firstLine = firstContainer.getChildAt(1);
            firstText.setAlpha(1.0f);
            firstText.setTypeface(null, android.graphics.Typeface.BOLD);
            firstLine.setVisibility(View.VISIBLE);
            mSelectedCommunityTag = null;
        }
    }

    private static class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private final int space;

        SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;
            if (position < 2) {
                outRect.top = space;
            }
        }
    }

    interface Listener {
        void onComment(int position, PostBean post, TopCommentBean topCommentBean);

        void onClickImage(int position, PostBean post, String imageUrl);

        void onDeletePost(int position, PostBean post);
    }

    class MomentsAdapter extends RecyclerView.Adapter<MomentsAdapter.VH> {
        private static final int TYPE_MOMENT = 0;
        private static final int TYPE_COMMUNITY = 1;
        private final List<PostBean> items;
        private Listener listener;

        MomentsAdapter(List<PostBean> items) {
            this.items = items;
        }

        void setListener(Listener l) {
            this.listener = l;
        }

        int getPositionById(String postId) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getPost_id().equals(postId))
                    return i;
            }
            return -1;
        }

        void setItems(List<PostBean> newItems) {
            items.clear();
            if (newItems != null) items.addAll(newItems);
            notifyDataSetChanged();
        }

        void addItems(List<PostBean> newItems) {
            if (newItems != null) {
                int startPosition = items.size();
                items.addAll(newItems);
                notifyItemRangeInserted(startPosition, newItems.size());
            }
        }

        @Override
        public int getItemViewType(int position) {
            PostBean post = items.get(position);
            // 有 community_info 的视为社区贴子
            return post.getCommunity_info() != null ? TYPE_COMMUNITY : TYPE_MOMENT;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int layoutId = (viewType == TYPE_COMMUNITY)
                    ? R.layout.item_moment_post_community
                    : R.layout.item_moment_post;
            View v = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            PostBean post = items.get(position);
            String text = (post.getContent() != null && post.getContent().getText() != null) ? post.getContent().getText() : "";
            // name
            if (post.getUser_info() != null) {
                holder.tvName.setText(post.getUser_info().getNickname());
            } else {
                holder.tvName.setText("匿名");
            }

            AvatarUtils.loadAvatar(holder.ivAvatar, post.getUser_info().getAvatar(), post.getUser_info().getNickname());

            // content text
            if (TextUtils.isEmpty(text)) {
                holder.tvContent.setVisibility(View.GONE);
            } else {
                holder.tvContent.setVisibility(VISIBLE);
                holder.tvContent.setText(text);
            }

            // 分社区模式与朋友圈模式渲染媒体区域
            holder.mediaContainer.removeAllViews();
            boolean isCommunity = post.getCommunity_info() != null;

            if (isCommunity) {
                // 社区模式：使用单张封面图形成瀑布流卡片，并支持点击进入详情页
                holder.mediaContainer.setVisibility(VISIBLE);
                ImageView cover = (ImageView) holder.itemView.findViewById(R.id.iv_cover);
                if (cover == null) {
                    cover = new ImageView(holder.itemView.getContext());
                    ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    cover.setLayoutParams(lp);
                    cover.setAdjustViewBounds(true);
                    cover.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    cover.setBackgroundColor(0xFFCCCCCC);
                    holder.mediaContainer.addView(cover);
                }

                boolean boundMedia = false;
                if (post.getContent() != null) {
                    if (post.getContent().getVideo() != null
                            && !TextUtils.isEmpty(post.getContent().getVideo().getUrl())) {
                        AvatarUtils.loadVideoCover(cover,
                                post.getContent().getVideo().getSnapshot_url(),
                                post.getContent().getVideo().getUrl());
                        boundMedia = true;
                        // 视频封面中央：圆形半透明黑底 + 白色播放图标
                        int iconSize = (int) (32 * holder.itemView.getResources().getDisplayMetrics().density);
                        int circleSize = (int) (56 * holder.itemView.getResources().getDisplayMetrics().density);
                        FrameLayout playContainer = new FrameLayout(holder.itemView.getContext());
                        FrameLayout.LayoutParams lpContainer = new FrameLayout.LayoutParams(circleSize, circleSize);
                        lpContainer.gravity = android.view.Gravity.CENTER;
                        playContainer.setLayoutParams(lpContainer);
                        playContainer.setBackgroundResource(R.drawable.bg_play_icon_circle);
                        ImageView playIcon = new ImageView(holder.itemView.getContext());
                        FrameLayout.LayoutParams lpPlay = new FrameLayout.LayoutParams(iconSize, iconSize);
                        lpPlay.gravity = android.view.Gravity.CENTER;
                        playIcon.setLayoutParams(lpPlay);
                        playIcon.setImageResource(R.drawable.ic_play);
                        playIcon.setColorFilter(0xFFFFFFFF);
                        playIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        playContainer.addView(playIcon);
                        holder.mediaContainer.addView(playContainer);
                    }

                    if (!boundMedia
                            && post.getContent().getImages() != null
                            && !post.getContent().getImages().isEmpty()) {
                        com.juggle.im.android.server.beans.ImageBean firstImg = post.getContent().getImages().get(0);
                        AvatarUtils.loadImage(cover, firstImg.getUrl());
                        boundMedia = true;
                    }
                }

                if (!boundMedia) {
                    cover.setImageResource(R.drawable.profile_cover);
                }

                View.OnClickListener goDetail = v -> {
                    Intent it = new Intent(v.getContext(), MomentDetailActivity.class);
                    it.putExtra(MomentDetailActivity.EXTRA_POST_ID, post.getPost_id());
                    v.getContext().startActivity(it);
                };
                cover.setOnClickListener(goDetail);
                holder.itemView.setOnClickListener(goDetail);
            } else {
                // 朋友圈模式：优先展示视频封面，其次展示 1~9 宫格图片
                if (post.getContent() != null
                        && post.getContent().getVideo() != null
                        && !TextUtils.isEmpty(post.getContent().getVideo().getUrl())) {
                    // 有视频：封面图 + 居中播放图标，点击直接播放
                    holder.mediaContainer.setVisibility(VISIBLE);
                    holder.mediaContainer.removeAllViews();

                    FrameLayout wrap = new FrameLayout(holder.itemView.getContext());
                    GridLayout.LayoutParams lpWrap = new GridLayout.LayoutParams();
                    lpWrap.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    lpWrap.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    wrap.setLayoutParams(lpWrap);

                    ImageView cover = new ImageView(holder.itemView.getContext());
                    FrameLayout.LayoutParams lpCover = new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    cover.setLayoutParams(lpCover);
                    cover.setAdjustViewBounds(true);
                    cover.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    cover.setBackgroundColor(0xFFCCCCCC);
                    wrap.addView(cover);

                    // 播放按钮：圆形半透明黑底 + 白色播放图标
                    int iconSize = (int) (32 * holder.itemView.getResources().getDisplayMetrics().density);
                    int circleSize = (int) (56 * holder.itemView.getResources().getDisplayMetrics().density);
                    FrameLayout playContainer = new FrameLayout(holder.itemView.getContext());
                    FrameLayout.LayoutParams lpContainer = new FrameLayout.LayoutParams(circleSize, circleSize);
                    lpContainer.gravity = android.view.Gravity.CENTER;
                    playContainer.setLayoutParams(lpContainer);
                    playContainer.setBackgroundResource(R.drawable.bg_play_icon_circle);
                    ImageView playIcon = new ImageView(holder.itemView.getContext());
                    FrameLayout.LayoutParams lpPlay = new FrameLayout.LayoutParams(iconSize, iconSize);
                    lpPlay.gravity = android.view.Gravity.CENTER;
                    playIcon.setLayoutParams(lpPlay);
                    playIcon.setImageResource(R.drawable.ic_play);
                    playIcon.setColorFilter(0xFFFFFFFF);
                    playIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    playContainer.addView(playIcon);
                    wrap.addView(playContainer);

                    holder.mediaContainer.addView(wrap);

                    final String videoUrl = post.getContent().getVideo().getUrl();
                    AvatarUtils.loadVideoCover(
                            cover,
                            post.getContent().getVideo().getSnapshot_url(),
                            videoUrl
                    );

                    // 朋友圈模式：点击视频直接播放，不跳详情页
                    View.OnClickListener playVideo = v -> VideoPlayerActivity.startWithUrl(v.getContext(), videoUrl);
                    wrap.setOnClickListener(playVideo);
                    holder.itemView.setOnClickListener(playVideo);

                } else if (post.getContent() != null
                        && post.getContent().getImages() != null
                        && !post.getContent().getImages().isEmpty()) {
                    holder.mediaContainer.setVisibility(VISIBLE);
                    int imageSize = post.getContent().getImages().size();

                    // 限制最多显示9张图片
                    int displaySize = Math.min(imageSize, 9);

                    // 根据图片数量确定行列数和尺寸
                    int rows, cols;
                    int spacing = 8; // 图片间距

                    if (displaySize == 1) {
                        rows = 1;
                        cols = 1;
                    } else if (displaySize == 2) {
                        rows = 1;
                        cols = 2;
                    } else if (displaySize == 3) {
                        rows = 1;
                        cols = 3;
                    } else if (displaySize == 4) {
                        rows = 2;
                        cols = 2;
                    } else if (displaySize <= 6) {
                        rows = 2;
                        cols = 3;
                    } else {
                        rows = 3;
                        cols = 3;
                    }

                    GridLayout gridLayout = (GridLayout) holder.mediaContainer;
                    gridLayout.setRowCount(rows);
                    gridLayout.setColumnCount(cols);

                    int screenWidth = holder.itemView.getResources().getDisplayMetrics().widthPixels;
                    int containerWidth = screenWidth - 32; // 减去左右padding (16dp * 2)
                    int totalSpacing = spacing * (cols - 1);
                    int imageWidth = (containerWidth - totalSpacing) / cols;
                    int imageHeight = imageWidth;

                    for (int i = 0; i < displaySize; i++) {
                        com.juggle.im.android.server.beans.ImageBean img = post.getContent().getImages().get(i);
                        ImageView iv = new ImageView(holder.itemView.getContext());

                        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                        lp.columnSpec = GridLayout.spec(i % cols);
                        lp.rowSpec = GridLayout.spec(i / cols);
                        lp.width = imageWidth;
                        lp.height = imageHeight;
                        int marginRight = (i % cols == cols - 1) ? 0 : spacing;
                        int marginBottom = (i / cols == rows - 1) ? 0 : spacing;
                        lp.setMargins(0, 0, marginRight, marginBottom);

                        iv.setLayoutParams(lp);
                        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        iv.setBackgroundColor(0xFFE0E0E0);
                        iv.setClipToOutline(true);
                        android.graphics.drawable.RippleDrawable ripple = new android.graphics.drawable.RippleDrawable(
                                android.content.res.ColorStateList.valueOf(0x20000000),
                                null,
                                null
                        );
                        iv.setForeground(ripple);

                        AvatarUtils.loadImage(iv, img.getUrl());

                        gridLayout.addView(iv);
                        final int positionCopy = position;
                        final String imageUrl = img.getUrl();
                        iv.setOnClickListener(l -> {
                            if (listener != null) listener.onClickImage(positionCopy, post, imageUrl);
                        });
                    }
                } else {
                    holder.mediaContainer.setVisibility(View.GONE);
                }
            }

            // time
            if (post.getCreated_time() > 0) {
                long currentTime = System.currentTimeMillis();
                long timeDifference = currentTime - post.getCreated_time();

                long minutes = timeDifference / (1000 * 60);
                long hours = timeDifference / (1000 * 60 * 60);
                long days = timeDifference / (1000 * 60 * 60 * 24);

                String timeText;
                if (minutes < 1) {
                    timeText = "刚刚";
                } else if (minutes < 60) {
                    timeText = minutes + "分钟前";
                } else if (hours < 24) {
                    timeText = hours + "小时前";
                } else if (days < 2) {
                    timeText = "昨天";
                } else {
                    timeText = days + "天前";
                }

                holder.tvTime.setText(timeText);
            } else {
                holder.tvTime.setText("");
            }

            // likes (reactions) - 社区模式仅展示总数，朋友圈模式保持原展示昵称样式
            boolean hasLikes = false;
            int likesCount = 0;
            if (post.getReactions() != null && !post.getReactions().isEmpty()) {
                if (post.getReactions() != null) {
                    for (java.util.Map.Entry<String, java.util.List<com.juggle.im.android.server.beans.ReactionItem>> entry : post.getReactions().entrySet()) {
                        if (entry.getValue() != null) {
                            likesCount += entry.getValue().size();
                        }
                    }
                }

                if (likesCount > 0) {
                    hasLikes = true;
                    holder.tvLikes.setVisibility(VISIBLE);
                    String likeText = isCommunity
                            ? likesCount + "赞"
                            : String.valueOf(likesCount) + "赞";
                    holder.tvLikes.setText(likeText);
                }
                holder.likesContainer.setVisibility(VISIBLE);
            } else {
                holder.likesContainer.setVisibility(View.GONE);
            }
            holder.dividerLikes.setVisibility(hasLikes ? VISIBLE : View.GONE);

            // comments：社区模式卡片不在列表内展开评论，只在详情页展示
            holder.commentsContainer.removeAllViews();
            boolean hasComments = false;
            if (!isCommunity && post.getTop_comments() != null && !post.getTop_comments().isEmpty()) {
                hasComments = true;
                for (com.juggle.im.android.server.beans.TopCommentBean c : post.getTop_comments()) {
                    TextView tv = new TextView(holder.itemView.getContext());
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 5, 0, 0);
                    tv.setLayoutParams(params);
                    tv.setTextSize(13f);
                    // build name + content with name colored
                    String name = c.getUser_info() != null ? c.getUser_info().getNickname() : "";
                    String content = c.getText() != null ? c.getText() : "";
                    CommentDetail commentDetail = gson.fromJson(content, CommentDetail.class);
                    String full = name + ": " + commentDetail.getContent();
                    android.text.SpannableStringBuilder ss = new android.text.SpannableStringBuilder(full);
                    if (!name.isEmpty()) {
                        ss.setSpan(new android.text.style.ForegroundColorSpan(0xFF576B95), 0, name.length(), android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    ss.setSpan(new android.text.style.ForegroundColorSpan(0xFF666666), name.length(), full.length(), android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    tv.setText(ss);
                    holder.commentsContainer.addView(tv);
                    tv.setOnClickListener(v -> {
                        if (listener != null) listener.onComment(position, post, c);
                    });
                }
            }

            holder.blockLikesComments.setVisibility(hasLikes || hasComments ? VISIBLE : View.GONE);

            // btnMore click listener for popup menu
            holder.btnMore.setOnClickListener(v -> {
                // 创建自定义弹出菜单
                View popupView = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.popup_moment_menu, null);
                PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                popupWindow.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                
                // 检查当前用户是否已点赞
                String currentUserId = JIM.getInstance().getCurrentUserId();
                boolean hasLiked = false;
                if (post.getReactions() != null && post.getReactions().containsKey("like")) {
                    for (com.juggle.im.android.server.beans.ReactionItem item : post.getReactions().get("like")) {
                        if (item.getUser_info() != null && item.getUser_info().getUserId().equals(currentUserId)) {
                            hasLiked = true;
                            break;
                        }
                    }
                }
                
                // 根据是否已点赞，更新菜单文本和图标颜色
                TextView likeText = popupView.findViewById(R.id.like_text);
                ImageView likeIcon = popupView.findViewById(R.id.like_icon);
                if (hasLiked) {
                    likeText.setText(R.string.txt_cancel);
                    likeIcon.setColorFilter(0xFFFF6B9D, android.graphics.PorterDuff.Mode.SRC_IN);
                } else {
                    likeText.setText(R.string.like);
                    likeIcon.setColorFilter(0xFFFFFFFF, android.graphics.PorterDuff.Mode.SRC_IN);
                }
                
                // 如果不是发布者，隐藏删除选项
                LinearLayout deleteLayout = popupView.findViewById(R.id.action_delete);
                if (post.getUser_info() == null || !post.getUser_info().getUserId().equals(currentUserId)) {
                    deleteLayout.setVisibility(View.GONE);
                }
                
                // 设置点赞按钮点击事件
                popupView.findViewById(R.id.action_like).setOnClickListener(view -> {
                    popupWindow.dismiss();
                    likePost(position, post);
                });
                
                // 设置评论按钮点击事件
                popupView.findViewById(R.id.action_comment).setOnClickListener(view -> {
                    popupWindow.dismiss();
                    showPostComment(position, post, null);
                });
                
                // 设置删除按钮点击事件
                popupView.findViewById(R.id.action_delete).setOnClickListener(view -> {
                    popupWindow.dismiss();
                    if (listener != null) listener.onDeletePost(position, post);
                });
                
                // 显示弹出菜单 - 让菜单右边靠近更多按钮的左侧
                popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                int popupWidth = popupView.getMeasuredWidth();
                int popupHeight = popupView.getMeasuredHeight();
                
                int[] location = new int[2];
                holder.btnMore.getLocationOnScreen(location);
                
                // 计算位置：菜单右边靠近更多按钮的左侧
                int xPos = location[0] - popupWidth - 8; // 8dp间距
                int yPos = location[1] - popupHeight - 8; // 8dp间距
                
                popupWindow.showAtLocation(holder.btnMore, Gravity.NO_GRAVITY, xPos, yPos);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class VH extends RecyclerView.ViewHolder {
            ImageView ivAvatar;
            TextView tvName;
            View vDelete;
            ImageView btnMore;
            TextView tvContent;
            ViewGroup mediaContainer;
            TextView tvTime;
            ImageView btnComment;
            View blockLikesComments;
            TextView tvLikes;
            ViewGroup likesContainer;
            View dividerLikes;
            LinearLayout commentsContainer;

            VH(@NonNull View itemView) {
                super(itemView);
                ivAvatar = itemView.findViewById(R.id.iv_avatar);
                tvName = itemView.findViewById(R.id.tv_name);
                tvContent = itemView.findViewById(R.id.tv_content);
                mediaContainer = itemView.findViewById(R.id.media_container);
                tvTime = itemView.findViewById(R.id.tv_time);
                btnMore = itemView.findViewById(R.id.btn_more);
                blockLikesComments = itemView.findViewById(R.id.block_likes_comments);
                tvLikes = itemView.findViewById(R.id.tv_likes);
                dividerLikes = itemView.findViewById(R.id.divider_likes);
                commentsContainer = itemView.findViewById(R.id.comments_container);
                likesContainer = itemView.findViewById(R.id.likes_container);
                vDelete = itemView.findViewById(R.id.delete_moment);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
            case REQUEST_CODE_CHOOSE_PHOTO:
                if (data != null) {
                    ArrayList<String> selectedImages = data.getStringArrayListExtra("selected_images");
                    if (selectedImages != null && !selectedImages.isEmpty()) {
                        Intent intent = new Intent(this, CreatePostActivity.class);
                        intent.putStringArrayListExtra("image_urls", selectedImages);
                        startActivityForResult(intent, REQUEST_CODE_CREATE_POST);
                    }
                }
                break;

            case REQUEST_CODE_TAKE_PHOTO:
                if (photoUri != null) {
                    ArrayList<String> imageUrls = new ArrayList<>();
                    imageUrls.add(photoUri.toString());
                    Intent intent = new Intent(this, CreatePostActivity.class);
                    intent.putStringArrayListExtra("image_urls", imageUrls);
                    startActivityForResult(intent, REQUEST_CODE_CREATE_POST);
                }
                break;

            case REQUEST_CODE_CREATE_POST:
                refreshMoments(); // 重新加载数据
                break;

            default:
                if (data != null) {
                    refreshMoments(); // 重新加载数据
                }
                break;
        }
    }
}