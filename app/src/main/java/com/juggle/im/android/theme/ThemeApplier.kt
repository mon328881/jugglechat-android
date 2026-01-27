package com.juggle.im.android.theme

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate

/**
 * 主题应用工具类
 * 负责将主题配置应用到 Activity 和应用级别
 */
object ThemeApplier {
    
    /**
     * 应用主题到 Activity
     */
    fun applyThemeToActivity(activity: Activity, theme: ThemeConfig) {
        // 应用系统深色模式设置
        val nightMode = if (theme.isDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
        
        // 更新状态栏颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.statusBarColor = theme.colors.primary
            activity.window.navigationBarColor = theme.colors.surface
        }
    }
    
    /**
     * 应用主题到应用
     */
    fun applyThemeToApp(context: Context, theme: ThemeConfig) {
        val nightMode = if (theme.isDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
    
    /**
     * 获取主题颜色
     */
    fun getThemeColor(context: Context, colorAttr: Int): Int {
        val typedArray = context.theme.obtainStyledAttributes(intArrayOf(colorAttr))
        val color = typedArray.getColor(0, 0)
        typedArray.recycle()
        return color
    }
}
