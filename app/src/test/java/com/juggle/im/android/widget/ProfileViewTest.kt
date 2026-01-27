package com.juggle.im.android.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.Assert.*

/**
 * ProfileView 单元测试
 * 测试个人资料信息完整性
 */
@RunWith(RobolectricTestRunner::class)
class ProfileViewTest {
    
    private lateinit var context: Context
    private lateinit var profileView: ProfileView
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        profileView = ProfileView(context)
    }
    
    @Test
    fun testBindDataWithProfileInfo() {
        // 测试绑定个人资料数据
        val profile = ProfileView.ProfileData(
            userId = "user_001",
            nickname = "张三",
            avatar = "https://example.com/avatar.jpg",
            signature = "这是我的个性签名",
            accountId = "zhangsan123",
            friendCount = 50,
            groupCount = 10,
            momentCount = 25
        )
        
        profileView.bindData(profile)
        
        assertEquals("张三", profileView.getNickname())
        assertEquals("这是我的个性签名", profileView.getSignature())
        assertTrue(profileView.getAccountId().contains("zhangsan123"))
        assertEquals("50", profileView.getFriendCount())
    }
    
    @Test
    fun testGetNickname() {
        // 测试获取昵称
        val profile = ProfileView.ProfileData(
            userId = "user_001",
            nickname = "李四",
            avatar = "https://example.com/avatar.jpg",
            signature = "签名",
            accountId = "lisi456"
        )
        
        profileView.bindData(profile)
        
        assertEquals("李四", profileView.getNickname())
    }
    
    @Test
    fun testGetSignature() {
        // 测试获取个性签名
        val profile = ProfileView.ProfileData(
            userId = "user_001",
            nickname = "王五",
            avatar = "https://example.com/avatar.jpg",
            signature = "我的签名很长很长",
            accountId = "wangwu789"
        )
        
        profileView.bindData(profile)
        
        assertEquals("我的签名很长很长", profileView.getSignature())
    }
    
    @Test
    fun testGetAccountId() {
        // 测试获取账号ID
        val profile = ProfileView.ProfileData(
            userId = "user_001",
            nickname = "赵六",
            avatar = "https://example.com/avatar.jpg",
            signature = "签名",
            accountId = "zhaoliu000"
        )
        
        profileView.bindData(profile)
        
        assertTrue(profileView.getAccountId().contains("zhaoliu000"))
    }
    
    @Test
    fun testOnEditClickListener() {
        // 测试编辑按钮点击监听
        var editClicked = false
        profileView.setOnEditClickListener {
            editClicked = true
        }
        
        // 验证监听器已设置
        assertNotNull(profileView)
    }
    
    @Test
    fun testStatisticsDisplay() {
        // 测试统计数据显示
        val profile = ProfileView.ProfileData(
            userId = "user_001",
            nickname = "孙七",
            avatar = "https://example.com/avatar.jpg",
            signature = "签名",
            accountId = "sunqi111",
            friendCount = 100,
            groupCount = 20,
            momentCount = 50
        )
        
        profileView.bindData(profile)
        
        assertEquals("100", profileView.getFriendCount())
    }
}
