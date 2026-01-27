package com.juggle.im.android.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * 位置分享按钮单元测试
 * 验证: 需求 16.1
 */
class LocationShareButtonTest {
    
    private lateinit var context: Context
    private lateinit var messageInputView: MessageInputView
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        messageInputView = MessageInputView(context)
    }
    
    @Test
    fun testLocationButtonClickListener() {
        var clicked = false
        
        messageInputView.setOnLocationClickListener {
            clicked = true
        }
        
        messageInputView.clickLocationButton()
        assertTrue(clicked)
    }
    
    @Test
    fun testLocationButtonMultipleClicks() {
        var clickCount = 0
        
        messageInputView.setOnLocationClickListener {
            clickCount++
        }
        
        repeat(3) {
            messageInputView.clickLocationButton()
        }
        
        assertEquals(3, clickCount)
    }
    
    @Test
    fun testLocationButtonListenerReplacement() {
        var firstCalled = false
        var secondCalled = false
        
        messageInputView.setOnLocationClickListener {
            firstCalled = true
        }
        
        messageInputView.setOnLocationClickListener {
            secondCalled = true
        }
        
        messageInputView.clickLocationButton()
        
        assertFalse(firstCalled)
        assertTrue(secondCalled)
    }
    
    @Test
    fun testLocationButtonIndependentFromOtherButtons() {
        var locationClicked = false
        var emojiClicked = false
        
        messageInputView.setOnLocationClickListener {
            locationClicked = true
        }
        
        messageInputView.setOnEmojiClickListener {
            emojiClicked = true
        }
        
        messageInputView.clickLocationButton()
        
        assertTrue(locationClicked)
        assertFalse(emojiClicked)
    }
}
