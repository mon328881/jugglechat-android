package com.juggle.im.android.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation

/**
 * 动画工具类
 * 提供各种常用的动画效果
 */
object AnimationUtils {
    
    /**
     * 界面切换动画 - 淡入淡出
     * @param view 要应用动画的视图
     * @param duration 动画时长（毫秒）
     * @param onAnimationEnd 动画结束回调
     */
    fun fadeInAnimation(
        view: View,
        duration: Long = 300,
        onAnimationEnd: (() -> Unit)? = null
    ) {
        view.alpha = 0f
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                onAnimationEnd?.invoke()
            }
            .start()
    }
    
    /**
     * 界面切换动画 - 淡出
     * @param view 要应用动画的视图
     * @param duration 动画时长（毫秒）
     * @param onAnimationEnd 动画结束回调
     */
    fun fadeOutAnimation(
        view: View,
        duration: Long = 300,
        onAnimationEnd: (() -> Unit)? = null
    ) {
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                onAnimationEnd?.invoke()
            }
            .start()
    }
    
    /**
     * 列表项加载动画 - 淡入
     * @param view 要应用动画的视图
     * @param delay 延迟时间（毫秒）
     * @param duration 动画时长（毫秒）
     */
    fun listItemFadeInAnimation(
        view: View,
        delay: Long = 0,
        duration: Long = 300
    ) {
        view.alpha = 0f
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .setStartDelay(delay)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }
    
    /**
     * 对话框动画 - 缩放和淡入
     * @param view 要应用动画的视图
     * @param duration 动画时长（毫秒）
     * @param onAnimationEnd 动画结束回调
     */
    fun dialogScaleInAnimation(
        view: View,
        duration: Long = 300,
        onAnimationEnd: (() -> Unit)? = null
    ) {
        view.scaleX = 0.8f
        view.scaleY = 0.8f
        view.alpha = 0f
        
        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                onAnimationEnd?.invoke()
            }
            .start()
    }
    
    /**
     * 对话框动画 - 缩放和淡出
     * @param view 要应用动画的视图
     * @param duration 动画时长（毫秒）
     * @param onAnimationEnd 动画结束回调
     */
    fun dialogScaleOutAnimation(
        view: View,
        duration: Long = 300,
        onAnimationEnd: (() -> Unit)? = null
    ) {
        view.animate()
            .scaleX(0.8f)
            .scaleY(0.8f)
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                onAnimationEnd?.invoke()
            }
            .start()
    }
    
    /**
     * 消息气泡动画 - 从下方滑入并淡入
     * @param view 要应用动画的视图
     * @param duration 动画时长（毫秒）
     */
    fun messageBubbleSlideInAnimation(
        view: View,
        duration: Long = 300
    ) {
        view.alpha = 0f
        view.translationY = 50f
        
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }
    
    /**
     * 加载动画 - 旋转
     * @param view 要应用动画的视图
     * @param duration 动画时长（毫秒）
     */
    fun loadingRotateAnimation(
        view: View,
        duration: Long = 1000
    ): ObjectAnimator {
        return ObjectAnimator.ofFloat(view, "rotation", 0f, 360f).apply {
            this.duration = duration
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }
    
    /**
     * 按钮涟漪效果 - 脉冲动画
     * @param view 要应用动画的视图
     * @param duration 动画时长（毫秒）
     */
    fun buttonPulseAnimation(
        view: View,
        duration: Long = 600
    ) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f)
        
        scaleX.duration = duration
        scaleY.duration = duration
        
        scaleX.start()
        scaleY.start()
    }
    
    /**
     * 创建平滑的过渡动画集合
     * @param view 要应用动画的视图
     * @param duration 动画时长（毫秒）
     * @return 动画集合
     */
    fun createSmoothTransitionAnimation(
        view: View,
        duration: Long = 300
    ): AnimationSet {
        val animationSet = AnimationSet(true)
        
        // 淡入动画
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = duration
        
        // 缩放动画
        val scale = ScaleAnimation(
            0.9f, 1f,
            0.9f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        scale.duration = duration
        
        animationSet.addAnimation(fadeIn)
        animationSet.addAnimation(scale)
        animationSet.interpolator = DecelerateInterpolator()
        
        return animationSet
    }
    
    /**
     * 创建列表项加载动画集合
     * @param view 要应用动画的视图
     * @param delay 延迟时间（毫秒）
     * @param duration 动画时长（毫秒）
     * @return 动画集合
     */
    fun createListItemLoadAnimation(
        view: View,
        delay: Long = 0,
        duration: Long = 300
    ): AnimationSet {
        val animationSet = AnimationSet(true)
        
        // 淡入动画
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = duration
        fadeIn.startOffset = delay
        
        // 从上方滑入
        val slideIn = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, -0.2f,
            Animation.RELATIVE_TO_SELF, 0f
        )
        slideIn.duration = duration
        slideIn.startOffset = delay
        
        animationSet.addAnimation(fadeIn)
        animationSet.addAnimation(slideIn)
        animationSet.interpolator = DecelerateInterpolator()
        
        return animationSet
    }
    
    /**
     * 创建对话框打开动画集合
     * @param view 要应用动画的视图
     * @param duration 动画时长（毫秒）
     * @return 动画集合
     */
    fun createDialogOpenAnimation(
        view: View,
        duration: Long = 300
    ): AnimationSet {
        val animationSet = AnimationSet(true)
        
        // 淡入动画
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = duration
        
        // 缩放动画
        val scale = ScaleAnimation(
            0.8f, 1f,
            0.8f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        scale.duration = duration
        
        animationSet.addAnimation(fadeIn)
        animationSet.addAnimation(scale)
        animationSet.interpolator = DecelerateInterpolator()
        
        return animationSet
    }
    
    /**
     * 创建对话框关闭动画集合
     * @param view 要应用动画的视图
     * @param duration 动画时长（毫秒）
     * @return 动画集合
     */
    fun createDialogCloseAnimation(
        view: View,
        duration: Long = 300
    ): AnimationSet {
        val animationSet = AnimationSet(true)
        
        // 淡出动画
        val fadeOut = AlphaAnimation(1f, 0f)
        fadeOut.duration = duration
        
        // 缩放动画
        val scale = ScaleAnimation(
            1f, 0.8f,
            1f, 0.8f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        scale.duration = duration
        
        animationSet.addAnimation(fadeOut)
        animationSet.addAnimation(scale)
        animationSet.interpolator = DecelerateInterpolator()
        
        return animationSet
    }
    
    /**
     * 创建消息气泡发送动画集合
     * @param view 要应用动画的视图
     * @param duration 动画时长（毫秒）
     * @return 动画集合
     */
    fun createMessageBubbleSendAnimation(
        view: View,
        duration: Long = 300
    ): AnimationSet {
        val animationSet = AnimationSet(true)
        
        // 淡入动画
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = duration
        
        // 从下方滑入
        val slideIn = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 0.3f,
            Animation.RELATIVE_TO_SELF, 0f
        )
        slideIn.duration = duration
        
        // 缩放动画
        val scale = ScaleAnimation(
            0.8f, 1f,
            0.8f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        scale.duration = duration
        
        animationSet.addAnimation(fadeIn)
        animationSet.addAnimation(slideIn)
        animationSet.addAnimation(scale)
        animationSet.interpolator = DecelerateInterpolator()
        
        return animationSet
    }
    
    /**
     * 创建加载动画集合 - 旋转和脉冲
     * @param view 要应用动画的视图
     * @param duration 动画时长（毫秒）
     * @return ObjectAnimator
     */
    fun createLoadingAnimation(
        view: View,
        duration: Long = 1000
    ): ObjectAnimator {
        return ObjectAnimator.ofFloat(view, "rotation", 0f, 360f).apply {
            this.duration = duration
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }
    }
    
    /**
     * 取消所有动画
     * @param view 要取消动画的视图
     */
    fun cancelAllAnimations(view: View) {
        view.animate().cancel()
    }
}
