package com.juggle.im.android.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.juggle.im.android.widget.ProfileView
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * 个人资料页面属性测试
 * 属性 36: 个人资料信息完整性
 * 验证需求 8.1
 */
@RunWith(RobolectricTestRunner::class)
class ProfileFragmentTest : FunSpec({
    
    val context: Context = ApplicationProvider.getApplicationContext()
    
    test("属性 36: 个人资料信息完整性 - 所有必需字段都应该显示") {
        checkAll(
            Arb.string(minSize = 1, maxSize = 50),
            Arb.string(minSize = 1, maxSize = 100),
            Arb.string(minSize = 1, maxSize = 50),
            Arb.string(minSize = 1, maxSize = 100)
        ) { nickname, signature, accountId, avatar ->
            val profileView = ProfileView(context)
            
            val profileData = ProfileView.ProfileData(
                userId = "test_user",
                nickname = nickname,
                avatar = avatar,
                signature = signature,
                accountId = accountId,
                friendCount = 10,
                groupCount = 5,
                momentCount = 20
            )
            
            profileView.bindData(profileData)
            
            // 验证所有必需字段都已显示
            profileView.getNickname() shouldBe nickname
            profileView.getSignature() shouldBe signature
            profileView.getAccountId().contains(accountId) shouldBe true
            profileView.getFriendCount() shouldBe "10"
        }
    }
    
    test("属性 36: 个人资料信息完整性 - 统计数据应该正确显示") {
        checkAll(
            Arb.int(0, 1000),
            Arb.int(0, 500),
            Arb.int(0, 5000)
        ) { friendCount, groupCount, momentCount ->
            val profileView = ProfileView(context)
            
            val profileData = ProfileView.ProfileData(
                userId = "test_user",
                nickname = "测试用户",
                avatar = "https://example.com/avatar.jpg",
                signature = "测试签名",
                accountId = "test_account",
                friendCount = friendCount,
                groupCount = groupCount,
                momentCount = momentCount
            )
            
            profileView.bindData(profileData)
            
            // 验证统计数据正确显示
            profileView.getFriendCount() shouldBe friendCount.toString()
        }
    }
    
    test("属性 36: 个人资料信息完整性 - 空值处理") {
        val profileView = ProfileView(context)
        
        val profileData = ProfileView.ProfileData(
            userId = "test_user",
            nickname = "",
            avatar = "",
            signature = "",
            accountId = "",
            friendCount = 0,
            groupCount = 0,
            momentCount = 0
        )
        
        profileView.bindData(profileData)
        
        // 验证空值处理正确
        profileView.getNickname() shouldBe ""
        profileView.getSignature() shouldBe ""
        profileView.getFriendCount() shouldBe "0"
    }
})
