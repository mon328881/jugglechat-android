package com.juggle.im.android.widget

import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Button
import com.juggle.im.android.theme.ThemeManager
import com.juggle.im.android.utils.VideoUtils
import java.io.File

/**
 * 视频选择器视图
 * 显示视频预览、文件信息和操作按钮
 * 验证: 需求 18.1, 18.2
 */
class VideoSelectorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    
    private val themeManager: ThemeManager = ThemeManager.getInstance(context)
    private val theme = themeManager.getCurrentTheme()
    
    private val container: LinearLayout
    private val previewImage: ImageView
    private val infoContainer: LinearLayout
    private val fileNameText: TextView
    private val fileSizeText: TextView
    private val durationText: TextView
    private val resolutionText: TextView
    private val buttonContainer: LinearLayout
    private val confirmButton: Button
    private val cancelButton: Button
    
    private var selectedVideoPath: String? = null
    private var onConfirmListener: ((String) -> Unit)? = null
    private var onCancelListener: (() -> Unit)? = null
    
    init {
        // 创建主容器
        container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
            setPadding(theme.spacing.md, theme.spacing.md, theme.spacing.md, theme.spacing.md)
            
            // 设置背景
            val shape = ShapeDrawable(RoundRectShape(
                floatArrayOf(12f, 12f, 12f, 12f, 12f, 12f, 12f, 12f),
                null,
                null
            )).apply {
                paint.color = theme.colors.surface
            }
            background = shape
        }
        
        // 创建预览图像
        previewImage = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                300
            ).apply {
                setMargins(0, 0, 0, theme.spacing.md)
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(theme.colors.background)
        }
        container.addView(previewImage)
        
        // 创建信息容器
        infoContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, theme.spacing.md)
            }
        }
        
        // 文件名
        fileNameText = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, theme.spacing.sm)
            }
            textSize = 14f
            setTextColor(theme.colors.onBackground)
        }
        infoContainer.addView(fileNameText)
        
        // 文件大小
        fileSizeText = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, theme.spacing.sm)
            }
            textSize = 12f
            setTextColor(theme.colors.onBackground.let { 
                (it and 0xFFFFFF) or 0x80000000.toInt()
            })
        }
        infoContainer.addView(fileSizeText)
        
        // 视频时长
        durationText = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, theme.spacing.sm)
            }
            textSize = 12f
            setTextColor(theme.colors.onBackground.let { 
                (it and 0xFFFFFF) or 0x80000000.toInt()
            })
        }
        infoContainer.addView(durationText)
        
        // 分辨率
        resolutionText = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, theme.spacing.sm)
            }
            textSize = 12f
            setTextColor(theme.colors.onBackground.let { 
                (it and 0xFFFFFF) or 0x80000000.toInt()
            })
        }
        infoContainer.addView(resolutionText)
        
        container.addView(infoContainer)
        
        // 创建按钮容器
        buttonContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        }
        
        // 取消按钮
        cancelButton = Button(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(0, 0, theme.spacing.sm, 0)
            }
            text = "取消"
            setBackgroundColor(theme.colors.secondary)
            setTextColor(theme.colors.onSecondary)
            setOnClickListener { onCancelListener?.invoke() }
        }
        buttonContainer.addView(cancelButton)
        
        // 确认按钮
        confirmButton = Button(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                1f
            )
            text = "确认"
            setBackgroundColor(theme.colors.primary)
            setTextColor(theme.colors.onPrimary)
            setOnClickListener {
                selectedVideoPath?.let { path ->
                    onConfirmListener?.invoke(path)
                }
            }
        }
        buttonContainer.addView(confirmButton)
        
        container.addView(buttonContainer)
        addView(container)
    }
    
    /**
     * 设置视频信息
     */
    fun setVideoInfo(videoPath: String) {
        selectedVideoPath = videoPath
        val file = File(videoPath)
        
        // 设置文件名
        fileNameText.text = "文件名: ${file.name}"
        
        // 设置文件大小
        val fileSize = VideoUtils.getVideoFileSize(videoPath)
        fileSizeText.text = "大小: ${formatFileSize(fileSize)}"
        
        // 设置视频时长
        val duration = VideoUtils.getVideoDurationFromUri(context, android.net.Uri.fromFile(file))
        durationText.text = "时长: ${VideoUtils.formatDuration(duration)}"
        
        // 设置分辨率
        val resolution = VideoUtils.getVideoResolution(videoPath)
        resolutionText.text = if (resolution != null) {
            "分辨率: ${resolution.first}x${resolution.second}"
        } else {
            "分辨率: 未知"
        }
        
        // 设置预览图像
        val thumbnail = VideoUtils.getVideoThumbnail(videoPath)
        if (thumbnail != null) {
            previewImage.setImageBitmap(thumbnail)
        } else {
            previewImage.setImageResource(android.R.drawable.ic_media_play)
        }
    }
    
    /**
     * 格式化文件大小
     */
    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
    
    /**
     * 设置确认监听器
     */
    fun setOnConfirmListener(listener: (String) -> Unit) {
        onConfirmListener = listener
    }
    
    /**
     * 设置取消监听器
     */
    fun setOnCancelListener(listener: () -> Unit) {
        onCancelListener = listener
    }
    
    /**
     * 获取选中的视频路径
     */
    fun getSelectedVideoPath(): String? = selectedVideoPath
    
    /**
     * 获取文件名（用于测试）
     */
    fun getFileName(): String = fileNameText.text.toString()
    
    /**
     * 获取文件大小（用于测试）
     */
    fun getFileSize(): String = fileSizeText.text.toString()
    
    /**
     * 获取视频时长（用于测试）
     */
    fun getVideoDuration(): String = durationText.text.toString()
    
    /**
     * 获取分辨率（用于测试）
     */
    fun getResolution(): String = resolutionText.text.toString()
}
