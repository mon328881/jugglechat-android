package com.juggle.im.android.widget

import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.widget.FrameLayout
import com.juggle.im.android.theme.ThemeManager
import com.juggle.im.android.utils.AccessibilityUtils

/**
 * Material Design 3 卡片组件
 * 提供圆角边框、阴影效果和可点击反馈
 */
class MaterialCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    
    private var cornerRadius: Float = 12f
    private var elevation: Float = 4f
    private var themeManager: ThemeManager = ThemeManager.getInstance(context)
    private var isClickable: Boolean = false
    
    init {
        // 应用默认样式
        applyDefaultStyle()
    }
    
    /**
     * 应用默认样式
     */
    private fun applyDefaultStyle() {
        val theme = themeManager.getCurrentTheme()
        
        // 创建背景 Drawable
        val shape = ShapeDrawable(RoundRectShape(
            floatArrayOf(cornerRadius, cornerRadius, cornerRadius, cornerRadius,
                cornerRadius, cornerRadius, cornerRadius, cornerRadius),
            null,
            null
        )).apply {
            paint.color = theme.colors.surface
        }
        
        background = shape
        
        // 设置阴影
        setElevation(elevation)
        
        // 设置内边距
        val padding = theme.spacing.md
        setPadding(padding, padding, padding, padding)
    }
    
    /**
     * 设置圆角半径
     */
    fun setCornerRadius(radius: Float) {
        cornerRadius = radius
        applyDefaultStyle()
    }
    
    /**
     * 设置阴影高度
     */
    fun setElevation(elevation: Float) {
        this.elevation = elevation
        super.setElevation(elevation)
    }
    
    /**
     * 设置是否可点击
     */
    override fun setClickable(clickable: Boolean) {
        super.setClickable(clickable)
        isClickable = clickable
        
        if (clickable) {
            // 添加点击反馈
            isPressed = false
        }
    }
    
    /**
     * 获取圆角半径
     */
    fun getCornerRadius(): Float = cornerRadius
    
    /**
     * 获取阴影高度
     */
    fun getElevation(): Float = elevation

    /**
     * 设置无障碍标签
     * 验证: 需求 12.1
     */
    fun setAccessibilityLabel(label: String) {
        AccessibilityUtils.setViewAccessibilityLabel(this, label)
    }

    /**
     * 设置完整的无障碍信息
     * 验证: 需求 12.1
     */
    fun setCompleteAccessibilityInfo(label: String, hint: String? = null) {
        AccessibilityUtils.setCompleteAccessibilityInfo(this, label, hint, isClickable)
    }
}
