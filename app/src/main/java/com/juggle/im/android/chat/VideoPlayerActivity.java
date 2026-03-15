package com.juggle.im.android.chat;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import com.juggle.im.android.utils.LogUtil;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.juggle.im.android.R;

import java.io.File;

/**
 * 自定义视频播放器Activity
 * 用于播放本地视频文件，支持多种视频格式
 */
public class VideoPlayerActivity extends AppCompatActivity {
    private static final String TAG = "VideoPlayerActivity";
    private static final String EXTRA_VIDEO_PATH = "video_path";
    private static final String EXTRA_VIDEO_NAME = "video_name";
    private static final String EXTRA_VIDEO_URL = "video_url";

    private VideoView videoView;
    private ProgressBar progressBar;
    private LinearLayout errorLayout;
    private TextView errorMessage;

    public static void start(Context context, String videoPath, String videoName) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtra(EXTRA_VIDEO_PATH, videoPath);
        intent.putExtra(EXTRA_VIDEO_NAME, videoName);
        context.startActivity(intent);
    }

    /** 通过网络 URL 直接播放（用于朋友圈列表点击视频） */
    public static void startWithUrl(Context context, String videoUrl) {
        if (context == null || videoUrl == null || videoUrl.isEmpty()) return;
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtra(EXTRA_VIDEO_URL, videoUrl);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        videoView = findViewById(R.id.video_view);
        progressBar = findViewById(R.id.progress_bar);
        errorLayout = findViewById(R.id.error_layout);
        errorMessage = findViewById(R.id.error_message);

        String videoUrl = getIntent().getStringExtra(EXTRA_VIDEO_URL);
        if (videoUrl != null && !videoUrl.isEmpty()) {
            playVideoUri(Uri.parse(videoUrl), "视频");
            return;
        }

        String videoPath = getIntent().getStringExtra(EXTRA_VIDEO_PATH);
        String videoName = getIntent().getStringExtra(EXTRA_VIDEO_NAME);

        if (videoPath == null || videoPath.isEmpty()) {
            showError("视频路径无效");
            return;
        }

        File videoFile = new File(videoPath);
        if (!videoFile.exists()) {
            showError("视频文件不存在");
            return;
        }

        playVideo(videoPath, videoName);
    }

    private void playVideoUri(Uri videoUri, String videoName) {
        try {
            progressBar.setVisibility(View.VISIBLE);
            errorLayout.setVisibility(View.GONE);
            videoView.setVideoURI(videoUri);
            videoView.setOnPreparedListener(mp -> {
                progressBar.setVisibility(View.GONE);
                videoView.start();
                LogUtil.d(TAG, "视频开始播放: " + videoName);
            });
            videoView.setOnCompletionListener(mp -> finish());
            videoView.setOnErrorListener((mp, what, extra) -> {
                progressBar.setVisibility(View.GONE);
                showError("视频播放失败 (错误码: " + what + ")");
                return true;
            });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            showError("播放异常: " + e.getMessage());
        }
    }

    private void playVideo(String videoPath, String videoName) {
        try {
            progressBar.setVisibility(View.VISIBLE);
            errorLayout.setVisibility(View.GONE);

            Uri videoUri = Uri.fromFile(new File(videoPath));
            videoView.setVideoURI(videoUri);

            // 设置视频准备完成监听
            videoView.setOnPreparedListener(mp -> {
                progressBar.setVisibility(View.GONE);
                videoView.start();
                LogUtil.d(TAG, "视频开始播放: " + videoName);
            });

            // 设置视频完成监听
            videoView.setOnCompletionListener(mp -> {
                LogUtil.d(TAG, "视频播放完成");
                finish();
            });

            // 设置错误监听
            videoView.setOnErrorListener((mp, what, extra) -> {
                LogUtil.e(TAG, "视频播放错误: what=" + what + ", extra=" + extra);
                progressBar.setVisibility(View.GONE);
                showError("视频播放失败 (错误码: " + what + ")");
                return true;
            });

        } catch (Exception e) {
            LogUtil.e(TAG, "播放视频异常", e);
            progressBar.setVisibility(View.GONE);
            showError("播放视频异常: " + e.getMessage());
        }
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);
        errorMessage.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoView != null && !videoView.isPlaying()) {
            videoView.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView != null) {
            videoView.stopPlayback();
        }
    }
}
