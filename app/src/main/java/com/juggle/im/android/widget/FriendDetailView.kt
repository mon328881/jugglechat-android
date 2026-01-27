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
 * 朋友详情视图组件
 * 显示头像、昵称、个性签名和操作按钮
 */
class FriendDetailView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    
    /**
     * 朋友详情数据类
     */
    data class FriendDetail(
        val id: String,
        val nickname: String,
        val avatar: String,
        val signature: String,
        val accountId: String,
        val isOnline: Boolean = false
    )
    
    private val themeManager: ThemeManager = ThemeManager.getInstance(context)
    private val theme = themeManager.getCurrentTheme()
    
    // UI 组件
    private val avatarView: ImageView
    private val nicknameView: TextView
    private val signatureView: TextView
    private val accountIdView: TextView
    private val statusView: TextView
    private val addButton: MaterialButton
    private val messageButton: MaterialButton
    
    private var onAddClickListener: (() -> Unit)? = null
    private var onMessageClickListener: (() -> Unit)? = null
    
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
                bottomMargin = theme.spacing.sm
            }
            textSize = 12f
            setTextColor(theme.colors.hint)
            gravity = Gravity.CENTER
        }
        addView(accountIdView)
        
        // 创建状态
        statusView = TextView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = theme.spacing.lg
            }
            textSize = 12f
            setTextColor(theme.colors.primary)
            gravity = Gravity.CENTER
        }
        addView(statusView)
        
        // 创建按钮容器
        val buttonContainer = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        }
        
        // 创建添加朋友按钮
        addButton = MaterialButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginEnd = theme.spacing.sm
            }
            setText("添加朋友")
            setOnClickListener { onAddClickListener?.invoke() }
        }
        buttonContainer.addView(addButton)
        
        // 创建发送消息按钮
        messageButton = MaterialButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginStart = theme.spacing.sm
            }
            setText("发送消息")
            setOnClickListener { onMessageClickListener?.invoke() }
        }
        buttonContainer.addView(messageButton)
        
        addView(buttonContainer)
    }
    
    /**
     * 绑定朋友详情数据
     */
    fun bindData(detail: FriendDetail) {
        // 加载头像
        Glide.with(context)
            .load(detail.avatar)
            .circleCrop()
            .into(avatarView)
        
        // 设置昵称
        nicknameView.text = detail.nickname
        
        // 设置个性签名
        signatureView.text = detail.signature
        
        // 设置账号ID
        accountIdView.text = "账号：${detail.accountId}"
        
        // 设置状态
        if (detail.isOnline) {
            statusView.text = "在线"
            statusView.setTextColor(theme.colors.primary)
        } else {
            statusView.text = "离线"
            statusView.setTextColor(theme.colors.hint)
        }
    }
    
    /**
     * 设置添加朋友按钮点击监听
     */
    fun setOnAddClickListener(listener: () -> Unit) {
        onAddClickListener = listener
    }
    
    /**
     * 设置发送消息按钮点击监听
     */
    fun setOnMessageClickListener(listener: () -> Unit) {
        onMessageClickListener = listener
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
}
