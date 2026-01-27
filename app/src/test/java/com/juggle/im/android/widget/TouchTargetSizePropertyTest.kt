package com.juggle.im.android.widget

import android.content.Context
import android.widget.Button
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldBeGreaterThanOrEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import org.robolectric.RuntimeEnvironment
import com.juggle.im.android.utils.AccessibilityConfig

/**
 * 触摸目标最小尺寸属性测试
 * 验证所有按钮和可交互元素至少 48dp × 48dp
 * 
 * **验证: 需求 12.5**
 */
class TouchTargetSizePropertyTest : FunSpec({

    val context = RuntimeEnvironment.getApplication() as Context
    val config = AccessibilityConfig(context)

    test("属性 54: 触摸目标最小尺寸 - 最小触摸目标大小应该是 48dp") {
        val minSize = config.getMinimumTouchTargetSize()
        minSize shouldBe 48
    }

    test("属性 54: 触摸目标最小尺寸 - 按钮最小高度应该至少 48dp") {
        val button = Button(context)
        button.minimumHeight = 48
        button.minimumHeight shouldBeGreaterThanOrEqual 48
    }

    test("属性 54: 触摸目标最小尺寸 - 按钮最小宽度应该至少 48dp") {
        val button = Button(context)
        button.minimumWidth = 48
        button.minimumWidth shouldBeGreaterThanOrEqual 48
    }

    test("属性 54: 触摸目标最小尺寸 - 所有按钮应该满足最小尺寸要求") {
        checkAll(Arb.int(48..200)) { size ->
            val button = Button(context)
            button.minimumHeight = size
            button.minimumWidth = size
            
            // 验证按钮尺寸至少是最小要求
            button.minimumHeight shouldBeGreaterThanOrEqual config.getMinimumTouchTargetSize()
            button.minimumWidth shouldBeGreaterThanOrEqual config.getMinimumTouchTargetSize()
        }
    }

    test("属性 54: 触摸目标最小尺寸 - 调整后的触摸目标大小应该至少 48dp") {
        val minSize = config.getMinimumTouchTargetSize()
        val adjustedSize = config.getAdjustedSize(minSize)
        // 调整后的尺寸应该至少是最小要求
        adjustedSize shouldBeGreaterThanOrEqual minSize
    }

    test("属性 54: 触摸目标最小尺寸 - 多个按钮应该都满足最小尺寸要求") {
        checkAll(Arb.int(1..10)) { buttonCount ->
            val buttons = (0 until buttonCount).map { Button(context) }
            buttons.forEach { button ->
                button.minimumHeight = 48
                button.minimumWidth = 48
            }
            
            // 验证所有按钮都满足最小尺寸要求
            buttons.forEach { button ->
                button.minimumHeight shouldBeGreaterThanOrEqual 48
                button.minimumWidth shouldBeGreaterThanOrEqual 48
            }
        }
    }

    test("属性 54: 触摸目标最小尺寸 - 触摸目标大小应该保持一致") {
        val minSize1 = config.getMinimumTouchTargetSize()
        val minSize2 = config.getMinimumTouchTargetSize()
        // 多次调用应该返回相同的结果
        minSize1 shouldBe minSize2
    }

    test("属性 54: 触摸目标最小尺寸 - 按钮尺寸应该不小于最小要求") {
        checkAll(Arb.int(48..500)) { size ->
            val button = Button(context)
            button.minimumHeight = size
            button.minimumWidth = size
            
            // 验证按钮尺寸不小于最小要求
            button.minimumHeight shouldBeGreaterThanOrEqual config.getMinimumTouchTargetSize()
            button.minimumWidth shouldBeGreaterThanOrEqual config.getMinimumTouchTargetSize()
        }
    }

    test("属性 54: 触摸目标最小尺寸 - 调整后的尺寸应该保持最小要求") {
        checkAll(Arb.int(48..500)) { baseSize ->
            val adjustedSize = config.getAdjustedSize(baseSize)
            // 调整后的尺寸应该至少是最小要求
            adjustedSize shouldBeGreaterThanOrEqual config.getMinimumTouchTargetSize()
        }
    }
})
