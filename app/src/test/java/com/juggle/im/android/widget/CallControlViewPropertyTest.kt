package com.juggle.im.android.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.checkAll
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith

/**
 * CallControlView 属性测试
 * 属性 46: 通话控制按钮完整性
 * 验证需求 10.3
 * 
 * 通话界面应该包含静音、扬声器、摄像头开关和挂断按钮，布局清晰
 */
@RunWith(RobolectricTestRunner::class)
class CallControlViewPropertyTest : FunSpec({
    
    val context: Context = ApplicationProvider.getApplicationContext()
    
    test("属性 46: 通话控制按钮完整性 - 所有按钮状态都应该被正确初始化") {
        val callControlView = CallControlView(context)
        
        // 验证所有按钮都初始化为 NORMAL 状态
        callControlView.getMuteState() shouldBe CallControlView.ButtonState.NORMAL
        callControlView.getSpeakerState() shouldBe CallControlView.ButtonState.NORMAL
        callControlView.getCameraState() shouldBe CallControlView.ButtonState.NORMAL
    }
    
    test("属性 46: 通话控制按钮完整性 - 任何按钮状态都应该被正确设置") {
        checkAll(Arb.enum<CallControlView.ButtonState>()) { state ->
            val callControlView = CallControlView(context)
            
            // 设置静音按钮状态
            callControlView.setMuteState(state)
            callControlView.getMuteState() shouldBe state
            
            // 设置扬声器按钮状态
            callControlView.setSpeakerState(state)
            callControlView.getSpeakerState() shouldBe state
            
            // 设置摄像头按钮状态
            callControlView.setCameraState(state)
            callControlView.getCameraState() shouldBe state
        }
    }
    
    test("属性 46: 通话控制按钮完整性 - 按钮状态应该独立变化") {
        checkAll(
            Arb.enum<CallControlView.ButtonState>(),
            Arb.enum<CallControlView.ButtonState>(),
            Arb.enum<CallControlView.ButtonState>()
        ) { muteState, speakerState, cameraState ->
            val callControlView = CallControlView(context)
            
            // 设置不同的按钮状态
            callControlView.setMuteState(muteState)
            callControlView.setSpeakerState(speakerState)
            callControlView.setCameraState(cameraState)
            
            // 验证每个按钮的状态都被正确设置
            callControlView.getMuteState() shouldBe muteState
            callControlView.getSpeakerState() shouldBe speakerState
            callControlView.getCameraState() shouldBe cameraState
        }
    }
    
    test("属性 46: 通话控制按钮完整性 - 按钮状态变化不应该相互影响") {
        val callControlView = CallControlView(context)
        
        // 设置静音按钮为 ACTIVE
        callControlView.setMuteState(CallControlView.ButtonState.ACTIVE)
        
        // 设置扬声器按钮为 DISABLED
        callControlView.setSpeakerState(CallControlView.ButtonState.DISABLED)
        
        // 验证静音按钮状态不变
        callControlView.getMuteState() shouldBe CallControlView.ButtonState.ACTIVE
        
        // 设置摄像头按钮为 NORMAL
        callControlView.setCameraState(CallControlView.ButtonState.NORMAL)
        
        // 验证前两个按钮状态不变
        callControlView.getMuteState() shouldBe CallControlView.ButtonState.ACTIVE
        callControlView.getSpeakerState() shouldBe CallControlView.ButtonState.DISABLED
        callControlView.getCameraState() shouldBe CallControlView.ButtonState.NORMAL
    }
    
    test("属性 46: 通话控制按钮完整性 - 所有按钮状态枚举值都应该被支持") {
        val callControlView = CallControlView(context)
        val states = CallControlView.ButtonState.values()
        
        // 验证有 3 个状态
        states.size shouldBe 3
        
        // 验证每个状态都能被设置
        for (state in states) {
            callControlView.setMuteState(state)
            callControlView.getMuteState() shouldBe state
        }
    }
})
