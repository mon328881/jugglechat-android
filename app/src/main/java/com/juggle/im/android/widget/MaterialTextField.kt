package com.juggle.im.android.widget

import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import com.juggle.im.android.theme.ThemeConfig
import com.juggle.im.android.theme.ThemeManager
import com.juggle.im.android.utils.AccessibilityUtils

/**
 * Material Design 3 输入框组件
 * 支持单行、多行、搜索、密码输入框
 */
class MaterialTextField @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    
    /**
     * 输入框类型
     */
    enum class InputType {
        TEXT,       // 单行文本
        MULTILINE,  // 多行文本
        SEARCH,     // 搜索框
        PASSWORD    // 密码框
    }
    
    private val editText: AppCompatEditText
    private val errorText: TextView
    private var inputType: InputType = InputType.TEXT
    private var themeManager: ThemeManager = ThemeManager.getInstance(context)
    private var cornerRadius: Float = 8f
    
    init {
        // 创建编辑框
        editText = AppCompatEditText(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        }
        addView(editText)
        
        // 创建错误提示文本
        errorText = TextView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
            visibility = View.GONE
            textSize = 12f
        }
        addView(errorText)
        
        // 应用默认样式
        applyDefaultStyle()
    }
    
    /**
     * 应用默认样式
     */
    private fun applyDefaultStyle() {
        val theme = themeManager.getCurrentTheme()
        
        // 设置背景
        val shape = ShapeDrawable(RoundRectShape(
            floatArrayOf(cornerRadius, cornerRadius, cornerRadius, cornerRadius,
                cornerRadius, cornerRadius, cornerRadius, cornerRadius),
            null,
            null
        )).apply {
            paint.color = theme.colors.surface
        }
        editText.background = shape
        
        // 设置文本颜色
        editText.setTextColor(theme.colors.onBackground)
        editText.setHintTextColor(theme.colors.hint)
        
        // 设置内边距
        val padding = theme.spacing.md
        editText.setPadding(padding, padding, padding, padding)
        
        // 设置最小高度
        editText.minimumHeight = 48
        
        // 应用输入框类型样式
        applyInputTypeStyle(theme)
    }
    
    /**
     * 应用输入框类型样式
     */
    private fun applyInputTypeStyle(theme: ThemeConfig) {
        when (inputType) {
            InputType.TEXT -> {
                editText.inputType = android.text.InputType.TYPE_CLASS_TEXT
                editText.maxLines = 1
            }
            InputType.MULTILINE -> {
                editText.inputType = android.text.InputType.TYPE_CLASS_TEXT or 
                    android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
                editText.maxLines = Int.MAX_VALUE
            }
            InputType.SEARCH -> {
                editText.inputType = android.text.InputType.TYPE_CLASS_TEXT
                editText.maxLines = 1
                editText.hint = "搜索..."
            }
            InputType.PASSWORD -> {
                editText.inputType = android.text.InputType.TYPE_CLASS_TEXT or 
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                editText.maxLines = 1
            }
        }
    }
    
    /**
     * 设置输入框类型
     */
    fun setInputType(type: InputType) {
        inputType = type
        applyDefaultStyle()
    }
    
    /**
     * 显示错误提示
     */
    fun setError(errorMessage: String?) {
        if (errorMessage != null) {
            errorText.text = errorMessage
            errorText.visibility = View.VISIBLE
            errorText.setTextColor(themeManager.getCurrentTheme().colors.error)
            
            // 高亮显示输入框
            val theme = themeManager.getCurrentTheme()
            val shape = ShapeDrawable(RoundRectShape(
                floatArrayOf(cornerRadius, cornerRadius, cornerRadius, cornerRadius,
                    cornerRadius, cornerRadius, cornerRadius, cornerRadius),
                null,
                null
            )).apply {
                paint.color = theme.colors.surface
                paint.style = android.graphics.Paint.Style.STROKE
                paint.strokeWidth = 2f
                paint.color = theme.colors.error
            }
            editText.background = shape
        } else {
            errorText.visibility = View.GONE
            applyDefaultStyle()
        }
    }
    
    /**
     * 获取输入框的文本
     */
    fun getText(): String = editText.text.toString()
    
    /**
     * 设置输入框的文本
     */
    fun setText(text: String) {
        editText.setText(text)
    }
    
    /**
     * 获取编辑框实例
     */
    fun getEditText(): EditText = editText
    
    /**
     * 设置圆角半径
     */
    fun setCornerRadius(radius: Float) {
        cornerRadius = radius
        applyDefaultStyle()
    }
    
    /**
     * 设置提示文本
     */
    fun setHint(hint: String) {
        editText.hint = hint
    }

    /**
     * 设置无障碍标签
     * 验证: 需求 12.1
     */
    fun setAccessibilityLabel(label: String, hint: String? = null) {
        AccessibilityUtils.setInputAccessibilityLabel(editText, label, hint)
    }

    /**
     * 设置完整的无障碍信息
     * 验证: 需求 12.1
     */
    fun setCompleteAccessibilityInfo(label: String, hint: String? = null) {
        AccessibilityUtils.setCompleteAccessibilityInfo(editText, label, hint, false)
    }
}
