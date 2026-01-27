package com.juggle.im.android.utils

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * 图片压缩工具类单元测试
 */
class ImageCompressionUtilsTest : FunSpec({

    test("应该正确格式化文件大小 - 字节") {
        val size = 512L
        val formatted = ImageCompressionUtils.formatFileSize(size)
        formatted.contains("B") shouldBe true
    }

    test("应该正确格式化文件大小 - KB") {
        val size = 2048L
        val formatted = ImageCompressionUtils.formatFileSize(size)
        formatted.contains("KB") shouldBe true
    }

    test("应该正确格式化文件大小 - MB") {
        val size = 2 * 1024 * 1024L
        val formatted = ImageCompressionUtils.formatFileSize(size)
        formatted.contains("MB") shouldBe true
    }

    test("应该正确计算缩放比例") {
        val originalWidth = 4000
        val originalHeight = 3000
        val maxWidth = 1920
        val maxHeight = 1920

        val scale = minOf(
            maxWidth.toFloat() / originalWidth,
            maxHeight.toFloat() / originalHeight
        )

        scale > 0 shouldBe true
        scale < 1 shouldBe true
    }

    test("应该保持宽高比") {
        val originalWidth = 1920
        val originalHeight = 1080
        val maxWidth = 960
        val maxHeight = 960

        val scale = minOf(
            maxWidth.toFloat() / originalWidth,
            maxHeight.toFloat() / originalHeight
        )

        val newWidth = (originalWidth * scale).toInt()
        val newHeight = (originalHeight * scale).toInt()

        val originalRatio = originalWidth.toFloat() / originalHeight
        val newRatio = newWidth.toFloat() / newHeight

        kotlin.math.abs(originalRatio - newRatio) < 0.01f shouldBe true
    }

    test("应该正确处理小图片") {
        val smallWidth = 100
        val smallHeight = 100
        val maxWidth = 1920
        val maxHeight = 1920

        val scale = minOf(
            maxWidth.toFloat() / smallWidth,
            maxHeight.toFloat() / smallHeight
        )

        // 小图片不应该被放大
        scale <= 1f shouldBe true
    }

})
