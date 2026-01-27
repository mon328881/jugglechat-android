package com.juggle.im.android.theme

import org.junit.Test
import org.junit.Assert.*

/**
 * 颜色工具类单元测试
 * 验证颜色相关的工具函数
 */
class ColorUtilsTest {
    
    /**
     * 属性 3: WCAG AA 对比度合规性
     * 验证: 需求 1.3
     * 
     * 对于任何两种颜色组合（前景色和背景色），它们的对比度应该满足 
     * WCAG AA 级别的要求（至少 4.5:1 用于文本）
     */
    @Test
    fun testWCAGAAContrastCompliance() {
        val lightTheme = ThemeConfig.lightTheme()
        val darkTheme = ThemeConfig.darkTheme()
        
        // 验证浅色主题的对比度
        // 主文本颜色与背景色的对比度
        val lightTextContrast = ColorUtils.calculateContrast(
            lightTheme.colors.onBackground,
            lightTheme.colors.background
        )
        assertTrue("浅色主题文本与背景对比度应该符合 WCAG AA 标准", 
            lightTextContrast >= 4.5f)
        
        // 主题色与背景色的对比度
        val lightPrimaryContrast = ColorUtils.calculateContrast(
            lightTheme.colors.primary,
            lightTheme.colors.background
        )
        assertTrue("浅色主题主题色与背景对比度应该符合 WCAG AA 标准", 
            lightPrimaryContrast >= 3f)
        
        // 验证深色主题的对比度
        val darkTextContrast = ColorUtils.calculateContrast(
            darkTheme.colors.onBackground,
            darkTheme.colors.background
        )
        assertTrue("深色主题文本与背景对比度应该符合 WCAG AA 标准", 
            darkTextContrast >= 4.5f)
        
        // 错误色与背景色的对比度
        val errorContrast = ColorUtils.calculateContrast(
            lightTheme.colors.error,
            lightTheme.colors.background
        )
        assertTrue("错误色与背景对比度应该符合 WCAG AA 标准", 
            errorContrast >= 3f)
    }
    
    /**
     * 验证对比度计算的正确性
     */
    @Test
    fun testContrastCalculation() {
        // 黑色和白色的对比度应该是最大的（21:1）
        val maxContrast = ColorUtils.calculateContrast(0xFFFFFFFF.toInt(), 0xFF000000.toInt())
        assertTrue("黑白对比度应该接近 21:1", maxContrast > 20f)
        
        // 相同颜色的对比度应该是 1:1
        val sameContrast = ColorUtils.calculateContrast(0xFF64B5F6.toInt(), 0xFF64B5F6.toInt())
        assertEquals("相同颜色的对比度应该是 1:1", 1f, sameContrast, 0.01f)
    }
    
    /**
     * 验证相对亮度计算
     */
    @Test
    fun testRelativeLuminance() {
        // 白色的相对亮度应该接近 1.0
        val whiteLuminance = ColorUtils.getRelativeLuminance(0xFFFFFFFF.toInt())
        assertTrue("白色的相对亮度应该接近 1.0", whiteLuminance > 0.99f)
        
        // 黑色的相对亮度应该接近 0.0
        val blackLuminance = ColorUtils.getRelativeLuminance(0xFF000000.toInt())
        assertTrue("黑色的相对亮度应该接近 0.0", blackLuminance < 0.01f)
        
        // 相对亮度应该在 0.0 到 1.0 之间
        val blueLuminance = ColorUtils.getRelativeLuminance(0xFF64B5F6.toInt())
        assertTrue("相对亮度应该在 0.0 到 1.0 之间", blueLuminance in 0f..1f)
    }
    
    /**
     * 验证 WCAG AA 合规性检查
     */
    @Test
    fun testWCAGAACompliance() {
        // 黑色文本在白色背景上应该符合 WCAG AA 标准
        assertTrue("黑色文本在白色背景上应该符合 WCAG AA 标准",
            ColorUtils.isWCAGAACompliant(0xFF000000.toInt(), 0xFFFFFFFF.toInt()))
        
        // 白色文本在黑色背景上应该符合 WCAG AA 标准
        assertTrue("白色文本在黑色背景上应该符合 WCAG AA 标准",
            ColorUtils.isWCAGAACompliant(0xFFFFFFFF.toInt(), 0xFF000000.toInt()))
        
        // 灰色文本在白色背景上可能不符合 WCAG AA 标准
        val grayContrast = ColorUtils.calculateContrast(0xFF808080.toInt(), 0xFFFFFFFF.toInt())
        val isCompliant = ColorUtils.isWCAGAACompliant(0xFF808080.toInt(), 0xFFFFFFFF.toInt())
        assertEquals("合规性检查应该与对比度计算一致", grayContrast >= 4.5f, isCompliant)
    }
    
    /**
     * 验证颜色转换函数
     */
    @Test
    fun testColorConversion() {
        // 测试十六进制字符串转换
        val hexString = "#FF64B5F6"
        val color = ColorUtils.fromHexString(hexString)
        val convertedHex = ColorUtils.toHexString(color)
        assertEquals("颜色转换应该保持一致", hexString, convertedHex)
        
        // 测试 RGB 格式的十六进制字符串
        val rgbHex = "#64B5F6"
        val rgbColor = ColorUtils.fromHexString(rgbHex)
        assertTrue("RGB 格式应该被正确解析", rgbColor != 0)
    }
    
    /**
     * 验证颜色混合函数
     */
    @Test
    fun testColorBlending() {
        val white = 0xFFFFFFFF.toInt()
        val black = 0xFF000000.toInt()
        
        // 混合比例为 0.5 时应该得到灰色
        val gray = ColorUtils.blendColors(white, black, 0.5f)
        val grayLuminance = ColorUtils.getRelativeLuminance(gray)
        assertTrue("混合颜色的亮度应该在两种颜色之间", 
            grayLuminance in 0.4f..0.6f)
        
        // 混合比例为 0.0 时应该得到第一种颜色
        val blended0 = ColorUtils.blendColors(white, black, 0f)
        assertEquals("混合比例为 0.0 时应该得到第一种颜色", white, blended0)
        
        // 混合比例为 1.0 时应该得到第二种颜色
        val blended1 = ColorUtils.blendColors(white, black, 1f)
        assertEquals("混合比例为 1.0 时应该得到第二种颜色", black, blended1)
    }
    
    /**
     * 验证深色模式颜色转换
     */
    @Test
    fun testDarkModeConversion() {
        val lightColor = 0xFFFFFFFF.toInt()  // 白色
        val darkColor = ColorUtils.convertToDarkMode(lightColor)
        
        // 深色模式下的颜色应该是深色
        val darkLuminance = ColorUtils.getRelativeLuminance(darkColor)
        assertTrue("深色模式下的颜色应该是深色", darkLuminance < 0.5f)
        
        // 再次转换应该接近原始颜色
        val reconvertedColor = ColorUtils.convertToDarkMode(darkColor)
        val reconvertedLuminance = ColorUtils.getRelativeLuminance(reconvertedColor)
        assertTrue("再次转换应该接近原始颜色", reconvertedLuminance > 0.5f)
    }
}
