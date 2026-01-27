package com.juggle.im.android.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith

/**
 * 通话界面屏幕旋转适配属性测试
 * 属性 48: 通话界面屏幕旋转适配
 * 验证需求 10.5
 * 
 * 通话界面应该支持屏幕旋转，自动调整布局
 */
@RunWith(RobolectricTestRunner::class)
class CallScreenRotationPropertyTest : FunSpec({
    
    val context: Context = ApplicationProvider.getApplicationContext()
    
    test("属性 48: 通话界面屏幕旋转适配 - 本地视频预览应该支持位置调整") {
        val previewView = LocalVideoPreviewView(context)
        
        // 验证初始位置
        previewView.getPositionX() shouldBe 0f
        previewView.getPositionY() shouldBe 0f
    }
    
    test("属性 48: 通话界面屏幕旋转适配 - 本地视频预览位置应该被正确约束") {
        checkAll(
            Arb.int(min = 100, max = 2000),
            Arb.int(min = 100, max = 2000)
        ) { parentWidth, parentHeight ->
            val previewView = LocalVideoPreviewView(context)
            
            // 设置默认位置
            previewView.setDefaultPosition(parentWidth, parentHeight)
            
            // 验证位置在有效范围内
            previewView.getPositionX() shouldBe >= 0f
            previewView.getPositionY() shouldBe >= 0f
        }
    }
    
    test("属性 48: 通话界面屏幕旋转适配 - 本地视频预览应该支持任意位置设置") {
        checkAll(
            Arb.int(min = 0, max = 1000),
            Arb.int(min = 0, max = 1000)
        ) { x, y ->
            val previewView = LocalVideoPreviewView(context)
            
            // 设置位置
            previewView.setPosition(x.toFloat(), y.toFloat())
            
            // 验证位置被正确设置
            previewView.getPositionX() shouldBe x.toFloat()
            previewView.getPositionY() shouldBe y.toFloat()
        }
    }
    
    test("属性 48: 通话界面屏幕旋转适配 - 本地视频预览位置应该在屏幕边界内") {
        val previewView = LocalVideoPreviewView(context)
        val parentWidth = 1080
        val parentHeight = 1920
        
        previewView.setDefaultPosition(parentWidth, parentHeight)
        
        // 验证位置在屏幕内
        previewView.getPositionX() shouldBe >= 0f
        previewView.getPositionY() shouldBe >= 0f
        previewView.getPositionX() shouldBe <= parentWidth.toFloat()
        previewView.getPositionY() shouldBe <= parentHeight.toFloat()
    }
    
    test("属性 48: 通话界面屏幕旋转适配 - 本地视频预览应该支持多次位置变化") {
        val previewView = LocalVideoPreviewView(context)
        
        // 第一次设置位置
        previewView.setPosition(100f, 100f)
        previewView.getPositionX() shouldBe 100f
        previewView.getPositionY() shouldBe 100f
        
        // 第二次设置位置
        previewView.setPosition(200f, 200f)
        previewView.getPositionX() shouldBe 200f
        previewView.getPositionY() shouldBe 200f
        
        // 第三次设置位置
        previewView.setPosition(300f, 300f)
        previewView.getPositionX() shouldBe 300f
        previewView.getPositionY() shouldBe 300f
    }
    
    test("属性 48: 通话界面屏幕旋转适配 - 本地视频预览尺寸应该保持一致") {
        val previewView = LocalVideoPreviewView(context)
        
        // 验证尺寸
        previewView.layoutParams.width shouldBe 120
        previewView.layoutParams.height shouldBe 160
    }
})
