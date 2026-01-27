package com.juggle.im.android.widget

import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.juggle.im.android.theme.ThemeManager
import com.juggle.im.android.utils.AccessibilityUtils

/**
 * 动态项视图组件
 * 显示发布者头像、昵称、发布时间、内容和互动按钮
 */
class FeedItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCard(context, attrs, defStyleAttr) {
    
    /**
     * 动态项数据类
     */
    data class FeedItem(
        val id: String,
        val publisherId: String,
        val publisherName: String,
        val publisherAvatar: String,
        val publishTime: String,
        val content: String,
        val images: List<String> = emptyList(),
        val videoUrl: String? = null,
        val likeCount: Int = 0,
        val commentCount: Int = 0,
        val isLiked: Boolean = false
    )
    
    private val themeManager: ThemeManager = ThemeManager.getInstance(context)
    private val theme = themeManager.getCurrentTheme()
    
    // UI 组件
    private val avatarView: ImageView
    private val publisherNameView: TextView
    private val publishTimeView: TextView
    private val contentView: TextView
    private val mediaContainer: LinearLayout
    private val likeButton: MaterialButton
    private val commentButton: MaterialButton
    private val shareButton: MaterialButton
    
    private var onLikeClickListener: (() -> Unit)? = null
    private var onCommentClickListener: (() -> Unit)? = null
    private var onShareClickListener: (() -> Unit)? = null
    
    init {
        // 创建主容器
        val mainContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(theme.spacing.md, theme.spacing.md, theme.spacing.md, theme.spacing.md)
            }
        }
        
        // 创建头部容器（头像、昵称、时间）
        val headerContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = theme.spacing.md
            }
        }
        
        // 创建头像
        avatarView = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(40, 40).apply {
                marginEnd = theme.spacing.md
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            
            // 设置圆形背景
            val ovalShape = ShapeDrawable(OvalShape()).apply {
                paint.color = theme.colors.surface
            }
            background = ovalShape
        }
        headerContainer.addView(avatarView)
        
        // 创建信息容器
        val infoContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                1f
            )
        }
        
        // 创建发布者名称
        publisherNameView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
            textSize = 14f
            setTextColor(theme.colors.onBackground)
        }
        infoContainer.addView(publisherNameView)
        
        // 创建发布时间
        publishTimeView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 2
            }
            textSize = 12f
            setTextColor(theme.colors.hint)
        }
        infoContainer.addView(publishTimeView)
        
        headerContainer.addView(infoContainer)
        mainContainer.addView(headerContainer)
        
        // 创建内容
        contentView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = theme.spacing.md
            }
            textSize = 14f
            setTextColor(theme.colors.onBackground)
        }
        mainContainer.addView(contentView)
        
        // 创建媒体容器
        mediaContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = theme.spacing.md
            }
        }
        mainContainer.addView(mediaContainer)
        
        // 创建互动按钮容器
        val actionContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        }
        
        // 创建点赞按钮
        likeButton = MaterialButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginEnd = theme.spacing.sm
            }
            setText("👍 赞")
            setOnClickListener { onLikeClickListener?.invoke() }
        }
        actionContainer.addView(likeButton)
        
        // 创建评论按钮
        commentButton = MaterialButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginStart = theme.spacing.sm
                marginEnd = theme.spacing.sm
            }
            setText("💬 评论")
            setOnClickListener { onCommentClickListener?.invoke() }
        }
        actionContainer.addView(commentButton)
        
        // 创建分享按钮
        shareButton = MaterialButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginStart = theme.spacing.sm
            }
            setText("↗️ 分享")
            setOnClickListener { onShareClickListener?.invoke() }
        }
        actionContainer.addView(shareButton)
        
        mainContainer.addView(actionContainer)
        addView(mainContainer)
        
        // 应用默认样式
        setCornerRadius(12f)
        setElevation(2f)
    }
    
    /**
     * 绑定动态项数据
     */
    fun bindData(item: FeedItem) {
        // 加载头像
        Glide.with(context)
            .load(item.publisherAvatar)
            .circleCrop()
            .into(avatarView)
        
        // 设置发布者名称
        publisherNameView.text = item.publisherName
        
        // 设置发布时间
        publishTimeView.text = item.publishTime
        
        // 设置内容
        contentView.text = item.content
        
        // 更新点赞按钮
        val likeText = if (item.isLiked) "👍 已赞(${item.likeCount})" else "👍 赞(${item.likeCount})"
        likeButton.setText(likeText)
        
        // 更新评论按钮
        commentButton.setText("💬 评论(${item.commentCount})")
    }
    
    /**
     * 设置点赞按钮点击监听
     */
    fun setOnLikeClickListener(listener: () -> Unit) {
        onLikeClickListener = listener
    }
    
    /**
     * 设置评论按钮点击监听
     */
    fun setOnCommentClickListener(listener: () -> Unit) {
        onCommentClickListener = listener
    }
    
    /**
     * 设置分享按钮点击监听
     */
    fun setOnShareClickListener(listener: () -> Unit) {
        onShareClickListener = listener
    }
    
    /**
     * 获取发布者名称（用于测试）
     */
    fun getPublisherName(): String = publisherNameView.text.toString()
    
    /**
     * 获取内容（用于测试）
     */
    fun getContent(): String = contentView.text.toString()
    
    /**
     * 获取发布时间（用于测试）
     */
    fun getPublishTime(): String = publishTimeView.text.toString()

    /**
     * 为动态项设置无障碍标签
     * 验证: 需求 12.1
     */
    fun setAccessibilityLabel(item: FeedItem) {
        val label = "${item.publisherName} 发布于 ${item.publishTime}，内容：${item.content}，" +
                "赞数：${item.likeCount}，评论数：${item.commentCount}"
        AccessibilityUtils.setViewAccessibilityLabel(this, label)
    }

    /**
     * 为头像设置无障碍描述
     * 验证: 需求 12.1, 12.6
     */
    fun setAvatarAccessibilityDescription(publisherName: String) {
        AccessibilityUtils.setImageAccessibilityDescription(avatarView, "$publisherName 的头像")
    }

    /**
     * 为互动按钮设置无障碍标签
     * 验证: 需求 12.1
     */
    fun setActionButtonsAccessibilityLabels(likeCount: Int, commentCount: Int) {
        likeButton.setAccessibilityLabel("赞，$likeCount 个赞")
        commentButton.setAccessibilityLabel("评论，$commentCount 条评论")
        shareButton.setAccessibilityLabel("分享")
    }
}
