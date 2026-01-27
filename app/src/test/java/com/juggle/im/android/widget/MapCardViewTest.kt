package com.juggle.im.android.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.juggle.im.android.model.LocationData
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * 地图卡片视图单元测试
 * 验证: 需求 16.2, 16.3, 16.4
 */
class MapCardViewTest {
    
    private lateinit var context: Context
    private lateinit var mapCardView: MapCardView
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        mapCardView = MapCardView(context)
    }
    
    @Test
    fun testSetLocation() {
        val location = LocationData(
            locationName = "测试位置",
            latitude = 39.9042,
            longitude = 116.4074,
            address = "北京市朝阳区"
        )
        
        mapCardView.setLocation(location)
        assertEquals(location, mapCardView.getLocation())
    }
    
    @Test
    fun testLocationCoordinateString() {
        val location = LocationData(
            locationName = "测试位置",
            latitude = 39.9042,
            longitude = 116.4074
        )
        
        mapCardView.setLocation(location)
        assertEquals("39.9042,116.4074", mapCardView.getLocation()?.getCoordinateString())
    }
    
    @Test
    fun testMapClickListener() {
        var clickedLocation: LocationData? = null
        
        val location = LocationData(
            locationName = "测试位置",
            latitude = 39.9042,
            longitude = 116.4074
        )
        
        mapCardView.setOnMapClickListener { loc ->
            clickedLocation = loc
        }
        
        mapCardView.setLocation(location)
        mapCardView.performClick()
        
        assertEquals(location, clickedLocation)
    }
    
    @Test
    fun testMultipleLocationUpdates() {
        val location1 = LocationData(
            locationName = "位置1",
            latitude = 39.9042,
            longitude = 116.4074
        )
        
        val location2 = LocationData(
            locationName = "位置2",
            latitude = 31.2304,
            longitude = 121.4737
        )
        
        mapCardView.setLocation(location1)
        assertEquals("位置1", mapCardView.getLocation()?.locationName)
        
        mapCardView.setLocation(location2)
        assertEquals("位置2", mapCardView.getLocation()?.locationName)
    }
    
    @Test
    fun testValidCoordinates() {
        val location = LocationData(
            locationName = "测试",
            latitude = 39.9042,
            longitude = 116.4074
        )
        
        assertTrue(location.isValid())
    }
    
    @Test
    fun testInvalidLatitude() {
        val location = LocationData(
            locationName = "测试",
            latitude = 91.0,
            longitude = 116.4074
        )
        
        assertFalse(location.isValid())
    }
    
    @Test
    fun testInvalidLongitude() {
        val location = LocationData(
            locationName = "测试",
            latitude = 39.9042,
            longitude = 181.0
        )
        
        assertFalse(location.isValid())
    }
    
    @Test
    fun testClickWithoutListener() {
        val location = LocationData(
            locationName = "测试位置",
            latitude = 39.9042,
            longitude = 116.4074
        )
        
        mapCardView.setLocation(location)
        // 不设置监听器，直接点击，应该不抛出异常
        mapCardView.performClick()
    }
    
    @Test
    fun testClickWithoutLocation() {
        var clickedLocation: LocationData? = null
        
        mapCardView.setOnMapClickListener { loc ->
            clickedLocation = loc
        }
        
        // 不设置位置数据，直接点击
        mapCardView.performClick()
        
        assertNull(clickedLocation)
    }
}
