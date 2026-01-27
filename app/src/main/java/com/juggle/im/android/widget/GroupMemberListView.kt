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
 * 群组成员列表项视图组件
 * 显示成员的头像、昵称和角色
 * 
 * **验证: 需求 9.3**
 */
class GroupMemberListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCard(context, attrs, defStyleAttr) {
    
    /**
     * 成员角色枚举
     */
    enum class MemberRole {
        OWNER,      // 群主
        ADMIN,      // 管理员
        MEMBER      // 普通成员
    }
    
    /**
     * 群组成员数据类
     */
    data class GroupMember(
        val id: String,
        val nickname: String,
        val avatar: String,
        val role: MemberRole = MemberRole.MEMBER
    )
    
    private val themeManager: ThemeManager = ThemeManager.getInstance(context)
    private val theme = themeManager.getCurrentTheme()
    
    // UI 组件
    private val avatarView: ImageView
    private val nicknameView: TextView
    private val roleView: TextView
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
        
        // 创建角色标签
        roleView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 4
            }
            textSize = 12f
            setTextColor(theme.colors.primary)
        }
        contentContainer.addView(roleView)
        
        mainContainer.addView(contentContainer)
        addView(mainContainer)
        
        // 应用默认样式
        setCornerRadius(12f)
        setElevation(2f)
    }
    
    /**
     * 绑定成员数据
     */
    fun bindData(member: GroupMember) {
        // 加载头像
        Glide.with(context)
            .load(member.avatar)
            .circleCrop()
            .into(avatarView)
        
        // 设置昵称
        nicknameView.text = member.nickname
        
        // 设置角色
        val roleText = when (member.role) {
            MemberRole.OWNER -> "群主"
            MemberRole.ADMIN -> "管理员"
            MemberRole.MEMBER -> "成员"
        }
        roleView.text = roleText
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
     * 获取角色（用于测试）
     */
    fun getRole(): String = roleView.text.toString()
    
    /**
     * 设置角色（用于测试）
     */
    fun setRole(role: MemberRole) {
        val roleText = when (role) {
            MemberRole.OWNER -> "群主"
            MemberRole.ADMIN -> "管理员"
            MemberRole.MEMBER -> "成员"
        }
        roleView.text = roleText
    }
}
