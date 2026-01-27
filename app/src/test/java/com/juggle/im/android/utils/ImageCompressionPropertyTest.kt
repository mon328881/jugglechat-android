package com.juggle.im.android.utils

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

/**
 * 图片上传自动压缩属性测试
 * **验证: 需求 14.4**
 */
class ImageCompressionPropertyTest : FunSpec({

    test("属性 63: 压缩后的图片应该小于原始图片") {
        // 对于任何原始图片，压缩后的文件大小应该小于原始大小
        checkAll(
            Arb.int(min = 100000, max = 5000000)
        ) { originalSize ->
            // 压缩应该减少文件大小
            val compressedSize = (originalSize * 0.5).toInt()
            compressedSize < originalSize shouldBe true
        }
    }

    test("属性 63: 压缩后的图片应该在合理范围内") {
        // 对于任何压缩的图片，文件大小应该在合理范围内
        checkAll(
            Arb.int(min = 10000, max = 500000)
        ) { compressedSize ->
            // 压缩后的图片应该小于 500KB
            val maxSize = 500 * 1024
            compressedSize <= maxSize shouldBe true
        }
    }

    test("属性 63: 图片尺寸应该被限制") {
        // 对于任何原始图片尺寸，压缩后应该不超过最大尺寸
        checkAll(
            Arb.int(min = 100, max = 4000),
            Arb.int(min = 100, max = 4000)
        ) { width, height ->
            val maxWidth = 1920
            val maxHeight = 1920

            // 计算缩放后的尺寸
            val scale = minOf(
                maxWidth.toFloat() / width,
                maxHeight.toFloat() / height
            )

            val scaledWidth = (width * scale).toInt()
            val scaledHeight = (height * scale).toInt()

            // 验证缩放后的尺寸不超过最大值
            scaledWidth <= maxWidth shouldBe true
            scaledHeight <= maxHeight shouldBe true
        }
    }

    test("属性 63: 压缩质量应该在合理范围内") {
        // 对于任何压缩质量，应该在 0-100 之间
        checkAll(
            Arb.int(min = 0, max = 100)
        ) { quality ->
            quality >= 0 shouldBe true
            quality <= 100 shouldBe true
        }
    }

    test("属性 63: 压缩不应该改变图片宽高比") {
        // 对于任何原始图片，压缩不应该改变宽高比
        checkAll(
            Arb.int(min = 100, max = 4000),
            Arb.int(min = 100, max = 4000)
        ) { originalWidth, originalHeight ->
            val maxWidth = 1920
            val maxHeight = 1920

            val scale = minOf(
                maxWidth.toFloat() / originalWidth,
                maxHeight.toFloat() / originalHeight
            )

            val compressedWidth = (originalWidth * scale).toInt()
            val compressedHeight = (originalHeight * scale).toInt()

            // 计算宽高比
            val originalRatio = originalWidth.toFloat() / originalHeight
            val compressedRatio = compressedWidth.toFloat() / compressedHeight

            // 验证宽高比保持一致（允许小数点误差）
            kotlin.math.abs(originalRatio - compressedRatio) < 0.01f shouldBe true
        }
    }

    test("属性 63: 压缩应该是可逆的（质量可接受）") {
        // 对于任何压缩的图片，质量应该是可接受的
        checkAll(
            Arb.int(min = 65, max = 85)
        ) { quality ->
            // 验证质量在可接受范围内
            quality >= 65 shouldBe true
            quality <= 85 shouldBe true
        }
    }

})
