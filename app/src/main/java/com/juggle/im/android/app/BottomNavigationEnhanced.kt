package com.juggle.im.android.app

import android.content.Context
import com.juggle.im.android.widget.MaterialBottomNavigation

/**
 * 底部导航增强功能
 * 实现 5 个导航项、未读徽章显示和深色模式适配
 */
class BottomNavigationEnhanced {
    
    companion object {
        const val NAV_CHAT = 0
        const val NAV_FRIENDS = 1
        const val NAV_DISCOVER = 2
        const val NAV_GROUPS = 3
        const val NAV_PROFILE = 4
        
        const val TOTAL_NAV_ITEMS = 5
    }
    
    /**
     * 初始化底部导航栏
     * 验证: 需求 3.1, 3.2, 3.3
     */
    fun initializeBottomNavigation(
        context: Context,
        navBar: MaterialBottomNavigation?
    ) {
        navBar?.apply {
            // 添加 5 个导航项
            addNavItem(MaterialBottomNavigation.NavItem(
                id = NAV_CHAT,
                icon = android.R.drawable.ic_menu_chat,
                label = "聊天"
            ))
            
            addNavItem(MaterialBottomNavigation.NavItem(
                id = NAV_FRIENDS,
                icon = android.R.drawable.ic_menu_myplaces,
                label = "朋友"
            ))
            
            addNavItem(MaterialBottomNavigation.NavItem(
                id = NAV_DISCOVER,
                icon = android.R.drawable.ic_menu_search,
                label = "发现"
            ))
            
            addNavItem(MaterialBottomNavigation.NavItem(
                id = NAV_GROUPS,
                icon = android.R.drawable.ic_menu_view,
                label = "群组"
            ))
            
            addNavItem(MaterialBottomNavigation.NavItem(
                id = NAV_PROFILE,
                icon = android.R.drawable.ic_menu_info_details,
                label = "个人资料"
            ))
            
            // 设置所有导航项的无障碍标签
            setAllNavItemsAccessibilityLabels()
            
            // 选择第一个导航项
            selectNavItem(NAV_CHAT)
        }
    }
    
    /**
     * 更新未读消息徽章
     * 验证: 需求 3.6
     */
    fun updateUnreadBadge(
        navBar: MaterialBottomNavigation?,
        navIndex: Int,
        unreadCount: Int
    ) {
        navBar?.updateUnreadCount(navIndex, unreadCount)
    }
    
    /**
     * 更新所有未读消息徽章
     */
    fun updateAllUnreadBadges(
        navBar: MaterialBottomNavigation?,
        unreadCounts: Map<Int, Int>
    ) {
        unreadCounts.forEach { (index, count) ->
            navBar?.updateUnreadCount(index, count)
        }
    }
    
    /**
     * 获取导航项数量
     * 验证: 需求 3.1
     */
    fun getNavItemCount(navBar: MaterialBottomNavigation?): Int {
        return navBar?.getItemCount() ?: 0
    }
    
    /**
     * 获取导航项标签列表
     */
    fun getNavItemLabels(navBar: MaterialBottomNavigation?): List<String> {
        return navBar?.getItemLabels() ?: emptyList()
    }
    
    /**
     * 验证导航项数量是否正确
     * 验证: 需求 3.1
     */
    fun validateNavItemCount(navBar: MaterialBottomNavigation?): Boolean {
        return getNavItemCount(navBar) == TOTAL_NAV_ITEMS
    }
    
    /**
     * 验证导航项标签是否正确
     */
    fun validateNavItemLabels(navBar: MaterialBottomNavigation?): Boolean {
        val labels = getNavItemLabels(navBar)
        val expectedLabels = listOf("聊天", "朋友", "发现", "群组", "个人资料")
        return labels == expectedLabels
    }
}
