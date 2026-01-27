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
import com.juggle.im.android.theme.ThemeManager

/**
 * 媒体消息显示组件
 * 支持图片和视频消息的缩略图显示
 */
class MediaMessageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    
    /**
     * 媒体类型
     */
    enum class MediaType {
        IMAGE,  // 图片
        VIDEO   // 视频
    }
    
    private val themeManager: ThemeManager = ThemeManager.getInstance(context)
    private val theme = themeManager.getCurrentTheme()
    
    private val mediaContainer: LinearLayout
    private val thumbnailImage: ImageView
    private val playButton: ImageView
    private val durationText: TextView
    
    private var mediaType: MediaType = MediaType.IMAGE
    private var onMediaClickListener: (() -> Unit)? = null
    
    init {
        // 创建媒体容器
        mediaContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                200,
                200
            )
            
            // 设置背景
            val shape = ShapeDrawable(RoundRectShape(
                floatArrayOf(8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f),
                null,
                null
            )).apply {
                paint.color = theme.colors.surface
            }
            background = shape
        }
        
        // 创建缩略图
        thumbnailImage = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(theme.colors.background)
            
            setOnClickListener {
                onMediaClickListener?.invoke()
            }
        }
        mediaContainer.addView(thumbnailImage)
        
        // 创建播放按钮（仅视频显示）
        playButton = ImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                48,
                48
            ).apply {
                gravity = Gravity.CENTER
            }
            setImageResource(android.R.drawable.ic_media_play)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            visibility = GONE
            
            setOnClickListener {
                onMediaClickListener?.invoke()
            }
        }
        
        // 创建时长文本（仅视频显示）
        durationText = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(theme.spacing.sm, 0, theme.spacing.sm, theme.spacing.sm)
                gravity = Gravity.END
            }
            textSize = 12f
            setTextColor(theme.colors.onBackground)
            visibility = GONE
        }
        mediaContainer.addView(durationText)
        
        addView(mediaContainer)
        addView(playButton)
    }
    
    /**
     * 设置媒体类型
     */
    fun setMediaType(type: MediaType) {
        mediaType = type
        
        when (type) {
            MediaType.IMAGE -> {
                playButton.visibility = GONE
                durationText.visibility = GONE
            }
            MediaType.VIDEO -> {
                playButton.visibility = VISIBLE
                durationText.visibility = VISIBLE
            }
        }
    }
    
    /**
     * 设置缩略图
     */
    fun setThumbnail(imageResource: Int) {
        thumbnailImage.setImageResource(imageResource)
    }
    
    /**
     * 设置视频时长
     */
    fun setVideoDuration(duration: String) {
        durationText.text = duration
    }
    
    /**
     * 设置媒体点击监听器
     */
    fun setOnMediaClickListener(listener: () -> Unit) {
        onMediaClickListener = listener
    }
    
    /**
     * 获取媒体类型
     */
    fun getMediaType(): MediaType = mediaType
    
    /**
     * 获取缩略图 Drawable（用于测试）
     */
    fun getThumbnailDrawable() = thumbnailImage.drawable
    
    /**
     * 获取视频时长（用于测试）
     */
    fun getVideoDuration(): String = durationText.text.toString()
    
    /**
     * 点击媒体（用于测试）
     */
    fun clickMedia() {
        onMediaClickListener?.invoke()
    }
    
    /**
     * 检查播放按钮是否可见（用于测试）
     */
    fun isPlayButtonVisible(): Boolean = playButton.visibility == VISIBLE
    
    /**
     * 检查时长文本是否可见（用于测试）
     */
    fun isDurationTextVisible(): Boolean = durationText.visibility == VISIBLE
}
