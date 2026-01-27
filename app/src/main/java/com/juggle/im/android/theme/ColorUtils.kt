package com.juggle.im.android.theme

import kotlin.math.pow

/**
 * 颜色工具类
 * 提供颜色相关的工具函数，如对比度计算、颜色转换等
 */
object ColorUtils {
    
    /**
     * 计算两种颜色之间的对比度（WCAG 标准）
     * 对比度 = (L1 + 0.05) / (L2 + 0.05)
     * 其中 L 是相对亮度
     * 
     * @param foreground 前景色（RGB 格式）
     * @param background 背景色（RGB 格式）
     * @return 对比度值（1.0 - 21.0）
     */
    fun calculateContrast(foreground: Int, background: Int): Float {
        val l1 = getRelativeLuminance(foreground)
        val l2 = getRelativeLuminance(background)
        
        val lighter = maxOf(l1, l2)
        val darker = minOf(l1, l2)
        
        return (lighter + 0.05f) / (darker + 0.05f)
    }
    
    /**
     * 获取颜色的相对亮度（WCAG 标准）
     * 
     * @param color RGB 颜色值
     * @return 相对亮度（0.0 - 1.0）
     */
    fun getRelativeLuminance(color: Int): Float {
        val r = ((color shr 16) and 0xFF) / 255f
        val g = ((color shr 8) and 0xFF) / 255f
        val b = (color and 0xFF) / 255f
        
        val rLinear = if (r <= 0.03928) r / 12.92f else ((r + 0.055f) / 1.055f).pow(2.4f)
        val gLinear = if (g <= 0.03928) g / 12.92f else ((g + 0.055f) / 1.055f).pow(2.4f)
        val bLinear = if (b <= 0.03928) b / 12.92f else ((b + 0.055f) / 1.055f).pow(2.4f)
        
        return 0.2126f * rLinear + 0.7152f * gLinear + 0.0722f * bLinear
    }
    
    /**
     * 检查对比度是否符合 WCAG AA 标准
     * WCAG AA 标准：普通文本对比度 >= 4.5:1，大文本对比度 >= 3:1
     * 
     * @param foreground 前景色
     * @param background 背景色
     * @param isLargeText 是否为大文本
     * @return 是否符合标准
     */
    fun isWCAGAACompliant(foreground: Int, background: Int, isLargeText: Boolean = false): Boolean {
        val contrast = calculateContrast(foreground, background)
        return if (isLargeText) contrast >= 3f else contrast >= 4.5f
    }
    
    /**
     * 将颜色从浅色模式转换为深色模式
     * 通过反转亮度来实现
     * 
     * @param color 原始颜色
     * @return 深色模式下的颜色
     */
    fun convertToDarkMode(color: Int): Int {
        val r = (color shr 16) and 0xFF
        val g = (color shr 8) and 0xFF
        val b = color and 0xFF
        
        // 反转 RGB 值
        val darkR = 255 - r
        val darkG = 255 - g
        val darkB = 255 - b
        
        return (0xFF shl 24) or (darkR shl 16) or (darkG shl 8) or darkB
    }
    
    /**
     * 获取颜色的十六进制字符串表示
     * 
     * @param color RGB 颜色值
     * @return 十六进制字符串（如 "#FF64B5F6"）
     */
    fun toHexString(color: Int): String {
        return String.format("#%08X", color)
    }
    
    /**
     * 从十六进制字符串解析颜色
     * 
     * @param hexString 十六进制字符串（如 "#FF64B5F6" 或 "#64B5F6"）
     * @return RGB 颜色值
     */
    fun fromHexString(hexString: String): Int {
        val hex = hexString.removePrefix("#")
        return when (hex.length) {
            6 -> {
                // RGB 格式，添加完全不透明的 Alpha
                (0xFF shl 24) or hex.toLong(16).toInt()
            }
            8 -> {
                // ARGB 格式
                hex.toLong(16).toInt()
            }
            else -> throw IllegalArgumentException("Invalid hex color: $hexString")
        }
    }
    
    /**
     * 混合两种颜色
     * 
     * @param color1 第一种颜色
     * @param color2 第二种颜色
     * @param ratio 混合比例（0.0 - 1.0），0.0 表示完全是 color1，1.0 表示完全是 color2
     * @return 混合后的颜色
     */
    fun blendColors(color1: Int, color2: Int, ratio: Float): Int {
        val r1 = (color1 shr 16) and 0xFF
        val g1 = (color1 shr 8) and 0xFF
        val b1 = color1 and 0xFF
        
        val r2 = (color2 shr 16) and 0xFF
        val g2 = (color2 shr 8) and 0xFF
        val b2 = color2 and 0xFF
        
        val r = (r1 + (r2 - r1) * ratio).toInt()
        val g = (g1 + (g2 - g1) * ratio).toInt()
        val b = (b1 + (b2 - b1) * ratio).toInt()
        
        return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
    }
}
