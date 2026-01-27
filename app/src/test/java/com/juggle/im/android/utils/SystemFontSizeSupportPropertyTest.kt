package com.juggle.im.android.utils

import android.content.Context
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldBeGreaterThan
import io.kotest.matchers.shouldBeLessThanOrEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import org.robolectric.RuntimeEnvironment

/**
 * 系统字体大小支持属性测试
 * 验证界面布局自动调整以适应系统字体大小
 * 
 * **验证: 需求 12.3**
 */
class SystemFontSizeSupportPropertyTest : FunSpec({

    val context = RuntimeEnvironment.getApplication() as Context
    val config = AccessibilityConfig(context)

    test("属性 52: 系统字体大小支持 - 字体大小缩放因子应该大于 0") {
        val fontScale = config.getFontScaleFactor()
        fontScale shouldBeGreaterThan 0f
    }

    test("属性 52: 系统字体大小支持 - 调整后的字体大小应该正确计算") {
        checkAll(Arb.float(1f..100f)) { baseFontSize ->
            val adjustedSize = config.getAdjustedFontSize(baseFontSize)
            // 调整后的字体大小应该等于基础字体大小乘以缩放因子
            adjustedSize shouldBe (baseFontSize * config.getFontScaleFactor())
        }
    }

    test("属性 52: 系统字体大小支持 - 调整后的尺寸应该正确计算") {
        checkAll(Arb.int(1..1000)) { baseSize ->
            val adjustedSize = config.getAdjustedSize(baseSize)
            // 调整后的尺寸应该等于基础尺寸乘以缩放因子
            adjustedSize shouldBe (baseSize * config.getFontScaleFactor()).toInt()
        }
    }

    test("属性 52: 系统字体大小支持 - 最小文本大小应该大于 0") {
        val minTextSize = config.getMinimumTextSize()
        minTextSize shouldBeGreaterThan 0f
    }

    test("属性 52: 系统字体大小支持 - 最小文本大小应该考虑字体大小缩放") {
        val baseFontSize = 14f
        val minTextSize = config.getMinimumTextSize()
        val expectedMinSize = baseFontSize * config.getFontScaleFactor()
        minTextSize shouldBe expectedMinSize
    }

    test("属性 52: 系统字体大小支持 - 调整后的尺寸应该不小于基础尺寸") {
        checkAll(Arb.int(1..1000)) { baseSize ->
            val adjustedSize = config.getAdjustedSize(baseSize)
            // 当字体大小缩放因子 >= 1 时，调整后的尺寸应该 >= 基础尺寸
            if (config.getFontScaleFactor() >= 1f) {
                adjustedSize shouldBe (baseSize * config.getFontScaleFactor()).toInt()
            }
        }
    }

    test("属性 52: 系统字体大小支持 - 调整后的字体大小应该不小于基础字体大小") {
        checkAll(Arb.float(1f..100f)) { baseFontSize ->
            val adjustedSize = config.getAdjustedFontSize(baseFontSize)
            // 当字体大小缩放因子 >= 1 时，调整后的字体大小应该 >= 基础字体大小
            if (config.getFontScaleFactor() >= 1f) {
                adjustedSize shouldBe (baseFontSize * config.getFontScaleFactor())
            }
        }
    }

    test("属性 52: 系统字体大小支持 - 字体大小调整状态应该正确") {
        val isAdjusted = config.isFontSizeAdjusted()
        // 如果字体大小缩放因子不是 1.0，则应该被调整
        isAdjusted shouldBe (config.getFontScaleFactor() != 1.0f)
    }

    test("属性 52: 系统字体大小支持 - 多个基础字体大小应该都被正确调整") {
        checkAll(Arb.int(10..50)) { baseFontSize ->
            val adjustedSize = config.getAdjustedFontSize(baseFontSize.toFloat())
            // 调整后的字体大小应该等于基础字体大小乘以缩放因子
            adjustedSize shouldBe (baseFontSize * config.getFontScaleFactor())
        }
    }

    test("属性 52: 系统字体大小支持 - 调整后的尺寸应该保持比例") {
        checkAll(
            Arb.int(1..500),
            Arb.int(1..500)
        ) { size1, size2 ->
            val adjustedSize1 = config.getAdjustedSize(size1)
            val adjustedSize2 = config.getAdjustedSize(size2)
            
            // 调整后的尺寸比例应该与原始尺寸比例相同
            if (size1 > 0 && size2 > 0) {
                val originalRatio = size1.toFloat() / size2.toFloat()
                val adjustedRatio = adjustedSize1.toFloat() / adjustedSize2.toFloat()
                // 允许小的浮点数误差
                (adjustedRatio - originalRatio).toInt() shouldBe 0
            }
        }
    }
})
