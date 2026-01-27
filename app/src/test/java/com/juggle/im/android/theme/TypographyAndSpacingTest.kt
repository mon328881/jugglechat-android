package com.juggle.im.android.theme

import org.junit.Test
import org.junit.Assert.*

/**
 * 排版和间距系统单元测试
 * 验证排版和间距规范的完整性和正确性
 */
class TypographyAndSpacingTest {
    
    /**
     * 验证排版系统的完整性
     */
    @Test
    fun testTypographySchemeCompleteness() {
        val theme = ThemeConfig.lightTheme()
        val typography = theme.typography
        
        // 验证所有排版样式都已定义
        assertNotNull("标题 L 不能为空", typography.headlineL)
        assertNotNull("标题 M 不能为空", typography.headlineM)
        assertNotNull("标题 S 不能为空", typography.headlineS)
        assertNotNull("正文 L 不能为空", typography.bodyL)
        assertNotNull("正文 M 不能为空", typography.bodyM)
        assertNotNull("正文 S 不能为空", typography.bodyS)
        assertNotNull("标签 L 不能为空", typography.labelL)
        assertNotNull("标签 M 不能为空", typography.labelM)
        assertNotNull("标签 S 不能为空", typography.labelS)
    }
    
    /**
     * 验证排版样式的字体大小递减
     */
    @Test
    fun testTypographyFontSizeHierarchy() {
        val theme = ThemeConfig.lightTheme()
        val typography = theme.typography
        
        // 标题字体大小应该递减
        assertTrue("标题字体大小应该递减", 
            typography.headlineL.fontSize > typography.headlineM.fontSize)
        assertTrue("标题字体大小应该递减", 
            typography.headlineM.fontSize > typography.headlineS.fontSize)
        
        // 正文字体大小应该递减
        assertTrue("正文字体大小应该递减", 
            typography.bodyL.fontSize > typography.bodyM.fontSize)
        assertTrue("正文字体大小应该递减", 
            typography.bodyM.fontSize > typography.bodyS.fontSize)
        
        // 标题字体大小应该大于正文
        assertTrue("标题字体大小应该大于正文", 
            typography.headlineS.fontSize > typography.bodyL.fontSize)
    }
    
    /**
     * 验证排版样式的字体粗细
     */
    @Test
    fun testTypographyFontWeight() {
        val theme = ThemeConfig.lightTheme()
        val typography = theme.typography
        
        // 标题应该是粗体（700）
        assertEquals("标题应该是粗体", 700, typography.headlineL.fontWeight)
        assertEquals("标题应该是粗体", 700, typography.headlineM.fontWeight)
        assertEquals("标题应该是粗体", 700, typography.headlineS.fontWeight)
        
        // 正文应该是常规（400）
        assertEquals("正文应该是常规", 400, typography.bodyL.fontWeight)
        assertEquals("正文应该是常规", 400, typography.bodyM.fontWeight)
        assertEquals("正文应该是常规", 400, typography.bodyS.fontWeight)
        
        // 标签应该是中等（500）
        assertEquals("标签应该是中等", 500, typography.labelL.fontWeight)
        assertEquals("标签应该是中等", 500, typography.labelM.fontWeight)
        assertEquals("标签应该是中等", 500, typography.labelS.fontWeight)
    }
    
    /**
     * 验证排版样式的行高
     */
    @Test
    fun testTypographyLineHeight() {
        val theme = ThemeConfig.lightTheme()
        val typography = theme.typography
        
        // 所有排版样式的行高都应该大于字体大小
        assertTrue("标题 L 行高应该大于字体大小", 
            typography.headlineL.lineHeight > typography.headlineL.fontSize)
        assertTrue("正文 L 行高应该大于字体大小", 
            typography.bodyL.lineHeight > typography.bodyL.fontSize)
        assertTrue("标签 L 行高应该大于字体大小", 
            typography.labelL.lineHeight > typography.labelL.fontSize)
    }
    
    /**
     * 验证间距系统的完整性
     */
    @Test
    fun testSpacingSchemeCompleteness() {
        val theme = ThemeConfig.lightTheme()
        val spacing = theme.spacing
        
        // 验证所有间距值都已定义
        assertTrue("极小间距应该大于 0", spacing.xs > 0)
        assertTrue("小间距应该大于 0", spacing.sm > 0)
        assertTrue("中间距应该大于 0", spacing.md > 0)
        assertTrue("大间距应该大于 0", spacing.lg > 0)
        assertTrue("极大间距应该大于 0", spacing.xl > 0)
    }
    
    /**
     * 验证间距的递增关系
     */
    @Test
    fun testSpacingHierarchy() {
        val theme = ThemeConfig.lightTheme()
        val spacing = theme.spacing
        
        // 间距应该递增
        assertTrue("间距应该递增", spacing.xs < spacing.sm)
        assertTrue("间距应该递增", spacing.sm < spacing.md)
        assertTrue("间距应该递增", spacing.md < spacing.lg)
        assertTrue("间距应该递增", spacing.lg < spacing.xl)
    }
    
    /**
     * 验证间距的标准值
     */
    @Test
    fun testSpacingStandardValues() {
        val spacing = SpacingScheme.default()
        
        // 验证标准间距值
        assertEquals("极小间距应该是 4dp", 4, spacing.xs)
        assertEquals("小间距应该是 8dp", 8, spacing.sm)
        assertEquals("中间距应该是 16dp", 16, spacing.md)
        assertEquals("大间距应该是 24dp", 24, spacing.lg)
        assertEquals("极大间距应该是 32dp", 32, spacing.xl)
    }
    
    /**
     * 验证浅色和深色主题的排版一致性
     */
    @Test
    fun testTypographyConsistency() {
        val lightTheme = ThemeConfig.lightTheme()
        val darkTheme = ThemeConfig.darkTheme()
        
        // 浅色和深色主题的排版应该相同
        assertEquals("排版应该一致", 
            lightTheme.typography.headlineL.fontSize, 
            darkTheme.typography.headlineL.fontSize)
        assertEquals("排版应该一致", 
            lightTheme.typography.bodyL.fontSize, 
            darkTheme.typography.bodyL.fontSize)
        assertEquals("排版应该一致", 
            lightTheme.typography.labelL.fontSize, 
            darkTheme.typography.labelL.fontSize)
    }
    
    /**
     * 验证浅色和深色主题的间距一致性
     */
    @Test
    fun testSpacingConsistency() {
        val lightTheme = ThemeConfig.lightTheme()
        val darkTheme = ThemeConfig.darkTheme()
        
        // 浅色和深色主题的间距应该相同
        assertEquals("间距应该一致", 
            lightTheme.spacing.xs, 
            darkTheme.spacing.xs)
        assertEquals("间距应该一致", 
            lightTheme.spacing.md, 
            darkTheme.spacing.md)
        assertEquals("间距应该一致", 
            lightTheme.spacing.xl, 
            darkTheme.spacing.xl)
    }
}
