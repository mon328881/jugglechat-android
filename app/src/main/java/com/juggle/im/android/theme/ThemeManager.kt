package com.juggle.im.android.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

/**
 * 主题管理器接口
 * 定义主题管理的核心功能
 */
interface IThemeManager {
    /**
     * 获取当前主题配置
     */
    fun getCurrentTheme(): ThemeConfig
    
    /**
     * 切换主题
     */
    fun switchTheme(themeId: String)
    
    /**
     * 启用深色模式
     */
    fun enableDarkMode(enable: Boolean)
    
    /**
     * 是否启用深色模式
     */
    fun isDarkModeEnabled(): Boolean
    
    /**
     * 获取所有可用主题
     */
    fun getAvailableThemes(): List<ThemeConfig>
    
    /**
     * 注册主题变化监听器
     */
    fun registerThemeChangeListener(listener: ThemeChangeListener)
    
    /**
     * 注销主题变化监听器
     */
    fun unregisterThemeChangeListener(listener: ThemeChangeListener)
}

/**
 * 主题变化监听器
 */
interface ThemeChangeListener {
    fun onThemeChanged(newTheme: ThemeConfig)
}

/**
 * 主题管理器实现
 */
class ThemeManager(private val context: Context) : IThemeManager {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private val listeners = mutableListOf<ThemeChangeListener>()
    
    private var currentTheme: ThemeConfig = loadTheme()
    
    companion object {
        private const val PREFS_NAME = "theme_prefs"
        private const val KEY_THEME_ID = "theme_id"
        private const val KEY_DARK_MODE = "dark_mode"
        
        private var instance: ThemeManager? = null
        
        /**
         * 获取单例实例
         */
        fun getInstance(context: Context): ThemeManager {
            return instance ?: synchronized(this) {
                instance ?: ThemeManager(context).also { instance = it }
            }
        }
    }
    
    /**
     * 加载保存的主题配置
     */
    private fun loadTheme(): ThemeConfig {
        val themeId = sharedPreferences.getString(KEY_THEME_ID, "light_blue") ?: "light_blue"
        val isDarkMode = sharedPreferences.getBoolean(KEY_DARK_MODE, false)
        
        return if (isDarkMode) {
            ThemeConfig.darkTheme()
        } else {
            ThemeConfig.lightTheme()
        }
    }
    
    override fun getCurrentTheme(): ThemeConfig = currentTheme
    
    override fun switchTheme(themeId: String) {
        val newTheme = when (themeId) {
            "dark_blue" -> ThemeConfig.darkTheme()
            else -> ThemeConfig.lightTheme()
        }
        
        currentTheme = newTheme
        sharedPreferences.edit().apply {
            putString(KEY_THEME_ID, themeId)
            putBoolean(KEY_DARK_MODE, newTheme.isDarkMode)
            apply()
        }
        
        notifyThemeChanged(newTheme)
    }
    
    override fun enableDarkMode(enable: Boolean) {
        val newTheme = if (enable) {
            ThemeConfig.darkTheme()
        } else {
            ThemeConfig.lightTheme()
        }
        
        currentTheme = newTheme
        sharedPreferences.edit().apply {
            putBoolean(KEY_DARK_MODE, enable)
            putString(KEY_THEME_ID, newTheme.id)
            apply()
        }
        
        // 应用系统深色模式设置
        AppCompatDelegate.setDefaultNightMode(
            if (enable) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        
        notifyThemeChanged(newTheme)
    }
    
    override fun isDarkModeEnabled(): Boolean = currentTheme.isDarkMode
    
    override fun getAvailableThemes(): List<ThemeConfig> {
        return listOf(
            ThemeConfig.lightTheme(),
            ThemeConfig.darkTheme()
        )
    }
    
    override fun registerThemeChangeListener(listener: ThemeChangeListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }
    
    override fun unregisterThemeChangeListener(listener: ThemeChangeListener) {
        listeners.remove(listener)
    }
    
    /**
     * 通知所有监听器主题已变化
     */
    private fun notifyThemeChanged(newTheme: ThemeConfig) {
        listeners.forEach { it.onThemeChanged(newTheme) }
    }
}
