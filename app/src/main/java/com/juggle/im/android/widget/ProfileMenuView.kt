package com.juggle.im.android.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.juggle.im.android.theme.ThemeManager

/**
 * 个人资料菜单视图
 * 显示设置、关于、帮助等功能入口
 */
class ProfileMenuView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    
    /**
     * 菜单项数据类
     */
    data class MenuItem(
        val id: String,
        val title: String,
        val icon: String? = null
    )
    
    private val themeManager: ThemeManager = ThemeManager.getInstance(context)
    private val theme = themeManager.getCurrentTheme()
    
    private val menuItems = mutableListOf<MenuItem>()
    private val clickListeners = mutableMapOf<String, () -> Unit>()
    
    init {
        orientation = VERTICAL
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        setPadding(theme.spacing.lg, theme.spacing.lg, theme.spacing.lg, theme.spacing.lg)
    }
    
    /**
     * 添加菜单项
     */
    fun addMenuItem(item: MenuItem, onClickListener: () -> Unit) {
        menuItems.add(item)
        clickListeners[item.id] = onClickListener
        
        val menuItemView = createMenuItemView(item)
        addView(menuItemView)
    }
    
    /**
     * 创建菜单项视图
     */
    private fun createMenuItemView(item: MenuItem): LinearLayout {
        return LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = theme.spacing.md
            }
            
            // 设置点击监听
            setOnClickListener {
                clickListeners[item.id]?.invoke()
            }
            
            // 创建标题
            val titleView = TextView(context).apply {
                text = item.title
                textSize = 16f
                setTextColor(theme.colors.onBackground)
                layoutParams = LayoutParams(
                    0,
                    LayoutParams.WRAP_CONTENT,
                    1f
                )
            }
            addView(titleView)
            
            // 创建箭头
            val arrowView = TextView(context).apply {
                text = ">"
                textSize = 16f
                setTextColor(theme.colors.hint)
                layoutParams = LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
                )
            }
            addView(arrowView)
        }
    }
    
    /**
     * 获取菜单项数量（用于测试）
     */
    fun getMenuItemCount(): Int = menuItems.size
    
    /**
     * 获取菜单项（用于测试）
     */
    fun getMenuItem(index: Int): MenuItem? = if (index < menuItems.size) menuItems[index] else null
}
