package com.juggle.im.android.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.juggle.im.android.model.LocationData
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * 地图卡片交互功能属性测试
 * 验证: 需求 16.3, 16.4
 * 属性 68: 地图卡片交互功能
 */
class MapCardInteractionPropertyTest : FunSpec({
    
    val context: Context = ApplicationProvider.getApplicationContext()
    
    test("地图卡片应该支持点击查看完整地图") {
        val mapCardView = MapCardView(context)
        var mapViewRequested = false
        
        val location = LocationData(
            locationName = "测试位置",
            latitude = 39.9042,
            longitude = 116.4074,
            address = "北京市朝阳区"
        )
        
        mapCardView.setOnMapClickListener { loc ->
            mapViewRequested = true
        }
        
        mapCardView.setLocation(location)
        mapCardView.performClick()
        
        mapViewRequested shouldBe true
    }
    
    test("地图卡片点击应该传递正确的位置数据") {
        val mapCardView = MapCardView(context)
        var receivedLocation: LocationData? = null
        
        val location = LocationData(
            locationName = "测试位置",
            latitude = 39.9042,
            longitude = 116.4074,
            address = "北京市朝阳区"
        )
        
        mapCardView.setOnMapClickListener { loc ->
            receivedLocation = loc
        }
        
        mapCardView.setLocation(location)
        mapCardView.performClick()
        
        receivedLocation?.locationName shouldBe "测试位置"
        receivedLocation?.latitude shouldBe 39.9042
        receivedLocation?.longitude shouldBe 116.4074
    }
    
    test("地图卡片应该支持导航功能") {
        val mapCardView = MapCardView(context)
        var navigationLocation: LocationData? = null
        
        val location = LocationData(
            locationName = "测试位置",
            latitude = 39.9042,
            longitude = 116.4074
        )
        
        mapCardView.setOnMapClickListener { loc ->
            navigationLocation = loc
        }
        
        mapCardView.setLocation(location)
        mapCardView.performClick()
        
        // 验证位置数据有效且可用于导航
        navigationLocation?.isValid() shouldBe true
        navigationLocation?.getCoordinateString() shouldNotBe null
    }
    
    test("地图卡片应该支持多次点击") {
        val mapCardView = MapCardView(context)
        var clickCount = 0
        
        val location = LocationData(
            locationName = "测试位置",
            latitude = 39.9042,
            longitude = 116.4074
        )
        
        mapCardView.setOnMapClickListener { _ ->
            clickCount++
        }
        
        mapCardView.setLocation(location)
        
        repeat(5) {
            mapCardView.performClick()
        }
        
        clickCount shouldBe 5
    }
    
    test("地图卡片点击监听器应该可以被替换") {
        val mapCardView = MapCardView(context)
        var firstListenerCalled = false
        var secondListenerCalled = false
        
        val location = LocationData(
            locationName = "测试位置",
            latitude = 39.9042,
            longitude = 116.4074
        )
        
        // 设置第一个监听器
        mapCardView.setOnMapClickListener { _ ->
            firstListenerCalled = true
        }
        
        // 替换为第二个监听器
        mapCardView.setOnMapClickListener { _ ->
            secondListenerCalled = true
        }
        
        mapCardView.setLocation(location)
        mapCardView.performClick()
        
        firstListenerCalled shouldBe false
        secondListenerCalled shouldBe true
    }
    
    test("地图卡片应该在没有监听器时安全处理点击") {
        val mapCardView = MapCardView(context)
        
        val location = LocationData(
            locationName = "测试位置",
            latitude = 39.9042,
            longitude = 116.4074
        )
        
        mapCardView.setLocation(location)
        // 不设置监听器，直接点击
        mapCardView.performClick()
        // 验证不会抛出异常
    }
    
    test("地图卡片应该在没有位置数据时安全处理点击") {
        val mapCardView = MapCardView(context)
        var clickedLocation: LocationData? = null
        
        mapCardView.setOnMapClickListener { loc ->
            clickedLocation = loc
        }
        
        // 不设置位置数据，直接点击
        mapCardView.performClick()
        
        clickedLocation shouldBe null
    }
    
    test("地图卡片应该支持位置数据更新后的点击") {
        val mapCardView = MapCardView(context)
        var lastClickedLocation: LocationData? = null
        
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
        
        mapCardView.setOnMapClickListener { loc ->
            lastClickedLocation = loc
        }
        
        mapCardView.setLocation(location1)
        mapCardView.performClick()
        lastClickedLocation?.locationName shouldBe "位置1"
        
        mapCardView.setLocation(location2)
        mapCardView.performClick()
        lastClickedLocation?.locationName shouldBe "位置2"
    }
})
