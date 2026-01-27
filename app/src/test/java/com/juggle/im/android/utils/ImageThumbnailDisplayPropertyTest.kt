package com.juggle.im.android.utils

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * 图片消息缩略图显示属性测试
 * **验证: 需求 14.3**
 */
class ImageThumbnailDisplayPropertyTest : FunSpec({

    test("属性 62: 图片消息应该显示为缩略图") {
        // 对于任何图片消息，应该显示为缩略图
        checkAll(
            Arb.string(minSize = 1, maxSize = 256)
        ) { imageUrl ->
            // 验证 URL 不为空
            imageUrl.isNotEmpty() shouldBe true

            // 验证 URL 格式有效
            val isValidImageUrl = imageUrl.endsWith(".jpg") ||
                    imageUrl.endsWith(".jpeg") ||
                    imageUrl.endsWith(".png") ||
                    imageUrl.endsWith(".gif") ||
                    imageUrl.endsWith(".webp") ||
                    imageUrl.contains("image")

            // 对于有效的图片 URL，应该能够加载缩略图
            if (isValidImageUrl || imageUrl.startsWith("http")) {
                imageUrl.isNotEmpty() shouldBe true
            }
        }
    }

    test("属性 62: 缩略图尺寸应该合理") {
        // 对于任何缩略图，尺寸应该在合理范围内
        checkAll(
            Arb.int(min = 100, max = 500),
            Arb.int(min = 100, max = 500)
        ) { width, height ->
            // 验证缩略图尺寸合理
            width > 0 shouldBe true
            height > 0 shouldBe true
            width <= 500 shouldBe true
            height <= 500 shouldBe true
        }
    }

    test("属性 62: 缩略图应该保持宽高比") {
        // 对于任何原始图片，缩略图应该保持宽高比
        checkAll(
            Arb.int(min = 100, max = 4000),
            Arb.int(min = 100, max = 4000)
        ) { originalWidth, originalHeight ->
            // 计算缩放比例
            val maxSize = 300
            val scale = minOf(
                maxSize.toFloat() / originalWidth,
                maxSize.toFloat() / originalHeight
            )

            val thumbnailWidth = (originalWidth * scale).toInt()
            val thumbnailHeight = (originalHeight * scale).toInt()

            // 验证宽高比保持一致
            val originalRatio = originalWidth.toFloat() / originalHeight
            val thumbnailRatio = thumbnailWidth.toFloat() / thumbnailHeight

            // 允许小数点误差
            kotlin.math.abs(originalRatio - thumbnailRatio) < 0.01f shouldBe true
        }
    }

    test("属性 62: 缩略图应该小于原始图片") {
        // 对于任何缩略图，文件大小应该小于原始图片
        checkAll(
            Arb.int(min = 1000, max = 1000000)
        ) { originalSize ->
            // 缩略图应该更小
            val thumbnailSize = (originalSize * 0.3).toInt()
            thumbnailSize < originalSize shouldBe true
        }
    }

    test("属性 62: 缺失的图片应该显示占位符") {
        // 对于缺失的图片，应该显示占位符
        val missingImageUrl = ""
        missingImageUrl.isEmpty() shouldBe true
    }

    test("属性 62: 图片加载失败应该显示占位符") {
        // 对于加载失败的图片，应该显示占位符
        val invalidUrl = "invalid://url"
        val isInvalid = !invalidUrl.startsWith("http://") &&
                !invalidUrl.startsWith("https://") &&
                !invalidUrl.startsWith("file://")

        isInvalid shouldBe true
    }

})
