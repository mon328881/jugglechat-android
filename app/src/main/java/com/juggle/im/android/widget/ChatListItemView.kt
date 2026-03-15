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
 * 聊天列表项视图组件
 * 显示对方头像、昵称、最后一条消息预览和时间戳
 */
class ChatListItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCard(context, attrs, defStyleAttr) {
    
    /**
     * 聊天列表项数据类
     */
    data class ChatItem(
        val id: String,
        val participantName: String,
        val participantAvatar: String,
        val lastMessage: String,
        val lastMessageTime: String,
        val unreadCount: Int = 0,
        val isPinned: Boolean = false,
        val isMuted: Boolean = false
    )
    
    private val themeManager: ThemeManager = ThemeManager.getInstance(context)
    private val theme = themeManager.getCurrentTheme()
    
    // UI 组件
    private val avatarView: ImageView
    private val nameView: TextView
    private val messageView: TextView
    private val timeView: TextView
    private val unreadBadge: TextView
    private val mainContainer: LinearLayout
    
    init {
        // 创建主容器
        mainContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(theme.spacing.md, theme.spacing.sm, theme.spacing.md, theme.spacing.sm)
            }
        }
        
        // 创建头像
        avatarView = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(48, 48).apply {
                marginEnd = theme.spacing.md
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            
            // 设置圆形背景
            val ovalShape = ShapeDrawable(OvalShape()).apply {
                paint.color = theme.colors.surface
            }
            background = ovalShape
        }
        mainContainer.addView(avatarView)
        
        // 创建右侧内容容器
        val contentContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                1f
            )
        }
        
        // 创建顶部行（昵称和时间）
        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        }
        
        nameView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                1f
            )
            textSize = 14f
            setTextColor(theme.colors.onBackground)
        }
        topRow.addView(nameView)
        
        timeView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = theme.spacing.md
            }
            textSize = 12f
            setTextColor(theme.colors.hint)
        }
        topRow.addView(timeView)
        
        contentContainer.addView(topRow)
        
        // 创建底部行（消息预览和未读徽章）
        val bottomRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 4
            }
        }
        
        messageView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                1f
            )
            textSize = 12f
            setTextColor(theme.colors.hint)
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        }
        bottomRow.addView(messageView)
        
        unreadBadge = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                24,
                24
            ).apply {
                marginStart = theme.spacing.md
            }
            textSize = 10f
            setTextColor(theme.colors.onError)
            setBackgroundColor(theme.colors.error)
            gravity = Gravity.CENTER
            visibility = GONE
            
            // 设置圆形背景
            val shape = ShapeDrawable(OvalShape()).apply {
                paint.color = theme.colors.error
            }
            background = shape
        }
        bottomRow.addView(unreadBadge)
        
        contentContainer.addView(bottomRow)
        mainContainer.addView(contentContainer)
        
        addView(mainContainer)
        
        // 应用默认样式
        setCornerRadius(12f)
        setElevation(2f)
    }
    
    /**
     * 绑定聊天项数据
     */
    fun bindData(item: ChatItem) {
        // 加载头像（占位图：加载中；失败时：默认头像）
        Glide.with(context)
            .load(item.participantAvatar)
            .circleCrop()
            .placeholder(com.juggle.im.android.R.drawable.ic_avatar_loading)
            .error(com.juggle.im.android.R.drawable.default_avatar)
            .into(avatarView)
        
        // 设置昵称
        nameView.text = item.participantName
        
        // 设置消息预览
        messageView.text = item.lastMessage
        
        // 设置时间
        timeView.text = item.lastMessageTime
        
        // 设置未读徽章
        if (item.unreadCount > 0) {
            unreadBadge.text = if (item.unreadCount > 99) "99+" else item.unreadCount.toString()
            unreadBadge.visibility = VISIBLE
            
            // 未读聊天使用加粗字体
            nameView.textStyle = android.graphics.Typeface.BOLD
            messageView.setTextColor(theme.colors.onBackground)
        } else {
            unreadBadge.visibility = GONE
            nameView.textStyle = android.graphics.Typeface.NORMAL
            messageView.setTextColor(theme.colors.hint)
        }
        
        // 设置背景色（未读聊天高亮）
        if (item.unreadCount > 0) {
            setBackgroundColor(theme.colors.surface)
        } else {
            setBackgroundColor(theme.colors.background)
        }
    }
    
    /**
     * 设置点击监听
     */
    fun setOnItemClickListener(listener: () -> Unit) {
        setOnClickListener { listener() }
    }
    
    /**
     * 设置长按监听
     */
    fun setOnItemLongClickListener(listener: () -> Unit) {
        setOnLongClickListener {
            listener()
            true
        }
    }
    
    /**
     * 获取昵称（用于测试）
     */
    fun getNickname(): String = nameView.text.toString()
    
    /**
     * 设置昵称（用于测试）
     */
    fun setNickname(nickname: String) {
        nameView.text = nickname
    }
    
    /**
     * 获取消息预览（用于测试）
     */
    fun getMessagePreview(): String = messageView.text.toString()
    
    /**
     * 设置消息预览（用于测试）
     */
    fun setMessagePreview(message: String) {
        messageView.text = message
    }
    
    /**
     * 获取时间戳（用于测试）
     */
    fun getTimestamp(): String = timeView.text.toString()
    
    /**
     * 设置时间戳（用于测试）
     */
    fun setTimestamp(timestamp: String) {
        timeView.text = timestamp
    }

    /**
     * 为聊天列表项设置无障碍标签
     * 验证: 需求 12.1
     */
    fun setAccessibilityLabel(item: ChatItem) {
        val unreadInfo = if (item.unreadCount > 0) "，有 ${item.unreadCount} 条未读消息" else ""
        val label = "${item.participantName}，最后消息：${item.lastMessage}，时间：${item.lastMessageTime}$unreadInfo"
        AccessibilityUtils.setViewAccessibilityLabel(this, label)
    }

    /**
     * 为头像设置无障碍描述
     * 验证: 需求 12.1, 12.6
     */
    fun setAvatarAccessibilityDescription(participantName: String) {
        AccessibilityUtils.setImageAccessibilityDescription(avatarView, "$participantName 的头像")
    }

    /**
     * 为未读徽章设置无障碍标签
     * 验证: 需求 12.1
     */
    fun setUnreadBadgeAccessibilityLabel(unreadCount: Int) {
        if (unreadCount > 0) {
            val label = "有 $unreadCount 条未读消息"
            AccessibilityUtils.setStatusAccessibilityLabel(unreadBadge, label)
        }
    }
}
