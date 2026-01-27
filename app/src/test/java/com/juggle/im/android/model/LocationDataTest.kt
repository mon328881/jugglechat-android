package com.juggle.im.android.model

import org.junit.Test
import org.junit.Assert.*

/**
 * 位置数据模型单元测试
 * 验证: 需求 16.1, 16.2
 */
class LocationDataTest {
    
    @Test
    fun testLocationDataCreation() {
        val location = LocationData(
            locationName = "测试位置",
            latitude = 39.9042,
            longitude = 116.4074,
            address = "北京市朝阳区"
        )
        
        assertEquals("测试位置", location.locationName)
        assertEquals(39.9042, location.latitude, 0.0001)
        assertEquals(116.4074, location.longitude, 0.0001)
        assertEquals("北京市朝阳区", location.address)
    }
    
    @Test
    fun testCoordinateString() {
        val location = LocationData(
            locationName = "测试",
            latitude = 39.9042,
            longitude = 116.4074
        )
        
        assertEquals("39.9042,116.4074", location.getCoordinateString())
    }
    
    @Test
    fun testValidCoordinates() {
        val validLocation = LocationData(
            locationName = "测试",
            latitude = 39.9042,
            longitude = 116.4074
        )
        
        assertTrue(validLocation.isValid())
    }
    
    @Test
    fun testInvalidLatitudeHigh() {
        val invalidLocation = LocationData(
            locationName = "测试",
            latitude = 91.0,
            longitude = 116.4074
        )
        
        assertFalse(invalidLocation.isValid())
    }
    
    @Test
    fun testInvalidLatitudeLow() {
        val invalidLocation = LocationData(
            locationName = "测试",
            latitude = -91.0,
            longitude = 116.4074
        )
        
        assertFalse(invalidLocation.isValid())
    }
    
    @Test
    fun testInvalidLongitudeHigh() {
        val invalidLocation = LocationData(
            locationName = "测试",
            latitude = 39.9042,
            longitude = 181.0
        )
        
        assertFalse(invalidLocation.isValid())
    }
    
    @Test
    fun testInvalidLongitudeLow() {
        val invalidLocation = LocationData(
            locationName = "测试",
            latitude = 39.9042,
            longitude = -181.0
        )
        
        assertFalse(invalidLocation.isValid())
    }
    
    @Test
    fun testBoundaryCoordinates() {
        val northPole = LocationData(
            locationName = "北极",
            latitude = 90.0,
            longitude = 0.0
        )
        assertTrue(northPole.isValid())
        
        val southPole = LocationData(
            locationName = "南极",
            latitude = -90.0,
            longitude = 0.0
        )
        assertTrue(southPole.isValid())
        
        val dateLine = LocationData(
            locationName = "日期线",
            latitude = 0.0,
            longitude = 180.0
        )
        assertTrue(dateLine.isValid())
        
        val antiDateLine = LocationData(
            locationName = "反日期线",
            latitude = 0.0,
            longitude = -180.0
        )
        assertTrue(antiDateLine.isValid())
    }
    
    @Test
    fun testTimestamp() {
        val location = LocationData(
            locationName = "测试",
            latitude = 39.9042,
            longitude = 116.4074
        )
        
        assertTrue(location.timestamp > 0)
    }
    
    @Test
    fun testDefaultValues() {
        val location = LocationData(
            locationName = "测试",
            latitude = 39.9042,
            longitude = 116.4074
        )
        
        assertEquals("", location.address)
        assertEquals("", location.mapThumbnailUrl)
        assertTrue(location.timestamp > 0)
    }
}
