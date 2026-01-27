package com.juggle.im.android.theme

/**
 * 主题配置数据类
 * 包含完整的主题定义，包括颜色、排版、间距等
 */
data class ThemeConfig(
    val id: String,
    val name: String,
    val isDarkMode: Boolean,
    val colors: ColorScheme,
    val typography: TypographyScheme,
    val spacing: SpacingScheme
) {
    companion object {
        /**
         * 创建浅色主题配置
         */
        fun lightTheme(): ThemeConfig {
            return ThemeConfig(
                id = "light_blue",
                name = "浅色蓝色主题",
                isDarkMode = false,
                colors = ColorScheme(
                    primary = 0xFF64B5F6.toInt(),
                    onPrimary = 0xFFFFFFFF.toInt(),
                    secondary = 0xFF2196F3.toInt(),
                    onSecondary = 0xFFFFFFFF.toInt(),
                    tertiary = 0xFF03DAC6.toInt(),
                    onTertiary = 0xFF000000.toInt(),
                    background = 0xFFFFFBFE.toInt(),
                    onBackground = 0xFF1C1B1F.toInt(),
                    surface = 0xFFFFFBFE.toInt(),
                    onSurface = 0xFF1C1B1F.toInt(),
                    error = 0xFFEF4444.toInt(),
                    onError = 0xFFFFFFFF.toInt(),
                    success = 0xFF09B83E.toInt(),
                    warning = 0xFFFBC6C4.toInt(),
                    info = 0xFF64B5F6.toInt(),
                    divider = 0xFFE0E0E0.toInt(),
                    hint = 0xFFBFBEBE.toInt(),
                    disabled = 0xFFE6E6E6.toInt()
                ),
                typography = TypographyScheme(
                    headlineL = TextStyle(fontSize = 32, fontWeight = 700, lineHeight = 40),
                    headlineM = TextStyle(fontSize = 28, fontWeight = 700, lineHeight = 36),
                    headlineS = TextStyle(fontSize = 24, fontWeight = 700, lineHeight = 32),
                    bodyL = TextStyle(fontSize = 16, fontWeight = 400, lineHeight = 24),
                    bodyM = TextStyle(fontSize = 14, fontWeight = 400, lineHeight = 20),
                    bodyS = TextStyle(fontSize = 12, fontWeight = 400, lineHeight = 16),
                    labelL = TextStyle(fontSize = 14, fontWeight = 500, lineHeight = 20),
                    labelM = TextStyle(fontSize = 12, fontWeight = 500, lineHeight = 16),
                    labelS = TextStyle(fontSize = 11, fontWeight = 500, lineHeight = 16)
                ),
                spacing = SpacingScheme.default()
            )
        }

        /**
         * 创建深色主题配置
         */
        fun darkTheme(): ThemeConfig {
            return ThemeConfig(
                id = "dark_blue",
                name = "深色蓝色主题",
                isDarkMode = true,
                colors = ColorScheme(
                    primary = 0xFFADD8FF.toInt(),
                    onPrimary = 0xFF003A70.toInt(),
                    secondary = 0xFFB3E5FC.toInt(),
                    onSecondary = 0xFF003A70.toInt(),
                    tertiary = 0xFF80F7F1.toInt(),
                    onTertiary = 0xFF003A3A.toInt(),
                    background = 0xFF1C1B1F.toInt(),
                    onBackground = 0xFFE6E1E5.toInt(),
                    surface = 0xFF2C2C2E.toInt(),
                    onSurface = 0xFFE6E1E5.toInt(),
                    error = 0xFFEF4444.toInt(),
                    onError = 0xFF000000.toInt(),
                    success = 0xFF09B83E.toInt(),
                    warning = 0xFFFBC6C4.toInt(),
                    info = 0xFF64B5F6.toInt(),
                    divider = 0xFF3F3F3F.toInt(),
                    hint = 0xFF717182.toInt(),
                    disabled = 0xFF4A4A4A.toInt()
                ),
                typography = TypographyScheme(
                    headlineL = TextStyle(fontSize = 32, fontWeight = 700, lineHeight = 40),
                    headlineM = TextStyle(fontSize = 28, fontWeight = 700, lineHeight = 36),
                    headlineS = TextStyle(fontSize = 24, fontWeight = 700, lineHeight = 32),
                    bodyL = TextStyle(fontSize = 16, fontWeight = 400, lineHeight = 24),
                    bodyM = TextStyle(fontSize = 14, fontWeight = 400, lineHeight = 20),
                    bodyS = TextStyle(fontSize = 12, fontWeight = 400, lineHeight = 16),
                    labelL = TextStyle(fontSize = 14, fontWeight = 500, lineHeight = 20),
                    labelM = TextStyle(fontSize = 12, fontWeight = 500, lineHeight = 16),
                    labelS = TextStyle(fontSize = 11, fontWeight = 500, lineHeight = 16)
                ),
                spacing = SpacingScheme.default()
            )
        }
    }
}
