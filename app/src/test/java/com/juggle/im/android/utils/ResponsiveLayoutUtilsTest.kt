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
 * 响应式布局工具类单元测试
 */
class ResponsiveLayoutUtilsTest {

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
    fun testGetScreenSizeSmall() {
        // 小屏幕：4 英寸
        displayMetrics.widthPixels = 480
        displayMetrics.heightPixels = 800

        val screenSize = ResponsiveLayoutUtils.getScreenSize(mockContext)
        assertEquals(ResponsiveLayoutUtils.ScreenSize.SMALL, screenSize)
    }

    @Test
    fun testGetScreenSizeMedium() {
        // 中等屏幕：5-6 英寸
        displayMetrics.widthPixels = 1080
        displayMetrics.heightPixels = 1920

        val screenSize = ResponsiveLayoutUtils.getScreenSize(mockContext)
        assertEquals(ResponsiveLayoutUtils.ScreenSize.MEDIUM, screenSize)
    }

    @Test
    fun testGetScreenSizeLarge() {
        // 大屏幕：7 英寸以上
        displayMetrics.widthPixels = 1440
        displayMetrics.heightPixels = 2560

        val screenSize = ResponsiveLayoutUtils.getScreenSize(mockContext)
        assertEquals(ResponsiveLayoutUtils.ScreenSize.LARGE, screenSize)
    }

    @Test
    fun testGetDeviceTypePhone() {
        // 手机设备
        displayMetrics.widthPixels = 1080
        displayMetrics.heightPixels = 1920

        val deviceType = ResponsiveLayoutUtils.getDeviceType(mockContext)
        assertEquals(ResponsiveLayoutUtils.DeviceType.PHONE, deviceType)
    }

    @Test
    fun testGetDeviceTypeTablet() {
        // 平板设备
        displayMetrics.widthPixels = 1440
        displayMetrics.heightPixels = 2560

        val deviceType = ResponsiveLayoutUtils.getDeviceType(mockContext)
        assertEquals(ResponsiveLayoutUtils.DeviceType.TABLET, deviceType)
    }

    @Test
    fun testGetScreenOrientationPortrait() {
        mockConfiguration.orientation = Configuration.ORIENTATION_PORTRAIT

        val orientation = ResponsiveLayoutUtils.getScreenOrientation(mockContext)
        assertEquals(ResponsiveLayoutUtils.ScreenOrientation.PORTRAIT, orientation)
    }

    @Test
    fun testGetScreenOrientationLandscape() {
        mockConfiguration.orientation = Configuration.ORIENTATION_LANDSCAPE

        val orientation = ResponsiveLayoutUtils.getScreenOrientation(mockContext)
        assertEquals(ResponsiveLayoutUtils.ScreenOrientation.LANDSCAPE, orientation)
    }

    @Test
    fun testIsSmallScreen() {
        displayMetrics.widthPixels = 480
        displayMetrics.heightPixels = 800

        assertTrue(ResponsiveLayoutUtils.isSmallScreen(mockContext))
    }

    @Test
    fun testIsMediumScreen() {
        displayMetrics.widthPixels = 1080
        displayMetrics.heightPixels = 1920

        assertTrue(ResponsiveLayoutUtils.isMediumScreen(mockContext))
    }

    @Test
    fun testIsLargeScreen() {
        displayMetrics.widthPixels = 1440
        displayMetrics.heightPixels = 2560

        assertTrue(ResponsiveLayoutUtils.isLargeScreen(mockContext))
    }

    @Test
    fun testIsTablet() {
        displayMetrics.widthPixels = 1440
        displayMetrics.heightPixels = 2560

        assertTrue(ResponsiveLayoutUtils.isTablet(mockContext))
    }

    @Test
    fun testIsPhone() {
        displayMetrics.widthPixels = 1080
        displayMetrics.heightPixels = 1920

        assertTrue(ResponsiveLayoutUtils.isPhone(mockContext))
    }

