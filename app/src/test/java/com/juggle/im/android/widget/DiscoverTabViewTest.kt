package com.juggle.im.android.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.Assert.*

/**
 * DiscoverTabView 单元测试
 * 测试发现界面标签页结构
 */
@RunWith(RobolectricTestRunner::class)
class DiscoverTabViewTest {
    
    private lateinit var context: Context
    private lateinit var discoverTabView: DiscoverTabView
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        discoverTabView = DiscoverTabView(context)
    }
    
    @Test
    fun testInitialTab() {
        // 验证初始选中的标签页
        assertEquals(DiscoverTabView.TabType.MOMENTS, discoverTabView.getSelectedTab())
    }
    
    @Test
    fun testSelectMomentsTab() {
        // 测试选择朋友圈标签页
        discoverTabView.selectTab(DiscoverTabView.TabType.MOMENTS)
        
        assertEquals(DiscoverTabView.TabType.MOMENTS, discoverTabView.getSelectedTab())
    }
    
    @Test
    fun testSelectCommunityTab() {
        // 测试选择社区标签页
        discoverTabView.selectTab(DiscoverTabView.TabType.COMMUNITY)
        
        assertEquals(DiscoverTabView.TabType.COMMUNITY, discoverTabView.getSelectedTab())
    }
    
    @Test
    fun testTabSwitching() {
        // 测试标签页切换
        assertEquals(DiscoverTabView.TabType.MOMENTS, discoverTabView.getSelectedTab())
        
        discoverTabView.selectTab(DiscoverTabView.TabType.COMMUNITY)
        assertEquals(DiscoverTabView.TabType.COMMUNITY, discoverTabView.getSelectedTab())
        
        discoverTabView.selectTab(DiscoverTabView.TabType.MOMENTS)
        assertEquals(DiscoverTabView.TabType.MOMENTS, discoverTabView.getSelectedTab())
    }
    
    @Test
    fun testOnTabChangeListener() {
        // 测试标签页切换监听
        var changedTab: DiscoverTabView.TabType? = null
        discoverTabView.setOnTabChangeListener { tab ->
            changedTab = tab
        }
        
        discoverTabView.selectTab(DiscoverTabView.TabType.COMMUNITY)
        
        assertEquals(DiscoverTabView.TabType.COMMUNITY, changedTab)
    }
    
    @Test
    fun testMultipleTabChanges() {
        // 测试多次标签页切换
        var changeCount = 0
        discoverTabView.setOnTabChangeListener { _ ->
            changeCount++
        }
        
        discoverTabView.selectTab(DiscoverTabView.TabType.COMMUNITY)
        discoverTabView.selectTab(DiscoverTabView.TabType.MOMENTS)
        discoverTabView.selectTab(DiscoverTabView.TabType.COMMUNITY)
        
        assertEquals(3, changeCount)
    }
    
    @Test
    fun testTabTypeEnum() {
        // 测试标签页类型枚举
        val tabTypes = DiscoverTabView.TabType.values()
        
        assertEquals(2, tabTypes.size)
        assertTrue(tabTypes.contains(DiscoverTabView.TabType.MOMENTS))
        assertTrue(tabTypes.contains(DiscoverTabView.TabType.COMMUNITY))
    }
}
