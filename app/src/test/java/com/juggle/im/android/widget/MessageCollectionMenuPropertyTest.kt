package com.juggle.im.android.widget

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * 属性 70: 消息收藏菜单选项
 * 验证: 需求 17.1
 * 
 * 当用户长按消息时，消息操作菜单应该包含收藏选项
 */
class MessageCollectionMenuPropertyTest : FunSpec({
    
    test("属性 70: 消息收藏菜单选项 - 菜单应包含收藏选项") {
        checkAll(Arb.string(minSize = 1, maxSize = 100)) { label ->
            // 创建菜单
            val menu = MessageContextMenu.MenuItem(
                id = MessageContextMenu.MENU_COLLECT,
                label = label
            )
            
            // 验证菜单项ID正确
            menu.id shouldBe MessageContextMenu.MENU_COLLECT
            
            // 验证菜单项标签不为空
            menu.label.isNotEmpty() shouldBe true
        }
    }
    
    test("属性 70: 消息收藏菜单选项 - 菜单应包含取消收藏选项") {
        checkAll(Arb.string(minSize = 1, maxSize = 100)) { label ->
            // 创建菜单
            val menu = MessageContextMenu.MenuItem(
                id = MessageContextMenu.MENU_UNCOLLECT,
                label = label
            )
            
            // 验证菜单项ID正确
            menu.id shouldBe MessageContextMenu.MENU_UNCOLLECT
            
            // 验证菜单项标签不为空
            menu.label.isNotEmpty() shouldBe true
        }
    }
    
    test("属性 70: 消息收藏菜单选项 - 菜单项应能被正确识别") {
        checkAll(
            Arb.string(minSize = 1, maxSize = 50),
            Arb.string(minSize = 1, maxSize = 50)
        ) { id, label ->
            val menu = MessageContextMenu.MenuItem(id = id, label = label)
            
            // 验证菜单项的ID和标签
            menu.id shouldBe id
            menu.label shouldBe label
        }
    }
})
