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
 * 个人资料视图组件
 * 显示用户头像、昵称、个性签名、账号信息和统计数据
 */
class ProfileView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    
    /**
     * 个人资料数据类
     */
    data class ProfileData(
        val userId: String,
        val nickname: String,
        val avatar: String,
        val signature: String,
        val accountId: String,
        val friendCount: Int = 0,
        val groupCount: Int = 0,
        val momentCount: Int = 0
    )
    
    private val themeManager: ThemeManager = ThemeManager.getInstance(context)
    private val theme = themeManager.getCurrentTheme()
    
    // UI 组件
    private val avatarView: ImageView
    private val nicknameView: TextView
    private val signatureView: TextView
    private val accountIdView: TextView
    private val friendCountView: TextView
    private val groupCountView: TextView
    private val momentCountView: TextView
    private val editButton: MaterialButton
    
    private var onEditClickListener: (() -> Unit)? = null
    private var onAvatarClickListener: (() -> Unit)? = null
    
    init {
        orientation = VERTICAL
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        setPadding(theme.spacing.lg, theme.spacing.lg, theme.spacing.lg, theme.spacing.lg)
        
        // 创建头像
        avatarView = ImageView(context).apply {
            layoutParams = LayoutParams(120, 120).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                bottomMargin = theme.spacing.lg
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            
            // 设置圆形背景
            val ovalShape = ShapeDrawable(OvalShape()).apply {
                paint.color = theme.colors.surface
            }
            background = ovalShape
            
            // 添加点击监听器用于头像编辑
            setOnClickListener {
                onAvatarClickListener?.invoke()
            }
        }
        addView(avatarView)
        
        // 创建昵称
        nicknameView = TextView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = theme.spacing.sm
            }
            textSize = 18f
            setTextColor(theme.colors.onBackground)
            gravity = Gravity.CENTER
        }
        addView(nicknameView)
        
        // 创建个性签名
        signatureView = TextView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = theme.spacing.md
            }
            textSize = 14f
            setTextColor(theme.colors.hint)
            gravity = Gravity.CENTER
        }
        addView(signatureView)
        
        // 创建账号ID
        accountIdView = TextView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = theme.spacing.lg
            }
            textSize = 12f
            setTextColor(theme.colors.hint)
            gravity = Gravity.CENTER
        }
        addView(accountIdView)
        
        // 创建统计数据容器
        val statsContainer = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = theme.spacing.lg
            }
        }
        
        // 创建朋友数统计
        friendCountView = createStatView("朋友", "0")
        statsContainer.addView(friendCountView)
        
        // 创建群组数统计
        groupCountView = createStatView("群组", "0")
        statsContainer.addView(groupCountView)
        
        // 创建动态数统计
        momentCountView = createStatView("动态", "0")
        statsContainer.addView(momentCountView)
        
        addView(statsContainer)
        
        // 创建编辑按钮
        editButton = MaterialButton(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
            setText("编辑资料")
            setOnClickListener { onEditClickListener?.invoke() }
        }
        addView(editButton)
    }
    
    /**
     * 创建统计视图
     */
    private fun createStatView(label: String, count: String): LinearLayout {
        return LinearLayout(context).apply {
            orientation = VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                1f
            )
            
            // 创建数字
            val countView = TextView(context).apply {
                text = count
                textSize = 16f
                setTextColor(theme.colors.primary)
                gravity = Gravity.CENTER
            }
            addView(countView)
            
            // 创建标签
            val labelView = TextView(context).apply {
                text = label
                textSize = 12f
                setTextColor(theme.colors.hint)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 4
                }
            }
            addView(labelView)
        }
    }
    
    /**
     * 绑定个人资料数据
     */
    fun bindData(profile: ProfileData) {
        // 加载头像
        Glide.with(context)
            .load(profile.avatar)
            .circleCrop()
            .into(avatarView)
        
        // 设置昵称
        nicknameView.text = profile.nickname
        
        // 设置个性签名
        signatureView.text = profile.signature
        
        // 设置账号ID
        accountIdView.text = "账号：${profile.accountId}"
        
        // 更新统计数据
        (friendCountView.getChildAt(0) as? TextView)?.text = profile.friendCount.toString()
        (groupCountView.getChildAt(0) as? TextView)?.text = profile.groupCount.toString()
        (momentCountView.getChildAt(0) as? TextView)?.text = profile.momentCount.toString()
    }
    
    /**
     * 设置编辑按钮点击监听
     */
    fun setOnEditClickListener(listener: () -> Unit) {
        onEditClickListener = listener
    }
    
    /**
     * 设置头像点击监听
     */
    fun setOnAvatarClickListener(listener: () -> Unit) {
        onAvatarClickListener = listener
    }
    
    /**
     * 获取昵称（用于测试）
     */
    fun getNickname(): String = nicknameView.text.toString()
    
    /**
     * 获取个性签名（用于测试）
     */
    fun getSignature(): String = signatureView.text.toString()
    
    /**
     * 获取账号ID（用于测试）
     */
    fun getAccountId(): String = accountIdView.text.toString()
    
    /**
     * 获取朋友数（用于测试）
     */
    fun getFriendCount(): String = (friendCountView.getChildAt(0) as? TextView)?.text.toString() ?: "0"
}
