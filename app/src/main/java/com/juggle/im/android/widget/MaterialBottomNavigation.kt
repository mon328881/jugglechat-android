package com.juggle.im.android.widget

import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.juggle.im.android.theme.ThemeManager
import com.juggle.im.android.utils.AccessibilityUtils

/**
 * Material Design 3 底部导航栏组件
 * 支持 5 个导航项、未读徽章显示和深色模式适配
 */
class MaterialBottomNavigation @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    
    /**
     * 导航项数据类
     */
    data class NavItem(
        val id: Int,
        val icon: Int,
        val label: String,
        var unreadCount: Int = 0
    )
    
    private val themeManager: ThemeManager = ThemeManager.getInstance(context)
    private val navItems = mutableListOf<NavItem>()
    private val navButtons = mutableListOf<FrameLayout>()
    private var selectedIndex: Int = 0
    private var onNavItemClickListener: ((Int) -> Unit)? = null
    
    init {
        orientation = HORIZONTAL
        applyDefaultStyle()
    }
    
    /**
     * 应用默认样式
     */
    private fun applyDefaultStyle() {
        val theme = themeManager.getCurrentTheme()
        
        // 设置背景颜色
        setBackgroundColor(theme.colors.surface)
        
        // 设置高度
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            56  // 标准底部导航栏高度
        )
    }
    
    /**
     * 添加导航项
     */
    fun addNavItem(item: NavItem) {
        navItems.add(item)
        createNavButton(item, navItems.size - 1)
    }
    
    /**
     * 创建导航按钮
     */
    private fun createNavButton(item: NavItem, index: Int) {
        val theme = themeManager.getCurrentTheme()
        
        // 创建按钮容器
        val buttonContainer = FrameLayout(context).apply {
            layoutParams = LayoutParams(
                0,
                LayoutParams.MATCH_PARENT,
                1f
            )
        }
        
        // 创建按钮内容（图标和标签）
        val buttonContent = LinearLayout(context).apply {
            orientation = VERTICAL
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        }
        
        // 创建图标
        val icon = AppCompatImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(24, 24)
            setImageResource(item.icon)
            setColorFilter(
                if (index == selectedIndex) theme.colors.primary else theme.colors.hint
            )
        }
        buttonContent.addView(icon)
        
        // 创建标签
        val label = TextView(context).apply {
            text = item.label
            textSize = 10f
            setTextColor(
                if (index == selectedIndex) theme.colors.primary else theme.colors.hint
            )
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 4
            }
        }
        buttonContent.addView(label)
        
        buttonContainer.addView(buttonContent)
        
        // 创建未读徽章
        if (item.unreadCount > 0) {
            createBadge(buttonContainer, item.unreadCount, theme)
        }
        
        // 设置点击监听
        buttonContainer.setOnClickListener {
            selectNavItem(index)
            onNavItemClickListener?.invoke(index)
        }
        
        navButtons.add(buttonContainer)
        addView(buttonContainer)
    }
    
    /**
     * 创建未读徽章
     */
    private fun createBadge(container: FrameLayout, count: Int, theme: com.juggle.im.android.theme.ThemeConfig) {
        val badge = TextView(context).apply {
            text = if (count > 99) "99+" else count.toString()
            textSize = 10f
            setTextColor(theme.colors.onError)
            setBackgroundColor(theme.colors.error)
            gravity = Gravity.CENTER
            
            layoutParams = FrameLayout.LayoutParams(
                24,
                24,
                Gravity.TOP or Gravity.END
            ).apply {
                rightMargin = 4
                topMargin = 4
            }
            
            // 设置圆形背景
            val shape = ShapeDrawable(RoundRectShape(
                floatArrayOf(12f, 12f, 12f, 12f, 12f, 12f, 12f, 12f),
                null,
                null
            )).apply {
                paint.color = theme.colors.error
            }
            background = shape
        }
        
        container.addView(badge)
    }
    
    /**
     * 选择导航项
     */
    fun selectNavItem(index: Int) {
        if (index < 0 || index >= navButtons.size) return
        
        selectedIndex = index
        updateNavButtonStyles()
    }
    
    /**
     * 更新导航按钮样式
     */
    private fun updateNavButtonStyles() {
        val theme = themeManager.getCurrentTheme()
        
        navButtons.forEachIndexed { index, button ->
            val isSelected = index == selectedIndex
            val color = if (isSelected) theme.colors.primary else theme.colors.hint
            
            // 更新图标颜色
            val icon = (button.getChildAt(0) as? LinearLayout)?.getChildAt(0) as? AppCompatImageView
            icon?.setColorFilter(color)
            
            // 更新标签颜色
            val label = (button.getChildAt(0) as? LinearLayout)?.getChildAt(1) as? TextView
            label?.setTextColor(color)
        }
    }
    
    /**
     * 更新未读消息数
     */
    fun updateUnreadCount(index: Int, count: Int) {
        if (index < 0 || index >= navItems.size) return
        
        navItems[index].unreadCount = count
        
        // 重新创建导航按钮以更新徽章
        removeAllViews()
        navButtons.clear()
        navItems.forEachIndexed { i, item ->
            createNavButton(item, i)
        }
        updateNavButtonStyles()
    }
    
    /**
     * 设置导航项点击监听
     */
    fun setOnNavItemClickListener(listener: (Int) -> Unit) {
        onNavItemClickListener = listener
    }
    
    /**
     * 获取选中的导航项索引
     */
    fun getSelectedIndex(): Int = selectedIndex
    
    /**
     * 获取导航项数量（用于测试）
     */
    fun getItemCount(): Int = navItems.size
    
    /**
     * 获取导航项标签列表（用于测试）
     */
    fun getItemLabels(): List<String> = navItems.map { it.label }

    /**
     * 为导航项设置无障碍标签
     * 验证: 需求 12.1
     */
    fun setNavItemAccessibilityLabel(index: Int, label: String) {
        if (index < 0 || index >= navButtons.size) return
        AccessibilityUtils.setViewAccessibilityLabel(navButtons[index], label)
    }

    /**
     * 为所有导航项设置无障碍标签
     * 验证: 需求 12.1
     */
    fun setAllNavItemsAccessibilityLabels() {
        navItems.forEachIndexed { index, item ->
            val label = "${item.label}，第 ${index + 1} 项，共 ${navItems.size} 项"
            if (item.unreadCount > 0) {
                setNavItemAccessibilityLabel(index, "$label，有 ${item.unreadCount} 条未读消息")
            } else {
                setNavItemAccessibilityLabel(index, label)
            }
        }
    }
}
