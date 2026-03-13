package com.juggle.im.android.chat.utils;

import android.location.Location;

/**
 * 坐标转换工具类
 * 用于 WGS84 和 GCJ02 坐标系的转换
 */
public class CoordinateUtils {
    private static final double EARTH_RADIUS = 6370996.81;
    private static final double PI = 3.14159265358979324;
    private static final double MCBAND[] = {12890595.86, 8362377.87, 5591021, 3481989.83, 1678043.12, 0};
    private static final double MC2LL[][] = {
            {1.289059486E7, 8362377.87, 5591021, 3481989.83, 1678043.12, 0},
            {12890595.86, 8362377.87, 5591021, 3481989.83, 1678043.12, 0},
            {12890595.86, 8362377.87, 5591021, 3481989.83, 1678043.12, 0},
            {12890595.86, 8362377.87, 5591021, 3481989.83, 1678043.12, 0},
            {12890595.86, 8362377.87, 5591021, 3481989.83, 1678043.12, 0},
            {12890595.86, 8362377.87, 5591021, 3481989.83, 1678043.12, 0}
    };
    private static final double LL2MC[][] = {
            {1.289059486E7, 8362377.87, 5591021, 3481989.83, 1678043.12, 0},
            {12890595.86, 8362377.87, 5591021, 3481989.83, 1678043.12, 0},
            {12890595.86, 8362377.87, 5591021, 3481989.83, 1678043.12, 0},
            {12890595.86, 8362377.87, 5591021, 3481989.83, 1678043.12, 0},
            {12890595.86, 8362377.87, 5591021, 3481989.83, 1678043.12, 0},
            {12890595.86, 8362377.87, 5591021, 3481989.83, 1678043.12, 0}
    };

    /**
     * 将 WGS84 坐标转换为 GCJ02 坐标
     */
    public static Location toGcj02Location(Location wgs84Location) {
        if (wgs84Location == null) return null;
        try {
            double[] gcj = toGcj02(wgs84Location.getLatitude(), wgs84Location.getLongitude());
            Location gcjLocation = new Location(wgs84Location);
            gcjLocation.setLatitude(gcj[0]);
            gcjLocation.setLongitude(gcj[1]);
            return gcjLocation;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * WGS84 转 GCJ02
     */
    private static double[] toGcj02(double lat, double lng) {
        if (outOfChina(lat, lng)) {
            return new double[]{lat, lng};
        }
        double dLat = transformLat(lng - 105.0, lat - 35.0);
        double dLng = transformLng(lng - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * PI;
        double magic = Math.sin(radLat);
        magic = 1 - 0.00669342162296594323 * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((EARTH_RADIUS * (1 - 0.00669342162296594323)) / (magic * sqrtMagic) * PI);
        dLng = (dLng * 180.0) / (EARTH_RADIUS / sqrtMagic * Math.cos(radLat) * PI);
        double mgLat = lat + dLat;
        double mgLng = lng + dLng;
        return new double[]{mgLat, mgLng};
    }

    private static boolean outOfChina(double lat, double lng) {
        if (lng < 73.56 || lng > 135.05 || lat < 3.86 || lat > 53.55) {
            return true;
        }
        return false;
    }

    private static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += ((20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0);
        ret += ((20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0);
        ret += ((160.0 * Math.sin(y / 12.0 * PI) + 320 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0);
        return ret;
    }

    private static double transformLng(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += ((20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0);
        ret += ((20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0);
        ret += ((150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0);
        return ret;
    }
}
