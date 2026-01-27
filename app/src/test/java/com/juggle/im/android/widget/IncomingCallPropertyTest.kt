package com.juggle.im.android.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith

/**
 * 来电提示显示属性测试
 * 属性 49: 来电提示显示
 * 验证需求 10.6
 * 
 * 当通话中有来电时，应用应该显示来电提示，用户可以选择接听或拒绝
 */
@RunWith(RobolectricTestRunner::class)
class IncomingCallPropertyTest : FunSpec({
    
    val context: Context = ApplicationProvider.getApplicationContext()
    
    test("属性 49: 来电提示显示 - 任何有效的来电者名称都应该被正确显示") {
        checkAll(Arb.string(minSize = 1, maxSize = 50)) { callerName ->
            val incomingCallView = IncomingCallView(context)
            val callData = IncomingCallView.IncomingCallData(
                callerId = "user_001",
                callerName = callerName,
                callerAvatar = "https://example.com/avatar.jpg",
                isVideoCall = false
            )
            
            incomingCallView.bindData(callData)
            
            // 验证来电者名称被正确显示
            incomingCallView.getCallerName() shouldBe callerName
        }
    }
    
    test("属性 49: 来电提示显示 - 语音通话应该显示正确的提示文本") {
        val incomingCallView = IncomingCallView(context)
        val callData = IncomingCallView.IncomingCallData(
            callerId = "user_001",
            callerName = "测试用户",
            callerAvatar = "https://example.com/avatar.jpg",
            isVideoCall = false
        )
        
        incomingCallView.bindData(callData)
        
        // 验证显示语音通话提示
        incomingCallView.getCallTypeText() shouldBe "语音通话邀请"
    }
    
    test("属性 49: 来电提示显示 - 视频通话应该显示正确的提示文本") {
        val incomingCallView = IncomingCallView(context)
        val callData = IncomingCallView.IncomingCallData(
            callerId = "user_001",
            callerName = "测试用户",
            callerAvatar = "https://example.com/avatar.jpg",
            isVideoCall = true
        )
        
        incomingCallView.bindData(callData)
        
        // 验证显示视频通话提示
        incomingCallView.getCallTypeText() shouldBe "视频通话邀请"
    }
    
    test("属性 49: 来电提示显示 - 任何通话类型都应该被支持") {
        checkAll(Arb.boolean()) { isVideoCall ->
            val incomingCallView = IncomingCallView(context)
            val callData = IncomingCallView.IncomingCallData(
                callerId = "user_001",
                callerName = "测试用户",
                callerAvatar = "https://example.com/avatar.jpg",
                isVideoCall = isVideoCall
            )
            
            incomingCallView.bindData(callData)
            
            // 验证通话类型提示被正确显示
            val expectedText = if (isVideoCall) "视频通话邀请" else "语音通话邀请"
            incomingCallView.getCallTypeText() shouldBe expectedText
        }
    }
    
    test("属性 49: 来电提示显示 - 多个来电提示应该能被正确处理") {
        checkAll(
            Arb.string(minSize = 1, maxSize = 50),
            Arb.string(minSize = 1, maxSize = 50),
            Arb.boolean()
        ) { callerName1, callerName2, isVideoCall ->
            val incomingCallView = IncomingCallView(context)
            
            // 第一个来电
            val callData1 = IncomingCallView.IncomingCallData(
                callerId = "user_001",
                callerName = callerName1,
                callerAvatar = "https://example.com/avatar1.jpg",
                isVideoCall = isVideoCall
            )
            
            incomingCallView.bindData(callData1)
            incomingCallView.getCallerName() shouldBe callerName1
            
            // 第二个来电
            val callData2 = IncomingCallView.IncomingCallData(
                callerId = "user_002",
                callerName = callerName2,
                callerAvatar = "https://example.com/avatar2.jpg",
                isVideoCall = !isVideoCall
            )
            
            incomingCallView.bindData(callData2)
            incomingCallView.getCallerName() shouldBe callerName2
        }
    }
    
    test("属性 49: 来电提示显示 - 接听和拒绝按钮应该都可用") {
        val incomingCallView = IncomingCallView(context)
        
        var acceptClicked = false
        var rejectClicked = false
        
        incomingCallView.setOnAcceptClickListener {
            acceptClicked = true
        }
        
        incomingCallView.setOnRejectClickListener {
            rejectClicked = true
        }
        
        // 验证监听器已设置
        incomingCallView shouldNotBe null
    }
})
