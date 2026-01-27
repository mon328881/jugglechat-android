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

/**
 * 群组列表项视图组件
 * 显示群组头像、名称、成员数和最后一条消息
 * 
 * **验证: 需求 9.1**
 */
class GroupListItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCard(context, attrs, defStyleAttr) {
    
    /**
     * 群组列表项数据类
     */
    data class GroupItem(
        val id: String,
        val name: String,
        val avatar: String,
        val memberCount: Int = 0,
        val lastMessage: String = "",
        val lastMessageTime: String = ""
    )
    
    private val themeManager: ThemeManager = ThemeManager.getInstance(context)
    private val theme = themeManager.getCurrentTheme()
    
    // UI 组件
    private val avatarView: ImageView
    private val nameView: TextView
    private val memberCountView: TextView
    private val lastMessageView: TextView
    private val lastMessageTimeView: TextView
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
            layoutParams = LinearLayout.LayoutParams(56, 56).apply {
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
        
        // 创建顶部行（名称和时间）
        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        }
        
        // 创建群组名称
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
        
        // 创建最后消息时间
        lastMessageTimeView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = theme.spacing.sm
            }
            textSize = 12f
            setTextColor(theme.colors.hint)
        }
        topRow.addView(lastMessageTimeView)
        
        contentContainer.addView(topRow)
        
        // 创建中间行（成员数）
        memberCountView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 4
            }
            textSize = 12f
            setTextColor(theme.colors.hint)
        }
        contentContainer.addView(memberCountView)
        
        // 创建最后消息
        lastMessageView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 4
            }
            textSize = 12f
            setTextColor(theme.colors.hint)
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        }
        contentContainer.addView(lastMessageView)
        
        mainContainer.addView(contentContainer)
        addView(mainContainer)
        
        // 应用默认样式
        setCornerRadius(12f)
        setElevation(2f)
    }
    
    /**
     * 绑定群组项数据
     */
    fun bindData(item: GroupItem) {
        // 加载头像
        Glide.with(context)
            .load(item.avatar)
            .circleCrop()
            .into(avatarView)
        
        // 设置群组名称
        nameView.text = item.name
        
        // 设置成员数
        memberCountView.text = "成员数：${item.memberCount}"
        
        // 设置最后消息
        lastMessageView.text = item.lastMessage
        
        // 设置最后消息时间
        lastMessageTimeView.text = item.lastMessageTime
    }
    
    /**
     * 设置点击监听
     */
    fun setOnItemClickListener(listener: () -> Unit) {
        setOnClickListener { listener() }
    }
    
    /**
     * 获取群组名称（用于测试）
     */
    fun getGroupName(): String = nameView.text.toString()
    
    /**
     * 设置群组名称（用于测试）
     */
    fun setGroupName(name: String) {
        nameView.text = name
    }
    
    /**
     * 获取成员数（用于测试）
     */
    fun getMemberCount(): String = memberCountView.text.toString()
    
    /**
     * 设置成员数（用于测试）
     */
    fun setMemberCount(count: Int) {
        memberCountView.text = "成员数：$count"
    }
    
    /**
     * 获取最后消息（用于测试）
     */
    fun getLastMessage(): String = lastMessageView.text.toString()
    
    /**
     * 设置最后消息（用于测试）
     */
    fun setLastMessage(message: String) {
        lastMessageView.text = message
    }
    
    /**
     * 获取最后消息时间（用于测试）
     */
    fun getLastMessageTime(): String = lastMessageTimeView.text.toString()
    
    /**
     * 设置最后消息时间（用于测试）
     */
    fun setLastMessageTime(time: String) {
        lastMessageTimeView.text = time
    }
}
