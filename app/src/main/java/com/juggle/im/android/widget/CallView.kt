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
 * 通话界面视图组件
 * 显示对方头像或视频画面，以及通话时长
 */
class CallView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    
    /**
     * 通话类型
     */
    enum class CallType {
        VOICE,  // 语音通话
        VIDEO   // 视频通话
    }
    
    /**
     * 通话数据类
     */
    data class CallData(
        val callerId: String,
        val callerName: String,
        val callerAvatar: String,
        val callType: CallType = CallType.VOICE,
        val callDuration: String = "00:00"
    )
    
    private val themeManager: ThemeManager = ThemeManager.getInstance(context)
    private val theme = themeManager.getCurrentTheme()
    
    // UI 组件
    private val backgroundView: ImageView
    private val avatarView: ImageView
    private val nameView: TextView
    private val durationView: TextView
    private val muteButton: MaterialButton
    private val speakerButton: MaterialButton
    private val cameraButton: MaterialButton
    private val hangupButton: MaterialButton
    
    private var onMuteClickListener: (() -> Unit)? = null
    private var onSpeakerClickListener: (() -> Unit)? = null
    private var onCameraClickListener: (() -> Unit)? = null
    private var onHangupClickListener: (() -> Unit)? = null
    
    init {
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
        setBackgroundColor(theme.colors.background)
        
        // 创建背景视图
        backgroundView = ImageView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        addView(backgroundView)
        
        // 创建中央信息容器
        val centerContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        }
        
        // 创建头像
        avatarView = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(120, 120).apply {
                bottomMargin = theme.spacing.lg
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            
            // 设置圆形背景
            val ovalShape = ShapeDrawable(OvalShape()).apply {
                paint.color = theme.colors.surface
            }
            background = ovalShape
        }
        centerContainer.addView(avatarView)
        
        // 创建名称
        nameView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = theme.spacing.md
            }
            textSize = 20f
            setTextColor(theme.colors.onBackground)
        }
        centerContainer.addView(nameView)
        
        // 创建通话时长
        durationView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = theme.spacing.lg
            }
            textSize = 16f
            setTextColor(theme.colors.hint)
        }
        centerContainer.addView(durationView)
        
        addView(centerContainer)
        
        // 创建底部控制按钮容器
        val controlContainer = LinearLayout(context).apply {
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
        
        // 创建静音按钮
        muteButton = MaterialButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                56,
                56
            ).apply {
                marginEnd = theme.spacing.md
            }
            setText("🔇")
            setOnClickListener { onMuteClickListener?.invoke() }
        }
        controlContainer.addView(muteButton)
        
        // 创建扬声器按钮
        speakerButton = MaterialButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                56,
                56
            ).apply {
                marginEnd = theme.spacing.md
            }
            setText("🔊")
            setOnClickListener { onSpeakerClickListener?.invoke() }
        }
        controlContainer.addView(speakerButton)
        
        // 创建摄像头按钮
        cameraButton = MaterialButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                56,
                56
            ).apply {
                marginEnd = theme.spacing.md
            }
            setText("📹")
            setOnClickListener { onCameraClickListener?.invoke() }
        }
        controlContainer.addView(cameraButton)
        
        // 创建挂断按钮
        hangupButton = MaterialButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                56,
                56
            )
            setText("📞")
            setOnClickListener { onHangupClickListener?.invoke() }
        }
        controlContainer.addView(hangupButton)
        
        addView(controlContainer)
    }
    
    /**
     * 绑定通话数据
     */
    fun bindData(callData: CallData) {
        // 加载头像
        Glide.with(context)
            .load(callData.callerAvatar)
            .circleCrop()
            .into(avatarView)
        
        // 设置名称
        nameView.text = callData.callerName
        
        // 设置通话时长
        durationView.text = callData.callDuration
    }
    
    /**
     * 更新通话时长
     */
    fun updateCallDuration(duration: String) {
        durationView.text = duration
    }
    
    /**
     * 设置静音按钮点击监听
     */
    fun setOnMuteClickListener(listener: () -> Unit) {
        onMuteClickListener = listener
    }
    
    /**
     * 设置扬声器按钮点击监听
     */
    fun setOnSpeakerClickListener(listener: () -> Unit) {
        onSpeakerClickListener = listener
    }
    
    /**
     * 设置摄像头按钮点击监听
     */
    fun setOnCameraClickListener(listener: () -> Unit) {
        onCameraClickListener = listener
    }
    
    /**
     * 设置挂断按钮点击监听
     */
    fun setOnHangupClickListener(listener: () -> Unit) {
        onHangupClickListener = listener
    }
    
    /**
     * 获取名称（用于测试）
     */
    fun getCallerName(): String = nameView.text.toString()
    
    /**
     * 获取通话时长（用于测试）
     */
    fun getCallDuration(): String = durationView.text.toString()
}
