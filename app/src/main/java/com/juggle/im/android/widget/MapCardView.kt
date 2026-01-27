package com.juggle.im.android.widget

import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.juggle.im.android.model.LocationData
import com.juggle.im.android.theme.ThemeManager
import com.juggle.im.android.utils.AccessibilityUtils
import com.juggle.im.android.utils.ImageLoadingUtils

/**
 * 地图卡片消息组件
 * 显示位置名称、坐标和地图缩略图
 * 支持点击查看完整地图
 * 验证: 需求 16.2, 16.3, 16.4
 */
class MapCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    
    private val themeManager: ThemeManager = ThemeManager.getInstance(context)
    private val theme = themeManager.getCurrentTheme()
    
    private val cardContainer: LinearLayout
    private val mapThumbnail: ImageView
    private val locationNameText: TextView
    private val coordinateText: TextView
    private val addressText: TextView
    
    private var onMapClickListener: ((LocationData) -> Unit)? = null
    private var currentLocation: LocationData? = null
    
    init {
        // 创建卡片容器
        cardContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
            setPadding(theme.spacing.md, theme.spacing.md, theme.spacing.md, theme.spacing.md)
            
            // 设置背景
            val shape = ShapeDrawable(RoundRectShape(
                floatArrayOf(8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f),
                null,
                null
            )).apply {
                paint.color = theme.colors.surface
            }
            background = shape
        }
        
        // 创建地图缩略图
        mapThumbnail = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                200
            ).apply {
                setMargins(0, 0, 0, theme.spacing.md)
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(theme.colors.background)
            
            // 设置圆角
            val shape = ShapeDrawable(RoundRectShape(
                floatArrayOf(6f, 6f, 6f, 6f, 6f, 6f, 6f, 6f),
                null,
                null
            )).apply {
                paint.color = theme.colors.background
            }
            background = shape
        }
        cardContainer.addView(mapThumbnail)
        
        // 创建位置名称文本
        locationNameText = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, theme.spacing.sm)
            }
            textSize = 16f
            setTextColor(theme.colors.onBackground)
            maxLines = 1
        }
        cardContainer.addView(locationNameText)
        
        // 创建坐标文本
        coordinateText = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, theme.spacing.xs)
            }
            textSize = 12f
            setTextColor(theme.colors.onBackground.let { 
                (it and 0xFFFFFF) or 0x80000000.toInt()
            })
            maxLines = 1
        }
        cardContainer.addView(coordinateText)
        
        // 创建地址文本
        addressText = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
            textSize = 12f
            setTextColor(theme.colors.onBackground.let { 
                (it and 0xFFFFFF) or 0x80000000.toInt()
            })
            maxLines = 2
        }
        cardContainer.addView(addressText)
        
        // 设置点击监听
        cardContainer.setOnClickListener {
            currentLocation?.let { location ->
                onMapClickListener?.invoke(location)
            }
        }
        
        addView(cardContainer)
    }
    
    /**
     * 设置位置数据
     */
    fun setLocation(location: LocationData) {
        currentLocation = location
        
        // 更新位置名称
        locationNameText.text = location.locationName
        
        // 更新坐标
        coordinateText.text = location.getCoordinateString()
        
        // 更新地址
        addressText.text = location.address
        
        // 加载地图缩略图
        if (location.mapThumbnailUrl.isNotEmpty()) {
            ImageLoadingUtils.loadImage(
                context,
                location.mapThumbnailUrl,
                mapThumbnail,
                200,
                200
            )
        }
        
        // 设置无障碍标签
        setAccessibilityLabel(location)
    }
    
    /**
     * 设置地图点击监听器
     */
    fun setOnMapClickListener(listener: (LocationData) -> Unit) {
        onMapClickListener = listener
    }
    
    /**
     * 获取当前位置数据
     */
    fun getLocation(): LocationData? = currentLocation
    
    /**
     * 设置无障碍标签
     */
    private fun setAccessibilityLabel(location: LocationData) {
        val label = "位置卡片: ${location.locationName}, 坐标: ${location.getCoordinateString()}"
        AccessibilityUtils.setViewAccessibilityLabel(cardContainer, label)
    }
}
