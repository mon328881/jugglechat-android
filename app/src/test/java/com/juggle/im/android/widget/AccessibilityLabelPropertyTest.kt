package com.juggle.im.android.widget

import android.content.Context
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.robolectric.RuntimeEnvironment
import com.juggle.im.android.utils.AccessibilityUtils

/**
 * 无障碍标签完整性属性测试
 * 验证所有交互元素都有清晰的无障碍标签
 * 
 * **验证: 需求 12.1**
 */
class AccessibilityLabelPropertyTest : FunSpec({

    val context = RuntimeEnvironment.getApplication() as Context

    test("属性 50: 无障碍标签完整性 - 按钮应该有无障碍标签") {
        checkAll(Arb.string(minSize = 1, maxSize = 50)) { label ->
            val button = Button(context)
            AccessibilityUtils.setButtonAccessibilityLabel(button, label)
            
            // 验证按钮有无障碍标签
            AccessibilityUtils.hasAccessibilityLabel(button) shouldBe true
            // 验证标签内容正确
            AccessibilityUtils.getAccessibilityLabel(button) shouldBe label
        }
    }

    test("属性 50: 无障碍标签完整性 - 图片应该有无障碍描述") {
        checkAll(Arb.string(minSize = 1, maxSize = 50)) { description ->
            val imageView = ImageView(context)
            AccessibilityUtils.setImageAccessibilityDescription(imageView, description)
            
            // 验证图片有无障碍标签
            AccessibilityUtils.hasAccessibilityLabel(imageView) shouldBe true
            // 验证描述内容正确
            AccessibilityUtils.getAccessibilityLabel(imageView) shouldBe description
        }
    }

    test("属性 50: 无障碍标签完整性 - 文本视图应该有无障碍标签") {
        checkAll(Arb.string(minSize = 1, maxSize = 50)) { label ->
            val textView = TextView(context)
            AccessibilityUtils.setTextViewAccessibilityLabel(textView, label)
            
            // 验证文本视图有无障碍标签
            AccessibilityUtils.hasAccessibilityLabel(textView) shouldBe true
            // 验证标签内容正确
            AccessibilityUtils.getAccessibilityLabel(textView) shouldBe label
        }
    }

    test("属性 50: 无障碍标签完整性 - 列表项应该有完整的无障碍标签") {
        checkAll(
            Arb.string(minSize = 1, maxSize = 30),
            Arb.int(0..99),
            Arb.int(1..100)
        ) { itemLabel, position, totalCount ->
            val button = Button(context)
            AccessibilityUtils.setListItemAccessibilityLabel(button, itemLabel, position, totalCount)
            
            // 验证列表项有无障碍标签
            AccessibilityUtils.hasAccessibilityLabel(button) shouldBe true
            // 验证标签包含位置信息
            val label = AccessibilityUtils.getAccessibilityLabel(button)
            label.contains(itemLabel) shouldBe true
            label.contains("${position + 1}") shouldBe true
            label.contains("$totalCount") shouldBe true
        }
    }

    test("属性 50: 无障碍标签完整性 - 状态指示器应该有无障碍标签") {
        checkAll(Arb.string(minSize = 1, maxSize = 30)) { status ->
            val textView = TextView(context)
            AccessibilityUtils.setStatusAccessibilityLabel(textView, status)
            
            // 验证状态指示器有无障碍标签
            AccessibilityUtils.hasAccessibilityLabel(textView) shouldBe true
            // 验证状态标签内容正确
            AccessibilityUtils.getAccessibilityLabel(textView) shouldBe status
        }
    }

    test("属性 50: 无障碍标签完整性 - 输入框应该有无障碍标签") {
        checkAll(
            Arb.string(minSize = 1, maxSize = 30),
            Arb.string(minSize = 1, maxSize = 30)
        ) { label, hint ->
            val textView = TextView(context)
            AccessibilityUtils.setInputAccessibilityLabel(textView, label, hint)
            
            // 验证输入框有无障碍标签
            AccessibilityUtils.hasAccessibilityLabel(textView) shouldBe true
            // 验证标签内容正确
            AccessibilityUtils.getAccessibilityLabel(textView) shouldBe label
        }
    }

    test("属性 50: 无障碍标签完整性 - 图标按钮应该有无障碍标签") {
        checkAll(Arb.string(minSize = 1, maxSize = 30)) { label ->
            val button = Button(context)
            AccessibilityUtils.setIconButtonAccessibilityLabel(button, label)
            
            // 验证图标按钮有无障碍标签
            AccessibilityUtils.hasAccessibilityLabel(button) shouldBe true
            // 验证标签内容正确
            AccessibilityUtils.getAccessibilityLabel(button) shouldBe label
        }
    }

    test("属性 50: 无障碍标签完整性 - 图标按钮带操作描述应该有完整的无障碍标签") {
        checkAll(
            Arb.string(minSize = 1, maxSize = 20),
            Arb.string(minSize = 1, maxSize = 20)
        ) { label, action ->
            val button = Button(context)
            AccessibilityUtils.setIconButtonAccessibilityLabel(button, label, action)
            
            // 验证图标按钮有无障碍标签
            AccessibilityUtils.hasAccessibilityLabel(button) shouldBe true
            // 验证标签包含标签和操作描述
            val fullLabel = AccessibilityUtils.getAccessibilityLabel(button)
            fullLabel.contains(label) shouldBe true
            fullLabel.contains(action) shouldBe true
        }
    }

    test("属性 50: 无障碍标签完整性 - 所有交互元素都应该有无障碍标签") {
        checkAll(Arb.string(minSize = 1, maxSize = 50)) { label ->
            val button = Button(context)
            val imageView = ImageView(context)
            val textView = TextView(context)
            
            // 为所有元素设置无障碍标签
            AccessibilityUtils.setButtonAccessibilityLabel(button, label)
            AccessibilityUtils.setImageAccessibilityDescription(imageView, label)
            AccessibilityUtils.setTextViewAccessibilityLabel(textView, label)
            
            // 验证所有元素都有无障碍标签
            AccessibilityUtils.hasAccessibilityLabel(button) shouldBe true
            AccessibilityUtils.hasAccessibilityLabel(imageView) shouldBe true
            AccessibilityUtils.hasAccessibilityLabel(textView) shouldBe true
        }
    }

    test("属性 50: 无障碍标签完整性 - 无障碍标签不应该为空") {
        checkAll(Arb.string(minSize = 1, maxSize = 50)) { label ->
            val button = Button(context)
            AccessibilityUtils.setButtonAccessibilityLabel(button, label)
            
            // 验证标签不为空
            AccessibilityUtils.getAccessibilityLabel(button).isNotEmpty() shouldBe true
            // 验证标签与设置的标签一致
            AccessibilityUtils.getAccessibilityLabel(button) shouldNotBe ""
        }
    }
})
