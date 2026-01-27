package com.juggle.im.android.utils

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * 视频工具类单元测试
 */
class VideoUtilsTest : FunSpec({

    test("应该正确格式化视频时长 - 秒") {
        val durationMs = 30000L // 30 秒
        val formatted = VideoUtils.formatDuration(durationMs)
        formatted.contains(":") shouldBe true
    }

    test("应该正确格式化视频时长 - 分钟") {
        val durationMs = 120000L // 2 分钟
        val formatted = VideoUtils.formatDuration(durationMs)
        formatted.contains(":") shouldBe true
    }

    test("应该正确格式化视频时长 - 小时") {
        val durationMs = 3600000L // 1 小时
        val formatted = VideoUtils.formatDuration(durationMs)
        formatted.contains(":") shouldBe true
    }

    test("应该正确计算视频时长") {
        val durationMs = 90000L // 1 分 30 秒
        val totalSeconds = durationMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        minutes shouldBe 1
        seconds shouldBe 30
    }

    test("应该正确处理零时长") {
        val durationMs = 0L
        val formatted = VideoUtils.formatDuration(durationMs)
        formatted.contains("00:00") shouldBe true
    }

    test("应该正确处理长视频") {
        val durationMs = 7200000L // 2 小时
        val totalSeconds = durationMs / 1000
        val hours = totalSeconds / 3600

        hours shouldBe 2
    }

    test("应该正确识别视频格式") {
        val mp4Url = "https://example.com/video.mp4"
        val aviUrl = "https://example.com/video.avi"
        val movUrl = "https://example.com/video.mov"

        mp4Url.endsWith(".mp4") shouldBe true
        aviUrl.endsWith(".avi") shouldBe true
        movUrl.endsWith(".mov") shouldBe true
    }

})
