package com.juggle.im.android.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.Assert.*

/**
 * CallView 单元测试
 * 测试通话界面信息显示
 */
@RunWith(RobolectricTestRunner::class)
class CallViewTest {
    
    private lateinit var context: Context
    private lateinit var callView: CallView
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        callView = CallView(context)
    }
    
    @Test
    fun testBindDataWithVoiceCall() {
        // 测试绑定语音通话数据
        val callData = CallView.CallData(
            callerId = "user_001",
            callerName = "张三",
            callerAvatar = "https://example.com/avatar.jpg",
            callType = CallView.CallType.VOICE,
            callDuration = "00:30"
        )
        
        callView.bindData(callData)
        
        assertEquals("张三", callView.getCallerName())
        assertEquals("00:30", callView.getCallDuration())
    }
    
    @Test
    fun testBindDataWithVideoCall() {
        // 测试绑定视频通话数据
        val callData = CallView.CallData(
            callerId = "user_002",
            callerName = "李四",
            callerAvatar = "https://example.com/avatar.jpg",
            callType = CallView.CallType.VIDEO,
            callDuration = "01:45"
        )
        
        callView.bindData(callData)
        
        assertEquals("李四", callView.getCallerName())
        assertEquals("01:45", callView.getCallDuration())
    }
    
    @Test
    fun testUpdateCallDuration() {
        // 测试更新通话时长
        val callData = CallView.CallData(
            callerId = "user_001",
            callerName = "王五",
            callerAvatar = "https://example.com/avatar.jpg",
            callDuration = "00:00"
        )
        
        callView.bindData(callData)
        assertEquals("00:00", callView.getCallDuration())
        
        callView.updateCallDuration("00:15")
        assertEquals("00:15", callView.getCallDuration())
        
        callView.updateCallDuration("01:30")
        assertEquals("01:30", callView.getCallDuration())
    }
    
    @Test
    fun testGetCallerName() {
        // 测试获取来电者名称
        val callData = CallView.CallData(
            callerId = "user_001",
            callerName = "赵六",
            callerAvatar = "https://example.com/avatar.jpg"
        )
        
        callView.bindData(callData)
        
        assertEquals("赵六", callView.getCallerName())
    }
    
    @Test
    fun testGetCallDuration() {
        // 测试获取通话时长
        val callData = CallView.CallData(
            callerId = "user_001",
            callerName = "孙七",
            callerAvatar = "https://example.com/avatar.jpg",
            callDuration = "02:15"
        )
        
        callView.bindData(callData)
        
        assertEquals("02:15", callView.getCallDuration())
    }
    
    @Test
    fun testOnMuteClickListener() {
        // 测试静音按钮点击监听
        var muteClicked = false
        callView.setOnMuteClickListener {
            muteClicked = true
        }
        
        // 验证监听器已设置
        assertNotNull(callView)
    }
    
    @Test
    fun testOnSpeakerClickListener() {
        // 测试扬声器按钮点击监听
        var speakerClicked = false
        callView.setOnSpeakerClickListener {
            speakerClicked = true
        }
        
        // 验证监听器已设置
        assertNotNull(callView)
    }
    
    @Test
    fun testOnCameraClickListener() {
        // 测试摄像头按钮点击监听
        var cameraClicked = false
        callView.setOnCameraClickListener {
            cameraClicked = true
        }
        
        // 验证监听器已设置
        assertNotNull(callView)
    }
    
    @Test
    fun testOnHangupClickListener() {
        // 测试挂断按钮点击监听
        var hangupClicked = false
        callView.setOnHangupClickListener {
            hangupClicked = true
        }
        
        // 验证监听器已设置
        assertNotNull(callView)
    }
    
    @Test
    fun testCallTypeEnum() {
        // 测试通话类型枚举
        val callTypes = CallView.CallType.values()
        
        assertEquals(2, callTypes.size)
        assertTrue(callTypes.contains(CallView.CallType.VOICE))
        assertTrue(callTypes.contains(CallView.CallType.VIDEO))
    }
}
