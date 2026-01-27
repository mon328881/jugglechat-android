package com.juggle.im.android.utils

import android.content.Context
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 无障碍工具类单元测试
 * 验证无障碍标签的添加和检查功能
 */
@RunWith(RobolectricTestRunner::class)
class AccessibilityUtilsTest {

    private lateinit var context: Context
    private lateinit var button: Button
    private lateinit var imageView: ImageView
    private lateinit var textView: TextView

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        button = Button(context)
        imageView = ImageView(context)
        textView = TextView(context)
    }

    @Test
    fun testSetButtonAccessibilityLabel() {
        val label = "登录按钮"
        AccessibilityUtils.setButtonAccessibilityLabel(button, label)
        
        assertEquals(label, button.contentDescription.toString())
        assertTrue(AccessibilityUtils.hasAccessibilityLabel(button))
    }

    @Test
    fun testSetImageAccessibilityDescription() {
        val description = "用户头像"
        AccessibilityUtils.setImageAccessibilityDescription(imageView, description)
        
        assertEquals(description, imageView.contentDescription.toString())
        assertTrue(AccessibilityUtils.hasAccessibilityLabel(imageView))
    }

    @Test
    fun testSetTextViewAccessibilityLabel() {
        val label = "欢迎文本"
        AccessibilityUtils.setTextViewAccessibilityLabel(textView, label)
        
        assertEquals(label, textView.contentDescription.toString())
        assertTrue(AccessibilityUtils.hasAccessibilityLabel(textView))
    }

    @Test
    fun testSetViewAccessibilityLabel() {
        val label = "自定义视图"
        AccessibilityUtils.setViewAccessibilityLabel(button, label)
        
        assertEquals(label, button.contentDescription.toString())
        assertTrue(AccessibilityUtils.hasAccessibilityLabel(button))
    }

    @Test
    fun testSetCompleteAccessibilityInfo() {
        val label = "搜索框"
        val hint = "输入关键词"
        AccessibilityUtils.setCompleteAccessibilityInfo(textView, label, hint, false)
        
        assertEquals(label, textView.contentDescription.toString())
        assertTrue(AccessibilityUtils.hasAccessibilityLabel(textView))
    }

    @Test
    fun testHasAccessibilityLabel() {
        assertFalse(AccessibilityUtils.hasAccessibilityLabel(button))
        
        AccessibilityUtils.setButtonAccessibilityLabel(button, "按钮")
        assertTrue(AccessibilityUtils.hasAccessibilityLabel(button))
    }

    @Test
    fun testGetAccessibilityLabel() {
        val label = "测试标签"
        AccessibilityUtils.setButtonAccessibilityLabel(button, label)
        
        assertEquals(label, AccessibilityUtils.getAccessibilityLabel(button))
    }

    @Test
    fun testGetAccessibilityLabelEmpty() {
        assertEquals("", AccessibilityUtils.getAccessibilityLabel(button))
    }

    @Test
    fun testSetListItemAccessibilityLabel() {
        val itemLabel = "朋友列表项"
        AccessibilityUtils.setListItemAccessibilityLabel(button, itemLabel, 0, 10)
        
        val expectedLabel = "$itemLabel, 第 1 项，共 10 项"
        assertEquals(expectedLabel, button.contentDescription.toString())
    }

    @Test
    fun testSetStatusAccessibilityLabel() {
        val status = "已连接"
        AccessibilityUtils.setStatusAccessibilityLabel(button, status)
        
        assertEquals(status, button.contentDescription.toString())
    }

    @Test
    fun testSetInputAccessibilityLabel() {
        val label = "账号输入框"
        val hint = "请输入账号"
        AccessibilityUtils.setInputAccessibilityLabel(textView, label, hint)
        
        assertEquals(label, textView.contentDescription.toString())
    }

    @Test
    fun testSetIconButtonAccessibilityLabel() {
        val label = "发送"
        val action = "发送消息"
        AccessibilityUtils.setIconButtonAccessibilityLabel(button, label, action)
        
        val expectedLabel = "$label，$action"
        assertEquals(expectedLabel, button.contentDescription.toString())
    }

    @Test
    fun testSetIconButtonAccessibilityLabelWithoutAction() {
        val label = "返回"
        AccessibilityUtils.setIconButtonAccessibilityLabel(button, label)
        
        assertEquals(label, button.contentDescription.toString())
    }
}
