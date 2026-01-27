package com.juggle.im.android.model

/**
 * 位置数据模型
 * 用于表示地图上的位置信息
 */
data class LocationData(
    // 位置名称
    val locationName: String,
    // 纬度
    val latitude: Double,
    // 经度
    val longitude: Double,
    // 地址
    val address: String = "",
    // 地图缩略图 URL
    val mapThumbnailUrl: String = "",
    // 时间戳
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * 获取坐标字符串
     */
    fun getCoordinateString(): String = "$latitude,$longitude"
    
    /**
     * 检查位置数据是否有效
     */
    fun isValid(): Boolean = latitude in -90.0..90.0 && longitude in -180.0..180.0
}
