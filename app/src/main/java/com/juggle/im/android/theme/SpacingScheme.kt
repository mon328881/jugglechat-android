package com.juggle.im.android.theme

/**
 * 间距方案数据类
 * 定义应用的所有间距值，用于保持一致的布局间距
 */
data class SpacingScheme(
    val xs: Int,    // 极小: 4dp
    val sm: Int,    // 小: 8dp
    val md: Int,    // 中: 16dp
    val lg: Int,    // 大: 24dp
    val xl: Int     // 极大: 32dp
) {
    companion object {
        /**
         * 创建标准间距方案
         */
        fun default(): SpacingScheme = SpacingScheme(
            xs = 4,
            sm = 8,
            md = 16,
            lg = 24,
            xl = 32
        )
    }
}
