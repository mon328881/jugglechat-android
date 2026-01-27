package com.juggle.im.android.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.juggle.im.android.widget.VideoSelectorView
import com.juggle.im.android.utils.VideoMessageManager

/**
 * 视频选择活动
 * 用于选择本地视频文件
 * 验证: 需求 18.1, 18.2
 */
class VideoSelectorActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "VideoSelectorActivity"
        private const val REQUEST_CODE_SELECT_VIDEO = 1001
        const val EXTRA_VIDEO_PATH = "video_path"
    }
    
    private lateinit var videoSelectorView: VideoSelectorView
    private var selectedVideoPath: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 创建视频选择器视图
        videoSelectorView = VideoSelectorView(this)
        setContentView(videoSelectorView)
        
        // 设置监听器
        videoSelectorView.setOnConfirmListener { videoPath ->
            // 返回选中的视频路径
            val intent = Intent()
            intent.putExtra(EXTRA_VIDEO_PATH, videoPath)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
        
        videoSelectorView.setOnCancelListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
        
        // 打开文件选择器
        openVideoSelector()
    }
    
    /**
     * 打开视频文件选择器
     */
    private fun openVideoSelector() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        intent.type = "video/*"
        startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO)
    }
    
    /**
     * 处理活动结果
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_CODE_SELECT_VIDEO) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val videoUri = data.data
                if (videoUri != null) {
                    handleVideoSelected(videoUri)
                } else {
                    Log.e(TAG, "视频 URI 为空")
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            } else {
                Log.d(TAG, "用户取消选择视频")
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
    }
    
    /**
     * 处理视频选择
     */
    private fun handleVideoSelected(videoUri: Uri) {
        try {
            // 选择视频
            val videoMessage = VideoMessageManager.selectVideo(this, videoUri)
            
            if (videoMessage != null) {
                selectedVideoPath = videoMessage.videoPath
                
                // 显示视频信息
                videoSelectorView.setVideoInfo(videoMessage.videoPath)
                
                Log.d(TAG, "视频选择成功: ${videoMessage.videoPath}")
            } else {
                Log.e(TAG, "视频选择失败")
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        } catch (e: Exception) {
            Log.e(TAG, "处理视频选择异常", e)
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
}
