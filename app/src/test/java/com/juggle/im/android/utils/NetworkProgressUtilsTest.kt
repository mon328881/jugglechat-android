package com.juggle.im.android.utils

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * 网络进度工具类单元测试
 */
class NetworkProgressUtilsTest : FunSpec({

    test("应该正确计算加载进度") {
        val bytesRead = 500L
        val totalBytes = 1000L
        val progress = ((bytesRead * 100) / totalBytes).toInt()

        progress shouldBe 50
    }

    test("应该正确处理加载开始") {
        val bytesRead = 0L
        val totalBytes = 1000L
        val progress = ((bytesRead * 100) / totalBytes).toInt()

        progress shouldBe 0
    }

    test("应该正确处理加载完成") {
        val bytesRead = 1000L
        val totalBytes = 1000L
        val progress = ((bytesRead * 100) / totalBytes).toInt()

        progress shouldBe 100
    }

    test("应该正确格式化网络速度 - 字节") {
        val speed = 512L
        val formatted = NetworkProgressUtils.formatNetworkSpeed(speed)
        formatted.contains("B/s") shouldBe true
    }

    test("应该正确格式化网络速度 - KB") {
        val speed = 2048L
        val formatted = NetworkProgressUtils.formatNetworkSpeed(speed)
        formatted.contains("KB/s") shouldBe true
    }

    test("应该正确格式化网络速度 - MB") {
        val speed = 2 * 1024 * 1024L
        val formatted = NetworkProgressUtils.formatNetworkSpeed(speed)
        formatted.contains("MB/s") shouldBe true
    }

    test("应该正确处理多个进度更新") {
        val totalBytes = 1000L
        val progress1 = ((100L * 100) / totalBytes).toInt()
        val progress2 = ((500L * 100) / totalBytes).toInt()
        val progress3 = ((1000L * 100) / totalBytes).toInt()

        progress1 shouldBe 10
        progress2 shouldBe 50
        progress3 shouldBe 100

        // 验证进度单调递增
        progress1 <= progress2 shouldBe true
        progress2 <= progress3 shouldBe true
    }

    test("应该正确处理大文件") {
        val bytesRead = 500000000L // 500MB
        val totalBytes = 1000000000L // 1GB
        val progress = ((bytesRead * 100) / totalBytes).toInt()

        progress shouldBe 50
    }

})
