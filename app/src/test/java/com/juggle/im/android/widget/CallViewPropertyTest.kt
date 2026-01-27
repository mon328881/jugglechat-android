package com.juggle.im.android.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith

/**
 * CallView 属性测试
 * 属性 45: 通话界面信息显示
 * 验证需求 10.1
 * 
 * 通话界面应该显示对方头像或视频画面，以及通话时长
 */
@RunWith(RobolectricTestRunner::class)
class CallViewPropertyTest : FunSpec({
    
    val context: Context = ApplicationProvider.getApplicationContext()
    
    test("属性 45: 通话界面信息显示 - 任何有效的来电者名称都应该被正确显示") {
        checkAll(Arb.string(minSize = 1, maxSize = 50)) { callerName ->
            val callView = CallView(context)
            val callData = CallView.CallData(
                callerId = "user_001",
                callerName = callerName,
                callerAvatar = "https://example.com/avatar.jpg",
                callType = CallView.CallType.VOICE,
                callDuration = "00:00"
            )
            
            callView.bindData(callData)
            
            // 验证来电者名称被正确显示
            callView.getCallerName() shouldBe callerName
        }
    }
    
    test("属性 45: 通话界面信息显示 - 任何有效的通话时长都应该被正确显示") {
        checkAll(Arb.string(minSize = 5, maxSize = 8)) { duration ->
            val callView = CallView(context)
            val callData = CallView.CallData(
                callerId = "user_001",
                callerName = "测试用户",
                callerAvatar = "https://example.com/avatar.jpg",
                callType = CallView.CallType.VOICE,
                callDuration = duration
            )
            
            callView.bindData(callData)
            
            // 验证通话时长被正确显示
            callView.getCallDuration() shouldBe duration
        }
    }
    
    test("属性 45: 通话界面信息显示 - 通话时长更新应该正确反映") {
        checkAll(
            Arb.string(minSize = 5, maxSize = 8),
            Arb.string(minSize = 5, maxSize = 8)
        ) { initialDuration, updatedDuration ->
            val callView = CallView(context)
            val callData = CallView.CallData(
                callerId = "user_001",
                callerName = "测试用户",
                callerAvatar = "https://example.com/avatar.jpg",
                callType = CallView.CallType.VOICE,
                callDuration = initialDuration
            )
            
            callView.bindData(callData)
            callView.getCallDuration() shouldBe initialDuration
            
            // 更新通话时长
            callView.updateCallDuration(updatedDuration)
            callView.getCallDuration() shouldBe updatedDuration
        }
    }
    
    test("属性 45: 通话界面信息显示 - 语音通话和视频通话都应该被支持") {
        checkAll(Arb.string(minSize = 1, maxSize = 50)) { callerName ->
            val callView = CallView(context)
            
            // 测试语音通话
            val voiceCallData = CallView.CallData(
                callerId = "user_001",
                callerName = callerName,
                callerAvatar = "https://example.com/avatar.jpg",
                callType = CallView.CallType.VOICE,
                callDuration = "00:00"
            )
            
            callView.bindData(voiceCallData)
            callView.getCallerName() shouldBe callerName
            
            // 测试视频通话
            val videoCallData = CallView.CallData(
                callerId = "user_002",
                callerName = callerName,
                callerAvatar = "https://example.com/avatar.jpg",
                callType = CallView.CallType.VIDEO,
                callDuration = "00:00"
            )
            
            callView.bindData(videoCallData)
            callView.getCallerName() shouldBe callerName
        }
    }
})
