package com.juggle.im.android.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * 其他用户资料页面属性测试
 * 属性 39: 其他用户资料操作按钮
 * 验证需求 8.6
 */
@RunWith(RobolectricTestRunner::class)
class OtherUserProfileActivityTest : FunSpec({
    
    val context: Context = ApplicationProvider.getApplicationContext()
    
    test("属性 39: 其他用户资料操作按钮 - 应该显示添加朋友按钮") {
        val activity = OtherUserProfileActivity()
        
        val addFriendButton = activity.getAddFriendButton()
        
        // 验证添加朋友按钮存在
        addFriendButton shouldNotBe null
    }
    
    test("属性 39: 其他用户资料操作按钮 - 应该显示发送消息按钮") {
        val activity = OtherUserProfileActivity()
        
        val sendMessageButton = activity.getSendMessageButton()
        
        // 验证发送消息按钮存在
        sendMessageButton shouldNotBe null
    }
    
    test("属性 39: 其他用户资料操作按钮 - 按钮应该可以被点击") {
        val activity = OtherUserProfileActivity()
        
        val addFriendButton = activity.getAddFriendButton()
        val sendMessageButton = activity.getSendMessageButton()
        
        // 验证按钮可以被点击
        addFriendButton.isEnabled shouldBe true
        sendMessageButton.isEnabled shouldBe true
    }
    
    test("属性 39: 其他用户资料操作按钮 - 按钮应该有正确的文本") {
        val activity = OtherUserProfileActivity()
        
        val addFriendButton = activity.getAddFriendButton()
        val sendMessageButton = activity.getSendMessageButton()
        
        // 验证按钮文本
        addFriendButton.text.toString() shouldBe "添加朋友"
        sendMessageButton.text.toString() shouldBe "发送消息"
    }
})
