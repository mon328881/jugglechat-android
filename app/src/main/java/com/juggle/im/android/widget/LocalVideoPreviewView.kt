package com.juggle.im.android.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import com.juggle.im.android.theme.ThemeManager

/**
 * 本地视频预览视图组件
 * 显示在右下角，用户可以拖动调整位置
 * 需求：10.2
 */
class LocalVideoPreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    
    private val themeManager: ThemeManager = ThemeManager.getInstance(context)
    private val theme = themeManager.getCurrentTheme()
    
    // 拖动相关
    private var lastX = 0f
    private var lastY = 0f
    private var isDragging = false
    
    // 边界约束
    private var parentWidth = 0
    private var parentHeight = 0
    
    init {
        layoutParams = LayoutParams(
            120,  // 宽度 120dp
            160   // 高度 160dp
        )
        setBackgroundColor(theme.colors.surface)
        
        // 设置圆角
        val radius = 8f
        val drawable = android.graphics.drawable.ShapeDrawable(
            android.graphics.drawable.shapes.RoundRectShape(
                floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius),
                null,
                null
            )
        )
        drawable.paint.color = theme.colors.surface
        background = drawable
    }
    
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return false
        
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.rawX
                lastY = event.rawY
                isDragging = true
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    val deltaX = event.rawX - lastX
                    val deltaY = event.rawY - lastY
                    
                    // 计算新位置
                    var newX = x + deltaX
                    var newY = y + deltaY
                    
                    // 约束在父容器内
                    if (parentWidth > 0 && parentHeight > 0) {
                        newX = newX.coerceIn(0f, (parentWidth - width).toFloat())
                        newY = newY.coerceIn(0f, (parentHeight - height).toFloat())
                    }
                    
                    // 更新位置
                    x = newX
                    y = newY
                    
                    lastX = event.rawX
                    lastY = event.rawY
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                isDragging = false
                return true
            }
        }
        
        return super.onTouchEvent(event)
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        // 获取父容器尺寸
        val parent = parent
        if (parent is FrameLayout) {
            parentWidth = parent.width
            parentHeight = parent.height
        }
    }
    
    /**
     * 设置预览位置（右下角）
     */
    fun setDefaultPosition(parentWidth: Int, parentHeight: Int) {
        this.parentWidth = parentWidth
        this.parentHeight = parentHeight
        
        // 设置到右下角
        val margin = 16  // 16dp 边距
        x = (parentWidth - width - margin).toFloat()
        y = (parentHeight - height - margin).toFloat()
    }
    
    /**
     * 获取当前位置 X
     */
    fun getPositionX(): Float = x
    
    /**
     * 获取当前位置 Y
     */
    fun getPositionY(): Float = y
    
    /**
     * 设置位置
     */
    fun setPosition(x: Float, y: Float) {
        this.x = x.coerceIn(0f, (parentWidth - width).toFloat())
        this.y = y.coerceIn(0f, (parentHeight - height).toFloat())
    }
}
