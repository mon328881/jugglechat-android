package com.juggle.im.android.widget

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.juggle.im.android.animation.AnimationUtils
import com.juggle.im.android.theme.ThemeManager

/**
 * Material Design 进度指示器组件
 * 支持圆形加载动画
 */
class MaterialProgressIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    private val themeManager: ThemeManager = ThemeManager.getInstance(context)
    private val theme = themeManager.getCurrentTheme()
    
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = theme.colors.primary
        style = Paint.Style.STROKE
        strokeWidth = 4f
        strokeCap = Paint.Cap.ROUND
    }
    
    private val rectF: RectF = RectF()
    private var rotation: Float = 0f
    private var animator: ObjectAnimator? = null
    private var isAnimating: Boolean = false
    
    /**
     * 启动加载动画
     * 验证: 需求 11.6
     */
    fun startLoading() {
        if (isAnimating) return
        
        isAnimating = true
        animator = AnimationUtils.createLoadingAnimation(this, 1000)
        animator?.start()
    }
    
    /**
     * 停止加载动画
     */
    fun stopLoading() {
        isAnimating = false
        animator?.cancel()
        animator = null
        rotation = 0f
        invalidate()
    }
    
    /**
     * 设置进度颜色
     */
    fun setProgressColor(color: Int) {
        paint.color = color
        invalidate()
    }
    
    /**
     * 设置进度宽度
     */
    fun setProgressWidth(width: Float) {
        paint.strokeWidth = width
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = minOf(width, height) / 2f - paint.strokeWidth
        
        // 设置矩形范围
        rectF.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )
        
        // 保存画布状态
        canvas.save()
        
        // 旋转画布
        canvas.rotate(rotation, centerX, centerY)
        
        // 绘制圆形进度条
        canvas.drawArc(rectF, 0f, 270f, false, paint)
        
        // 恢复画布状态
        canvas.restore()
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = 48 // 默认大小 48dp
        setMeasuredDimension(size, size)
    }
}
