package com.juggle.im.android.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.juggle.im.android.R

/**
 * 媒体加载进度显示 View
 * 用于显示图片、视频等媒体的加载进度
 */
class MediaLoadingProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var cancelButton: android.widget.Button

    private var onCancelListener: (() -> Unit)? = null

    init {
        initView()
    }

    private fun initView() {
        val view = LayoutInflater.from(context).inflate(R.layout.view_media_loading_progress, this, true)
        progressBar = view.findViewById(R.id.progress_bar)
        progressText = view.findViewById(R.id.progress_text)
        cancelButton = view.findViewById(R.id.cancel_button)

        cancelButton.setOnClickListener {
            onCancelListener?.invoke()
        }
    }

    /**
     * 更新加载进度
     * @param progress 进度百分比（0-100）
     * @param bytesRead 已读取字节数
     * @param totalBytes 总字节数
     */
    fun updateProgress(progress: Int, bytesRead: Long, totalBytes: Long) {
        progressBar.progress = progress
        val progressStr = formatProgress(bytesRead, totalBytes, progress)
        progressText.text = progressStr
    }

    /**
     * 设置加载完成
     */
    fun setComplete() {
        progressBar.progress = 100
        progressText.text = "加载完成"
        cancelButton.isEnabled = false
    }

    /**
     * 设置加载失败
     * @param error 错误信息
     */
    fun setError(error: String) {
        progressText.text = "加载失败: $error"
        cancelButton.isEnabled = false
    }

    /**
     * 设置加载取消
     */
    fun setCancel() {
        progressText.text = "已取消"
        cancelButton.isEnabled = false
    }

    /**
     * 设置取消按钮点击监听器
     * @param listener 监听器
     */
    fun setOnCancelListener(listener: () -> Unit) {
        onCancelListener = listener
    }

    /**
     * 格式化进度信息
     * @param bytesRead 已读取字节数
     * @param totalBytes 总字节数
     * @param progress 进度百分比
     * @return 格式化后的进度字符串
     */
    private fun formatProgress(bytesRead: Long, totalBytes: Long, progress: Int): String {
        val readStr = formatBytes(bytesRead)
        val totalStr = formatBytes(totalBytes)
        return "$readStr / $totalStr ($progress%)"
    }

    /**
     * 格式化字节数
     * @param bytes 字节数
     * @return 格式化后的字符串
     */
    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / (1024 * 1024)} MB"
        }
    }
}
