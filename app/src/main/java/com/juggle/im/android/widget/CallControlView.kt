package com.juggle.im.android.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import com.juggle.im.android.theme.ThemeManager

/**
 * 通话控制按钮视图组件
 * 包含静音、扬声器、摄像头开关和挂断按钮
 * 需求：10.3, 10.4
 */
class CallControlView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    
    /**
     * 按钮状态
     */
    enum class ButtonState {
        NORMAL,      // 正常状态
        ACTIVE,      // 激活状态（如已静音）
        DISABLED     // 禁用状态
    }
    
    private val themeManager: ThemeManager = ThemeManager.getInstance(context)
    private val theme = themeManager.getCurrentTheme()
    
    // 控制按钮
    private val muteButton: MaterialButton
    private val speakerButton: MaterialButton
    private val cameraButton: MaterialButton
    private val hangupButton: MaterialButton
    
    // 按钮状态
    private var muteState: ButtonState = ButtonState.NORMAL
    private var speakerState: ButtonState = ButtonState.NORMAL
    private var cameraState: ButtonState = ButtonState.NORMAL
    
    // 点击监听
    private var onMuteClickListener: ((Boolean) -> Unit)? = null
    private var onSpeakerClickListener: ((Boolean) -> Unit)? = null
    private var onCameraClickListener: ((Boolean) -> Unit)? = null
    private var onHangupClickListener: (() -> Unit)? = null
    
    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        setPadding(
            theme.spacing.md,
            theme.spacing.md,
            theme.spacing.md,
            theme.spacing.md
        )
        setBackgroundColor(theme.colors.surface)
        
        // 创建静音按钮
        muteButton = MaterialButton(context).apply {
            layoutParams = LayoutParams(
                56,
                56
            ).apply {
                marginEnd = theme.spacing.md
            }
            setText("🔇")
            setOnClickListener {
                toggleMuteState()
                onMuteClickListener?.invoke(muteState == ButtonState.ACTIVE)
            }
        }
        addView(muteButton)
        
        // 创建扬声器按钮
        speakerButton = MaterialButton(context).apply {
            layoutParams = LayoutParams(
                56,
                56
            ).apply {
                marginEnd = theme.spacing.md
            }
            setText("🔊")
            setOnClickListener {
                toggleSpeakerState()
                onSpeakerClickListener?.invoke(speakerState == ButtonState.ACTIVE)
            }
        }
        addView(speakerButton)
        
        // 创建摄像头按钮
        cameraButton = MaterialButton(context).apply {
            layoutParams = LayoutParams(
                56,
                56
            ).apply {
                marginEnd = theme.spacing.md
            }
            setText("📹")
            setOnClickListener {
                toggleCameraState()
                onCameraClickListener?.invoke(cameraState == ButtonState.ACTIVE)
            }
        }
        addView(cameraButton)
        
        // 创建挂断按钮
        hangupButton = MaterialButton(context).apply {
            layoutParams = LayoutParams(
                56,
                56
            )
            setText("📞")
            setOnClickListener {
                onHangupClickListener?.invoke()
            }
        }
        addView(hangupButton)
    }
    
    /**
     * 切换静音状态
     */
    private fun toggleMuteState() {
        muteState = when (muteState) {
            ButtonState.NORMAL -> ButtonState.ACTIVE
            ButtonState.ACTIVE -> ButtonState.NORMAL
            ButtonState.DISABLED -> ButtonState.DISABLED
        }
        updateMuteButtonStyle()
    }
    
    /**
     * 切换扬声器状态
     */
    private fun toggleSpeakerState() {
        speakerState = when (speakerState) {
            ButtonState.NORMAL -> ButtonState.ACTIVE
            ButtonState.ACTIVE -> ButtonState.NORMAL
            ButtonState.DISABLED -> ButtonState.DISABLED
        }
        updateSpeakerButtonStyle()
    }
    
    /**
     * 切换摄像头状态
     */
    private fun toggleCameraState() {
        cameraState = when (cameraState) {
            ButtonState.NORMAL -> ButtonState.ACTIVE
            ButtonState.ACTIVE -> ButtonState.NORMAL
            ButtonState.DISABLED -> ButtonState.DISABLED
        }
        updateCameraButtonStyle()
    }
    
    /**
     * 更新静音按钮样式
     */
    private fun updateMuteButtonStyle() {
        when (muteState) {
            ButtonState.NORMAL -> {
                muteButton.setText("🔇")
                muteButton.alpha = 1.0f
            }
            ButtonState.ACTIVE -> {
                muteButton.setText("🔇")
                muteButton.alpha = 0.5f
            }
            ButtonState.DISABLED -> {
                muteButton.alpha = 0.3f
            }
        }
    }
    
    /**
     * 更新扬声器按钮样式
     */
    private fun updateSpeakerButtonStyle() {
        when (speakerState) {
            ButtonState.NORMAL -> {
                speakerButton.setText("🔊")
                speakerButton.alpha = 1.0f
            }
            ButtonState.ACTIVE -> {
                speakerButton.setText("🔊")
                speakerButton.alpha = 0.5f
            }
            ButtonState.DISABLED -> {
                speakerButton.alpha = 0.3f
            }
        }
    }
    
    /**
     * 更新摄像头按钮样式
     */
    private fun updateCameraButtonStyle() {
        when (cameraState) {
            ButtonState.NORMAL -> {
                cameraButton.setText("📹")
                cameraButton.alpha = 1.0f
            }
            ButtonState.ACTIVE -> {
                cameraButton.setText("📹")
                cameraButton.alpha = 0.5f
            }
            ButtonState.DISABLED -> {
                cameraButton.alpha = 0.3f
            }
        }
    }
    
    /**
     * 设置静音按钮点击监听
     */
    fun setOnMuteClickListener(listener: (Boolean) -> Unit) {
        onMuteClickListener = listener
    }
    
    /**
     * 设置扬声器按钮点击监听
     */
    fun setOnSpeakerClickListener(listener: (Boolean) -> Unit) {
        onSpeakerClickListener = listener
    }
    
    /**
     * 设置摄像头按钮点击监听
     */
    fun setOnCameraClickListener(listener: (Boolean) -> Unit) {
        onCameraClickListener = listener
    }
    
    /**
     * 设置挂断按钮点击监听
     */
    fun setOnHangupClickListener(listener: () -> Unit) {
        onHangupClickListener = listener
    }
    
    /**
     * 获取静音状态
     */
    fun getMuteState(): ButtonState = muteState
    
    /**
     * 获取扬声器状态
     */
    fun getSpeakerState(): ButtonState = speakerState
    
    /**
     * 获取摄像头状态
     */
    fun getCameraState(): ButtonState = cameraState
    
    /**
     * 设置静音状态
     */
    fun setMuteState(state: ButtonState) {
        muteState = state
        updateMuteButtonStyle()
    }
    
    /**
     * 设置扬声器状态
     */
    fun setSpeakerState(state: ButtonState) {
        speakerState = state
        updateSpeakerButtonStyle()
    }
    
    /**
     * 设置摄像头状态
     */
    fun setCameraState(state: ButtonState) {
        cameraState = state
        updateCameraButtonStyle()
    }
}
