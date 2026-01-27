package com.juggle.im.android.theme

import android.content.Context
import android.content.SharedPreferences
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.*

/**
 * 主题管理器单元测试
 * 验证主题管理的核心功能
 */
class ThemeManagerTest {
    
    private lateinit var mockContext: Context
    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var themeManager: ThemeManager
    
    @Before
    fun setUp() {
        // 创建 Mock 对象
        mockContext = mock(Context::class.java)
        mockSharedPreferences = mock(SharedPreferences::class.java)
        mockEditor = mock(SharedPreferences.Editor::class.java)
        
        // 配置 Mock 行为
        `when`(mockContext.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE))
            .thenReturn(mockSharedPreferences)
        `when`(mockSharedPreferences.edit()).thenReturn(mockEditor)
        `when`(mockEditor.apply()).thenReturn(Unit)
        `when`(mockSharedPreferences.getString("theme_id", "light_blue"))
            .thenReturn("light_blue")
        `when`(mockSharedPreferences.getBoolean("dark_mode", false))
            .thenReturn(false)
        
        // 创建主题管理器实例
        themeManager = ThemeManager(mockContext)
    }
    
    /**
     * 属性 2: 深色模式自动切换
     * 验证: 需求 1.2
     * 
     * 对于任何主题配置，当系统启用深色模式时，应用中的所有颜色应该自动转换为深色主题的对应颜色
     */
    @Test
    fun testDarkModeAutoSwitch() {
        // 初始状态应该是浅色模式
        assertFalse("初始状态应该是浅色模式", themeManager.isDarkModeEnabled())
        
        // 启用深色模式
        themeManager.enableDarkMode(true)
        
        // 验证深色模式已启用
        assertTrue("深色模式应该已启用", themeManager.isDarkModeEnabled())
        
        // 验证当前主题是深色主题
        val currentTheme = themeManager.getCurrentTheme()
        assertTrue("当前主题应该是深色主题", currentTheme.isDarkMode)
        
        // 禁用深色模式
        themeManager.enableDarkMode(false)
        
        // 验证深色模式已禁用
        assertFalse("深色模式应该已禁用", themeManager.isDarkModeEnabled())
    }
    
    /**
     * 属性 4: 主题切换功能
     * 验证: 需求 1.4
     * 
     * 对于任何支持的主题配置，用户应该能够成功切换到该主题，
     * 并且应用界面应该立即反映新主题的颜色
     */
    @Test
    fun testThemeSwitching() {
        // 获取初始主题
        val initialTheme = themeManager.getCurrentTheme()
        assertEquals("初始主题应该是浅色蓝色主题", "light_blue", initialTheme.id)
        
        // 切换到深色主题
        themeManager.switchTheme("dark_blue")
        
        // 验证主题已切换
        val newTheme = themeManager.getCurrentTheme()
        assertEquals("主题应该已切换到深色蓝色", "dark_blue", newTheme.id)
        assertTrue("新主题应该是深色主题", newTheme.isDarkMode)
        
        // 切换回浅色主题
        themeManager.switchTheme("light_blue")
        
        // 验证主题已切换回浅色
        val lightTheme = themeManager.getCurrentTheme()
        assertEquals("主题应该已切换回浅色蓝色", "light_blue", lightTheme.id)
        assertFalse("浅色主题不应该启用深色模式", lightTheme.isDarkMode)
    }
    
    /**
     * 验证可用主题列表
     */
    @Test
    fun testAvailableThemes() {
        val availableThemes = themeManager.getAvailableThemes()
        
        // 应该至少有两个主题（浅色和深色）
        assertTrue("应该至少有两个可用主题", availableThemes.size >= 2)
        
        // 验证浅色主题存在
        val lightTheme = availableThemes.find { it.id == "light_blue" }
        assertNotNull("浅色主题应该存在", lightTheme)
        assertFalse("浅色主题不应该启用深色模式", lightTheme?.isDarkMode ?: true)
        
        // 验证深色主题存在
        val darkTheme = availableThemes.find { it.id == "dark_blue" }
        assertNotNull("深色主题应该存在", darkTheme)
        assertTrue("深色主题应该启用深色模式", darkTheme?.isDarkMode ?: false)
    }
    
    /**
     * 验证主题变化监听器
     */
    @Test
    fun testThemeChangeListener() {
        var themeChangedCount = 0
        var lastChangedTheme: ThemeConfig? = null
        
        val listener = object : ThemeChangeListener {
            override fun onThemeChanged(newTheme: ThemeConfig) {
                themeChangedCount++
                lastChangedTheme = newTheme
            }
        }
        
        // 注册监听器
        themeManager.registerThemeChangeListener(listener)
        
        // 切换主题
        themeManager.switchTheme("dark_blue")
        
        // 验证监听器被调用
        assertEquals("监听器应该被调用一次", 1, themeChangedCount)
        assertEquals("监听器应该收到新主题", "dark_blue", lastChangedTheme?.id)
        
        // 再次切换主题
        themeManager.switchTheme("light_blue")
        
        // 验证监听器被再次调用
        assertEquals("监听器应该被调用两次", 2, themeChangedCount)
        assertEquals("监听器应该收到新主题", "light_blue", lastChangedTheme?.id)
        
        // 注销监听器
        themeManager.unregisterThemeChangeListener(listener)
        
        // 切换主题
        themeManager.switchTheme("dark_blue")
        
        // 验证监听器不再被调用
        assertEquals("监听器应该仍然是两次", 2, themeChangedCount)
    }
    
    /**
     * 验证主题配置的完整性
     */
    @Test
    fun testThemeConfigCompleteness() {
        val currentTheme = themeManager.getCurrentTheme()
        
        // 验证主题配置的所有字段都已设置
        assertNotNull("主题 ID 不能为空", currentTheme.id)
        assertNotNull("主题名称不能为空", currentTheme.name)
        assertNotNull("颜色方案不能为空", currentTheme.colors)
        assertNotNull("排版方案不能为空", currentTheme.typography)
        assertNotNull("间距方案不能为空", currentTheme.spacing)
    }
}
