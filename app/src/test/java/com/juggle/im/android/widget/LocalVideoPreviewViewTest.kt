package com.juggle.im.android.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.Assert.*

/**
 * LocalVideoPreviewView 单元测试
 * 测试本地视频预览位置和拖动功能
 * 需求：10.2
 */
@RunWith(RobolectricTestRunner::class)
class LocalVideoPreviewViewTest {
    
    private lateinit var context: Context
    private lateinit var previewView: LocalVideoPreviewView
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        previewView = LocalVideoPreviewView(context)
    }
    
    @Test
    fun testInitialPosition() {
        // 测试初始位置
        assertEquals(0f, previewView.getPositionX())
        assertEquals(0f, previewView.getPositionY())
    }
    
    @Test
    fun testSetDefaultPosition() {
        // 测试设置默认位置（右下角）
        val parentWidth = 1080
        val parentHeight = 1920
        
        previewView.setDefaultPosition(parentWidth, parentHeight)
        
        // 验证位置在右下角附近
        assertTrue(previewView.getPositionX() > 0)
        assertTrue(previewView.getPositionY() > 0)
    }
    
    @Test
    fun testSetPosition() {
        // 测试设置位置
        previewView.setPosition(100f, 200f)
        
        assertEquals(100f, previewView.getPositionX())
        assertEquals(200f, previewView.getPositionY())
    }
    
    @Test
    fun testSetPositionWithConstraints() {
        // 测试位置约束
        val parentWidth = 1080
        val parentHeight = 1920
        
        previewView.setDefaultPosition(parentWidth, parentHeight)
        
        // 尝试设置超出边界的位置
        previewView.setPosition(-100f, -100f)
        
        // 验证位置被约束在 0 以上
        assertTrue(previewView.getPositionX() >= 0)
        assertTrue(previewView.getPositionY() >= 0)
    }
    
    @Test
    fun testPositionBoundaryConstraints() {
        // 测试位置边界约束
        val parentWidth = 1080
        val parentHeight = 1920
        
        previewView.setDefaultPosition(parentWidth, parentHeight)
        
        // 尝试设置超出右边界的位置
        previewView.setPosition(2000f, 500f)
        
        // 验证位置被约束在父容器内
        assertTrue(previewView.getPositionX() <= parentWidth)
    }
    
    @Test
    fun testMultiplePositionChanges() {
        // 测试多次位置变化
        previewView.setPosition(100f, 100f)
        assertEquals(100f, previewView.getPositionX())
        assertEquals(100f, previewView.getPositionY())
        
        previewView.setPosition(200f, 200f)
        assertEquals(200f, previewView.getPositionX())
        assertEquals(200f, previewView.getPositionY())
        
        previewView.setPosition(300f, 300f)
        assertEquals(300f, previewView.getPositionX())
        assertEquals(300f, previewView.getPositionY())
    }
    
    @Test
    fun testPreviewViewDimensions() {
        // 测试预览视图尺寸
        assertEquals(120, previewView.layoutParams.width)
        assertEquals(160, previewView.layoutParams.height)
    }
    
    @Test
    fun testDefaultPositionCalculation() {
        // 测试默认位置计算
        val parentWidth = 1080
        val parentHeight = 1920
        val margin = 16
        
        previewView.setDefaultPosition(parentWidth, parentHeight)
        
        // 验证位置大约在右下角
        val expectedX = (parentWidth - 120 - margin).toFloat()
        val expectedY = (parentHeight - 160 - margin).toFloat()
        
        // 允许一定的误差范围
        assertTrue(Math.abs(previewView.getPositionX() - expectedX) < 10)
        assertTrue(Math.abs(previewView.getPositionY() - expectedY) < 10)
    }
}
