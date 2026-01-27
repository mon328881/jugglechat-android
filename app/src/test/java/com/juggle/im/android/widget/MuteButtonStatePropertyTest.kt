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
 * 静音按钮状态反馈属性测试
 * 属性 47: 静音按钮状态反馈
 * 验证需求 10.4
 * 
 * 当用户点击静音按钮时，按钮应该显示不同的样式，表示已静音
 */
@RunWith(RobolectricTestRunner::class)
class MuteButtonStatePropertyTest : FunSpec({
    
    val context: Context = ApplicationProvider.getApplicationContext()
    
    test("属性 47: 静音按钮状态反馈 - 静音按钮初始状态应该是 NORMAL") {
        val callControlView = CallControlView(context)
        
        // 验证初始状态
        callControlView.getMuteState() shouldBe CallControlView.ButtonState.NORMAL
    }
    
    test("属性 47: 静音按钮状态反馈 - 静音按钮状态应该能切换到 ACTIVE") {
        val callControlView = CallControlView(context)
        
        // 设置为 ACTIVE
        callControlView.setMuteState(CallControlView.ButtonState.ACTIVE)
        
        // 验证状态已改变
        callControlView.getMuteState() shouldBe CallControlView.ButtonState.ACTIVE
    }
    
    test("属性 47: 静音按钮状态反馈 - 静音按钮状态应该能从 ACTIVE 切换回 NORMAL") {
        val callControlView = CallControlView(context)
        
        // 设置为 ACTIVE
        callControlView.setMuteState(CallControlView.ButtonState.ACTIVE)
        callControlView.getMuteState() shouldBe CallControlView.ButtonState.ACTIVE
        
        // 切换回 NORMAL
        callControlView.setMuteState(CallControlView.ButtonState.NORMAL)
        callControlView.getMuteState() shouldBe CallControlView.ButtonState.NORMAL
    }
    
    test("属性 47: 静音按钮状态反馈 - 静音按钮状态应该能切换到 DISABLED") {
        val callControlView = CallControlView(context)
        
        // 设置为 DISABLED
        callControlView.setMuteState(CallControlView.ButtonState.DISABLED)
        
        // 验证状态已改变
        callControlView.getMuteState() shouldBe CallControlView.ButtonState.DISABLED
    }
    
    test("属性 47: 静音按钮状态反馈 - 静音按钮状态变化应该不影响其他按钮") {
        val callControlView = CallControlView(context)
        
        // 设置扬声器按钮为 ACTIVE
        callControlView.setSpeakerState(CallControlView.ButtonState.ACTIVE)
        
        // 改变静音按钮状态
        callControlView.setMuteState(CallControlView.ButtonState.ACTIVE)
        
        // 验证扬声器按钮状态不变
        callControlView.getSpeakerState() shouldBe CallControlView.ButtonState.ACTIVE
        callControlView.getMuteState() shouldBe CallControlView.ButtonState.ACTIVE
    }
    
    test("属性 47: 静音按钮状态反馈 - 任何有效的按钮状态都应该被支持") {
        checkAll(Arb.enum<CallControlView.ButtonState>()) { state ->
            val callControlView = CallControlView(context)
            
            // 设置静音按钮状态
            callControlView.setMuteState(state)
            
            // 验证状态被正确设置
            callControlView.getMuteState() shouldBe state
        }
    }
    
    test("属性 47: 静音按钮状态反馈 - 多次状态变化应该被正确处理") {
        val callControlView = CallControlView(context)
        
        // 进行多次状态变化
        callControlView.setMuteState(CallControlView.ButtonState.NORMAL)
        callControlView.getMuteState() shouldBe CallControlView.ButtonState.NORMAL
        
        callControlView.setMuteState(CallControlView.ButtonState.ACTIVE)
        callControlView.getMuteState() shouldBe CallControlView.ButtonState.ACTIVE
        
        callControlView.setMuteState(CallControlView.ButtonState.DISABLED)
        callControlView.getMuteState() shouldBe CallControlView.ButtonState.DISABLED
        
        callControlView.setMuteState(CallControlView.ButtonState.NORMAL)
        callControlView.getMuteState() shouldBe CallControlView.ButtonState.NORMAL
    }
})
