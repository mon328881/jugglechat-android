package com.juggle.im.android.animation

import android.view.View
import android.view.ViewGroup

/**
 * 过渡管理器
 * 管理界面切换时的动画效果
 */
class TransitionManager {
    
    companion object {
        private var instance: TransitionManager? = null
        
        /**
         * 获取单例实例
         */
        fun getInstance(): TransitionManager {
            return instance ?: synchronized(this) {
                instance ?: TransitionManager().also { instance = it }
            }
        }
    }
    
    /**
     * 执行界面切换动画
     * 验证: 需求 11.1
     * @param fromView 源视图
     * @param toView 目标视图
     * @param duration 动画时长（毫秒）
     * @param onTransitionEnd 过渡结束回调
     */
    fun transitionBetweenViews(
        fromView: View,
        toView: View,
        duration: Long = 300,
        onTransitionEnd: (() -> Unit)? = null
    ) {
        // 淡出源视图
        AnimationUtils.fadeOutAnimation(fromView, duration / 2) {
            fromView.visibility = View.GONE
            
            // 淡入目标视图
            toView.visibility = View.VISIBLE
            AnimationUtils.fadeInAnimation(toView, duration / 2) {
                onTransitionEnd?.invoke()
            }
        }
    }
    
    /**
     * 执行平滑过渡动画
     * 验证: 需求 11.1
     * @param view 要应用动画的视图
     * @param duration 动画时长（毫秒）
     * @param onTransitionEnd 过渡结束回调
     */
    fun smoothTransition(
        view: View,
        duration: Long = 300,
        onTransitionEnd: (() -> Unit)? = null
    ) {
        val animation = AnimationUtils.createSmoothTransitionAnimation(view, duration)
        animation.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation?) {}
            
            override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                onTransitionEnd?.invoke()
            }
            
            override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
        })
        view.startAnimation(animation)
    }
    
    /**
     * 执行列表项加载动画
     * 验证: 需求 11.3
     * @param container 容器视图
     * @param itemCount 项目数量
     * @param duration 动画时长（毫秒）
     */
    fun animateListItems(
        container: ViewGroup,
        itemCount: Int,
        duration: Long = 300
    ) {
        for (i in 0 until minOf(itemCount, container.childCount)) {
            val child = container.getChildAt(i)
            val delay = (i * 50).toLong()
            AnimationUtils.listItemFadeInAnimation(child, delay, duration)
        }
    }
    
    /**
     * 执行对话框打开动画
     * 验证: 需求 11.4
     * @param view 对话框视图
     * @param duration 动画时长（毫秒）
     * @param onAnimationEnd 动画结束回调
     */
    fun showDialogWithAnimation(
        view: View,
        duration: Long = 300,
        onAnimationEnd: (() -> Unit)? = null
    ) {
        AnimationUtils.dialogScaleInAnimation(view, duration, onAnimationEnd)
    }
    
    /**
     * 执行对话框关闭动画
     * 验证: 需求 11.4
     * @param view 对话框视图
     * @param duration 动画时长（毫秒）
     * @param onAnimationEnd 动画结束回调
     */
    fun hideDialogWithAnimation(
        view: View,
        duration: Long = 300,
        onAnimationEnd: (() -> Unit)? = null
    ) {
        AnimationUtils.dialogScaleOutAnimation(view, duration, onAnimationEnd)
    }
    
    /**
     * 执行消息气泡发送动画
     * 验证: 需求 11.5
     * @param view 消息气泡视图
     * @param duration 动画时长（毫秒）
     */
    fun animateMessageBubbleSend(
        view: View,
        duration: Long = 300
    ) {
        val animation = AnimationUtils.createMessageBubbleSendAnimation(view, duration)
        view.startAnimation(animation)
    }
    
    /**
     * 执行加载动画
     * 验证: 需求 11.6
     * @param view 加载指示器视图
     */
    fun startLoadingAnimation(view: View) {
        AnimationUtils.loadingRotateAnimation(view, 1000)
    }
    
    /**
     * 停止加载动画
     * @param view 加载指示器视图
     */
    fun stopLoadingAnimation(view: View) {
        AnimationUtils.cancelAllAnimations(view)
    }
}
