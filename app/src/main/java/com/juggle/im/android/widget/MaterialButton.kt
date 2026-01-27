package com.juggle.im.android.widget

import android.content.Context
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.StateListDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import com.juggle.im.android.animation.AnimationUtils
import com.juggle.im.android.theme.ThemeApplier
import com.juggle.im.android.theme.ThemeConfig
import com.juggle.im.android.theme.ThemeManager
import com.juggle.im.android.utils.AccessibilityUtils

/**
 * Material Design 3 按钮组件
 * 支持填充、描边、文本三种按钮类型
 */
class MaterialButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatButton(context, attrs, defStyleAttr) {
    
    /**
     * 按钮类型
     */
    enum class ButtonType {
        FILLED,      // 填充按钮
        OUTLINED,    // 描边按钮
        TEXT         // 文本按钮
    }
    
    private var buttonType: ButtonType = ButtonType.FILLED
    private var cornerRadius: Float = 8f
    private var themeManager: ThemeManager = ThemeManager.getInstance(context)
    
    init {
        // 设置默认样式
        applyDefaultStyle()
    }
    
    /**
     * 应用默认样式
     */
    private fun applyDefaultStyle() {
        val theme = themeManager.getCurrentTheme()
        
        // 设置文本颜色
        setTextColor(theme.colors.onPrimary)
        
        // 设置最小高度（触摸目标最小尺寸）
        minimumHeight = 48
        
        // 设置内边距
        val padding = theme.spacing.md
        setPadding(padding * 2, padding, padding * 2, padding)
        
        // 应用按钮类型样式
        applyButtonTypeStyle(theme)
    }
    
    /**
     * 应用按钮类型样式
     */
    private fun applyButtonTypeStyle(theme: ThemeConfig) {
        when (buttonType) {
            ButtonType.FILLED -> applyFilledStyle(theme)
            ButtonType.OUTLINED -> applyOutlinedStyle(theme)
            ButtonType.TEXT -> applyTextStyle(theme)
        }
    }
    
    /**
     * 应用填充按钮样式
     */
    private fun applyFilledStyle(theme: ThemeConfig) {
        // 创建背景 Drawable
        val normalShape = ShapeDrawable(RoundRectShape(
            floatArrayOf(cornerRadius, cornerRadius, cornerRadius, cornerRadius,
                cornerRadius, cornerRadius, cornerRadius, cornerRadius),
            null,
            null
        )).apply {
            paint.color = theme.colors.primary
        }
        
        val pressedShape = ShapeDrawable(RoundRectShape(
            floatArrayOf(cornerRadius, cornerRadius, cornerRadius, cornerRadius,
                cornerRadius, cornerRadius, cornerRadius, cornerRadius),
            null,
            null
        )).apply {
            paint.color = theme.colors.secondary
        }
        
        val disabledShape = ShapeDrawable(RoundRectShape(
            floatArrayOf(cornerRadius, cornerRadius, cornerRadius, cornerRadius,
                cornerRadius, cornerRadius, cornerRadius, cornerRadius),
            null,
            null
        )).apply {
            paint.color = theme.colors.disabled
        }
        
        // 创建状态列表 Drawable
        val stateListDrawable = StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), pressedShape)
            addState(intArrayOf(-android.R.attr.state_enabled), disabledShape)
            addState(intArrayOf(), normalShape)
        }
        
        // 创建涟漪效果
        val rippleDrawable = RippleDrawable(
            android.content.res.ColorStateList.valueOf(theme.colors.secondary),
            stateListDrawable,
            null
        )
        
        background = rippleDrawable
        setTextColor(theme.colors.onPrimary)
    }
    
    /**
     * 应用描边按钮样式
     */
    private fun applyOutlinedStyle(theme: ThemeConfig) {
        // 创建背景 Drawable
        val normalShape = ShapeDrawable(RoundRectShape(
            floatArrayOf(cornerRadius, cornerRadius, cornerRadius, cornerRadius,
                cornerRadius, cornerRadius, cornerRadius, cornerRadius),
            null,
            null
        )).apply {
            paint.color = theme.colors.background
            paint.style = android.graphics.Paint.Style.STROKE
            paint.strokeWidth = 2f
            paint.color = theme.colors.primary
        }
        
        val pressedShape = ShapeDrawable(RoundRectShape(
            floatArrayOf(cornerRadius, cornerRadius, cornerRadius, cornerRadius,
                cornerRadius, cornerRadius, cornerRadius, cornerRadius),
            null,
            null
        )).apply {
            paint.color = theme.colors.surface
            paint.style = android.graphics.Paint.Style.STROKE
            paint.strokeWidth = 2f
            paint.color = theme.colors.primary
        }
        
        val stateListDrawable = StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), pressedShape)
            addState(intArrayOf(), normalShape)
        }
        
        background = stateListDrawable
        setTextColor(theme.colors.primary)
    }
    
    /**
     * 应用文本按钮样式
     */
    private fun applyTextStyle(theme: ThemeConfig) {
        // 文本按钮没有背景，只有文本颜色
        setBackgroundColor(android.graphics.Color.TRANSPARENT)
        setTextColor(theme.colors.primary)
    }
    
    /**
     * 设置按钮类型
     */
    fun setButtonType(type: ButtonType) {
        buttonType = type
        applyDefaultStyle()
    }
    
    /**
     * 设置圆角半径
     */
    fun setCornerRadius(radius: Float) {
        cornerRadius = radius
        applyDefaultStyle()
    }
    
    /**
     * 设置加载状态
     */
    fun setLoading(isLoading: Boolean) {
        isEnabled = !isLoading
        if (isLoading) {
            // 显示加载动画
            text = "加载中..."
            alpha = 0.6f
        } else {
            alpha = 1f
        }
    }
    
    /**
     * 启用涟漪效果动画
     * 验证: 需求 11.2
     */
    fun enableRippleAnimation() {
        setOnClickListener {
            // 显示脉冲动画
            AnimationUtils.buttonPulseAnimation(this, 600)
        }
    }
    
    /**
     * 启用平滑过渡动画
     * 验证: 需求 11.1
     */
    fun enableSmoothTransition() {
        val animation = AnimationUtils.createSmoothTransitionAnimation(this, 300)
        startAnimation(animation)
    }

    /**
     * 设置无障碍标签
     * 验证: 需求 12.1
     */
    fun setAccessibilityLabel(label: String) {
        AccessibilityUtils.setButtonAccessibilityLabel(this, label)
    }

    /**
     * 设置完整的无障碍信息
     * 验证: 需求 12.1
     */
    fun setCompleteAccessibilityInfo(label: String, hint: String? = null) {
        AccessibilityUtils.setCompleteAccessibilityInfo(this, label, hint, true)
    }
}
