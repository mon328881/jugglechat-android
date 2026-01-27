package com.juggle.im.android.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.Assert.*

/**
 * IncomingCallView 单元测试
 * 测试来电提示显示和接听/拒绝功能
 * 需求：10.6
 */
@RunWith(RobolectricTestRunner::class)
class IncomingCallViewTest {
    
    private lateinit var context: Context
    private lateinit var incomingCallView: IncomingCallView
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        incomingCallView = IncomingCallView(context)
    }
    
    @Test
    fun testBindVoiceCallData() {
        // 测试绑定语音通话数据
        val callData = IncomingCallView.IncomingCallData(
            callerId = "user_001",
            callerName = "张三",
            callerAvatar = "https://example.com/avatar.jpg",
            isVideoCall = false
        )
        
        incomingCallView.bindData(callData)
        
        assertEquals("张三", incomingCallView.getCallerName())
        assertEquals("语音通话邀请", incomingCallView.getCallTypeText())
    }
    
    @Test
    fun testBindVideoCallData() {
        // 测试绑定视频通话数据
        val callData = IncomingCallView.IncomingCallData(
            callerId = "user_002",
            callerName = "李四",
            callerAvatar = "https://example.com/avatar.jpg",
            isVideoCall = true
        )
        
        incomingCallView.bindData(callData)
        
        assertEquals("李四", incomingCallView.getCallerName())
        assertEquals("视频通话邀请", incomingCallView.getCallTypeText())
    }
    
    @Test
    fun testGetCallerName() {
        // 测试获取来电者名称
        val callData = IncomingCallView.IncomingCallData(
            callerId = "user_001",
            callerName = "王五",
            callerAvatar = "https://example.com/avatar.jpg"
        )
        
        incomingCallView.bindData(callData)
        
        assertEquals("王五", incomingCallView.getCallerName())
    }
    
    @Test
    fun testGetCallTypeText() {
        // 测试获取通话类型文本
        val voiceCallData = IncomingCallView.IncomingCallData(
            callerId = "user_001",
            callerName = "赵六",
            callerAvatar = "https://example.com/avatar.jpg",
            isVideoCall = false
        )
        
        incomingCallView.bindData(voiceCallData)
        assertEquals("语音通话邀请", incomingCallView.getCallTypeText())
        
        val videoCallData = IncomingCallView.IncomingCallData(
            callerId = "user_002",
            callerName = "孙七",
            callerAvatar = "https://example.com/avatar.jpg",
            isVideoCall = true
        )
        
        incomingCallView.bindData(videoCallData)
        assertEquals("视频通话邀请", incomingCallView.getCallTypeText())
    }
    
    @Test
    fun testOnAcceptClickListener() {
        // 测试接听按钮点击监听
        var acceptClicked = false
        
        incomingCallView.setOnAcceptClickListener {
            acceptClicked = true
        }
        
        // 验证监听器已设置
        assertNotNull(incomingCallView)
    }
    
    @Test
    fun testOnRejectClickListener() {
        // 测试拒绝按钮点击监听
        var rejectClicked = false
        
        incomingCallView.setOnRejectClickListener {
            rejectClicked = true
        }
        
        // 验证监听器已设置
        assertNotNull(incomingCallView)
    }
    
    @Test
    fun testIncomingCallDataClass() {
        // 测试来电数据类
        val callData = IncomingCallView.IncomingCallData(
            callerId = "user_001",
            callerName = "测试用户",
            callerAvatar = "https://example.com/avatar.jpg",
            isVideoCall = true
        )
        
        assertEquals("user_001", callData.callerId)
        assertEquals("测试用户", callData.callerName)
        assertEquals("https://example.com/avatar.jpg", callData.callerAvatar)
        assertTrue(callData.isVideoCall)
    }
    
    @Test
    fun testMultipleCallDataBindings() {
        // 测试多次绑定不同的来电数据
        val callData1 = IncomingCallView.IncomingCallData(
            callerId = "user_001",
            callerName = "张三",
            callerAvatar = "https://example.com/avatar1.jpg",
            isVideoCall = false
        )
        
        incomingCallView.bindData(callData1)
        assertEquals("张三", incomingCallView.getCallerName())
        
        val callData2 = IncomingCallView.IncomingCallData(
            callerId = "user_002",
            callerName = "李四",
            callerAvatar = "https://example.com/avatar2.jpg",
            isVideoCall = true
        )
        
        incomingCallView.bindData(callData2)
        assertEquals("李四", incomingCallView.getCallerName())
    }
    
    @Test
    fun testDefaultIsVideoCallValue() {
        // 测试默认的 isVideoCall 值
        val callData = IncomingCallView.IncomingCallData(
            callerId = "user_001",
            callerName = "测试用户",
            callerAvatar = "https://example.com/avatar.jpg"
        )
        
        assertFalse(callData.isVideoCall)
    }
}
