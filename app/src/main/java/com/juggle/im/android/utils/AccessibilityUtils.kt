package com.juggle.im.android.utils

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat

/**
 * 无障碍设计工具类
 * 提供为 UI 元素添加无障碍标签和描述的方法
 */
object AccessibilityUtils {

    /**
     * 为按钮添加无障碍标签
     * @param button 按钮视图
     * @param label 无障碍标签文本
     */
    fun setButtonAccessibilityLabel(button: Button, label: String) {
        button.contentDescription = label
        ViewCompat.setAccessibilityDelegate(button, object : androidx.core.view.AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.contentDescription = label
                info.isClickable = true
            }
        })
    }

    /**
     * 为图片视图添加无障碍标签
     * @param imageView 图片视图
     * @param description 图片描述文本
     */
    fun setImageAccessibilityDescription(imageView: ImageView, description: String) {
        imageView.contentDescription = description
        ViewCompat.setAccessibilityDelegate(imageView, object : androidx.core.view.AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.contentDescription = description
            }
        })
    }

    /**
     * 为文本视图添加无障碍标签
     * @param textView 文本视图
     * @param label 无障碍标签文本
     */
    fun setTextViewAccessibilityLabel(textView: TextView, label: String) {
        textView.contentDescription = label
        ViewCompat.setAccessibilityDelegate(textView, object : androidx.core.view.AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.contentDescription = label
            }
        })
    }

    /**
     * 为通用视图添加无障碍标签
     * @param view 视图
     * @param label 无障碍标签文本
     */
    fun setViewAccessibilityLabel(view: View, label: String) {
        view.contentDescription = label
        ViewCompat.setAccessibilityDelegate(view, object : androidx.core.view.AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.contentDescription = label
            }
        })
    }

    /**
     * 为可交互元素添加完整的无障碍信息
     * @param view 视图
     * @param label 标签文本
     * @param hint 提示文本
     * @param isClickable 是否可点击
     */
    fun setCompleteAccessibilityInfo(
        view: View,
        label: String,
        hint: String? = null,
        isClickable: Boolean = false
    ) {
        view.contentDescription = label
        ViewCompat.setAccessibilityDelegate(view, object : androidx.core.view.AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.contentDescription = label
                if (!hint.isNullOrEmpty()) {
                    info.hintText = hint
                }
                info.isClickable = isClickable
            }
        })
    }

    /**
     * 检查视图是否有无障碍标签
     * @param view 视图
     * @return 如果有无障碍标签返回 true，否则返回 false
     */
    fun hasAccessibilityLabel(view: View): Boolean {
        return !view.contentDescription.isNullOrEmpty()
    }

    /**
     * 获取视图的无障碍标签
     * @param view 视图
     * @return 无障碍标签文本，如果没有则返回空字符串
     */
    fun getAccessibilityLabel(view: View): String {
        return view.contentDescription?.toString() ?: ""
    }

    /**
     * 为列表项添加无障碍标签
     * @param view 列表项视图
     * @param itemLabel 列表项标签
     * @param itemPosition 列表项位置
     * @param itemCount 列表项总数
     */
    fun setListItemAccessibilityLabel(
        view: View,
        itemLabel: String,
        itemPosition: Int,
        itemCount: Int
    ) {
        val label = "$itemLabel, 第 ${itemPosition + 1} 项，共 $itemCount 项"
        view.contentDescription = label
        ViewCompat.setAccessibilityDelegate(view, object : androidx.core.view.AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.contentDescription = label
                info.isClickable = true
            }
        })
    }

    /**
     * 为状态指示器添加无障碍标签
     * @param view 视图
     * @param status 状态文本
     */
    fun setStatusAccessibilityLabel(view: View, status: String) {
        view.contentDescription = status
        ViewCompat.setAccessibilityDelegate(view, object : androidx.core.view.AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.contentDescription = status
            }
        })
    }

    /**
     * 为输入框添加无障碍标签
     * @param view 输入框视图
     * @param label 标签文本
     * @param hint 提示文本
     */
    fun setInputAccessibilityLabel(view: View, label: String, hint: String? = null) {
        view.contentDescription = label
        ViewCompat.setAccessibilityDelegate(view, object : androidx.core.view.AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.contentDescription = label
                if (!hint.isNullOrEmpty()) {
                    info.hintText = hint
                }
            }
        })
    }

    /**
     * 为图标按钮添加无障碍标签
     * @param view 图标按钮视图
     * @param label 按钮标签
     * @param action 按钮操作描述
     */
    fun setIconButtonAccessibilityLabel(view: View, label: String, action: String? = null) {
        val fullLabel = if (action != null) "$label，$action" else label
        view.contentDescription = fullLabel
        ViewCompat.setAccessibilityDelegate(view, object : androidx.core.view.AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.contentDescription = fullLabel
                info.isClickable = true
            }
        })
    }
}
