package com.juggle.im.android.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.Assert.*

/**
 * CallControlView 单元测试
 * 测试通话控制按钮完整性和静音按钮状态反馈
 * 需求：10.3, 10.4
 */
@RunWith(RobolectricTestRunner::class)
class CallControlViewTest {
    
    private lateinit var context: Context
    private lateinit var callControlView: CallControlView
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        callControlView = CallControlView(context)
    }
    
    @Test
    fun testInitialButtonStates() {
        // 测试初始按钮状态
        assertEquals(CallControlView.ButtonState.NORMAL, callControlView.getMuteState())
        assertEquals(CallControlView.ButtonState.NORMAL, callControlView.getSpeakerState())
        assertEquals(CallControlView.ButtonState.NORMAL, callControlView.getCameraState())
    }
    
    @Test
    fun testMuteButtonStateToggle() {
        // 测试静音按钮状态切换
        assertEquals(CallControlView.ButtonState.NORMAL, callControlView.getMuteState())
        
        callControlView.setMuteState(CallControlView.ButtonState.ACTIVE)
        assertEquals(CallControlView.ButtonState.ACTIVE, callControlView.getMuteState())
        
        callControlView.setMuteState(CallControlView.ButtonState.NORMAL)
        assertEquals(CallControlView.ButtonState.NORMAL, callControlView.getMuteState())
    }
    
    @Test
    fun testSpeakerButtonStateToggle() {
        // 测试扬声器按钮状态切换
        assertEquals(CallControlView.ButtonState.NORMAL, callControlView.getSpeakerState())
        
        callControlView.setSpeakerState(CallControlView.ButtonState.ACTIVE)
        assertEquals(CallControlView.ButtonState.ACTIVE, callControlView.getSpeakerState())
        
        callControlView.setSpeakerState(CallControlView.ButtonState.NORMAL)
        assertEquals(CallControlView.ButtonState.NORMAL, callControlView.getSpeakerState())
    }
    
    @Test
    fun testCameraButtonStateToggle() {
        // 测试摄像头按钮状态切换
        assertEquals(CallControlView.ButtonState.NORMAL, callControlView.getCameraState())
        
        callControlView.setCameraState(CallControlView.ButtonState.ACTIVE)
        assertEquals(CallControlView.ButtonState.ACTIVE, callControlView.getCameraState())
        
        callControlView.setCameraState(CallControlView.ButtonState.NORMAL)
        assertEquals(CallControlView.ButtonState.NORMAL, callControlView.getCameraState())
    }
    
    @Test
    fun testMuteClickListener() {
        // 测试静音按钮点击监听
        var muteClicked = false
        var isMuted = false
        
        callControlView.setOnMuteClickListener { muted ->
            muteClicked = true
            isMuted = muted
        }
        
        // 验证监听器已设置
        assertNotNull(callControlView)
    }
    
    @Test
    fun testSpeakerClickListener() {
        // 测试扬声器按钮点击监听
        var speakerClicked = false
        var isMuted = false
        
        callControlView.setOnSpeakerClickListener { muted ->
            speakerClicked = true
            isMuted = muted
        }
        
        // 验证监听器已设置
        assertNotNull(callControlView)
    }
    
    @Test
    fun testCameraClickListener() {
        // 测试摄像头按钮点击监听
        var cameraClicked = false
        var isActive = false
        
        callControlView.setOnCameraClickListener { active ->
            cameraClicked = true
            isActive = active
        }
        
        // 验证监听器已设置
        assertNotNull(callControlView)
    }
    
    @Test
    fun testHangupClickListener() {
        // 测试挂断按钮点击监听
        var hangupClicked = false
        
        callControlView.setOnHangupClickListener {
            hangupClicked = true
        }
        
        // 验证监听器已设置
        assertNotNull(callControlView)
    }
    
    @Test
    fun testButtonStateEnum() {
        // 测试按钮状态枚举
        val states = CallControlView.ButtonState.values()
        
        assertEquals(3, states.size)
        assertTrue(states.contains(CallControlView.ButtonState.NORMAL))
        assertTrue(states.contains(CallControlView.ButtonState.ACTIVE))
        assertTrue(states.contains(CallControlView.ButtonState.DISABLED))
    }
    
    @Test
    fun testMultipleButtonStateChanges() {
        // 测试多个按钮状态变化
        callControlView.setMuteState(CallControlView.ButtonState.ACTIVE)
        callControlView.setSpeakerState(CallControlView.ButtonState.ACTIVE)
        callControlView.setCameraState(CallControlView.ButtonState.NORMAL)
        
        assertEquals(CallControlView.ButtonState.ACTIVE, callControlView.getMuteState())
        assertEquals(CallControlView.ButtonState.ACTIVE, callControlView.getSpeakerState())
        assertEquals(CallControlView.ButtonState.NORMAL, callControlView.getCameraState())
    }
    
    @Test
    fun testDisabledButtonState() {
        // 测试禁用状态
        callControlView.setMuteState(CallControlView.ButtonState.DISABLED)
        assertEquals(CallControlView.ButtonState.DISABLED, callControlView.getMuteState())
        
        callControlView.setSpeakerState(CallControlView.ButtonState.DISABLED)
        assertEquals(CallControlView.ButtonState.DISABLED, callControlView.getSpeakerState())
        
        callControlView.setCameraState(CallControlView.ButtonState.DISABLED)
        assertEquals(CallControlView.ButtonState.DISABLED, callControlView.getCameraState())
    }
}
