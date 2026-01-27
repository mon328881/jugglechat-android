package com.juggle.im.android.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.juggle.im.android.theme.ThemeManager

/**
 * 来电提示视图组件
 * 显示来电提示，用户可以选择接听或拒绝
 * 需求：10.6
 */
class IncomingCallView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    
    /**
     * 来电数据类
     */
    data class IncomingCallData(
        val callerId: String,
        val callerName: String,
        val callerAvatar: String,
        val isVideoCall: Boolean = false
    )
    
    private val themeManager: ThemeManager = ThemeManager.getInstance(context)
    private val theme = themeManager.getCurrentTheme()
    
    // UI 组件
    private val containerView: LinearLayout
    private val avatarView: ImageView
    private val nameView: TextView
    private val callTypeView: TextView
    private val acceptButton: MaterialButton
    private val rejectButton: MaterialButton
    
    // 点击监听
    private var onAcceptClickListener: (() -> Unit)? = null
    private var onRejectClickListener: (() -> Unit)? = null
    
    init {
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
        setBackgroundColor(theme.colors.background)
        
        // 创建主容器
        containerView = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
            setPadding(
                theme.spacing.lg,
                theme.spacing.lg,
                theme.spacing.lg,
                theme.spacing.lg
            )
        }
        
        // 创建头像
        avatarView = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(120, 120).apply {
                bottomMargin = theme.spacing.lg
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            
            // 设置圆形背景
            val ovalShape = android.graphics.drawable.ShapeDrawable(
                android.graphics.drawable.shapes.OvalShape()
            ).apply {
                paint.color = theme.colors.surface
            }
            background = ovalShape
        }
        containerView.addView(avatarView)
        
        // 创建名称
        nameView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = theme.spacing.md
            }
            textSize = 20f
            setTextColor(theme.colors.onBackground)
        }
        containerView.addView(nameView)
        
        // 创建通话类型提示
        callTypeView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = theme.spacing.xl
            }
            textSize = 14f
            setTextColor(theme.colors.hint)
        }
        containerView.addView(callTypeView)
        
        addView(containerView)
        
        // 创建按钮容器
        val buttonContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM
                setMargins(0, 0, 0, theme.spacing.lg)
            }
        }
        
        // 创建拒绝按钮
        rejectButton = MaterialButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                56,
                56
            ).apply {
                marginEnd = theme.spacing.lg
            }
            setText("❌")
            setOnClickListener { onRejectClickListener?.invoke() }
        }
        buttonContainer.addView(rejectButton)
        
        // 创建接听按钮
        acceptButton = MaterialButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                56,
                56
            )
            setText("✅")
            setOnClickListener { onAcceptClickListener?.invoke() }
        }
        buttonContainer.addView(acceptButton)
        
        addView(buttonContainer)
    }
    
    /**
     * 绑定来电数据
     */
    fun bindData(callData: IncomingCallData) {
        // 加载头像
        Glide.with(context)
            .load(callData.callerAvatar)
            .circleCrop()
            .into(avatarView)
        
        // 设置名称
        nameView.text = callData.callerName
        
        // 设置通话类型
        callTypeView.text = if (callData.isVideoCall) "视频通话邀请" else "语音通话邀请"
    }
    
    /**
     * 设置接听按钮点击监听
     */
    fun setOnAcceptClickListener(listener: () -> Unit) {
        onAcceptClickListener = listener
    }
    
    /**
     * 设置拒绝按钮点击监听
     */
    fun setOnRejectClickListener(listener: () -> Unit) {
        onRejectClickListener = listener
    }
    
    /**
     * 获取来电者名称（用于测试）
     */
    fun getCallerName(): String = nameView.text.toString()
    
    /**
     * 获取通话类型提示（用于测试）
     */
    fun getCallTypeText(): String = callTypeView.text.toString()
}
