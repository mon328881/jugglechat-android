package com.juggle.im.android.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.util.DisplayMetrics
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 响应式布局集成测试
 * 验证响应式布局在不同场景下的完整功能
 */
class ResponsiveLayoutIntegrationTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockResources: Resources

    @Mock
    private lateinit var mockConfiguration: Configuration

    private lateinit var displayMetrics: DisplayMetrics

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        displayMetrics = DisplayMetrics()
        displayMetrics.density = 2.0f
        displayMetrics.xdpi = 160f
        displayMetrics.ydpi = 160f

        `when`(mockContext.resources).thenReturn(mockResources)
        `when`(mockResources.displayMetrics).thenReturn(displayMetrics)
        `when`(mockResources.configuration).thenReturn(mockConfiguration)
    }

    @Test
    fun testSmallPhonePortraitLayout() {
        // 小屏幕手机竖屏场景
        displayMetrics.widthPixels = 480
        displayMetrics.heightPixels = 800
        mockConfiguration.orientation = Configuration.ORIENTATION_PORTRAIT

        val screenSize = ResponsiveLayoutUtils.getScreenSize(mockContext)
        val deviceType = ResponsiveLayoutUtils.getDeviceType(mockContext)
        val orientation = ResponsiveLayoutUtils.getScreenOrientation(mockContext)
        val columnCount = ResponsiveLayoutUtils.getRecommendedColumnCount(mockContext)

        assertEquals(ResponsiveLayoutUtils.ScreenSize.SMALL, screenSize)
        assertEquals(ResponsiveLayoutUtils.DeviceType.PHONE, deviceType)
        assertEquals(ResponsiveLayoutUtils.ScreenOrientation.PORTRAIT, orientation)
        assertEquals(1, columnCount)
        assertFalse(ResponsiveLayoutUtils.shouldShowSideNavigation(mockContext))
        assertFalse(ResponsiveLayoutUtils.shouldHideBottomNavigation(mockContext))
    }

    @Test
    fun testMediumPhoneLandscapeLayout() {
        // 中等屏幕手机横屏场景
        displayMetrics.widthPixels = 1920
        displayMetrics.heightPixels = 1080
        mockConfiguration.orientation = Configuration.ORIENTATION_LANDSCAPE

        val screenSize = ResponsiveLayoutUtils.getScreenSize(mockContext)
        val deviceType = ResponsiveLayoutUtils.getDeviceType(mockContext)
        val orientation = ResponsiveLayoutUtils.getScreenOrientation(mockContext)
        val columnCount = ResponsiveLayoutUtils.getRecommendedColumnCount(mockContext)

        assertEquals(ResponsiveLayoutUtils.ScreenSize.MEDIUM, screenSize)
        assertEquals(ResponsiveLayoutUtils.DeviceType.PHONE, deviceType)
        assertEquals(ResponsiveLayoutUtils.ScreenOrientation.LANDSCAPE, orientation)
        assertEquals(1, columnCount)
        assertFalse(ResponsiveLayoutUtils.shouldShowSideNavigation(mockContext))
        assertTrue(ResponsiveLayoutUtils.shouldHideBottomNavigation(mockContext))
    }

    @Test
    fun testTabletPortraitLayout() {
        // 平板竖屏场景
        displayMetrics.widthPixels = 1440
        displayMetrics.heightPixels = 2560
        mockConfiguration.orientation = Configuration.ORIENTATION_PORTRAIT

        val screenSize = ResponsiveLayoutUtils.getScreenSize(mockContext)
        val deviceType = ResponsiveLayoutUtils.getDeviceType(mockContext)
        val orientation = ResponsiveLayoutUtils.getScreenOrientation(mockContext)
        val columnCount = ResponsiveLayoutUtils.getRecommendedColumnCount(mockContext)

        assertEquals(ResponsiveLayoutUtils.ScreenSize.LARGE, screenSize)
        assertEquals(ResponsiveLayoutUtils.DeviceType.TABLET, deviceType)
        assertEquals(ResponsiveLayoutUtils.ScreenOrientation.PORTRAIT, orientation)
        assertEquals(2, columnCount)
        assertFalse(ResponsiveLayoutUtils.shouldShowSideNavigation(mockContext))
        assertFalse(ResponsiveLayoutUtils.shouldHideBottomNavigation(mockContext))
    }

    @Test
    fun testTabletLandscapeLayout() {
        // 平板横屏场景
        displayMetrics.widthPixels = 2560
        displayMetrics.heightPixels = 1440
        mockConfiguration.orientation = Configuration.ORIENTATION_LANDSCAPE

        val screenSize = ResponsiveLayoutUtils.getScreenSize(mockContext)
        val deviceType = ResponsiveLayoutUtils.getDeviceType(mockContext)
        val orientation = ResponsiveLayoutUtils.getScreenOrientation(mockContext)
        val columnCount = ResponsiveLayoutUtils.getRecommendedColumnCount(mockContext)

        assertEquals(ResponsiveLayoutUtils.ScreenSize.LARGE, screenSize)
        assertEquals(ResponsiveLayoutUtils.DeviceType.TABLET, deviceType)
        assertEquals(ResponsiveLayoutUtils.ScreenOrientation.LANDSCAPE, orientation)
        assertEquals(3, columnCount)
        assertTrue(ResponsiveLayoutUtils.shouldShowSideNavigation(mockContext))
        assertTrue(ResponsiveLayoutUtils.shouldHideBottomNavigation(mockContext))
    }

    @Test
    fun testLayoutAdaptationForSmallScreen() {
        // 小屏幕布局适配
        displayMetrics.widthPixels = 480
        displayMetrics.heightPixels = 800

        val padding = ResponsiveLayoutUtils.getRecommendedPadding(mockContext)
        val fontSize = ResponsiveLayoutUtils.getRecommendedFontSize(mockContext)
        val listItemHeight = ResponsiveLayoutUtils.getRecommendedListItemHeight(mockContext)
        val buttonHeight = ResponsiveLayoutUtils.getRecommendedButtonHeight(mockContext)

        val expectedPadding = ResponsiveLayoutUtils.dpToPx(mockContext, 8)
        val expectedListItemHeight = ResponsiveLayoutUtils.dpToPx(mockContext, 56)
        val expectedButtonHeight = ResponsiveLayoutUtils.dpToPx(mockContext, 40)

        assertEquals(expectedPadding, padding)
        assertEquals(12f, fontSize)
        assertEquals(expectedListItemHeight, listItemHeight)
        assertEquals(expectedButtonHeight, buttonHeight)
    }

    @Test
    fun testLayoutAdaptationForLargeScreen() {
        // 大屏幕布局适配
        displayMetrics.widthPixels = 1440
        displayMetrics.heightPixels = 2560

        val padding = ResponsiveLayoutUtils.getRecommendedPadding(mockContext)
        val fontSize = ResponsiveLayoutUtils.getRecommendedFontSize(mockContext)
        val listItemHeight = ResponsiveLayoutUtils.getRecommendedListItemHeight(mockContext)
        val buttonHeight = ResponsiveLayoutUtils.getRecommendedButtonHeight(mockContext)

        val expectedPadding = ResponsiveLayoutUtils.dpToPx(mockContext, 16)
        val expectedListItemHeight = ResponsiveLayoutUtils.dpToPx(mockContext, 72)
        val expectedButtonHeight = ResponsiveLayoutUtils.dpToPx(mockContext, 48)

        assertEquals(expectedPadding, padding)
        assertEquals(16f, fontSize)
        assertEquals(expectedListItemHeight, listItemHeight)
        assertEquals(expectedButtonHeight, buttonHeight)
    }

    @Test
    fun testMaxContentWidthAdaptation() {
        // 最大内容宽度适配
        displayMetrics.widthPixels = 1440
        displayMetrics.heightPixels = 2560

        val maxWidth = ResponsiveLayoutUtils.getMaxContentWidth(mockContext)
        val screenWidth = ResponsiveLayoutUtils.getScreenWidth(mockContext)

        // 平板设备应该限制内容宽度为屏幕宽度的 80%
        val expectedMaxWidth = (screenWidth * 0.8).toInt()
        assertEquals(expectedMaxWidth, maxWidth)
    }

    @Test
    fun testTouchTargetSizeCompliance() {
        // 触摸目标最小尺寸合规性
        val minTouchSize = ResponsiveLayoutUtils.getMinTouchTargetSize(mockContext)
        val expectedMinSize = ResponsiveLayoutUtils.dpToPx(mockContext, 48)

        assertEquals(expectedMinSize, minTouchSize)
    }

    @Test
    fun testConversionConsistency() {
        // dp 和像素转换一致性
        val originalDp = 16
        val px = ResponsiveLayoutUtils.dpToPx(mockContext, originalDp)
        val convertedDp = ResponsiveLayoutUtils.pxToDp(mockContext, px)

        // 由于舍入，允许 1dp 的误差
        assertTrue(Math.abs(convertedDp - originalDp) <= 1)
    }

    @Test
    fun testScreenDimensionsConsistency() {
        // 屏幕尺寸一致性
        displayMetrics.widthPixels = 1080
        displayMetrics.heightPixels = 1920

        val width1 = ResponsiveLayoutUtils.getScreenWidth(mockContext)
        val width2 = ResponsiveLayoutUtils.getScreenWidth(mockContext)
        val height1 = ResponsiveLayoutUtils.getScreenHeight(mockContext)
        val height2 = ResponsiveLayoutUtils.getScreenHeight(mockContext)

        assertEquals(width1, width2)
        assertEquals(height1, height2)
        assertEquals(1080, width1)
        assertEquals(1920, height1)
    }

    @Test
    fun testNavigationBarHeightRetrieval() {
        // 导航栏高度获取
        val navBarHeight = ResponsiveLayoutUtils.getNavigationBarHeight(mockContext)
        assertTrue(navBarHeight >= 0)
    }

    @Test
    fun testStatusBarHeightRetrieval() {
        // 状态栏高度获取
        val statusBarHeight = ResponsiveLayoutUtils.getStatusBarHeight(mockContext)
        assertTrue(statusBarHeight >= 0)
    }

    @Test
    fun testScreenDensityRange() {
        // 屏幕密度范围验证
        val density = ResponsiveLayoutUtils.getScreenDensity(mockContext)
        assertTrue(density > 0.5f)
        assertTrue(density <= 4f)
    }

    @Test
    fun testOrientationChangeHandling() {
        // 屏幕方向改变处理
        // 初始竖屏
        mockConfiguration.orientation = Configuration.ORIENTATION_PORTRAIT
        var orientation = ResponsiveLayoutUtils.getScreenOrientation(mockContext)
        assertEquals(ResponsiveLayoutUtils.ScreenOrientation.PORTRAIT, orientation)

        // 改变为横屏
        mockConfiguration.orientation = Configuration.ORIENTATION_LANDSCAPE
        orientation = ResponsiveLayoutUtils.getScreenOrientation(mockContext)
        assertEquals(ResponsiveLayoutUtils.ScreenOrientation.LANDSCAPE, orientation)

        // 改变为竖屏
        mockConfiguration.orientation = Configuration.ORIENTATION_PORTRAIT
        orientation = ResponsiveLayoutUtils.getScreenOrientation(mockContext)
        assertEquals(ResponsiveLayoutUtils.ScreenOrientation.PORTRAIT, orientation)
    }

    @Test
    fun testMultipleScreenSizeTransitions() {
        // 多个屏幕尺寸转换
        // 小屏幕
        displayMetrics.widthPixels = 480
        displayMetrics.heightPixels = 800
        var screenSize = ResponsiveLayoutUtils.getScreenSize(mockContext)
        assertEquals(ResponsiveLayoutUtils.ScreenSize.SMALL, screenSize)

        // 中等屏幕
        displayMetrics.widthPixels = 1080
        displayMetrics.heightPixels = 1920
        screenSize = ResponsiveLayoutUtils.getScreenSize(mockContext)
        assertEquals(ResponsiveLayoutUtils.ScreenSize.MEDIUM, screenSize)

        // 大屏幕
        displayMetrics.widthPixels = 1440
        displayMetrics.heightPixels = 2560
        screenSize = ResponsiveLayoutUtils.getScreenSize(mockContext)
        assertEquals(ResponsiveLayoutUtils.ScreenSize.LARGE, screenSize)
    }
}
