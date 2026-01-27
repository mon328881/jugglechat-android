package com.juggle.im.android.utils

import android.content.Context
import android.content.res.Configuration
import android.util.DisplayMetrics
import android.view.WindowManager
import kotlin.math.sqrt

/**
 * 响应式布局工具类
 * 提供屏幕尺寸检测、设备类型判断和布局适配功能
 */
object ResponsiveLayoutUtils {

    /**
     * 屏幕尺寸类型枚举
     */
    enum class ScreenSize {
        SMALL,      // 小屏幕（4 英寸）
        MEDIUM,     // 中等屏幕（5-6 英寸）
        LARGE       // 大屏幕（7 英寸以上）
    }

    /**
     * 设备类型枚举
     */
    enum class DeviceType {
        PHONE,      // 手机
        TABLET      // 平板
    }

    /**
     * 屏幕方向枚举
     */
    enum class ScreenOrientation {
        PORTRAIT,   // 竖屏
        LANDSCAPE   // 横屏
    }

    /**
     * 获取屏幕尺寸类型
     * @param context 上下文
     * @return 屏幕尺寸类型
     */
    fun getScreenSize(context: Context): ScreenSize {
        val displayMetrics = context.resources.displayMetrics
        val screenDiagonalInches = calculateScreenDiagonalInches(context)

        return when {
            screenDiagonalInches < 4.5 -> ScreenSize.SMALL
            screenDiagonalInches < 6.5 -> ScreenSize.MEDIUM
            else -> ScreenSize.LARGE
        }
    }

    /**
     * 获取设备类型
     * @param context 上下文
     * @return 设备类型
     */
    fun getDeviceType(context: Context): DeviceType {
        val screenDiagonalInches = calculateScreenDiagonalInches(context)
        return if (screenDiagonalInches >= 6.5) DeviceType.TABLET else DeviceType.PHONE
    }

    /**
     * 获取屏幕方向
     * @param context 上下文
     * @return 屏幕方向
     */
    fun getScreenOrientation(context: Context): ScreenOrientation {
        return when (context.resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> ScreenOrientation.LANDSCAPE
            else -> ScreenOrientation.PORTRAIT
        }
    }

    /**
     * 计算屏幕对角线长度（英寸）
     * @param context 上下文
     * @return 屏幕对角线长度（英寸）
     */
    private fun calculateScreenDiagonalInches(context: Context): Double {
        val displayMetrics = context.resources.displayMetrics
        val widthInches = displayMetrics.widthPixels / displayMetrics.xdpi
        val heightInches = displayMetrics.heightPixels / displayMetrics.ydpi
        return sqrt((widthInches * widthInches + heightInches * heightInches).toDouble())
    }

    /**
     * 获取屏幕宽度（像素）
     * @param context 上下文
     * @return 屏幕宽度（像素）
     */
    fun getScreenWidth(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }

    /**
     * 获取屏幕高度（像素）
     * @param context 上下文
     * @return 屏幕高度（像素）
     */
    fun getScreenHeight(context: Context): Int {
        return context.resources.displayMetrics.heightPixels
    }

    /**
     * 获取屏幕密度
     * @param context 上下文
     * @return 屏幕密度
     */
    fun getScreenDensity(context: Context): Float {
        return context.resources.displayMetrics.density
    }

    /**
     * 判断是否为小屏幕
     * @param context 上下文
     * @return 是否为小屏幕
     */
    fun isSmallScreen(context: Context): Boolean {
        return getScreenSize(context) == ScreenSize.SMALL
    }

    /**
     * 判断是否为中等屏幕
     * @param context 上下文
     * @return 是否为中等屏幕
     */
    fun isMediumScreen(context: Context): Boolean {
        return getScreenSize(context) == ScreenSize.MEDIUM
    }

    /**
     * 判断是否为大屏幕
     * @param context 上下文
     * @return 是否为大屏幕
     */
    fun isLargeScreen(context: Context): Boolean {
        return getScreenSize(context) == ScreenSize.LARGE
    }

    /**
     * 判断是否为平板设备
     * @param context 上下文
     * @return 是否为平板设备
     */
    fun isTablet(context: Context): Boolean {
        return getDeviceType(context) == DeviceType.TABLET
    }

    /**
     * 判断是否为手机设备
     * @param context 上下文
     * @return 是否为手机设备
     */
    fun isPhone(context: Context): Boolean {
        return getDeviceType(context) == DeviceType.PHONE
    }

    /**
     * 判断是否为竖屏
     * @param context 上下文
     * @return 是否为竖屏
     */
    fun isPortrait(context: Context): Boolean {
        return getScreenOrientation(context) == ScreenOrientation.PORTRAIT
    }

