package com.juggle.im.android.theme

/**
 * 排版方案数据类
 * 定义应用的所有文本样式，包括标题、正文、标签等
 */
data class TypographyScheme(
    // 标题样式
    val headlineL: TextStyle,  // 32sp, 粗体
    val headlineM: TextStyle,  // 28sp, 粗体
    val headlineS: TextStyle,  // 24sp, 粗体
    
    // 正文样式
    val bodyL: TextStyle,      // 16sp, 常规
    val bodyM: TextStyle,      // 14sp, 常规
    val bodyS: TextStyle,      // 12sp, 常规
    
    // 标签样式
    val labelL: TextStyle,     // 14sp, 中等
    val labelM: TextStyle,     // 12sp, 中等
    val labelS: TextStyle      // 11sp, 中等
)

/**
 * 文本样式数据类
 */
data class TextStyle(
    val fontSize: Int,         // 字体大小（sp）
    val fontWeight: Int,       // 字体粗细（400=常规, 500=中等, 700=粗体）
    val lineHeight: Int        // 行高（sp）
)
