package com.juggle.im.android.widget

import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.juggle.im.android.theme.ThemeManager

/**
 * 消息长按上下文菜单组件
 * 显示复制、删除、转发、反应等选项
 */
class MessageContextMenu @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    
    private val themeManager: ThemeManager = ThemeManager.getInstance(context)
    private val theme = themeManager.getCurrentTheme()
    
    private val menuItems = mutableListOf<MenuItem>()
    private var onMenuItemClickListener: ((MenuItem) -> Unit)? = null
    
    /**
     * 菜单项数据类
     */
    data class MenuItem(
        val id: String,
        val label: String,
        val icon: Int? = null
    )
    
    companion object {
        // 菜单项ID常量
        const val MENU_COPY = "copy"
        const val MENU_DELETE = "delete"
        const val MENU_FORWARD = "forward"
        const val MENU_REACTION = "reaction"
        const val MENU_COLLECT = "collect"
        const val MENU_UNCOLLECT = "uncollect"
        const val MENU_RECALL = "recall"
    }
    
    init {
        orientation = VERTICAL
        layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        
        // 设置背景
        val shape = ShapeDrawable(RoundRectShape(
            floatArrayOf(8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f),
            null,
            null
        )).apply {
            paint.color = theme.colors.surface
        }
        background = shape
        
        setPadding(0, theme.spacing.sm, 0, theme.spacing.sm)
    }
    
    /**
     * 添加菜单项
     */
    fun addMenuItem(item: MenuItem) {
        menuItems.add(item)
        
        val itemView = TextView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(theme.spacing.md, theme.spacing.sm, theme.spacing.md, theme.spacing.sm)
            }
            text = item.label
            textSize = 14f
            setTextColor(theme.colors.onBackground)
            gravity = Gravity.CENTER_VERTICAL
            
            setOnClickListener {
                onMenuItemClickListener?.invoke(item)
            }
        }
        
        addView(itemView)
    }
    
    /**
     * 清空菜单项
     */
    fun clearMenuItems() {
        menuItems.clear()
        removeAllViews()
    }
    
    /**
     * 设置菜单项点击监听器
     */
    fun setOnMenuItemClickListener(listener: (MenuItem) -> Unit) {
        onMenuItemClickListener = listener
    }
    
    /**
     * 获取所有菜单项
     */
    fun getMenuItems(): List<MenuItem> = menuItems.toList()
    
    /**
     * 点击菜单项（用于测试）
     */
    fun clickMenuItem(item: MenuItem) {
        onMenuItemClickListener?.invoke(item)
    }
}
