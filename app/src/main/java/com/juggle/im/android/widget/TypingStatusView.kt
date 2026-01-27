package com.juggle.im.android.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.juggle.im.android.theme.ThemeManager

/**
 * 输入状态显示组件
 * 显示"对方正在输入..."提示
 */
class TypingStatusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    
    private val themeManager: ThemeManager = ThemeManager.getInstance(context)
    private val theme = themeManager.getCurrentTheme()
    
    private val statusContainer: LinearLayout
    private val statusText: TextView
    private val animationDots: TextView
    
    private var isAnimating = false
    private var animationRunnable: Runnable? = null
    private var dotCount = 0
    
    init {
        // 创建状态容器
        statusContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.START or Gravity.CENTER_VERTICAL
                setMargins(theme.spacing.lg, theme.spacing.sm, 0, theme.spacing.sm)
            }
        }
        
        // 创建状态文本
        statusText = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
            text = "对方正在输入"
            textSize = 12f
            setTextColor(theme.colors.onBackground.let { 
                (it and 0xFFFFFF) or 0x80000000.toInt()
            })
        }
        statusContainer.addView(statusText)
        
        // 创建动画点
        animationDots = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(theme.spacing.xs, 0, 0, 0)
            }
            text = "."
            textSize = 12f
            setTextColor(theme.colors.onBackground.let { 
                (it and 0xFFFFFF) or 0x80000000.toInt()
            })
        }
        statusContainer.addView(animationDots)
        
        addView(statusContainer)
        
        visibility = GONE
    }
    
    /**
     * 显示输入状态
     */
    fun showTypingStatus() {
        visibility = VISIBLE
        if (!isAnimating) {
            isAnimating = true
            startAnimation()
        }
    }
    
    /**
     * 隐藏输入状态
     */
    fun hideTypingStatus() {
        visibility = GONE
        isAnimating = false
        removeCallbacks(animationRunnable)
        dotCount = 0
    }
    
    /**
     * 启动动画
     */
    private fun startAnimation() {
        animationRunnable = Runnable {
            if (isAnimating) {
                dotCount = (dotCount + 1) % 4
                animationDots.text = ".".repeat(dotCount)
                postDelayed(animationRunnable!!, 500)
            }
        }
        postDelayed(animationRunnable!!, 500)
    }
    
    /**
     * 检查是否正在显示输入状态
     */
    fun isShowingTypingStatus(): Boolean = visibility == VISIBLE
}
