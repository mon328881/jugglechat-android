package com.juggle.im.android.chat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.juggle.im.JIM;
import com.juggle.im.android.R;
import com.juggle.im.android.app.AddFriendActivity;
import com.juggle.im.android.server.beans.PostBean;
import com.juggle.im.android.server.beans.ReactionItem;
import com.juggle.im.android.server.beans.TopCommentBean;
import com.juggle.im.android.server.http.ApiCallback;
import com.juggle.im.android.server.http.ServiceManager;
import com.juggle.im.android.utils.AvatarUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 单条朋友圈/社区动态详情页。
 * 展示完整文本、图片、点赞统计和评论列表，并支持点赞与评论。
 */
public class MomentDetailActivity extends AppCompatActivity {

    public static final String EXTRA_POST_ID = "extra_post_id";
    private static final long VIDEO_START_OFFSET_MS = 60_000L;
    private static final String PREF_NAME_PENDING_FRIEND = "moment_detail_prefs";
    private static final String PREF_KEY_PENDING_IDS = "pending_friend_request_ids";

    private static final Gson gson = new Gson();

    private ImageView ivAvatar;
    private ImageView ivCover;
    private PlayerView videoPlayerView;
    private View videoContainer;
    private TextView tvName;
    private TextView tvTime;
    private TextView tvContent;
    private TextView tvLikeCount;
    private TextView tvCommentCount;
    private TextView tvCommentsHeader;
    private TextView btnFriendAction;
    private LinearLayout commentsContainer;
    private View loadingView;
    private View errorView;

    private TextView tvInputHint;
    private ImageView btnLike;

    private PostBean currentPost;
    private boolean isLiked = false;
    private boolean isFriend = false;
    private ExoPlayer exoPlayer;
    private String currentVideoUrl;
    /** 当前视频是否已尝试过 404 换链，避免无限重试 */
    private boolean hasRetriedPlayUrlForCurrentVideo = false;
    /** 是否已尝试过「下载后播放」（与会话页一致），避免重复下载 */
    private boolean hasTriedDownloadPlay = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moment_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("动态详情");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        ivAvatar = findViewById(R.id.iv_avatar);
        ivCover = findViewById(R.id.iv_cover);
        videoPlayerView = findViewById(R.id.video_player_view);
        tvName = findViewById(R.id.tv_name);
        tvTime = findViewById(R.id.tv_time);
        tvContent = findViewById(R.id.tv_content);
        tvLikeCount = findViewById(R.id.tv_like_count);
        tvCommentCount = findViewById(R.id.tv_comment_count);
        tvCommentsHeader = findViewById(R.id.tv_comments_header);
        btnFriendAction = findViewById(R.id.btn_friend_action);
        commentsContainer = findViewById(R.id.comments_container);
        loadingView = findViewById(R.id.loading_view);
        errorView = findViewById(R.id.error_view);

        tvInputHint = findViewById(R.id.tv_input_hint);
        btnLike = findViewById(R.id.btn_like);

        tvInputHint.setOnClickListener(v -> showCommentDialog());
        btnLike.setOnClickListener(v -> onLikeClicked());

        // 点击视频区域时，支持使用外部播放器播放
        if (videoPlayerView != null) {
            View.OnClickListener videoClick = v -> openVideoExternally();
            videoPlayerView.setOnClickListener(videoClick);
        }

        String postId = getIntent().getStringExtra(EXTRA_POST_ID);
        if (TextUtils.isEmpty(postId)) {
            finish();
            return;
        }

