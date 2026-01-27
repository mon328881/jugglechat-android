package com.juggle.im.android.widget

import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ImageView
import com.juggle.im.android.animation.AnimationUtils
import com.juggle.im.android.theme.ThemeManager
import com.juggle.im.android.utils.AccessibilityUtils

/**
 * 聊天气泡组件
 * 支持发送和接收消息的不同样式
 */
class ChatBubbleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    
    /**
     * 消息类型
     */
    enum class MessageType {
        TEXT,      // 文本消息
        IMAGE,     // 图片消息
        VIDEO,     // 视频消息
        AUDIO      // 音频消息
    }
    
    /**
     * 消息方向
     */
    enum class MessageDirection {
        SENT,      // 发送的消息
        RECEIVED   // 接收的消息
    }
    
    /**
     * 消息状态
     */
    enum class MessageStatus {
        SENDING,   // 发送中
        SENT,      // 已发送
        DELIVERED, // 已送达
        READ       // 已读
    }
    
    private val themeManager: ThemeManager = ThemeManager.getInstance(context)
    private val theme = themeManager.getCurrentTheme()
    
    private val bubbleContainer: LinearLayout
    private val messageText: TextView
    private val statusIcon: ImageView
    private var messageDirection: MessageDirection = MessageDirection.SENT
    private var messageStatus: MessageStatus = MessageStatus.SENDING
    
    init {
        // 创建气泡容器
        bubbleContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
        }
        
        // 创建消息文本
        messageText = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(theme.spacing.md, theme.spacing.sm, theme.spacing.md, theme.spacing.sm)
            }
            textSize = 14f
            setTextColor(theme.colors.onBackground)
        }
        bubbleContainer.addView(messageText)
        
        // 创建状态指示器
        statusIcon = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(16, 16).apply {
                gravity = Gravity.END
                setMargins(0, 0, theme.spacing.sm, theme.spacing.sm)
            }
            visibility = GONE
        }
        bubbleContainer.addView(statusIcon)
        
        addView(bubbleContainer)
        
        // 应用默认样式
        applyDefaultStyle()
    }
    
    /**
     * 应用默认样式
     */
    private fun applyDefaultStyle() {
        val params = bubbleContainer.layoutParams as FrameLayout.LayoutParams
        
        when (messageDirection) {
            MessageDirection.SENT -> {
                // 发送的消息显示在右侧
                params.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                params.marginEnd = theme.spacing.lg
                
                // 设置蓝色背景
                val shape = ShapeDrawable(RoundRectShape(
                    floatArrayOf(12f, 12f, 4f, 4f, 12f, 12f, 12f, 12f),
                    null,
                    null
                )).apply {
                    paint.color = theme.colors.primary
                }
                bubbleContainer.background = shape
                messageText.setTextColor(theme.colors.onPrimary)
            }
            MessageDirection.RECEIVED -> {
                // 接收的消息显示在左侧
                params.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                params.marginStart = theme.spacing.lg
                
                // 设置灰色背景
                val shape = ShapeDrawable(RoundRectShape(
                    floatArrayOf(4f, 4f, 12f, 12f, 12f, 12f, 12f, 12f),
                    null,
                    null
                )).apply {
                    paint.color = theme.colors.surface
                }
                bubbleContainer.background = shape
                messageText.setTextColor(theme.colors.onBackground)
            }
        }
        
        bubbleContainer.layoutParams = params
    }
    
    /**
     * 设置消息内容
     */
    fun setMessage(text: String) {
        messageText.text = text
    }
    
    /**
     * 设置消息方向
     */
    fun setMessageDirection(direction: MessageDirection) {
        messageDirection = direction
        applyDefaultStyle()
    }
    
    /**
     * 设置消息状态
     */
    fun setMessageStatus(status: MessageStatus) {
        messageStatus = status
        
        // 只在发送的消息上显示状态指示器
        if (messageDirection == MessageDirection.SENT) {
            statusIcon.visibility = VISIBLE
            
            // 根据状态设置不同的图标
            when (status) {
                MessageStatus.SENDING -> {
                    // 显示加载动画
                    statusIcon.setImageResource(android.R.drawable.ic_dialog_info)
                }
                MessageStatus.SENT -> {
                    // 显示已发送图标
                    statusIcon.setImageResource(android.R.drawable.ic_menu_view)
                }
                MessageStatus.DELIVERED -> {
                    // 显示已送达图标
                    statusIcon.setImageResource(android.R.drawable.ic_menu_view)
                }
                MessageStatus.READ -> {
                    // 显示已读图标
                    statusIcon.setImageResource(android.R.drawable.ic_menu_view)
                }
            }
        } else {
            statusIcon.visibility = GONE
        }
    }
    
    /**
     * 获取消息方向
     */
    fun getMessageDirection(): MessageDirection = messageDirection
    
    /**
     * 获取消息状态
     */
    fun getMessageStatus(): MessageStatus = messageStatus
    
    /**
     * 启用消息气泡发送动画
     * 验证: 需求 11.5
     */
    fun enableSendAnimation() {
        val animation = AnimationUtils.createMessageBubbleSendAnimation(this, 300)
        startAnimation(animation)
    }
    
    /**
     * 启用消息气泡加载动画
     * 验证: 需求 11.3
     */
    fun enableLoadAnimation(delay: Long = 0) {
        val animation = AnimationUtils.createListItemLoadAnimation(this, delay, 300)
        startAnimation(animation)
    }

    /**
     * 设置消息气泡的无障碍标签
     * 验证: 需求 12.1
     */
    fun setAccessibilityLabel(senderName: String, messageContent: String) {
        val direction = if (messageDirection == MessageDirection.SENT) "发送" else "接收"
        val status = when (messageStatus) {
            MessageStatus.SENDING -> "发送中"
            MessageStatus.SENT -> "已发送"
            MessageStatus.DELIVERED -> "已送达"
            MessageStatus.READ -> "已读"
        }
        
        val label = if (messageDirection == MessageDirection.SENT) {
            "$direction 消息，$status，内容：$messageContent"
        } else {
            "$senderName $direction 消息，内容：$messageContent"
        }
        
        AccessibilityUtils.setViewAccessibilityLabel(this, label)
    }

    /**
     * 设置消息状态的无障碍标签
     * 验证: 需求 12.1
     */
    fun setStatusAccessibilityLabel() {
        val statusText = when (messageStatus) {
            MessageStatus.SENDING -> "消息发送中"
            MessageStatus.SENT -> "消息已发送"
            MessageStatus.DELIVERED -> "消息已送达"
            MessageStatus.READ -> "消息已读"
        }
        
        AccessibilityUtils.setStatusAccessibilityLabel(statusIcon, statusText)
    }
}
