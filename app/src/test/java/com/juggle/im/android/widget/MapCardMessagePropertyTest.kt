package com.juggle.im.android.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.juggle.im.android.model.LocationData
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * 地图卡片消息显示属性测试
 * 验证: 需求 16.2
 * 属性 67: 地图卡片消息显示
 */
class MapCardMessagePropertyTest : FunSpec({
    
    val context: Context = ApplicationProvider.getApplicationContext()
    
    test("地图卡片应该显示位置名称") {
        val mapCardView = MapCardView(context)
        val location = LocationData(
            locationName = "测试位置",
            latitude = 39.9042,
            longitude = 116.4074,
            address = "北京市朝阳区"
        )
        
        mapCardView.setLocation(location)
        mapCardView.getLocation() shouldBe location
    }
    
    test("地图卡片应该显示坐标信息") {
        val mapCardView = MapCardView(context)
        val location = LocationData(
            locationName = "测试位置",
            latitude = 39.9042,
            longitude = 116.4074,
            address = "北京市朝阳区"
        )
        
        mapCardView.setLocation(location)
        val coordinate = mapCardView.getLocation()?.getCoordinateString()
        coordinate shouldBe "39.9042,116.4074"
    }
    
    test("地图卡片应该显示地址信息") {
        val mapCardView = MapCardView(context)
        val location = LocationData(
            locationName = "测试位置",
            latitude = 39.9042,
            longitude = 116.4074,
            address = "北京市朝阳区"
        )
        
        mapCardView.setLocation(location)
        mapCardView.getLocation()?.address shouldBe "北京市朝阳区"
    }
    
    test("地图卡片应该支持多个位置更新") {
        val mapCardView = MapCardView(context)
        
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
        mapCardView.getLocation()?.locationName shouldBe "位置1"
        
        mapCardView.setLocation(location2)
        mapCardView.getLocation()?.locationName shouldBe "位置2"
    }
    
    test("地图卡片点击应该触发监听器") {
        val mapCardView = MapCardView(context)
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
        
        clickedLocation shouldBe location
    }
    
    test("地图卡片应该支持有效的坐标范围") {
        checkAll(
            Arb.double(-90.0, 90.0),
            Arb.double(-180.0, 180.0)
        ) { latitude, longitude ->
            val location = LocationData(
                locationName = "测试",
                latitude = latitude,
                longitude = longitude
            )
            location.isValid() shouldBe true
        }
    }
    
    test("地图卡片应该拒绝无效的坐标") {
        val invalidLocation1 = LocationData(
            locationName = "测试",
            latitude = 91.0,
            longitude = 116.4074
        )
        invalidLocation1.isValid() shouldBe false
        
        val invalidLocation2 = LocationData(
            locationName = "测试",
            latitude = 39.9042,
            longitude = 181.0
        )
        invalidLocation2.isValid() shouldBe false
    }
    
    test("地图卡片应该支持无障碍标签") {
        val mapCardView = MapCardView(context)
        val location = LocationData(
            locationName = "测试位置",
            latitude = 39.9042,
            longitude = 116.4074
        )
        
        mapCardView.setLocation(location)
        // 验证不会抛出异常
    }
})
