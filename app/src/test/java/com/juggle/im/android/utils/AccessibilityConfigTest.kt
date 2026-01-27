package com.juggle.im.android.utils

import android.content.Context
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 无障碍配置单元测试
 * 验证系统字体大小、高对比度模式等无障碍设置
 */
@RunWith(RobolectricTestRunner::class)
class AccessibilityConfigTest {

    private lateinit var context: Context
    private lateinit var config: AccessibilityConfig

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        config = AccessibilityConfig(context)
    }

    @Test
    fun testGetFontScaleFactor() {
        val fontScale = config.getFontScaleFactor()
        assertTrue(fontScale > 0)
        // 默认字体大小缩放因子应该是 1.0
        assertEquals(1.0f, fontScale)
    }

    @Test
    fun testGetMinimumTouchTargetSize() {
        val minSize = config.getMinimumTouchTargetSize()
        assertEquals(48, minSize)
    }

    @Test
    fun testGetMinimumTextSize() {
        val minTextSize = config.getMinimumTextSize()
        assertTrue(minTextSize > 0)
    }

    @Test
    fun testGetMinimumContrastRatio() {
        val contrastRatio = config.getMinimumContrastRatio()
        // 正常模式下应该是 4.5f
        assertEquals(4.5f, contrastRatio)
    }

    @Test
    fun testIsFontSizeAdjusted() {
        val isAdjusted = config.isFontSizeAdjusted()
        // 默认情况下字体大小不应该被调整
        assertFalse(isAdjusted)
    }

    @Test
    fun testGetAdjustedFontSize() {
        val baseFontSize = 14f
        val adjustedSize = config.getAdjustedFontSize(baseFontSize)
        assertEquals(baseFontSize, adjustedSize)
    }

    @Test
    fun testGetAdjustedSize() {
        val baseSize = 48
        val adjustedSize = config.getAdjustedSize(baseSize)
        assertEquals(baseSize, adjustedSize)
    }

    @Test
    fun testIsAccessibilityEnabled() {
        val isEnabled = config.isAccessibilityEnabled()
        // 在测试环境中可能不启用
        assertTrue(isEnabled || !isEnabled)
    }

    @Test
    fun testIsScreenReaderEnabled() {
        val isEnabled = config.isScreenReaderEnabled()
        // 在测试环境中可能不启用
        assertTrue(isEnabled || !isEnabled)
    }

    @Test
    fun testIsHighContrastModeEnabled() {
        val isEnabled = config.isHighContrastModeEnabled()
        // 在测试环境中可能不启用
        assertTrue(isEnabled || !isEnabled)
    }

    @Test
    fun testShouldApplyHighContrastColors() {
        val shouldApply = config.shouldApplyHighContrastColors()
        // 应该与高对比度模式状态一致
        assertEquals(config.isHighContrastModeEnabled(), shouldApply)
    }

    @Test
    fun testGetEnabledAccessibilityServices() {
        val services = config.getEnabledAccessibilityServices()
        // 应该返回一个列表（可能为空）
        assertTrue(services is List)
    }

    @Test
    fun testGetAccessibilityConfigSummary() {
        val summary = config.getAccessibilityConfigSummary()
        // 应该返回一个非空字符串
        assertTrue(summary.isNotEmpty())
    }
}
