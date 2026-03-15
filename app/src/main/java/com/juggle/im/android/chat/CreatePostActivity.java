package com.juggle.im.android.chat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import com.juggle.im.android.utils.LogUtil;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.juggle.im.JIM;
import com.juggle.im.JIMConst;
import com.juggle.im.android.R;
import com.juggle.im.android.chat.utils.FileUtils;
import com.juggle.im.android.server.beans.CommunityInfoBean;
import com.juggle.im.android.server.beans.ContentBean;
import com.juggle.im.android.server.beans.ImageBean;
import com.juggle.im.android.server.beans.PostBean;
import com.juggle.im.android.server.beans.VideoBean;
import com.juggle.im.android.server.http.ApiCallback;
import com.juggle.im.android.server.http.ServiceManager;
import com.juggle.im.android.utils.AvatarUtils;
import com.qiniu.android.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class CreatePostActivity extends AppCompatActivity {
    private EditText editPostContent;
    private RecyclerView mImageRecyclerView;
    private RecyclerView mCommunityTagsRecyclerView;
    private LinearLayout mCommunityTagSection;
    private MediaAdapter mMediaAdapter;
    private CommunityTagAdapter mCommunityTagAdapter;
    private LinkedHashMap<String, String> mImageUrls = new LinkedHashMap<>();
    private String mVideoUrl = null;
    private String mVideoSnapshotUrl = null;
    private Bitmap mVideoThumbnail = null;
    private boolean mIsUploadingVideo = false;
    private static final int REQUEST_CODE_PICK_IMAGES = 1001;
    private static final int REQUEST_CODE_PICK_VIDEO = 1002;
    private int uploadingCount = 0;
    private boolean isSubmitting = false;
    
    private String mCurrentPage = "moments";
    private List<String> mAvailableCommunityTags = new ArrayList<>();
    private Set<String> mSelectedCommunityTags = new HashSet<>();
    
    private static final String[] DEFAULT_TAGS = {"推荐", "直播", "短剧", "美食", "穿搭", "旅行"};

    public static void start(Context context) {
        start(context, "moments");
    }

    public static void start(Context context, String currentPage) {
        Intent intent = new Intent(context, CreatePostActivity.class);
        intent.putExtra("current_page", currentPage);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        Intent intent = getIntent();
        if (intent != null) {
            mCurrentPage = intent.getStringExtra("current_page");
            if (TextUtils.isEmpty(mCurrentPage)) {
                mCurrentPage = "moments";
            }
        }

        Toolbar toolbar = findViewById(R.id.toolbar_create_post);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("发表动态");
        }

        toolbar.setNavigationOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(editPostContent.getWindowToken(), 0);
            editPostContent.clearFocus();
            finish();
        });

        editPostContent = findViewById(R.id.edit_post_content);
        mImageRecyclerView = findViewById(R.id.rv_images);
        mCommunityTagSection = findViewById(R.id.community_tag_section);
        mCommunityTagsRecyclerView = findViewById(R.id.rv_community_tags);

        mImageRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mMediaAdapter = new MediaAdapter(this, mImageUrls);
        mImageRecyclerView.setAdapter(mMediaAdapter);

        mCommunityTagsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mCommunityTagAdapter = new CommunityTagAdapter(this, mAvailableCommunityTags, mSelectedCommunityTags);
        mCommunityTagsRecyclerView.setAdapter(mCommunityTagAdapter);

        if ("community".equals(mCurrentPage)) {
            mCommunityTagSection.setVisibility(View.VISIBLE);
            // 从后端获取社区标签
            loadCommunityTags();
        } else {
            mCommunityTagSection.setVisibility(View.GONE);
        }

        if (intent != null) {
            ArrayList<String> imageUrls = intent.getStringArrayListExtra("image_urls");
            if (imageUrls != null && !imageUrls.isEmpty()) {
                uploadingCount = imageUrls.size();
                for (String path: imageUrls) {
                    mImageUrls.put(path, "");
                    String u = FileUtils.convertContentUriToFile(getApplicationContext(), path);
                    JIM.getInstance().getMessageManager().uploadImage(u, new JIMConst.IResultCallback<String>() {
                        @Override
                        public void onSuccess(String s) {
                            mImageUrls.put(path, s);
                            uploadingCount--;
                            if (uploadingCount == 0 && isSubmitting) {
                                doSubmitPost();
                            }
                        }

                        @Override
                        public void onError(int i) {
                            LogUtil.e("createpost", "error: " + i);
                            uploadingCount--;
                            Toast.makeText(CreatePostActivity.this, "图片上传失败", Toast.LENGTH_SHORT).show();
                            isSubmitting = false;
                        }
                    });
                }
                mMediaAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_post) {
            submitPost();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGES && resultCode == RESULT_OK && data != null) {
            ArrayList<String> selectedImages = data.getStringArrayListExtra("selected_images");
            if (selectedImages != null && !selectedImages.isEmpty()) {
                for (String imagePath : selectedImages) {
                    if (mImageUrls.size() < 9) {
                        mImageUrls.put(imagePath, "");
                        uploadingCount++;
                        String u = FileUtils.convertContentUriToFile(getApplicationContext(), imagePath);
                        JIM.getInstance().getMessageManager().uploadImage(u, new JIMConst.IResultCallback<String>() {
                            @Override
                            public void onSuccess(String s) {
                                mImageUrls.put(imagePath, s);
                                uploadingCount--;
                                if (uploadingCount == 0 && isSubmitting) {
                                    doSubmitPost();
                                }
                            }

                            @Override
                            public void onError(int i) {
                                LogUtil.e("createpost", "error: " + i);
                                uploadingCount--;
                                Toast.makeText(CreatePostActivity.this, "图片上传失败", Toast.LENGTH_SHORT).show();
                                isSubmitting = false;
                            }
                        });
                    }
                }
                mMediaAdapter.notifyDataSetChanged();
            }
        } else if (requestCode == REQUEST_CODE_PICK_VIDEO && resultCode == RESULT_OK && data != null) {
            android.net.Uri videoUri = data.getData();
            if (videoUri != null) {
                handlePickedVideo(videoUri);
            }
        }
    }

    private void submitPost() {
        String content = editPostContent.getText().toString().trim();
        if (TextUtils.isEmpty(content) && mImageUrls.isEmpty() && TextUtils.isEmpty(mVideoUrl)) {
            Toast.makeText(this, "内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("community".equals(mCurrentPage) && mSelectedCommunityTags.isEmpty()) {
            Toast.makeText(this, "请选择至少一个社区标签", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean allImagesUploaded = true;
        for (String url : mImageUrls.values()) {
            if (StringUtils.isBlank(url)) {
                allImagesUploaded = false;
                break;
            }
        }

        if (!allImagesUploaded) {
            isSubmitting = true;
            Toast.makeText(this, "图片上传中，请稍候...", Toast.LENGTH_SHORT).show();
            return;
        }

        doSubmitPost();
    }

    private void doSubmitPost() {
        String content = editPostContent.getText().toString().trim();
        
        ContentBean postContent = new ContentBean();
        postContent.setText(content);

        if (!mImageUrls.isEmpty()) {
            List<ImageBean> images = new ArrayList<>();
            for (String url : mImageUrls.values()) {
                if (!StringUtils.isBlank(url)) {
                    ImageBean image = new ImageBean();
                    image.setUrl(url);
                    images.add(image);
                }
            }
            if (!images.isEmpty()) {
                postContent.setImages(images);
            }
        }

        if (!TextUtils.isEmpty(mVideoUrl)) {
            VideoBean video = new VideoBean();
            video.setUrl(mVideoUrl);
            if (!TextUtils.isEmpty(mVideoSnapshotUrl)) {
                video.setSnapshot_url(mVideoSnapshotUrl);
            }
            postContent.setVideo(video);
        }

        PostBean post = new PostBean();
        post.setContent(postContent);

        if ("community".equals(mCurrentPage) && !mSelectedCommunityTags.isEmpty()) {
            CommunityInfoBean communityInfo = new CommunityInfoBean();
            communityInfo.setTags(new ArrayList<>(mSelectedCommunityTags));
            post.setCommunity_info(communityInfo);
        }

        ServiceManager.getMomentService().addPost(post, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(CreatePostActivity.this, "发表成功", Toast.LENGTH_SHORT).show();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(editPostContent.getWindowToken(), 0);
                editPostContent.clearFocus();
                Intent it = new Intent();
                it.putExtra("result", 0);
                setResult(RESULT_OK, it);
                finish();
            }

            @Override
            public void onError(int code, String message) {
                Toast.makeText(CreatePostActivity.this, "发表失败: " + message, Toast.LENGTH_SHORT).show();
                isSubmitting = false;
            }
        });
    }

    private class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {
        private Context mContext;
        private LinkedHashMap<String, String> mImageUrls;

        public MediaAdapter(Context context, LinkedHashMap<String, String> imageUrls) {
            this.mContext = context;
            this.mImageUrls = imageUrls;
        }

        @Override
        public MediaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_media, parent, false);
            return new MediaViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MediaViewHolder holder, int position) {
            if (position == mImageUrls.size() && mImageUrls.size() < 9 && TextUtils.isEmpty(mVideoUrl)) {
                holder.btnAdd.setVisibility(View.VISIBLE);
                holder.ivMedia.setVisibility(View.GONE);
                holder.btnDelete.setVisibility(View.GONE);
                holder.ivVideoPlay.setVisibility(View.GONE);
                holder.progressUpload.setVisibility(View.GONE);

                holder.btnAdd.setOnClickListener(v -> showMediaPickerMenu());
            } else if (position < mImageUrls.size()) {
                holder.btnAdd.setVisibility(View.GONE);
                holder.ivMedia.setVisibility(View.VISIBLE);
                holder.btnDelete.setVisibility(View.VISIBLE);
                holder.ivVideoPlay.setVisibility(View.GONE);
                holder.progressUpload.setVisibility(View.GONE);

                final String imageUrl = new ArrayList<>(mImageUrls.keySet()).get(position);
                AvatarUtils.loadImage(holder.ivMedia, imageUrl);

                holder.btnDelete.setOnClickListener(v -> {
                    mImageUrls.remove(imageUrl);
                    notifyItemRemoved(position);
                });
            } else if (!TextUtils.isEmpty(mVideoUrl)) {
                holder.btnAdd.setVisibility(View.GONE);
                holder.ivMedia.setVisibility(View.VISIBLE);
                holder.btnDelete.setVisibility(View.VISIBLE);
                holder.ivVideoPlay.setVisibility(View.VISIBLE);
                
                if (mIsUploadingVideo) {
                    holder.progressUpload.setVisibility(View.VISIBLE);
                    holder.ivMedia.setImageBitmap(mVideoThumbnail);
                } else {
                    holder.progressUpload.setVisibility(View.GONE);
                    if (mVideoThumbnail != null) {
                        holder.ivMedia.setImageBitmap(mVideoThumbnail);
                    } else {
                        holder.ivMedia.setImageResource(R.drawable.ic_input_img);
                    }
                }

                holder.btnDelete.setOnClickListener(v -> {
                    mVideoUrl = null;
                    mVideoSnapshotUrl = null;
                    mVideoThumbnail = null;
                    mIsUploadingVideo = false;
                    notifyDataSetChanged();
                });
            }
        }

        @Override
        public int getItemCount() {
            if (TextUtils.isEmpty(mVideoUrl) && mImageUrls.size() < 9) {
                return mImageUrls.size() + 1;
            } else if (!TextUtils.isEmpty(mVideoUrl)) {
                return mImageUrls.size() + 1;
            } else {
                return mImageUrls.size();
            }
        }

        class MediaViewHolder extends RecyclerView.ViewHolder {
            ImageView ivMedia;
            ImageView btnAdd;
            ImageView btnDelete;
            ImageView ivVideoPlay;
            ProgressBar progressUpload;

            MediaViewHolder(View itemView) {
                super(itemView);
                ivMedia = itemView.findViewById(R.id.iv_media);
                btnAdd = itemView.findViewById(R.id.btn_add);
                btnDelete = itemView.findViewById(R.id.btn_delete);
                ivVideoPlay = itemView.findViewById(R.id.iv_video_play);
                progressUpload = itemView.findViewById(R.id.progress_upload);
            }
        }
    }

    private class CommunityTagAdapter extends RecyclerView.Adapter<CommunityTagAdapter.TagViewHolder> {
        private Context mContext;
        private List<String> mTags;
        private Set<String> mSelectedTags;

        public CommunityTagAdapter(Context context, List<String> tags, Set<String> selectedTags) {
            this.mContext = context;
            this.mTags = tags;
            this.mSelectedTags = selectedTags;
        }

        @Override
        public TagViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_community_tag, parent, false);
            return new TagViewHolder(view);
        }

        @Override
        public void onBindViewHolder(TagViewHolder holder, int position) {
            String tag = mTags.get(position);
            holder.chip.setText(tag);
            holder.chip.setChecked(mSelectedTags.contains(tag));
            holder.chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    mSelectedTags.add(tag);
                } else {
                    mSelectedTags.remove(tag);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mTags.size();
        }

        class TagViewHolder extends RecyclerView.ViewHolder {
            com.google.android.material.chip.Chip chip;

            TagViewHolder(View itemView) {
                super(itemView);
                chip = itemView.findViewById(R.id.chip_tag);
            }
        }
    }

    private void showMediaPickerMenu() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("选择媒体类型");
        builder.setItems(new String[]{"图片", "视频"}, (dialog, which) -> {
            if (which == 0) {
                Intent intent = new Intent(CreatePostActivity.this, AlbumActivity.class);
                startActivityForResult(intent, REQUEST_CODE_PICK_IMAGES);
            } else {
                pickVideoFromGallery();
            }
        });
        builder.show();
    }

    private void loadCommunityTags() {
        ServiceManager.getMomentService().getCommunityTags(new ApiCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> tagList) {
                runOnUiThread(() -> {
                    if (tagList != null && !tagList.isEmpty()) {
                        mAvailableCommunityTags.clear();
                        mAvailableCommunityTags.addAll(tagList);
                    } else {
                        // 如果后端返回空列表，使用默认标签
                        mAvailableCommunityTags.clear();
                        mAvailableCommunityTags.addAll(Arrays.asList(DEFAULT_TAGS));
                    }
                    mCommunityTagAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(int code, String message) {
                LogUtil.e("CreatePostActivity", "获取社区标签失败: " + message);
                runOnUiThread(() -> {
                    // 获取失败时使用默认标签
                    mAvailableCommunityTags.clear();
                    mAvailableCommunityTags.addAll(Arrays.asList(DEFAULT_TAGS));
                    mCommunityTagAdapter.notifyDataSetChanged();
                });
            }
        });
    }

    private void pickVideoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        intent.setType("video/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_VIDEO);
    }

    private void handlePickedVideo(android.net.Uri uri) {
        String localPath = FileUtils.convertContentUriToFile(getApplicationContext(), uri.toString(), "temp_video.mp4");
        if (TextUtils.isEmpty(localPath)) {
            Toast.makeText(this, "无法读取视频文件", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(localPath, MediaStore.Images.Thumbnails.MINI_KIND);
            mVideoThumbnail = thumb;
        } catch (Exception e) {
            LogUtil.e("createpost", "generate video thumbnail error", e);
            mVideoThumbnail = null;
        }

        mIsUploadingVideo = true;
        mVideoUrl = null;
        mMediaAdapter.notifyDataSetChanged();
        Toast.makeText(this, "正在上传", Toast.LENGTH_SHORT).show();

        // 与图片同一通道：用 JIM.uploadImage 上传视频文件，走 im-server file_cred，拿到的 URL 与图片同源、可播放
        JIM.getInstance().getMessageManager().uploadImage(localPath, new JIMConst.IResultCallback<String>() {
            @Override
            public void onSuccess(String url) {
                mVideoUrl = url;
                mIsUploadingVideo = false;
                mImageUrls.clear();
                mMediaAdapter.notifyDataSetChanged();
                Toast.makeText(CreatePostActivity.this, "视频上传成功", Toast.LENGTH_SHORT).show();
                // 上传首帧作为封面，与图片同通道
                uploadVideoThumbnailIfNeed();
            }

            @Override
            public void onError(int errorCode) {
                mIsUploadingVideo = false;
                mVideoUrl = null;
                mVideoThumbnail = null;
                mVideoSnapshotUrl = null;
                mMediaAdapter.notifyDataSetChanged();
                Toast.makeText(CreatePostActivity.this, "视频上传失败: " + errorCode, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** 若有首帧缩略图则上传为封面 URL，供发表时写入 snapshot_url */
    private void uploadVideoThumbnailIfNeed() {
        if (mVideoThumbnail == null) return;
        java.io.File cacheDir = getCacheDir();
        java.io.File thumbFile = new java.io.File(cacheDir, "video_cover_" + System.currentTimeMillis() + ".jpg");
        try {
            java.io.FileOutputStream fos = new java.io.FileOutputStream(thumbFile);
            mVideoThumbnail.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, fos);
            fos.close();
            String path = thumbFile.getAbsolutePath();
            JIM.getInstance().getMessageManager().uploadImage(path, new JIMConst.IResultCallback<String>() {
                @Override
                public void onSuccess(String url) {
                    mVideoSnapshotUrl = url;
                }
                @Override
                public void onError(int errorCode) {
                    // 封面上传失败不影响发表，仅无封面图
                }
            });
        } catch (Throwable e) {
            LogUtil.e("createpost", "save/upload video thumbnail error", e);
        }
    }
}
