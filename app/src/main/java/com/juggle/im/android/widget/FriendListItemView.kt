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
 * 朋友列表项视图组件
 * 显示头像、昵称、在线状态和最后活跃时间
 */
class FriendListItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCard(context, attrs, defStyleAttr) {
    
    /**
     * 朋友列表项数据类
     */
    data class FriendItem(
        val id: String,
        val nickname: String,
        val avatar: String,
        val isOnline: Boolean = false,
        val lastActiveTime: String = "",
        val signature: String = ""
    )
    
    private val themeManager: ThemeManager = ThemeManager.getInstance(context)
    private val theme = themeManager.getCurrentTheme()
    
    // UI 组件
    private val avatarView: ImageView
    private val onlineIndicator: ImageView
    private val nicknameView: TextView
    private val statusView: TextView
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
        
        // 创建头像容器（用于放置在线指示器）
        val avatarContainer = FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(48, 48).apply {
                marginEnd = theme.spacing.md
            }
        }
        
        // 创建头像
        avatarView = ImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            
            // 设置圆形背景
            val ovalShape = ShapeDrawable(OvalShape()).apply {
                paint.color = theme.colors.surface
            }
            background = ovalShape
        }
        avatarContainer.addView(avatarView)
        
        // 创建在线指示器
        onlineIndicator = ImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(12, 12).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                setMargins(0, 0, 2, 2)
            }
            
            // 设置绿色圆形背景
            val shape = ShapeDrawable(OvalShape()).apply {
                paint.color = theme.colors.primary
            }
            background = shape
            visibility = GONE
        }
        avatarContainer.addView(onlineIndicator)
        
        mainContainer.addView(avatarContainer)
        
        // 创建右侧内容容器
        val contentContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                1f
            )
        }
        
        // 创建昵称
        nicknameView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
            textSize = 14f
            setTextColor(theme.colors.onBackground)
        }
        contentContainer.addView(nicknameView)
        
        // 创建状态/最后活跃时间
        statusView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 4
            }
            textSize = 12f
            setTextColor(theme.colors.hint)
        }
        contentContainer.addView(statusView)
        
        mainContainer.addView(contentContainer)
        addView(mainContainer)
        
        // 应用默认样式
        setCornerRadius(12f)
        setElevation(2f)
    }
    
    /**
     * 绑定朋友项数据
     */
    fun bindData(item: FriendItem) {
        // 加载头像
        Glide.with(context)
            .load(item.avatar)
            .circleCrop()
            .into(avatarView)
        
        // 设置昵称
        nicknameView.text = item.nickname
        
        // 设置在线状态
        if (item.isOnline) {
            onlineIndicator.visibility = VISIBLE
            statusView.text = "在线"
            statusView.setTextColor(theme.colors.primary)
        } else {
            onlineIndicator.visibility = GONE
            statusView.text = "最后活跃：${item.lastActiveTime}"
            statusView.setTextColor(theme.colors.hint)
        }
    }
    
    /**
     * 设置点击监听
     */
    fun setOnItemClickListener(listener: () -> Unit) {
        setOnClickListener { listener() }
    }
    
    /**
     * 获取昵称（用于测试）
     */
    fun getNickname(): String = nicknameView.text.toString()
    
    /**
     * 设置昵称（用于测试）
     */
    fun setNickname(nickname: String) {
        nicknameView.text = nickname
    }
    
    /**
     * 获取状态（用于测试）
     */
    fun getStatus(): String = statusView.text.toString()
    
    /**
     * 设置状态（用于测试）
     */
    fun setStatus(status: String) {
        statusView.text = status
    }
    
    /**
     * 检查在线指示器是否可见（用于测试）
     */
    fun isOnlineIndicatorVisible(): Boolean = onlineIndicator.visibility == VISIBLE

    /**
     * 为朋友列表项设置无障碍标签
     * 验证: 需求 12.1
     */
    fun setAccessibilityLabel(item: FriendItem) {
        val status = if (item.isOnline) "在线" else "离线，最后活跃：${item.lastActiveTime}"
        val label = "${item.nickname}，$status"
        AccessibilityUtils.setViewAccessibilityLabel(this, label)
    }

    /**
     * 为头像设置无障碍描述
     * 验证: 需求 12.1, 12.6
     */
    fun setAvatarAccessibilityDescription(nickname: String) {
        AccessibilityUtils.setImageAccessibilityDescription(avatarView, "$nickname 的头像")
    }
}