    /**
     * 判断是否为横屏
     * @param context 上下文
     * @return 是否为横屏
     */
    fun isLandscape(context: Context): Boolean {
        return getScreenOrientation(context) == ScreenOrientation.LANDSCAPE
    }

    /**
     * 根据屏幕尺寸获取推荐的列数
     * 用于多列布局（如 RecyclerView GridLayoutManager）
     * @param context 上下文
     * @return 推荐的列数
     */
    fun getRecommendedColumnCount(context: Context): Int {
        return when {
            isTablet(context) && isLandscape(context) -> 3
            isTablet(context) && isPortrait(context) -> 2
            isLargeScreen(context) && isLandscape(context) -> 2
            else -> 1
        }
    }

    /**
     * 根据屏幕尺寸获取推荐的内边距
     * @param context 上下文
     * @return 推荐的内边距（像素）
     */
    fun getRecommendedPadding(context: Context): Int {
        return when (getScreenSize(context)) {
            ScreenSize.SMALL -> dpToPx(context, 8)
            ScreenSize.MEDIUM -> dpToPx(context, 12)
            ScreenSize.LARGE -> dpToPx(context, 16)
        }
    }

    /**
     * 根据屏幕尺寸获取推荐的字体大小
     * @param context 上下文
     * @return 推荐的字体大小（sp）
     */
    fun getRecommendedFontSize(context: Context): Float {
        return when (getScreenSize(context)) {
            ScreenSize.SMALL -> 12f
            ScreenSize.MEDIUM -> 14f
            ScreenSize.LARGE -> 16f
        }
    }

    /**
     * 将 dp 转换为像素
     * @param context 上下文
     * @param dp dp 值
     * @return 像素值
     */
    fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    /**
     * 将像素转换为 dp
     * @param context 上下文
     * @param px 像素值
     * @return dp 值
     */
    fun pxToDp(context: Context, px: Int): Int {
        return (px / context.resources.displayMetrics.density).toInt()
    }

    /**
     * 获取状态栏高度
     * @param context 上下文
     * @return 状态栏高度（像素）
     */
    fun getStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) context.resources.getDimensionPixelSize(resourceId) else 0
    }

    /**
     * 获取导航栏高度
     * @param context 上下文
     * @return 导航栏高度（像素）
     */
    fun getNavigationBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) context.resources.getDimensionPixelSize(resourceId) else 0
    }

    /**
     * 判断是否需要显示侧边导航
     * 在平板横屏模式下显示侧边导航
     * @param context 上下文
     * @return 是否需要显示侧边导航
     */
    fun shouldShowSideNavigation(context: Context): Boolean {
        return isTablet(context) && isLandscape(context)
    }

    /**
     * 判断是否需要隐藏底部导航
     * 在横屏模式下隐藏底部导航
     * @param context 上下文
     * @return 是否需要隐藏底部导航
     */
    fun shouldHideBottomNavigation(context: Context): Boolean {
        return isLandscape(context)
    }

    /**
     * 获取推荐的最大内容宽度
     * 用于限制大屏幕上的内容宽度
     * @param context 上下文
     * @return 推荐的最大内容宽度（像素）
     */
    fun getMaxContentWidth(context: Context): Int {
        val screenWidth = getScreenWidth(context)
        return when {
            isTablet(context) -> (screenWidth * 0.8).toInt()
            isLargeScreen(context) -> (screenWidth * 0.9).toInt()
            else -> screenWidth
        }
    }

    /**
     * 获取推荐的列表项高度
     * @param context 上下文
     * @return 推荐的列表项高度（像素）
     */
    fun getRecommendedListItemHeight(context: Context): Int {
        return when (getScreenSize(context)) {
            ScreenSize.SMALL -> dpToPx(context, 56)
            ScreenSize.MEDIUM -> dpToPx(context, 64)
            ScreenSize.LARGE -> dpToPx(context, 72)
        }
    }

    /**
     * 获取推荐的按钮高度
     * @param context 上下文
     * @return 推荐的按钮高度（像素）
     */
    fun getRecommendedButtonHeight(context: Context): Int {
        return when (getScreenSize(context)) {
            ScreenSize.SMALL -> dpToPx(context, 40)
            ScreenSize.MEDIUM -> dpToPx(context, 44)
            ScreenSize.LARGE -> dpToPx(context, 48)
        }
    }

    /**
     * 获取推荐的触摸目标最小尺寸（48dp）
     * @param context 上下文
     * @return 推荐的触摸目标最小尺寸（像素）
     */
    fun getMinTouchTargetSize(context: Context): Int {
        return dpToPx(context, 48)
    }
}
