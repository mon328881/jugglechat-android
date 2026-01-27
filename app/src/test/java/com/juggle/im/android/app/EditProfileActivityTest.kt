package com.juggle.im.android.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * 编辑个人资料页面属性测试
 * 属性 37: 保存按钮状态管理
 * 验证需求 8.4
 */
@RunWith(RobolectricTestRunner::class)
class EditProfileActivityTest : FunSpec({
    
    val context: Context = ApplicationProvider.getApplicationContext()
    
    test("属性 37: 保存按钮状态管理 - 初始状态应该禁用") {
        val activity = EditProfileActivity()
        
        // 验证初始状态保存按钮禁用
        activity.isSaveButtonEnabled() shouldBe false
    }
    
    test("属性 37: 保存按钮状态管理 - 有改动时启用") {
        checkAll(
            Arb.string(minSize = 1, maxSize = 50)
        ) { newNickname ->
            val activity = EditProfileActivity()
            
            // 设置初始值
            activity.setNickname("原始昵称")
            activity.setSignature("原始签名")
            activity.setGender("男")
            
            // 修改昵称
            activity.setNickname(newNickname)
            
            // 验证保存按钮启用
            if (newNickname != "原始昵称") {
                activity.isSaveButtonEnabled() shouldBe true
            }
        }
    }
    
    test("属性 37: 保存按钮状态管理 - 修改个性签名时启用") {
        checkAll(
            Arb.string(minSize = 1, maxSize = 100)
        ) { newSignature ->
            val activity = EditProfileActivity()
            
            // 设置初始值
            activity.setNickname("原始昵称")
            activity.setSignature("原始签名")
            activity.setGender("男")
            
            // 修改个性签名
            activity.setSignature(newSignature)
            
            // 验证保存按钮启用
            if (newSignature != "原始签名") {
                activity.isSaveButtonEnabled() shouldBe true
            }
        }
    }
    
    test("属性 37: 保存按钮状态管理 - 修改性别时启用") {
        checkAll(
            Arb.string(minSize = 1, maxSize = 10)
        ) { newGender ->
            val activity = EditProfileActivity()
            
            // 设置初始值
            activity.setNickname("原始昵称")
            activity.setSignature("原始签名")
            activity.setGender("男")
            
            // 修改性别
            activity.setGender(newGender)
            
            // 验证保存按钮启用
            if (newGender != "男") {
                activity.isSaveButtonEnabled() shouldBe true
            }
        }
    }
    
    test("属性 37: 保存按钮状态管理 - 恢复原始值时禁用") {
        val activity = EditProfileActivity()
        
        // 设置初始值
        activity.setNickname("原始昵称")
        activity.setSignature("原始签名")
        activity.setGender("男")
        
        // 修改后恢复
        activity.setNickname("新昵称")
        activity.setNickname("原始昵称")
        
        // 验证保存按钮禁用
        activity.isSaveButtonEnabled() shouldBe false
    }
})
