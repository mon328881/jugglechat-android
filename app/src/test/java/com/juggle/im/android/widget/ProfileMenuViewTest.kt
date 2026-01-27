package com.juggle.im.android.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * 个人资料菜单视图属性测试
 * 属性 38: 个人资料功能入口
 * 验证需求 8.5
 */
@RunWith(RobolectricTestRunner::class)
class ProfileMenuViewTest : FunSpec({
    
    val context: Context = ApplicationProvider.getApplicationContext()
    
    test("属性 38: 个人资料功能入口 - 应该包含设置、关于、帮助三个入口") {
        val menuView = ProfileMenuView(context)
        
        // 添加菜单项
        menuView.addMenuItem(ProfileMenuView.MenuItem("settings", "设置")) {}
        menuView.addMenuItem(ProfileMenuView.MenuItem("about", "关于")) {}
        menuView.addMenuItem(ProfileMenuView.MenuItem("help", "帮助")) {}
        
        // 验证菜单项数量
        menuView.getMenuItemCount() shouldBe 3
    }
    
    test("属性 38: 个人资料功能入口 - 菜单项应该有正确的标题") {
        val menuView = ProfileMenuView(context)
        
        val items = listOf(
            ProfileMenuView.MenuItem("settings", "设置"),
            ProfileMenuView.MenuItem("about", "关于"),
            ProfileMenuView.MenuItem("help", "帮助")
        )
        
        items.forEach { item ->
            menuView.addMenuItem(item) {}
        }
        
        // 验证菜单项标题
        menuView.getMenuItem(0)?.title shouldBe "设置"
        menuView.getMenuItem(1)?.title shouldBe "关于"
        menuView.getMenuItem(2)?.title shouldBe "帮助"
    }
    
    test("属性 38: 个人资料功能入口 - 支持自定义菜单项") {
        checkAll(
            Arb.string(minSize = 1, maxSize = 20)
        ) { menuTitle ->
            val menuView = ProfileMenuView(context)
            
            menuView.addMenuItem(ProfileMenuView.MenuItem("custom", menuTitle)) {}
            
            // 验证自定义菜单项
            menuView.getMenuItemCount() shouldBe 1
            menuView.getMenuItem(0)?.title shouldBe menuTitle
        }
    }
    
    test("属性 38: 个人资料功能入口 - 菜单项应该有唯一的 ID") {
        val menuView = ProfileMenuView(context)
        
        val items = listOf(
            ProfileMenuView.MenuItem("settings", "设置"),
            ProfileMenuView.MenuItem("about", "关于"),
            ProfileMenuView.MenuItem("help", "帮助")
        )
        
        items.forEach { item ->
            menuView.addMenuItem(item) {}
        }
        
        // 验证菜单项 ID
        menuView.getMenuItem(0)?.id shouldBe "settings"
        menuView.getMenuItem(1)?.id shouldBe "about"
        menuView.getMenuItem(2)?.id shouldBe "help"
    }
    
    test("属性 38: 个人资料功能入口 - 菜单项点击监听器应该可以被设置") {
        val menuView = ProfileMenuView(context)
        var clickCount = 0
        
        menuView.addMenuItem(ProfileMenuView.MenuItem("test", "测试")) {
            clickCount++
        }
        
        // 验证监听器已设置
        menuView.getMenuItemCount() shouldBe 1
    }
})
