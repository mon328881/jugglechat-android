package com.juggle.im.android.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.forAll
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * 群组列表属性测试
 * 
 * **验证: 需求 9.1**
 */
@RunWith(RobolectricTestRunner::class)
class GroupListPropertyTest : FunSpec({
    
    val context: Context = ApplicationProvider.getApplicationContext()
    
    test("属性 40: 群组列表完整性 - 群组项应包含所有必需字段") {
        forAll(
            Arb.string(minSize = 1, maxSize = 50),
            Arb.string(minSize = 1, maxSize = 100),
            Arb.int(1..1000),
            Arb.string(minSize = 0, maxSize = 200),
            Arb.string(minSize = 0, maxSize = 50)
        ) { id, name, memberCount, lastMessage, lastMessageTime ->
            val groupItem = GroupListItemView.GroupItem(
                id = id,
                name = name,
                avatar = "https://example.com/avatar.jpg",
                memberCount = memberCount,
                lastMessage = lastMessage,
                lastMessageTime = lastMessageTime
            )
            
            // 验证所有字段都被正确设置
            groupItem.id shouldBe id
            groupItem.name shouldBe name
            groupItem.memberCount shouldBe memberCount
            groupItem.lastMessage shouldBe lastMessage
            groupItem.lastMessageTime shouldBe lastMessageTime
            
            true
        }
    }
    
    test("属性 40: 群组列表完整性 - 群组项绑定后应显示正确的数据") {
        forAll(
            Arb.string(minSize = 1, maxSize = 50),
            Arb.int(1..1000)
        ) { name, memberCount ->
            val groupListItemView = GroupListItemView(context)
            val groupItem = GroupListItemView.GroupItem(
                id = "group_test",
                name = name,
                avatar = "https://example.com/avatar.jpg",
                memberCount = memberCount,
                lastMessage = "测试消息",
                lastMessageTime = "14:30"
            )
            
            groupListItemView.bindData(groupItem)
            
            // 验证绑定后的数据正确
            groupListItemView.getGroupName() shouldBe name
            groupListItemView.getMemberCount() shouldBe "成员数：$memberCount"
            groupListItemView.getLastMessage() shouldBe "测试消息"
            groupListItemView.getLastMessageTime() shouldBe "14:30"
            
            true
        }
    }
    
    test("属性 40: 群组列表完整性 - 群组成员数应为正整数") {
        forAll(Arb.int(1..10000)) { memberCount ->
            val groupItem = GroupListItemView.GroupItem(
                id = "group_test",
                name = "测试群组",
                avatar = "https://example.com/avatar.jpg",
                memberCount = memberCount,
                lastMessage = "测试消息",
                lastMessageTime = "14:30"
            )
            
            // 验证成员数为正整数
            groupItem.memberCount shouldBe memberCount
            groupItem.memberCount > 0 shouldBe true
            
            true
        }
    }
    
    test("属性 40: 群组列表完整性 - 群组名称不应为空") {
        forAll(Arb.string(minSize = 1, maxSize = 100)) { name ->
            val groupItem = GroupListItemView.GroupItem(
                id = "group_test",
                name = name,
                avatar = "https://example.com/avatar.jpg",
                memberCount = 5,
                lastMessage = "测试消息",
                lastMessageTime = "14:30"
            )
            
            // 验证群组名称不为空
            groupItem.name.isNotEmpty() shouldBe true
            groupItem.name shouldNotBe ""
            
            true
        }
    }
    
    test("属性 40: 群组列表完整性 - 多个群组项应独立存在") {
        forAll(
            Arb.string(minSize = 1, maxSize = 50),
            Arb.string(minSize = 1, maxSize = 50),
            Arb.int(1..100),
            Arb.int(1..100)
        ) { name1, name2, count1, count2 ->
            val group1 = GroupListItemView.GroupItem(
                id = "group1",
                name = name1,
                avatar = "https://example.com/avatar1.jpg",
                memberCount = count1,
                lastMessage = "消息1",
                lastMessageTime = "14:30"
            )
            
            val group2 = GroupListItemView.GroupItem(
                id = "group2",
                name = name2,
                avatar = "https://example.com/avatar2.jpg",
                memberCount = count2,
                lastMessage = "消息2",
                lastMessageTime = "15:00"
            )
            
            // 验证两个群组项独立存在
            group1.id shouldNotBe group2.id
            group1.name shouldBe name1
            group2.name shouldBe name2
            group1.memberCount shouldBe count1
            group2.memberCount shouldBe count2
            
            true
        }
    }
})
