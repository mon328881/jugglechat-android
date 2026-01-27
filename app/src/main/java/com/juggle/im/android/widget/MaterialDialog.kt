package com.juggle.im.android.widget

import android.content.Context
import android.view.animation.AlphaAnimation
import android.view.animation.ScaleAnimation
import android.view.animation.AnimationSet
import androidx.appcompat.app.AlertDialog
import com.juggle.im.android.animation.AnimationUtils
import com.juggle.im.android.theme.ThemeManager
import com.juggle.im.android.utils.AccessibilityUtils

/**
 * Material Design 3 对话框组件
 * 支持警告、确认、输入、列表对话框
 */
class MaterialDialog(context: Context) {
    
    private val themeManager: ThemeManager = ThemeManager.getInstance(context)
    private val theme = themeManager.getCurrentTheme()
    private val builder: AlertDialog.Builder = AlertDialog.Builder(context)
    
    /**
     * 对话框类型
     */
    enum class DialogType {
        ALERT,      // 警告对话框
        CONFIRM,    // 确认对话框
        INPUT,      // 输入对话框
        LIST        // 列表对话框
    }
    
    init {
        // 应用主题样式
        applyThemeStyle()
    }
    
    /**
     * 应用主题样式
     */
    private fun applyThemeStyle() {
        // 设置背景颜色
        builder.setBackgroundInsetStart(0)
        builder.setBackgroundInsetEnd(0)
    }
    
    /**
     * 创建警告对话框
     */
    fun createAlertDialog(
        title: String,
        message: String,
        positiveButtonText: String = "确定",
        onPositiveClick: () -> Unit = {}
    ): AlertDialog {
        builder.apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton(positiveButtonText) { dialog, _ ->
                onPositiveClick()
                dialog.dismiss()
            }
        }
        
        return builder.create().apply {
            // 添加缩放和淡入动画
            setOnShowListener {
                val animation = createShowAnimation()
                window?.decorView?.startAnimation(animation)
            }
        }
    }
    
    /**
     * 创建确认对话框
     */
    fun createConfirmDialog(
        title: String,
        message: String,
        positiveButtonText: String = "确定",
        negativeButtonText: String = "取消",
        onPositiveClick: () -> Unit = {},
        onNegativeClick: () -> Unit = {}
    ): AlertDialog {
        builder.apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton(positiveButtonText) { dialog, _ ->
                onPositiveClick()
                dialog.dismiss()
            }
            setNegativeButton(negativeButtonText) { dialog, _ ->
                onNegativeClick()
                dialog.dismiss()
            }
        }
        
        return builder.create().apply {
            setOnShowListener {
                val animation = createShowAnimation()
                window?.decorView?.startAnimation(animation)
            }
        }
    }
    
    /**
     * 创建输入对话框
     */
    fun createInputDialog(
        title: String,
        hint: String = "",
        positiveButtonText: String = "确定",
        negativeButtonText: String = "取消",
        onPositiveClick: (String) -> Unit = {},
        onNegativeClick: () -> Unit = {}
    ): AlertDialog {
        val inputField = MaterialTextField(builder.context).apply {
            setHint(hint)
        }
        
        builder.apply {
            setTitle(title)
            setView(inputField)
            setPositiveButton(positiveButtonText) { dialog, _ ->
                onPositiveClick(inputField.getText())
                dialog.dismiss()
            }
            setNegativeButton(negativeButtonText) { dialog, _ ->
                onNegativeClick()
                dialog.dismiss()
            }
        }
        
        return builder.create().apply {
            setOnShowListener {
                val animation = createShowAnimation()
                window?.decorView?.startAnimation(animation)
            }
        }
    }
    
    /**
     * 创建列表对话框
     */
    fun createListDialog(
        title: String,
        items: Array<String>,
        onItemClick: (Int, String) -> Unit = { _, _ -> }
    ): AlertDialog {
        builder.apply {
            setTitle(title)
            setItems(items) { dialog, which ->
                onItemClick(which, items[which])
                dialog.dismiss()
            }
        }
        
        return builder.create().apply {
            setOnShowListener {
                val animation = createShowAnimation()
                window?.decorView?.startAnimation(animation)
            }
        }
    }
    
    /**
     * 创建显示动画（缩放和淡入）
     * 验证: 需求 11.4
     */
    private fun createShowAnimation(): AnimationSet {
        return AnimationUtils.createDialogOpenAnimation(null, 300)
    }

    /**
     * 为对话框设置无障碍标签
     * 验证: 需求 12.1
     */
    fun setAccessibilityLabel(dialog: AlertDialog, label: String) {
        dialog.window?.decorView?.let {
            AccessibilityUtils.setViewAccessibilityLabel(it, label)
        }
    }

    /**
     * 为对话框按钮设置无障碍标签
     * 验证: 需求 12.1
     */
    fun setButtonAccessibilityLabel(dialog: AlertDialog, buttonId: Int, label: String) {
        dialog.getButton(buttonId)?.let {
            AccessibilityUtils.setButtonAccessibilityLabel(it, label)
        }
    }
}
