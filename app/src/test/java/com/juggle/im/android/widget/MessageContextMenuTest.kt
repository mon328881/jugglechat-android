package com.juggle.im.android.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.Assert.*

/**
 * MessageContextMenu 单元测试
 * 测试消息长按操作菜单功能
 */
@RunWith(RobolectricTestRunner::class)
class MessageContextMenuTest {
    
    private lateinit var context: Context
    private lateinit var messageContextMenu: MessageContextMenu
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        messageContextMenu = MessageContextMenu(context)
    }
    
    @Test
    fun testAddMenuItem() {
        // 测试添加菜单项
        val item = MessageContextMenu.MenuItem("copy", "复制")
        messageContextMenu.addMenuItem(item)
        
        val items = messageContextMenu.getMenuItems()
        assertEquals(1, items.size)
        assertEquals("copy", items[0].id)
        assertEquals("复制", items[0].label)
    }
    
    @Test
    fun testAddMultipleMenuItems() {
        // 测试添加多个菜单项
        val copyItem = MessageContextMenu.MenuItem("copy", "复制")
        val deleteItem = MessageContextMenu.MenuItem("delete", "删除")
        val forwardItem = MessageContextMenu.MenuItem("forward", "转发")
        
        messageContextMenu.addMenuItem(copyItem)
        messageContextMenu.addMenuItem(deleteItem)
        messageContextMenu.addMenuItem(forwardItem)
        
        val items = messageContextMenu.getMenuItems()
        assertEquals(3, items.size)
        assertEquals("copy", items[0].id)
        assertEquals("delete", items[1].id)
        assertEquals("forward", items[2].id)
    }
    
    @Test
    fun testClearMenuItems() {
        // 测试清空菜单项
        messageContextMenu.addMenuItem(MessageContextMenu.MenuItem("copy", "复制"))
        messageContextMenu.addMenuItem(MessageContextMenu.MenuItem("delete", "删除"))
        
        messageContextMenu.clearMenuItems()
        
        val items = messageContextMenu.getMenuItems()
        assertEquals(0, items.size)
    }
    
    @Test
    fun testMenuItemClickListener() {
        // 测试菜单项点击监听器
        var clickedItemId = ""
        messageContextMenu.setOnMenuItemClickListener { item ->
            clickedItemId = item.id
        }
        
        val item = MessageContextMenu.MenuItem("copy", "复制")
        messageContextMenu.addMenuItem(item)
        messageContextMenu.clickMenuItem(item)
        
        assertEquals("copy", clickedItemId)
    }
    
    @Test
    fun testMenuItemWithIcon() {
        // 测试带图标的菜单项
        val item = MessageContextMenu.MenuItem("copy", "复制", android.R.drawable.ic_menu_view)
        messageContextMenu.addMenuItem(item)
        
        val items = messageContextMenu.getMenuItems()
        assertEquals(1, items.size)
        assertNotNull(items[0].icon)
        assertEquals(android.R.drawable.ic_menu_view, items[0].icon)
    }
    
    @Test
    fun testGetMenuItems() {
        // 测试获取菜单项列表
        val copyItem = MessageContextMenu.MenuItem("copy", "复制")
        val deleteItem = MessageContextMenu.MenuItem("delete", "删除")
        
        messageContextMenu.addMenuItem(copyItem)
        messageContextMenu.addMenuItem(deleteItem)
        
        val items = messageContextMenu.getMenuItems()
        assertEquals(2, items.size)
        assertTrue(items.contains(copyItem))
        assertTrue(items.contains(deleteItem))
    }
}
