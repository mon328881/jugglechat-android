package com.juggle.im.android.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.juggle.im.android.theme.ThemeManager

/**
 * 群组管理员权限菜单视图组件
 * 显示添加成员、移除成员、设置管理员等选项
 * 
 * **验证: 需求 9.4**
 */
class GroupAdminMenuView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    
    /**
     * 管理员操作类型枚举
     */
    enum class AdminAction {
        ADD_MEMBER,         // 添加成员
        REMOVE_MEMBER,      // 移除成员
        SET_ADMIN,          // 设置管理员
        REMOVE_ADMIN,       // 移除管理员
        DISSOLVE_GROUP      // 解散群组
    }
    
    private val themeManager: ThemeManager = ThemeManager.getInstance(context)
    private val theme = themeManager.getCurrentTheme()
    
    // UI 组件
    private val menuItems = mutableMapOf<AdminAction, TextView>()
    private val actionListeners = mutableMapOf<AdminAction, () -> Unit>()
    
    init {
        orientation = VERTICAL
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        setPadding(theme.spacing.md, theme.spacing.md, theme.spacing.md, theme.spacing.md)
        
        // 创建菜单项
        createMenuItem(AdminAction.ADD_MEMBER, "添加成员")
        createMenuItem(AdminAction.REMOVE_MEMBER, "移除成员")
        createMenuItem(AdminAction.SET_ADMIN, "设置管理员")
        createMenuItem(AdminAction.REMOVE_ADMIN, "移除管理员")
        createMenuItem(AdminAction.DISSOLVE_GROUP, "解散群组")
    }
    
    /**
     * 创建菜单项
     */
    private fun createMenuItem(action: AdminAction, label: String) {
        val menuItem = TextView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = theme.spacing.sm
            }
            text = label
            textSize = 14f
            setTextColor(theme.colors.onBackground)
            setPadding(theme.spacing.md, theme.spacing.sm, theme.spacing.md, theme.spacing.sm)
            
            setOnClickListener {
                actionListeners[action]?.invoke()
            }
        }
        
        menuItems[action] = menuItem
        addView(menuItem)
    }
    
    /**
     * 设置管理员操作监听
     */
    fun setOnAdminActionListener(action: AdminAction, listener: () -> Unit) {
        actionListeners[action] = listener
    }
    
    /**
     * 获取菜单项（用于测试）
     */
    fun getMenuItem(action: AdminAction): TextView? = menuItems[action]
    
    /**
     * 检查菜单项是否存在（用于测试）
     */
    fun hasMenuItem(action: AdminAction): Boolean = menuItems.containsKey(action)
    
    /**
     * 获取所有菜单项数量（用于测试）
     */
    fun getMenuItemCount(): Int = menuItems.size
}
