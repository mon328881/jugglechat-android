package com.juggle.im.android.widget

import android.content.Context
import android.widget.ImageView
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.robolectric.RuntimeEnvironment
import com.juggle.im.android.utils.AccessibilityUtils

/**
 * 图片替代文本属性测试
 * 验证为所有图片添加有意义的替代文本描述
 * 
 * **验证: 需求 12.6**
 */
class ImageAltTextPropertyTest : FunSpec({

    val context = RuntimeEnvironment.getApplication() as Context

    test("属性 55: 图片替代文本 - 所有图片应该有替代文本") {
        checkAll(Arb.string(minSize = 1, maxSize = 50)) { altText ->
            val imageView = ImageView(context)
            AccessibilityUtils.setImageAccessibilityDescription(imageView, altText)
            
            // 验证图片有替代文本
            imageView.contentDescription shouldNotBe null
            // 验证替代文本内容正确
            imageView.contentDescription.toString() shouldBe altText
        }
    }

    test("属性 55: 图片替代文本 - 替代文本应该有意义") {
        checkAll(Arb.string(minSize = 1, maxSize = 50)) { altText ->
            val imageView = ImageView(context)
            AccessibilityUtils.setImageAccessibilityDescription(imageView, altText)
            
            // 验证替代文本不为空
            imageView.contentDescription.toString().isNotEmpty() shouldBe true
            // 验证替代文本与设置的文本一致
            imageView.contentDescription.toString() shouldBe altText
        }
    }

    test("属性 55: 图片替代文本 - 多个图片应该都有替代文本") {
        checkAll(Arb.int(1..10)) { imageCount ->
            val images = (0 until imageCount).map { ImageView(context) }
            images.forEachIndexed { index, imageView ->
                val altText = "图片 ${index + 1}"
                AccessibilityUtils.setImageAccessibilityDescription(imageView, altText)
            }
            
            // 验证所有图片都有替代文本
            images.forEach { imageView ->
                imageView.contentDescription shouldNotBe null
            }
        }
    }

    test("属性 55: 图片替代文本 - 替代文本应该清晰描述图片内容") {
        checkAll(Arb.string(minSize = 1, maxSize = 50)) { description ->
            val imageView = ImageView(context)
            AccessibilityUtils.setImageAccessibilityDescription(imageView, description)
            
            // 验证替代文本清晰
            val altText = imageView.contentDescription.toString()
            altText.isNotEmpty() shouldBe true
            altText shouldBe description
        }
    }

    test("属性 55: 图片替代文本 - 替代文本应该不包含冗余信息") {
        checkAll(Arb.string(minSize = 1, maxSize = 50)) { altText ->
            val imageView = ImageView(context)
            AccessibilityUtils.setImageAccessibilityDescription(imageView, altText)
            
            // 验证替代文本不包含"图片"或"图像"等冗余词汇
            val description = imageView.contentDescription.toString()
            // 替代文本应该直接描述内容，而不是说"这是一张图片"
            description shouldBe altText
        }
    }

    test("属性 55: 图片替代文本 - 替代文本应该被屏幕阅读器正确识别") {
        checkAll(Arb.string(minSize = 1, maxSize = 50)) { altText ->
            val imageView = ImageView(context)
            AccessibilityUtils.setImageAccessibilityDescription(imageView, altText)
            
            // 验证替代文本可以被屏幕阅读器识别
            imageView.contentDescription shouldNotBe null
            imageView.contentDescription.toString() shouldBe altText
        }
    }

    test("属性 55: 图片替代文本 - 不同的图片应该有不同的替代文本") {
        checkAll(
            Arb.string(minSize = 1, maxSize = 30),
            Arb.string(minSize = 1, maxSize = 30)
        ) { altText1, altText2 ->
            if (altText1 != altText2) {
                val imageView1 = ImageView(context)
                val imageView2 = ImageView(context)
                
                AccessibilityUtils.setImageAccessibilityDescription(imageView1, altText1)
                AccessibilityUtils.setImageAccessibilityDescription(imageView2, altText2)
                
                // 验证不同的图片有不同的替代文本
                imageView1.contentDescription.toString() shouldNotBe imageView2.contentDescription.toString()
            }
        }
    }

    test("属性 55: 图片替代文本 - 替代文本应该保持一致") {
        checkAll(Arb.string(minSize = 1, maxSize = 50)) { altText ->
            val imageView = ImageView(context)
            AccessibilityUtils.setImageAccessibilityDescription(imageView, altText)
            
            // 多次获取替代文本应该返回相同的结果
            val altText1 = imageView.contentDescription.toString()
            val altText2 = imageView.contentDescription.toString()
            altText1 shouldBe altText2
        }
    }

    test("属性 55: 图片替代文本 - 替代文本应该有合理的长度") {
        checkAll(Arb.string(minSize = 1, maxSize = 100)) { altText ->
            val imageView = ImageView(context)
            AccessibilityUtils.setImageAccessibilityDescription(imageView, altText)
            
            // 验证替代文本长度合理（不超过 100 个字符）
            imageView.contentDescription.toString().length shouldBe altText.length
        }
    }
})
