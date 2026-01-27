package com.juggle.im.android.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldBeGreaterThan
import io.kotest.matchers.shouldBeLessThan
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll

/**
 * 视频自动压缩属性测试
 * 验证: 需求 18.3
 * 属性 76: 视频自动压缩
 */
class VideoCompressionPropertyTest : FunSpec({
    
    val context: Context = ApplicationProvider.getApplicationContext()
    
    test("小文件不应该被压缩") {
        // 小于 50MB 的文件不需要压缩
        val smallFileSize = 10 * 1024 * 1024L // 10MB
        
        val shouldCompress = VideoCompressionUtils.shouldCompress(smallFileSize)
        shouldCompress.shouldBe(false)
    }
    
    test("大文件应该被标记为需要压缩") {
        // 大于 50MB 的文件需要压缩
        val largeFileSize = 100 * 1024 * 1024L // 100MB
        
        val shouldCompress = VideoCompressionUtils.shouldCompress(largeFileSize)
        shouldCompress.shouldBe(true)
    }
    
    test("压缩后的文件大小应该小于原始文件") {
        checkAll(Arb.long(min = 50 * 1024 * 1024, max = 500 * 1024 * 1024)) { originalSize ->
            val estimatedCompressedSize = VideoCompressionUtils.estimateCompressedSize(originalSize)
            
            // 压缩后的大小应该小于原始大小
            estimatedCompressedSize.shouldBeLessThan(originalSize)
        }
    }
    
    test("压缩比例应该在合理范围内") {
        checkAll(Arb.long(min = 50 * 1024 * 1024, max = 500 * 1024 * 1024)) { originalSize ->
            val compressedSize = VideoCompressionUtils.estimateCompressedSize(originalSize)
            val ratio = VideoCompressionUtils.getCompressionRatio(originalSize, compressedSize)
            
            // 压缩比例应该在 30-50% 之间
            ratio.shouldBeGreaterThan(20f)
            ratio.shouldBeLessThan(60f)
        }
    }
    
    test("压缩比例计算应该正确") {
        val originalSize = 100L
        val compressedSize = 50L
        
        val ratio = VideoCompressionUtils.getCompressionRatio(originalSize, compressedSize)
        
        // 压缩比例应该是 50%
        ratio.shouldBe(50f)
    }
    
    test("零大小文件的压缩比例应该为零") {
        val ratio = VideoCompressionUtils.getCompressionRatio(0, 0)
        
        ratio.shouldBe(0f)
    }
    
    test("估计的压缩大小应该总是正数") {
        checkAll(Arb.long(min = 1, max = 1000 * 1024 * 1024)) { fileSize ->
            val estimatedSize = VideoCompressionUtils.estimateCompressedSize(fileSize)
            
            estimatedSize.shouldBeGreaterThan(0)
        }
    }
    
    test("压缩阈值应该是 50MB") {
        // 恰好 50MB 的文件应该被标记为需要压缩
        val thresholdSize = 50 * 1024 * 1024L
        
        val shouldCompress = VideoCompressionUtils.shouldCompress(thresholdSize)
        shouldCompress.shouldBe(true)
    }
    
    test("压缩阈值以下的文件不应该被压缩") {
        // 49.9MB 的文件不应该被标记为需要压缩
        val belowThresholdSize = (50 * 1024 * 1024 - 1).toLong()
        
        val shouldCompress = VideoCompressionUtils.shouldCompress(belowThresholdSize)
        shouldCompress.shouldBe(false)
    }
})
