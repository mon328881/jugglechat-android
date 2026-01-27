package com.juggle.im.android.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.Assert.*

/**
 * TypingStatusView 单元测试
 * 测试输入状态显示功能
 */
@RunWith(RobolectricTestRunner::class)
class TypingStatusViewTest {
    
    private lateinit var context: Context
    private lateinit var typingStatusView: TypingStatusView
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        typingStatusView = TypingStatusView(context)
    }
    
    @Test
    fun testInitialState() {
        // 验证初始状态为隐藏
        assertFalse(typingStatusView.isShowingTypingStatus())
    }
    
    @Test
    fun testShowTypingStatus() {
        // 测试显示输入状态
        typingStatusView.showTypingStatus()
        
        assertTrue(typingStatusView.isShowingTypingStatus())
    }
    
    @Test
    fun testHideTypingStatus() {
        // 测试隐藏输入状态
        typingStatusView.showTypingStatus()
        assertTrue(typingStatusView.isShowingTypingStatus())
        
        typingStatusView.hideTypingStatus()
        assertFalse(typingStatusView.isShowingTypingStatus())
    }
    
    @Test
    fun testToggleTypingStatus() {
        // 测试切换输入状态
        assertFalse(typingStatusView.isShowingTypingStatus())
        
        typingStatusView.showTypingStatus()
        assertTrue(typingStatusView.isShowingTypingStatus())
        
        typingStatusView.hideTypingStatus()
        assertFalse(typingStatusView.isShowingTypingStatus())
        
        typingStatusView.showTypingStatus()
        assertTrue(typingStatusView.isShowingTypingStatus())
    }
    
    @Test
    fun testMultipleShowCalls() {
        // 测试多次调用 showTypingStatus
        typingStatusView.showTypingStatus()
        typingStatusView.showTypingStatus()
        
        assertTrue(typingStatusView.isShowingTypingStatus())
    }
    
    @Test
    fun testMultipleHideCalls() {
        // 测试多次调用 hideTypingStatus
        typingStatusView.showTypingStatus()
        typingStatusView.hideTypingStatus()
        typingStatusView.hideTypingStatus()
        
        assertFalse(typingStatusView.isShowingTypingStatus())
    }
}
