package com.juggle.im.android.widget

import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.view.Gravity
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ImageButton
import com.juggle.im.android.theme.ThemeManager
import com.juggle.im.android.utils.AccessibilityUtils

/**
 * 消息输入框组件
 * 支持多行输入，自动扩展高度
 * 支持表情符号和附件按钮
 */
class MessageInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    
    private val themeManager: ThemeManager = ThemeManager.getInstance(context)
    private val theme = themeManager.getCurrentTheme()
    
    private val inputContainer: LinearLayout
    private val inputField: EditText
    private val emojiButton: ImageButton
    private val attachmentButton: ImageButton
    private val locationButton: ImageButton
    private val sendButton: ImageButton
    
    private var onSendListener: ((String) -> Unit)? = null
    private var onEmojiClickListener: (() -> Unit)? = null
    private var onAttachmentClickListener: (() -> Unit)? = null
    private var onLocationClickListener: (() -> Unit)? = null
    
    init {
        // 创建输入容器
        inputContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
            setPadding(theme.spacing.md, theme.spacing.sm, theme.spacing.md, theme.spacing.sm)
            
            // 设置背景
            val shape = ShapeDrawable(RoundRectShape(
                floatArrayOf(12f, 12f, 12f, 12f, 12f, 12f, 12f, 12f),
                null,
                null
            )).apply {
                paint.color = theme.colors.surface
            }
            background = shape
        }
        
        // 创建表情按钮
        emojiButton = ImageButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(40, 40).apply {
                gravity = Gravity.CENTER_VERTICAL
                setMargins(0, 0, theme.spacing.sm, 0)
            }
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setImageResource(android.R.drawable.ic_dialog_info)
            setOnClickListener { onEmojiClickListener?.invoke() }
        }
        inputContainer.addView(emojiButton)
        
        // 创建输入框
        inputField = EditText(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                gravity = Gravity.CENTER_VERTICAL
                setMargins(theme.spacing.sm, 0, theme.spacing.sm, 0)
            }
            hint = "输入消息..."
            setTextColor(theme.colors.onBackground)
            setHintTextColor(theme.colors.onBackground.let { 
                (it and 0xFFFFFF) or 0x80000000.toInt()
            })
            maxLines = 5
            minLines = 1
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            textSize = 14f
        }
        inputContainer.addView(inputField)
        
        // 创建附件按钮
        attachmentButton = ImageButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(40, 40).apply {
                gravity = Gravity.CENTER_VERTICAL
                setMargins(theme.spacing.sm, 0, theme.spacing.sm, 0)
            }
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setImageResource(android.R.drawable.ic_menu_view)
            setOnClickListener { onAttachmentClickListener?.invoke() }
        }
        inputContainer.addView(attachmentButton)
        
        // 创建位置分享按钮
        locationButton = ImageButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(40, 40).apply {
                gravity = Gravity.CENTER_VERTICAL
                setMargins(theme.spacing.sm, 0, theme.spacing.sm, 0)
            }
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setImageResource(android.R.drawable.ic_menu_mylocation)
            setOnClickListener { onLocationClickListener?.invoke() }
        }
        inputContainer.addView(locationButton)
        
        // 创建发送按钮
        sendButton = ImageButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(40, 40).apply {
                gravity = Gravity.CENTER_VERTICAL
            }
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setImageResource(android.R.drawable.ic_menu_send)
            setOnClickListener {
                val message = inputField.text.toString().trim()
                if (message.isNotEmpty()) {
                    onSendListener?.invoke(message)
                    inputField.text.clear()
                }
            }
        }
        inputContainer.addView(sendButton)
        
        addView(inputContainer)
    }
    
    /**
     * 获取输入框内容
     */
    fun getInputText(): String = inputField.text.toString()
    
    /**
     * 设置消息内容
     */
    fun setMessage(text: String) {
        inputField.setText(text)
    }
    
    /**
     * 清空输入框
     */
    fun clearInput() {
        inputField.text.clear()
    }
    
    /**
     * 发送消息
     */
    fun sendMessage() {
        val message = inputField.text.toString().trim()
        if (message.isNotEmpty()) {
            onSendListener?.invoke(message)
            inputField.text.clear()
        }
    }
    
    /**
     * 点击表情按钮
     */
    fun clickEmojiButton() {
        onEmojiClickListener?.invoke()
    }
    
    /**
     * 点击附件按钮
     */
    fun clickAttachmentButton() {
        onAttachmentClickListener?.invoke()
    }
    
    /**
     * 点击位置分享按钮
     */
    fun clickLocationButton() {
        onLocationClickListener?.invoke()
    }
    
    /**
     * 设置发送监听器
     */
    fun setOnSendListener(listener: (String) -> Unit) {
        onSendListener = listener
    }
    
    /**
     * 设置表情按钮点击监听器
     */
    fun setOnEmojiClickListener(listener: () -> Unit) {
        onEmojiClickListener = listener
    }
    
    /**
     * 设置附件按钮点击监听器
     */
    fun setOnAttachmentClickListener(listener: () -> Unit) {
        onAttachmentClickListener = listener
    }
    
    /**
     * 设置位置分享按钮点击监听器
     */
    fun setOnLocationClickListener(listener: () -> Unit) {
        onLocationClickListener = listener
    }
    
    /**
     * 获取输入框焦点
     */
    fun requestInputFocus() {
        inputField.requestFocus()
    }
    
    /**
     * 检查输入框是否有焦点
     */
    fun hasInputFocus(): Boolean = inputField.hasFocus()

    /**
     * 为输入框设置无障碍标签
     * 验证: 需求 12.1
     */
    fun setInputAccessibilityLabel(label: String, hint: String? = null) {
        AccessibilityUtils.setInputAccessibilityLabel(inputField, label, hint)
    }

    /**
     * 为按钮设置无障碍标签
     * 验证: 需求 12.1
     */
    fun setButtonsAccessibilityLabels() {
        AccessibilityUtils.setIconButtonAccessibilityLabel(emojiButton, "表情", "打开表情选择器")
        AccessibilityUtils.setIconButtonAccessibilityLabel(attachmentButton, "附件", "选择附件")
        AccessibilityUtils.setIconButtonAccessibilityLabel(locationButton, "位置", "分享位置")
        AccessibilityUtils.setIconButtonAccessibilityLabel(sendButton, "发送", "发送消息")
    }
}
