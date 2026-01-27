package com.juggle.im.android.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.Assert.*

/**
 * MessageInputView 单元测试
 * 测试多行输入框自动扩展功能
 */
@RunWith(RobolectricTestRunner::class)
class MessageInputViewTest {
    
    private lateinit var context: Context
    private lateinit var messageInputView: MessageInputView
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        messageInputView = MessageInputView(context)
    }
    
    @Test
    fun testInputFieldInitialization() {
        // 验证输入框初始化
        val inputText = messageInputView.getInputText()
        assertEquals("", inputText)
    }
    
    @Test
    fun testSetAndGetInputText() {
        // 测试设置和获取输入框内容
        val testMessage = "Hello, World!"
        messageInputView.setMessage(testMessage)
        
        val inputText = messageInputView.getInputText()
        assertEquals(testMessage, inputText)
    }
    
    @Test
    fun testClearInput() {
        // 测试清空输入框
        messageInputView.setMessage("Test message")
        messageInputView.clearInput()
        
        val inputText = messageInputView.getInputText()
        assertEquals("", inputText)
    }
    
    @Test
    fun testOnSendListener() {
        // 测试发送监听器
        var sentMessage = ""
        messageInputView.setOnSendListener { message ->
            sentMessage = message
        }
        
        messageInputView.setMessage("Test message")
        messageInputView.sendMessage()
        
        assertEquals("Test message", sentMessage)
    }
    
    @Test
    fun testOnEmojiClickListener() {
        // 测试表情按钮点击监听器
        var emojiClicked = false
        messageInputView.setOnEmojiClickListener {
            emojiClicked = true
        }
        
        messageInputView.clickEmojiButton()
        
        assertTrue(emojiClicked)
    }
    
    @Test
    fun testOnAttachmentClickListener() {
        // 测试附件按钮点击监听器
        var attachmentClicked = false
        messageInputView.setOnAttachmentClickListener {
            attachmentClicked = true
        }
        
        messageInputView.clickAttachmentButton()
        
        assertTrue(attachmentClicked)
    }
    
    @Test
    fun testRequestInputFocus() {
        // 测试获取输入框焦点
        messageInputView.requestInputFocus()
        
        // 验证输入框获得焦点
        assertTrue(messageInputView.hasInputFocus())
    }
    
    @Test
    fun testMultilineInput() {
        // 测试多行输入
        val multilineMessage = "Line 1\nLine 2\nLine 3"
        messageInputView.setMessage(multilineMessage)
        
        val inputText = messageInputView.getInputText()
        assertEquals(multilineMessage, inputText)
    }
}