        loadPostDetail(postId);
    }

    private void loadPostDetail(String postId) {
        showLoading(true);
        ServiceManager.getMomentService().getPost(postId, new ApiCallback<PostBean>() {
            @Override
            public void onSuccess(PostBean data) {
                runOnUiThread(() -> {
                    showLoading(false);
                    if (data != null) {
                        currentPost = data;
                        bindPost(data);
                    } else {
                        showError(true);
                    }
                });
            }

            @Override
            public void onError(int code, String message) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showError(true);
                });
            }
        });
    }

    private void bindPost(PostBean post) {
        // avatar & name
        String authorId = null;
        if (post.getUser_info() != null) {
            authorId = post.getUser_info().getUserId();
            String name = !TextUtils.isEmpty(post.getUser_info().getNickname())
                    ? post.getUser_info().getNickname()
                    : post.getUser_info().getUserId();
            tvName.setText(name);
            AvatarUtils.loadAvatar(ivAvatar, post.getUser_info().getAvatar(), name);
        }

        // 好友状态按钮：自己发的动态不显示按钮
        if (authorId != null && authorId.equals(JIM.getInstance().getCurrentUserId())) {
            btnFriendAction.setVisibility(View.GONE);
        } else if (authorId != null) {
            btnFriendAction.setVisibility(View.VISIBLE);
            final String targetAuthorId = authorId;
            // 先与服务器同步待通过列表（对方拒绝/过期后移除），再刷新按钮状态
            AddFriendActivity.syncPendingFriendRequestsFromServer(this, () -> runOnUiThread(() -> updateFriendStatus(targetAuthorId)));
        } else {
            btnFriendAction.setVisibility(View.GONE);
        }

        // time
        tvTime.setText(formatTime(post.getCreated_time()));

        // content text（详情页不做行数限制）
        String text = post.getContent() != null ? post.getContent().getText() : null;
        if (TextUtils.isEmpty(text)) {
            tvContent.setVisibility(View.GONE);
        } else {
            tvContent.setVisibility(View.VISIBLE);
            tvContent.setText(text);
        }

        // 媒体内容：优先展示视频，其次展示第一张图片
        if (post.getContent() != null
                && post.getContent().getVideo() != null
                && !TextUtils.isEmpty(post.getContent().getVideo().getUrl())) {
            String videoUrl = post.getContent().getVideo().getUrl();
            currentVideoUrl = videoUrl;
            hasRetriedPlayUrlForCurrentVideo = false;
            hasTriedDownloadPlay = false;
            ivCover.setVisibility(View.GONE);
            if (videoPlayerView != null) {
                videoPlayerView.setVisibility(View.VISIBLE);
                initializePlayer(videoUrl);
            }
        } else {
            // 无视频时，沿用列表逻辑：展示第一张图片为大图
            releasePlayer();
            if (post.getContent() != null
                    && post.getContent().getImages() != null
                    && !post.getContent().getImages().isEmpty()) {
                ivCover.setVisibility(View.VISIBLE);
                AvatarUtils.loadImage(ivCover, post.getContent().getImages().get(0).getUrl());
            } else {
                ivCover.setVisibility(View.GONE);
            }
            if (videoPlayerView != null) {
                videoPlayerView.setVisibility(View.GONE);
            }
        }

        // likes summary & 当前用户是否已点赞
        updateLikeUiFromPost(post);

        // comments
        commentsContainer.removeAllViews();
        List<TopCommentBean> comments = post.getTop_comments();
        int commentCount = comments != null ? comments.size() : 0;
        tvCommentCount.setText(commentCount + " 评论");
        tvCommentsHeader.setText("共 " + commentCount + " 条评论");

        if (comments != null && !comments.isEmpty()) {
            commentsContainer.setVisibility(View.VISIBLE);
            for (TopCommentBean c : comments) {
                View item = getLayoutInflater().inflate(R.layout.item_moment_comment, commentsContainer, false);
                ImageView iv = item.findViewById(R.id.iv_comment_avatar);
                TextView tvAuthor = item.findViewById(R.id.tv_comment_author);
                TextView tvContent = item.findViewById(R.id.tv_comment_content);
                TextView tvTime = item.findViewById(R.id.tv_comment_time);

                String author = c.getUser_info() != null
                        ? (!TextUtils.isEmpty(c.getUser_info().getNickname())
                                ? c.getUser_info().getNickname()
                                : c.getUser_info().getUserId())
                        : "";

                String avatarUrl = c.getUser_info() != null ? c.getUser_info().getAvatar() : null;
                AvatarUtils.loadAvatar(iv, avatarUrl, author);
                tvAuthor.setText(author);

                String content = c.getText();
                // 服务端 text 字段为 JSON（{"content": "...", "type": "..."}），这里只提取 content 字段展示
                if (!TextUtils.isEmpty(content) && content.trim().startsWith("{")) {
                    try {
                        Map<?, ?> m = gson.fromJson(content, Map.class);
                        Object v = m != null ? m.get("content") : null;
                        if (v != null) {
                            content = String.valueOf(v);
                        }
                    } catch (Exception ignore) {
                    }
                }
                tvContent.setText(content != null ? content : "");

                tvTime.setText(formatTime(c.getCreated_time()));

                commentsContainer.addView(item);
            }
        } else {
            commentsContainer.setVisibility(View.GONE);
        }
    }

    private static SharedPreferences getPendingFriendPrefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREF_NAME_PENDING_FRIEND, Context.MODE_PRIVATE);
    }

    private static boolean hasPendingFriendRequest(Context context, String userId) {
        if (userId == null) return false;
        Set<String> set = getPendingFriendPrefs(context).getStringSet(PREF_KEY_PENDING_IDS, null);
        return set != null && set.contains(userId);
    }

    private static void addPendingFriendRequest(Context context, String userId) {
        if (userId == null) return;
        SharedPreferences prefs = getPendingFriendPrefs(context);
        Set<String> set = new HashSet<>(prefs.getStringSet(PREF_KEY_PENDING_IDS, new HashSet<>()));
        set.add(userId);
        prefs.edit().putStringSet(PREF_KEY_PENDING_IDS, set).apply();
    }

    private static void removePendingFriendRequest(Context context, String userId) {
        if (userId == null) return;
        SharedPreferences prefs = getPendingFriendPrefs(context);
        Set<String> set = new HashSet<>(prefs.getStringSet(PREF_KEY_PENDING_IDS, new HashSet<>()));
        set.remove(userId);
        prefs.edit().putStringSet(PREF_KEY_PENDING_IDS, set).apply();
    }

    private void updateFriendStatus(String userId) {
        // 使用 getUserInfo，后端会返回 is_friend 字段，更可靠
        ServiceManager.getUserService().getUserInfo(userId,
                new ApiCallback<com.juggle.im.android.server.beans.UserInfoBean>() {
                    @Override
                    public void onSuccess(com.juggle.im.android.server.beans.UserInfoBean data) {
                        runOnUiThread(() -> {
                            boolean friendFlag = data != null && data.isFriend();
                            isFriend = friendFlag;
                            if (isFriend) {
                                removePendingFriendRequest(MomentDetailActivity.this, userId);
                                btnFriendAction.setText("好友");
                                btnFriendAction.setEnabled(false);
                                btnFriendAction.setAlpha(0.7f);
                                btnFriendAction.setOnClickListener(null);
                            } else if (hasPendingFriendRequest(MomentDetailActivity.this, userId)) {
                                // 已发送过申请，显示待通过，避免重复点击
                                btnFriendAction.setText("待通过");
                                btnFriendAction.setEnabled(false);
                                btnFriendAction.setAlpha(0.7f);
                                btnFriendAction.setOnClickListener(null);
                            } else {
                                btnFriendAction.setText("添加好友");
                                btnFriendAction.setEnabled(true);
                                btnFriendAction.setAlpha(1f);
                                btnFriendAction.setOnClickListener(v -> applyFriend(userId));
                            }
                        });
                    }

                    @Override
                    public void onError(int code, String message) {
                        // 查询失败时，若有本地「待通过」记录则显示待通过，否则显示添加好友
                        runOnUiThread(() -> {
                            isFriend = false;
                            if (hasPendingFriendRequest(MomentDetailActivity.this, userId)) {
                                btnFriendAction.setText("待通过");
                                btnFriendAction.setEnabled(false);
                                btnFriendAction.setAlpha(0.7f);
                                btnFriendAction.setOnClickListener(null);
                            } else {
                                btnFriendAction.setText("添加好友");
                                btnFriendAction.setEnabled(true);
                                btnFriendAction.setAlpha(1f);
                                btnFriendAction.setOnClickListener(v -> applyFriend(userId));
                            }
                        });
                    }
                });
    }

    private void applyFriend(String userId) {
        ServiceManager.getUserService().applyFriend(userId, new ApiCallback<com.juggle.im.android.server.beans.FriendApplicationBean>() {
            @Override
            public void onSuccess(com.juggle.im.android.server.beans.FriendApplicationBean data) {
                runOnUiThread(() -> {
                    addPendingFriendRequest(MomentDetailActivity.this, userId);
                    Toast.makeText(MomentDetailActivity.this, "好友申请已发送", Toast.LENGTH_SHORT).show();
                    // 改为「待通过」并禁用，重新进入详情页时也会显示待通过
                    btnFriendAction.setText("待通过");
                    btnFriendAction.setEnabled(false);
                    btnFriendAction.setAlpha(0.7f);
                    btnFriendAction.setOnClickListener(null);
                });
            }

            @Override
            public void onError(int code, String message) {
                runOnUiThread(() -> Toast.makeText(MomentDetailActivity.this, "添加好友失败: " + message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateLikeUiFromPost(PostBean post) {
        int likesCount = 0;
        boolean liked = false;
        String me = JIM.getInstance().getCurrentUserId();

        if (post.getReactions() != null && !post.getReactions().isEmpty()) {
            for (Map.Entry<String, List<ReactionItem>> entry : post.getReactions().entrySet()) {
                List<ReactionItem> list = entry.getValue();
                if (list != null) {
                    likesCount += list.size();
                    for (ReactionItem item : list) {
                        if (item != null && item.getUser_info() != null
                                && me != null
                                && me.equals(item.getUser_info().getUserId())) {
                            liked = true;
                        }
                    }
                }
            }
        }

        isLiked = liked;

        tvLikeCount.setText(likesCount + " 赞");
        tvLikeCount.setVisibility(likesCount > 0 ? View.VISIBLE : View.GONE);

        updateLikeButtonTint();
    }

    private void updateLikeButtonTint() {
        // 未点赞：灰色爱心；已点赞：粉色爱心
        btnLike.setImageResource(isLiked ? R.drawable.ic_heart_pink : R.drawable.ic_heart_gray);
        btnLike.setColorFilter(null);
    }

    private void onLikeClicked() {
        if (currentPost == null) return;
        String postId = currentPost.getPost_id();
        if (isLiked) {
            // 取消点赞
            ServiceManager.getMomentService().removeReaction(postId, "key", new ApiCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    runOnUiThread(() -> {
                        Toast.makeText(MomentDetailActivity.this, "已取消点赞", Toast.LENGTH_SHORT).show();
                        loadPostDetail(postId);
                    });
                }

                @Override
                public void onError(int code, String message) {
                    runOnUiThread(() ->
                            Toast.makeText(MomentDetailActivity.this, "取消点赞失败: " + message, Toast.LENGTH_SHORT).show());
                }
            });
            return;
        }
        // 点赞
        ServiceManager.getMomentService().addReaction(postId, "key", "v", new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                runOnUiThread(() -> {
                    Toast.makeText(MomentDetailActivity.this, "已点赞", Toast.LENGTH_SHORT).show();
                    loadPostDetail(postId);
                });
            }

            @Override
            public void onError(int code, String message) {
                runOnUiThread(() ->
                        Toast.makeText(MomentDetailActivity.this, "点赞失败: " + message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void showCommentDialog() {
        if (currentPost == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheet = getLayoutInflater().inflate(R.layout.dialog_moment_comment_input, null);
        EditText et = sheet.findViewById(R.id.et_comment);
        TextView btnSend = sheet.findViewById(R.id.btn_send_comment);
        btnSend.setOnClickListener(v -> {
            String content = et.getText().toString().trim();
            if (TextUtils.isEmpty(content)) {
                Toast.makeText(MomentDetailActivity.this, "请输入内容", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, String> body = new HashMap<>();
            body.put("content", content);
            body.put("type", "jg:text");

            ServiceManager.getMomentService().addComment(
                    currentPost.getPost_id(),
                    null,
                    null,
                    gson.toJson(body),
                    new ApiCallback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                            runOnUiThread(() -> Toast.makeText(MomentDetailActivity.this, "评论成功", Toast.LENGTH_SHORT).show());
                            // 重新拉取详情，刷新评论列表与统计
                            loadPostDetail(currentPost.getPost_id());
                            dialog.dismiss();
                        }

                        @Override
                        public void onError(int code, String message) {
                            runOnUiThread(() -> Toast.makeText(MomentDetailActivity.this, "评论失败: " + message, Toast.LENGTH_SHORT).show());
                        }
                    }
            );
        });

        dialog.setContentView(sheet);

        dialog.setOnShowListener(d -> {
            BottomSheetDialog dd = (BottomSheetDialog) d;
            FrameLayout bottomSheet = dd.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<?> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setSkipCollapsed(true);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
            et.requestFocus();
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(et, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
            if (dd.getWindow() != null) {
                dd.getWindow().setSoftInputMode(
                        android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                                | android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            }
        });

        dialog.show();
    }

    private String formatTime(long millis) {
        if (millis <= 0) return "";
        Date date = new Date(millis);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    private void showLoading(boolean loading) {
        if (loadingView != null) {
            loadingView.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
        if (errorView != null && loading) {
            errorView.setVisibility(View.GONE);
        }
    }

    private void showError(boolean show) {
        if (errorView != null) {
            errorView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * 使用系统外部播放器播放当前视频
     */
    private void openVideoExternally() {
        if (TextUtils.isEmpty(currentVideoUrl)) {
            Toast.makeText(this, "视频地址无效", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(currentVideoUrl), "video/*");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(Intent.createChooser(intent, "选择播放器"));
        } catch (Exception e) {
            Toast.makeText(this, "无法打开外部播放器", Toast.LENGTH_SHORT).show();
        }
    }

    // ========== ExoPlayer 生命周期管理 ==========

    private void initializePlayer(String url) {
        if (TextUtils.isEmpty(url)) return;
        currentVideoUrl = url;

        if (exoPlayer == null) {
            exoPlayer = new ExoPlayer.Builder(this).build();
            exoPlayer.addListener(new Player.Listener() {
                @Override
                public void onPlayerError(com.google.android.exoplayer2.PlaybackException error) {
                    if (error.getCause() instanceof HttpDataSource.InvalidResponseCodeException) {
                        HttpDataSource.InvalidResponseCodeException e =
                                (HttpDataSource.InvalidResponseCodeException) error.getCause();
                        // 404 时尝试向后端换取可播放 URL 并重试（含过期预签名链接），每视频只换链一次避免无限重试
                        if (e.responseCode == 404 && !TextUtils.isEmpty(currentVideoUrl) && !hasRetriedPlayUrlForCurrentVideo) {
                            hasRetriedPlayUrlForCurrentVideo = true;
                            Log.w("MomentDetail", "视频 404，换链请求 storedUrl=" + (currentVideoUrl.length() > 80 ? currentVideoUrl.substring(0, 80) + "..." : currentVideoUrl));
                            ServiceManager.getFileService().getPlayUrl(currentVideoUrl, new ApiCallback<String>() {
                                @Override
                                public void onSuccess(String playUrl) {
                                    runOnUiThread(() -> {
                                        if (TextUtils.isEmpty(playUrl)) {
                                            Log.e("MomentDetail", "换链返回空 URL");
                                            Toast.makeText(MomentDetailActivity.this, "视频加载失败，换链返回空地址", Toast.LENGTH_LONG).show();
                                            return;
                                        }
                                        Log.d("MomentDetail", "换链成功，使用新 URL 重试");
                                        currentVideoUrl = playUrl;
                                        // 先释放再重建播放器，避免沿用错误状态
                                        releasePlayer();
                                        initializePlayer(playUrl);
                                        Toast.makeText(MomentDetailActivity.this, "已换链，正在重新加载", Toast.LENGTH_SHORT).show();
                                    });
                                }

                                @Override
                                public void onError(int code, String message) {
                                    Log.e("MomentDetail", "换链失败 code=" + code + " message=" + message);
                                    runOnUiThread(() -> {
                                        Toast.makeText(MomentDetailActivity.this,
                                                "视频加载失败(404)，换链失败，正在尝试下载后播放…",
                                                Toast.LENGTH_LONG).show();
                                        downloadAndPlayVideo(currentVideoUrl);
                                    });
                                }
                            });
                            return;
                        }
                    }
                    runOnUiThread(() -> {
                        String msg = "视频加载失败";
                        if (error.getCause() instanceof HttpDataSource.InvalidResponseCodeException) {
                            HttpDataSource.InvalidResponseCodeException e =
                                    (HttpDataSource.InvalidResponseCodeException) error.getCause();
                            msg = "视频加载失败(HTTP " + e.responseCode + ")";
                        } else if (error.getCause() != null) {
                            msg = "视频加载失败: " + error.getCause().getMessage();
                        }
                        Toast.makeText(MomentDetailActivity.this, msg, Toast.LENGTH_LONG).show();
                        if (!hasTriedDownloadPlay && !TextUtils.isEmpty(currentVideoUrl)) {
                            downloadAndPlayVideo(currentVideoUrl);
                        }
                    });
                }
            });
            if (videoPlayerView != null) {
                videoPlayerView.setPlayer(exoPlayer);
            }
        } else {
            exoPlayer.stop();
            exoPlayer.clearMediaItems();
        }

        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(url));
        if (shouldStartAtOffset(url)) {
            exoPlayer.setMediaItem(mediaItem, VIDEO_START_OFFSET_MS);
        } else {
            exoPlayer.setMediaItem(mediaItem);
        }
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(true);
    }

    private boolean shouldStartAtOffset(String url) {
        if (TextUtils.isEmpty(url)) return false;
        String u = url.toLowerCase(Locale.ROOT);
        return u.contains(".m3u8");
    }

    private void releasePlayer() {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    /**
     * 参考会话页 FileMessageView：先下载到本地再播放（与会话页视频一致，提高成功率）
     */
    private void downloadAndPlayVideo(String videoUrl) {
        if (TextUtils.isEmpty(videoUrl)) return;
        hasTriedDownloadPlay = true;

        File downloadDir = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "moment_videos");
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }
        String fileName = "video_" + System.currentTimeMillis() + ".mp4";
        int q = videoUrl.indexOf('?');
        if (q > 0) {
            String path = videoUrl.substring(0, q);
            int last = path.lastIndexOf('/');
            if (last >= 0 && last < path.length() - 1) {
                String segment = path.substring(last + 1);
                if (segment.endsWith(".mp4") || segment.endsWith(".mov") || segment.endsWith(".m4v")) {
                    fileName = segment;
                }
            }
        }
        File localFile = new File(downloadDir, fileName);

        if (localFile.exists()) {
            runOnUiThread(() -> {
                releasePlayer();
                initializePlayer(Uri.fromFile(localFile).toString());
                Toast.makeText(this, "正在播放已缓存视频", Toast.LENGTH_SHORT).show();
            });
            return;
        }

        Toast.makeText(this, "正在下载视频…", Toast.LENGTH_SHORT).show();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(videoUrl).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("MomentDetail", "视频下载失败", e);
                runOnUiThread(() ->
                        Toast.makeText(MomentDetailActivity.this, "视频下载失败: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(MomentDetailActivity.this, "视频下载失败，响应码: " + response.code(), Toast.LENGTH_LONG).show());
                    return;
                }
                ResponseBody body = response.body();
                if (body == null) {
                    runOnUiThread(() ->
                            Toast.makeText(MomentDetailActivity.this, "视频下载失败，响应体为空", Toast.LENGTH_LONG).show());
                    return;
                }
                try (InputStream in = body.byteStream(); FileOutputStream out = new FileOutputStream(localFile)) {
                    byte[] buf = new byte[4096];
                    int n;
                    while ((n = in.read(buf)) != -1) {
                        out.write(buf, 0, n);
                    }
                } catch (IOException e) {
                    Log.e("MomentDetail", "视频保存失败", e);
                    runOnUiThread(() ->
                            Toast.makeText(MomentDetailActivity.this, "视频保存失败: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    return;
                }
                runOnUiThread(() -> {
                    releasePlayer();
                    initializePlayer(Uri.fromFile(localFile).toString());
                    Toast.makeText(MomentDetailActivity.this, "下载完成，正在播放", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentPost != null
                && currentPost.getContent() != null
                && currentPost.getContent().getVideo() != null
                && !TextUtils.isEmpty(currentPost.getContent().getVideo().getUrl())
                && exoPlayer == null) {
            initializePlayer(currentPost.getContent().getVideo().getUrl());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从其他页面返回时同步服务器状态，对方拒绝后按钮会变为「添加好友」
        if (currentPost != null && currentPost.getUser_info() != null) {
            final String authorId = currentPost.getUser_info().getUserId();
            if (authorId != null && !authorId.equals(JIM.getInstance().getCurrentUserId())
                    && btnFriendAction != null && btnFriendAction.getVisibility() == View.VISIBLE) {
                AddFriendActivity.syncPendingFriendRequestsFromServer(this, () -> runOnUiThread(() -> updateFriendStatus(authorId)));
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
    }
}