    @Test
    fun testIsPortrait() {
        mockConfiguration.orientation = Configuration.ORIENTATION_PORTRAIT

        assertTrue(ResponsiveLayoutUtils.isPortrait(mockContext))
    }

    @Test
    fun testIsLandscape() {
        mockConfiguration.orientation = Configuration.ORIENTATION_LANDSCAPE

        assertTrue(ResponsiveLayoutUtils.isLandscape(mockContext))
    }

    @Test
    fun testGetRecommendedColumnCountPhonePortrait() {
        displayMetrics.widthPixels = 1080
        displayMetrics.heightPixels = 1920
        mockConfiguration.orientation = Configuration.ORIENTATION_PORTRAIT

        val columnCount = ResponsiveLayoutUtils.getRecommendedColumnCount(mockContext)
        assertEquals(1, columnCount)
    }

    @Test
    fun testGetRecommendedColumnCountTabletPortrait() {
        displayMetrics.widthPixels = 1440
        displayMetrics.heightPixels = 2560
        mockConfiguration.orientation = Configuration.ORIENTATION_PORTRAIT

        val columnCount = ResponsiveLayoutUtils.getRecommendedColumnCount(mockContext)
        assertEquals(2, columnCount)
    }

    @Test
    fun testGetRecommendedColumnCountTabletLandscape() {
        displayMetrics.widthPixels = 2560
        displayMetrics.heightPixels = 1440
        mockConfiguration.orientation = Configuration.ORIENTATION_LANDSCAPE

        val columnCount = ResponsiveLayoutUtils.getRecommendedColumnCount(mockContext)
        assertEquals(3, columnCount)
    }

    @Test
    fun testGetRecommendedPaddingSmallScreen() {
        displayMetrics.widthPixels = 480
        displayMetrics.heightPixels = 800

        val padding = ResponsiveLayoutUtils.getRecommendedPadding(mockContext)
        val expectedPadding = ResponsiveLayoutUtils.dpToPx(mockContext, 8)
        assertEquals(expectedPadding, padding)
    }

    @Test
    fun testGetRecommendedPaddingMediumScreen() {
        displayMetrics.widthPixels = 1080
        displayMetrics.heightPixels = 1920

        val padding = ResponsiveLayoutUtils.getRecommendedPadding(mockContext)
        val expectedPadding = ResponsiveLayoutUtils.dpToPx(mockContext, 12)
        assertEquals(expectedPadding, padding)
    }

    @Test
    fun testGetRecommendedPaddingLargeScreen() {
        displayMetrics.widthPixels = 1440
        displayMetrics.heightPixels = 2560

        val padding = ResponsiveLayoutUtils.getRecommendedPadding(mockContext)
        val expectedPadding = ResponsiveLayoutUtils.dpToPx(mockContext, 16)
        assertEquals(expectedPadding, padding)
    }

    @Test
    fun testDpToPx() {
        val px = ResponsiveLayoutUtils.dpToPx(mockContext, 10)
        assertEquals(20, px) // 10 * 2.0 (density)
    }

    @Test
    fun testPxToDp() {
        val dp = ResponsiveLayoutUtils.pxToDp(mockContext, 20)
        assertEquals(10, dp) // 20 / 2.0 (density)
    }

    @Test
    fun testGetScreenWidth() {
        displayMetrics.widthPixels = 1080

        val width = ResponsiveLayoutUtils.getScreenWidth(mockContext)
        assertEquals(1080, width)
    }

    @Test
    fun testGetScreenHeight() {
        displayMetrics.heightPixels = 1920

        val height = ResponsiveLayoutUtils.getScreenHeight(mockContext)
        assertEquals(1920, height)
    }

    @Test
    fun testGetScreenDensity() {
        val density = ResponsiveLayoutUtils.getScreenDensity(mockContext)
        assertEquals(2.0f, density)
    }

