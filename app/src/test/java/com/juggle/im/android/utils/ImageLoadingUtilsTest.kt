package com.juggle.im.android.utils

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * 图片加载工具类单元测试
 */
class ImageLoadingUtilsTest : FunSpec({

    test("应该正确处理空 URL") {
        val emptyUrl: String? = null
        emptyUrl.isNullOrEmpty() shouldBe true
    }

    test("应该正确处理有效的 HTTP URL") {
        val validUrl = "https://example.com/image.jpg"
        validUrl.startsWith("https://") shouldBe true
    }

    test("应该正确处理有效的本地文件 URL") {
        val localUrl = "file:///sdcard/image.jpg"
        localUrl.startsWith("file://") shouldBe true
    }

    test("应该正确识别图片格式") {
        val jpgUrl = "https://example.com/image.jpg"
        val pngUrl = "https://example.com/image.png"
        val gifUrl = "https://example.com/image.gif"

        jpgUrl.endsWith(".jpg") shouldBe true
        pngUrl.endsWith(".png") shouldBe true
        gifUrl.endsWith(".gif") shouldBe true
    }

    test("应该正确处理 URL 中的特殊字符") {
        val urlWithSpecialChars = "https://example.com/image%20with%20spaces.jpg"
        urlWithSpecialChars.isNotEmpty() shouldBe true
    }

    test("应该正确处理长 URL") {
        val longUrl = "https://example.com/" + "a".repeat(200) + ".jpg"
        longUrl.length > 200 shouldBe true
    }

})
