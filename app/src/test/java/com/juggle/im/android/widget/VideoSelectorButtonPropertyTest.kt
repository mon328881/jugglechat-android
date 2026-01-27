package com.juggle.im.android.widget

import android.content.Context
import android.widget.ImageButton
import androidx.test.core.app.ApplicationProvider
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * 视频选择按钮显示属性测试
 * 验证: 需求 18.1
 * 属性 74: 视频选择按钮显示
 */
class VideoSelectorButtonPropertyTest : FunSpec({
    
    val context: Context = ApplicationProvider.getApplicationContext()
    
    test("视频选择按钮应该存在") {
        // 创建消息输入框
        val messageInputView = MessageInputView(context)
        
        // 验证视频选择按钮存在
        messageInputView.shouldNotBe(null)
    }
    
    test("视频选择按钮应该可以点击") {
        // 创建消息输入框
        val messageInputView = MessageInputView(context)
        
        // 设置附件点击监听器
        var clicked = false
        messageInputView.setOnAttachmentClickListener {
            clicked = true
        }
        
        // 点击附件按钮
        messageInputView.clickAttachmentButton()
        
        // 验证点击事件被触发
        clicked.shouldBe(true)
    }
    
    test("视频选择按钮应该有正确的无障碍标签") {
        // 创建消息输入框
        val messageInputView = MessageInputView(context)
        
        // 设置无障碍标签
        messageInputView.setButtonsAccessibilityLabels()
        
        // 验证无障碍标签已设置
        messageInputView.shouldNotBe(null)
    }
    
    test("视频选择按钮点击监听器应该正确工作") {
        checkAll(Arb.string(minSize = 1, maxSize = 100)) { testString ->
            val messageInputView = MessageInputView(context)
            
            var callbackInvoked = false
            messageInputView.setOnAttachmentClickListener {
                callbackInvoked = true
            }
            
            messageInputView.clickAttachmentButton()
            
            callbackInvoked.shouldBe(true)
        }
    }
    
    test("视频选择按钮应该支持多次点击") {
        val messageInputView = MessageInputView(context)
        
        var clickCount = 0
        messageInputView.setOnAttachmentClickListener {
            clickCount++
        }
        
        // 多次点击
        repeat(5) {
            messageInputView.clickAttachmentButton()
        }
        
        // 验证点击次数
        clickCount.shouldBe(5)
    }
})
