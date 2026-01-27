package com.juggle.im.android.utils

import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.core.content.ContextCompat

/**
 * 无障碍配置管理类
 * 处理系统字体大小、高对比度模式等无障碍设置
 */
class AccessibilityConfig(private val context: Context) {

    private val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

    /**
     * 获取系统字体大小缩放因子
     * @return 字体大小缩放因子（1.0 为正常大小）
     */
    fun getFontScaleFactor(): Float {
        return context.resources.configuration.fontScale
    }

    /**
     * 检查是否启用了高对比度模式
     * @return 如果启用了高对比度模式返回 true，否则返回 false
     */
    fun isHighContrastModeEnabled(): Boolean {
        return try {
            Settings.Secure.getInt(context.contentResolver, "high_text_contrast_enabled", 0) == 1
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 检查是否启用了屏幕阅读器
     * @return 如果启用了屏幕阅读器返回 true，否则返回 false
     */
    fun isScreenReaderEnabled(): Boolean {
        return accessibilityManager.isEnabled && accessibilityManager.isTouchExplorationEnabled
    }

    /**
     * 获取所有启用的无障碍服务
     * @return 启用的无障碍服务列表
     */
    fun getEnabledAccessibilityServices(): List<String> {
        return try {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: ""
            enabledServices.split(":").filter { it.isNotEmpty() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 检查是否启用了任何无障碍服务
     * @return 如果启用了任何无障碍服务返回 true，否则返回 false
     */
    fun isAccessibilityEnabled(): Boolean {
        return accessibilityManager.isEnabled
    }

    /**
     * 获取推荐的最小触摸目标大小（dp）
     * @return 最小触摸目标大小（dp）
     */
    fun getMinimumTouchTargetSize(): Int {
        return 48 // WCAG 2.1 AA 标准推荐的最小尺寸
    }

    /**
     * 获取推荐的最小文本大小（sp）
     * @return 最小文本大小（sp）
     */
    fun getMinimumTextSize(): Float {
        val baseFontSize = 14f // 基础字体大小
        return baseFontSize * getFontScaleFactor()
    }

    /**
     * 获取推荐的最小对比度比率
     * @return 最小对比度比率
     */
    fun getMinimumContrastRatio(): Float {
        return if (isHighContrastModeEnabled()) {
            7f // 高对比度模式下的最小对比度比率（AAA 级别）
        } else {
            4.5f // 正常模式下的最小对比度比率（AA 级别）
        }
    }

    /**
     * 检查字体大小是否需要调整
     * @return 如果字体大小不是正常大小返回 true，否则返回 false
     */
    fun isFontSizeAdjusted(): Boolean {
        return getFontScaleFactor() != 1.0f
    }

    /**
     * 获取调整后的字体大小
     * @param baseFontSize 基础字体大小（sp）
     * @return 调整后的字体大小（sp）
     */
    fun getAdjustedFontSize(baseFontSize: Float): Float {
        return baseFontSize * getFontScaleFactor()
    }

    /**
     * 获取调整后的尺寸
     * @param baseSize 基础尺寸（dp）
     * @return 调整后的尺寸（dp）
     */
    fun getAdjustedSize(baseSize: Int): Int {
        return (baseSize * getFontScaleFactor()).toInt()
    }

    /**
     * 检查是否需要应用高对比度颜色
     * @return 如果需要应用高对比度颜色返回 true，否则返回 false
     */
    fun shouldApplyHighContrastColors(): Boolean {
        return isHighContrastModeEnabled()
    }

    /**
     * 获取无障碍配置摘要
     * @return 无障碍配置摘要字符串
     */
    fun getAccessibilityConfigSummary(): String {
        val configs = mutableListOf<String>()
        
        if (isAccessibilityEnabled()) {
            configs.add("无障碍已启用")
        }
        
        if (isScreenReaderEnabled()) {
            configs.add("屏幕阅读器已启用")
        }
        
        if (isHighContrastModeEnabled()) {
            configs.add("高对比度模式已启用")
        }
        
        if (isFontSizeAdjusted()) {
            configs.add("字体大小已调整（${String.format("%.1f", getFontScaleFactor())}x）")
        }
        
        return if (configs.isEmpty()) {
            "无特殊无障碍设置"
        } else {
            configs.joinToString("，")
        }
    }
}
