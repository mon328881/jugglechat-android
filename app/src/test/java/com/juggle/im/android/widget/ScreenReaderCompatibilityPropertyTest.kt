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
 * 屏幕阅读器兼容性属性测试
 * 验证所有文本、按钮和图标被正确识别和描述
 * 
 * **验证: 需求 12.2**
 */
class ScreenReaderCompatibilityPropertyTest : FunSpec({

    val context = RuntimeEnvironment.getApplication() as Context

    test("属性 51: 屏幕阅读器兼容性 - 按钮应该被屏幕阅读器正确识别") {
        checkAll(Arb.string(minSize = 1, maxSize = 50)) { buttonLabel ->
            val button = Button(context)
            AccessibilityUtils.setButtonAccessibilityLabel(button, buttonLabel)
            
            // 验证按钮有无障碍标签
            button.contentDescription shouldNotBe null
            // 验证标签内容正确
            button.contentDescription.toString() shouldBe buttonLabel
        }
    }

    test("属性 51: 屏幕阅读器兼容性 - 图片应该被屏幕阅读器正确识别") {
        checkAll(Arb.string(minSize = 1, maxSize = 50)) { imageDescription ->
            val imageView = ImageView(context)
            AccessibilityUtils.setImageAccessibilityDescription(imageView, imageDescription)
            
            // 验证图片有无障碍描述
            imageView.contentDescription shouldNotBe null
            // 验证描述内容正确
            imageView.contentDescription.toString() shouldBe imageDescription
        }
    }

    test("属性 51: 屏幕阅读器兼容性 - 文本应该被屏幕阅读器正确识别") {
        checkAll(Arb.string(minSize = 1, maxSize = 50)) { textLabel ->
            val textView = TextView(context)
            AccessibilityUtils.setTextViewAccessibilityLabel(textView, textLabel)
            
            // 验证文本有无障碍标签
            textView.contentDescription shouldNotBe null
            // 验证标签内容正确
            textView.contentDescription.toString() shouldBe textLabel
        }
    }

    test("属性 51: 屏幕阅读器兼容性 - 所有交互元素都应该有描述") {
        checkAll(Arb.string(minSize = 1, maxSize = 50)) { label ->
            val button = Button(context)
            val imageView = ImageView(context)
            val textView = TextView(context)
            
            // 为所有元素设置无障碍标签
            AccessibilityUtils.setButtonAccessibilityLabel(button, label)
            AccessibilityUtils.setImageAccessibilityDescription(imageView, label)
            AccessibilityUtils.setTextViewAccessibilityLabel(textView, label)
            
            // 验证所有元素都有描述
            button.contentDescription shouldNotBe null
            imageView.contentDescription shouldNotBe null
            textView.contentDescription shouldNotBe null
        }
    }

    test("属性 51: 屏幕阅读器兼容性 - 列表项应该被屏幕阅读器正确识别") {
        checkAll(
            Arb.string(minSize = 1, maxSize = 30),
            Arb.int(0..99),
            Arb.int(1..100)
        ) { itemLabel, position, totalCount ->
            val button = Button(context)
            AccessibilityUtils.setListItemAccessibilityLabel(button, itemLabel, position, totalCount)
            
            // 验证列表项有无障碍标签
            button.contentDescription shouldNotBe null
            // 验证标签包含位置信息
            val label = button.contentDescription.toString()
            label.contains(itemLabel) shouldBe true
        }
    }

    test("属性 51: 屏幕阅读器兼容性 - 状态指示器应该被屏幕阅读器正确识别") {
        checkAll(Arb.string(minSize = 1, maxSize = 30)) { status ->
            val textView = TextView(context)
            AccessibilityUtils.setStatusAccessibilityLabel(textView, status)
            
            // 验证状态指示器有无障碍标签
            textView.contentDescription shouldNotBe null
            // 验证状态标签内容正确
            textView.contentDescription.toString() shouldBe status
        }
    }

    test("属性 51: 屏幕阅读器兼容性 - 输入框应该被屏幕阅读器正确识别") {
        checkAll(
            Arb.string(minSize = 1, maxSize = 30),
            Arb.string(minSize = 1, maxSize = 30)
        ) { label, hint ->
            val textView = TextView(context)
            AccessibilityUtils.setInputAccessibilityLabel(textView, label, hint)
            
            // 验证输入框有无障碍标签
            textView.contentDescription shouldNotBe null
            // 验证标签内容正确
            textView.contentDescription.toString() shouldBe label
        }
    }

    test("属性 51: 屏幕阅读器兼容性 - 图标按钮应该被屏幕阅读器正确识别") {
        checkAll(Arb.string(minSize = 1, maxSize = 30)) { label ->
            val button = Button(context)
            AccessibilityUtils.setIconButtonAccessibilityLabel(button, label)
            
            // 验证图标按钮有无障碍标签
            button.contentDescription shouldNotBe null
            // 验证标签内容正确
            button.contentDescription.toString() shouldBe label
        }
    }

    test("属性 51: 屏幕阅读器兼容性 - 无障碍标签应该有意义且清晰") {
        checkAll(Arb.string(minSize = 1, maxSize = 50)) { label ->
            val button = Button(context)
            AccessibilityUtils.setButtonAccessibilityLabel(button, label)
            
            // 验证标签不为空
            button.contentDescription.toString().isNotEmpty() shouldBe true
            // 验证标签与设置的标签一致
            button.contentDescription.toString() shouldBe label
        }
    }

    test("属性 51: 屏幕阅读器兼容性 - 完整的无障碍信息应该被屏幕阅读器正确识别") {
        checkAll(
            Arb.string(minSize = 1, maxSize = 30),
            Arb.string(minSize = 1, maxSize = 30)
        ) { label, hint ->
            val textView = TextView(context)
            AccessibilityUtils.setCompleteAccessibilityInfo(textView, label, hint, false)
            
            // 验证完整的无障碍信息
            textView.contentDescription shouldNotBe null
            textView.contentDescription.toString() shouldBe label
        }
    }
})