    @Test
    fun testShouldShowSideNavigationTabletLandscape() {
        displayMetrics.widthPixels = 2560
        displayMetrics.heightPixels = 1440
        mockConfiguration.orientation = Configuration.ORIENTATION_LANDSCAPE

        assertTrue(ResponsiveLayoutUtils.shouldShowSideNavigation(mockContext))
    }

    @Test
    fun testShouldShowSideNavigationPhonePortrait() {
        displayMetrics.widthPixels = 1080
        displayMetrics.heightPixels = 1920
        mockConfiguration.orientation = Configuration.ORIENTATION_PORTRAIT

        assertFalse(ResponsiveLayoutUtils.shouldShowSideNavigation(mockContext))
    }

    @Test
    fun testShouldHideBottomNavigationLandscape() {
        mockConfiguration.orientation = Configuration.ORIENTATION_LANDSCAPE

        assertTrue(ResponsiveLayoutUtils.shouldHideBottomNavigation(mockContext))
    }

    @Test
    fun testShouldHideBottomNavigationPortrait() {
        mockConfiguration.orientation = Configuration.ORIENTATION_PORTRAIT

        assertFalse(ResponsiveLayoutUtils.shouldHideBottomNavigation(mockContext))
    }

    @Test
    fun testGetMaxContentWidthTablet() {
        displayMetrics.widthPixels = 1440
        displayMetrics.heightPixels = 2560

        val maxWidth = ResponsiveLayoutUtils.getMaxContentWidth(mockContext)
        assertEquals((1440 * 0.8).toInt(), maxWidth)
    }

    @Test
    fun testGetMaxContentWidthLargeScreen() {
        displayMetrics.widthPixels = 1440
        displayMetrics.heightPixels = 2560

        val maxWidth = ResponsiveLayoutUtils.getMaxContentWidth(mockContext)
        assertTrue(maxWidth <= 1440)
    }

    @Test
    fun testGetRecommendedListItemHeightSmallScreen() {
        displayMetrics.widthPixels = 480
        displayMetrics.heightPixels = 800

        val height = ResponsiveLayoutUtils.getRecommendedListItemHeight(mockContext)
        val expectedHeight = ResponsiveLayoutUtils.dpToPx(mockContext, 56)
        assertEquals(expectedHeight, height)
    }

    @Test
    fun testGetRecommendedButtonHeightMediumScreen() {
        displayMetrics.widthPixels = 1080
        displayMetrics.heightPixels = 1920

        val height = ResponsiveLayoutUtils.getRecommendedButtonHeight(mockContext)
        val expectedHeight = ResponsiveLayoutUtils.dpToPx(mockContext, 44)
        assertEquals(expectedHeight, height)
    }

    @Test
    fun testGetMinTouchTargetSize() {
        val minSize = ResponsiveLayoutUtils.getMinTouchTargetSize(mockContext)
        val expectedSize = ResponsiveLayoutUtils.dpToPx(mockContext, 48)
        assertEquals(expectedSize, minSize)
    }

    @Test
    fun testGetRecommendedFontSizeSmallScreen() {
        displayMetrics.widthPixels = 480
        displayMetrics.heightPixels = 800

        val fontSize = ResponsiveLayoutUtils.getRecommendedFontSize(mockContext)
        assertEquals(12f, fontSize)
    }

    @Test
    fun testGetRecommendedFontSizeMediumScreen() {
        displayMetrics.widthPixels = 1080
        displayMetrics.heightPixels = 1920

        val fontSize = ResponsiveLayoutUtils.getRecommendedFontSize(mockContext)
        assertEquals(14f, fontSize)
    }

    @Test
    fun testGetRecommendedFontSizeLargeScreen() {
        displayMetrics.widthPixels = 1440
        displayMetrics.heightPixels = 2560

        val fontSize = ResponsiveLayoutUtils.getRecommendedFontSize(mockContext)
        assertEquals(16f, fontSize)
    }
}
