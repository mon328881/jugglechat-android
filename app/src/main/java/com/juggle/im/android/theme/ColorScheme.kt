package com.juggle.im.android.theme

import androidx.annotation.ColorInt

/**
 * 颜色方案数据类
 * 定义应用的所有颜色值，包括主题色、辅助色、背景色等
 */
data class ColorScheme(
    // 主要颜色
    @ColorInt val primary: Int,
    @ColorInt val onPrimary: Int,
    
    // 辅助颜色
    @ColorInt val secondary: Int,
    @ColorInt val onSecondary: Int,
    
    // 三级颜色
    @ColorInt val tertiary: Int,
    @ColorInt val onTertiary: Int,
    
    // 背景和表面颜色
    @ColorInt val background: Int,
    @ColorInt val onBackground: Int,
    @ColorInt val surface: Int,
    @ColorInt val onSurface: Int,
    
    // 错误颜色
    @ColorInt val error: Int,
    @ColorInt val onError: Int,
    
    // 状态颜色
    @ColorInt val success: Int,
    @ColorInt val warning: Int,
    @ColorInt val info: Int,
    
    // 其他颜色
    @ColorInt val divider: Int,
    @ColorInt val hint: Int,
    @ColorInt val disabled: Int
)
