package com.juggle.im.android.chat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;
import com.juggle.im.android.R;
import com.juggle.im.android.utils.LogUtil;

import java.io.File;
import java.util.Locale;

/**
 * 视频播放页：使用 ExoPlayer，支持进度条、播放/暂停、全屏/退出全屏、音量（系统键）。
 * 用于朋友圈模式点击视频、以及本地文件播放。
 */
public class VideoPlayerActivity extends AppCompatActivity {

    private static final String TAG = "VideoPlayerActivity";
    private static final String EXTRA_VIDEO_PATH = "video_path";
    private static final String EXTRA_VIDEO_NAME = "video_name";
    private static final String EXTRA_VIDEO_URL = "video_url";

    private PlayerView playerView;
    private ProgressBar progressBar;
    private LinearLayout errorLayout;
    private TextView errorMessage;
    private SeekBar seekBar;
    private ImageButton btnPlayPause;
    private TextView tvCurrentTime;
    private TextView tvDuration;

    private ExoPlayer exoPlayer;
    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private Runnable progressRunnable;
    private boolean seekBarUserDragging;

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
        // 进入即全屏：隐藏状态栏，更多空间给视频与控制条
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_video_player);

        playerView = findViewById(R.id.player_view);
        progressBar = findViewById(R.id.progress_bar);
        errorLayout = findViewById(R.id.error_layout);
        errorMessage = findViewById(R.id.error_message);
        seekBar = findViewById(R.id.seek_bar);
        btnPlayPause = findViewById(R.id.btn_play_pause);
        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvDuration = findViewById(R.id.tv_duration);

        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (seekBar != null) {
            seekBar.setEnabled(false);
        }

        applyFullscreen(true);

        String videoUrl = getIntent().getStringExtra(EXTRA_VIDEO_URL);
        if (videoUrl != null && !videoUrl.isEmpty()) {
            playWithUri(Uri.parse(videoUrl));
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

        playWithUri(Uri.fromFile(videoFile));
    }

    private void playWithUri(Uri uri) {
        progressBar.setVisibility(View.VISIBLE);
        errorLayout.setVisibility(View.GONE);

        exoPlayer = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(exoPlayer);

        // 播放/暂停按钮（播放结束后点击可重新播放）
        if (btnPlayPause != null) {
            btnPlayPause.setOnClickListener(v -> {
                if (exoPlayer != null) {
                    if (exoPlayer.getPlaybackState() == Player.STATE_ENDED) {
                        exoPlayer.seekTo(0);
                        exoPlayer.setPlayWhenReady(true);
                    } else {
                        exoPlayer.setPlayWhenReady(!exoPlayer.getPlayWhenReady());
                    }
                    updatePlayPauseIcon();
                }
            });
        }

        // 进度条：拖动时 seek
        if (seekBar != null) {
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    seekBarUserDragging = true;
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser && exoPlayer != null) {
                        long duration = exoPlayer.getDuration();
                        if (duration > 0) {
                            long pos = (long) progress * duration / 1000;
                            exoPlayer.seekTo(pos);
                        }
                    }
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    seekBarUserDragging = false;
                }
            });
        }

        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                runOnUiThread(() -> {
                    if (playbackState == Player.STATE_READY) {
                        progressBar.setVisibility(View.GONE);
                        updatePlayPauseIcon();
                        long duration = exoPlayer.getDuration();
                        if (duration > 0) {
                            if (seekBar != null) seekBar.setEnabled(true);
                            if (tvDuration != null) tvDuration.setText(formatTime(duration));
                        }
                        startProgressUpdates();
                    } else if (playbackState == Player.STATE_ENDED) {
                        progressBar.setVisibility(View.GONE);
                        stopProgressUpdates();
                        updatePlayPauseIcon();
                        // 不自动退出，用户可点播放重新播放
                    }
                });
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                runOnUiThread(() -> updatePlayPauseIcon());
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    stopProgressUpdates();
                    String msg = error.getMessage() != null ? error.getMessage() : "视频播放失败";
                    showError(msg);
                    LogUtil.e(TAG, "ExoPlayer error", error);
                });
            }
        });

        MediaItem mediaItem = MediaItem.fromUri(uri);
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(true);
    }

    private void updatePlayPauseIcon() {
        if (btnPlayPause == null || exoPlayer == null) return;
        boolean showPlay = exoPlayer.getPlaybackState() == Player.STATE_ENDED
                || !exoPlayer.getPlayWhenReady();
        btnPlayPause.setImageResource(showPlay ? R.drawable.ic_play : R.drawable.ic_pause);
        btnPlayPause.setColorFilter(0xFFFFFFFF);
    }

    private void startProgressUpdates() {
        stopProgressUpdates();
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (exoPlayer == null || seekBar == null) return;
                long duration = exoPlayer.getDuration();
                long position = exoPlayer.getCurrentPosition();
                if (duration > 0 && !seekBarUserDragging) {
                    seekBar.setProgress((int) (position * 1000 / duration));
                }
                if (tvCurrentTime != null) {
                    tvCurrentTime.setText(formatTime(position));
                }
                progressHandler.postDelayed(this, 500);
            }
        };
        progressHandler.post(progressRunnable);
    }

    private void stopProgressUpdates() {
        progressHandler.removeCallbacks(progressRunnable);
        progressRunnable = null;
    }

    private static String formatTime(long ms) {
        if (ms < 0) return "0:00";
        long s = ms / 1000;
        long m = s / 60;
        s = s % 60;
        return String.format(Locale.getDefault(), "%d:%02d", m, s);
    }

    private void applyFullscreen(boolean fullscreen) {
        Window window = getWindow();
        if (fullscreen) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowCompat.setDecorFitsSystemWindows(window, false);
                window.setStatusBarColor(android.graphics.Color.TRANSPARENT);
                WindowInsetsControllerCompat insets = new WindowInsetsControllerCompat(window, window.getDecorView());
                insets.hide(WindowInsetsCompat.Type.statusBars());
                insets.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            } else {
                View decor = window.getDecorView();
                int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN;
                decor.setSystemUiVisibility(flags);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                }
            }
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().show();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowCompat.setDecorFitsSystemWindows(window, true);
            } else {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
        }
    }

    private void showError(String message) {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (errorLayout != null) {
            errorLayout.setVisibility(View.VISIBLE);
            if (errorMessage != null) errorMessage.setText(message);
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (exoPlayer != null && exoPlayer.getPlaybackState() == Player.STATE_READY) {
            exoPlayer.setPlayWhenReady(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopProgressUpdates();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
        if (playerView != null) {
            playerView.setPlayer(null);
        }
    }
}
