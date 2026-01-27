package com.juggle.im.android.widget

import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.juggle.im.android.theme.ThemeManager

/**
 * 发现界面标签页组件
 * 支持朋友圈和社区两个标签页
 */
class DiscoverTabView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    
    /**
     * 标签页类型
     */
    enum class TabType {
        MOMENTS,    // 朋友圈
        COMMUNITY   // 社区
    }
    
    private val themeManager: ThemeManager = ThemeManager.getInstance(context)
    private val theme = themeManager.getCurrentTheme()
    
    private val momentsTab: TextView
    private val communityTab: TextView
    private val underline: FrameLayout
    
    private var selectedTab: TabType = TabType.MOMENTS
    private var onTabChangeListener: ((TabType) -> Unit)? = null
    
    init {
        orientation = HORIZONTAL
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            56
        )
        
        // 设置背景
        setBackgroundColor(theme.colors.surface)
        
        // 创建朋友圈标签
        momentsTab = TextView(context).apply {
            text = "朋友圈"
            textSize = 14f
            setTextColor(theme.colors.primary)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                0,
                LayoutParams.MATCH_PARENT,
                1f
            )
            setOnClickListener {
                selectTab(TabType.MOMENTS)
            }
        }
        addView(momentsTab)
        
        // 创建社区标签
        communityTab = TextView(context).apply {
            text = "社区"
            textSize = 14f
            setTextColor(theme.colors.hint)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                0,
                LayoutParams.MATCH_PARENT,
                1f
            )
            setOnClickListener {
                selectTab(TabType.COMMUNITY)
            }
        }
        addView(communityTab)
        
        // 创建下划线指示器
        underline = FrameLayout(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                4
            )
            
            // 设置背景
            val shape = ShapeDrawable(RoundRectShape(
                floatArrayOf(2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f),
                null,
                null
            )).apply {
                paint.color = theme.colors.primary
            }
            background = shape
        }
        addView(underline)
    }
    
    /**
     * 选择标签页
     */
    fun selectTab(tabType: TabType) {
        selectedTab = tabType
        
        when (tabType) {
            TabType.MOMENTS -> {
                momentsTab.setTextColor(theme.colors.primary)
                communityTab.setTextColor(theme.colors.hint)
            }
            TabType.COMMUNITY -> {
                momentsTab.setTextColor(theme.colors.hint)
                communityTab.setTextColor(theme.colors.primary)
            }
        }
        
        onTabChangeListener?.invoke(tabType)
    }
    
    /**
     * 设置标签页切换监听
     */
    fun setOnTabChangeListener(listener: (TabType) -> Unit) {
        onTabChangeListener = listener
    }
    
    /**
     * 获取选中的标签页
     */
    fun getSelectedTab(): TabType = selectedTab
}
