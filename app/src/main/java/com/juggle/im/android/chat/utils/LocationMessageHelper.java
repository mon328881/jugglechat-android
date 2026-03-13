package com.juggle.im.android.chat.utils;

/**
 * 位置消息辅助类
 * 用于构建和解析位置消息内容
 * 格式: [LOCATION]lat,lng|address
 */
public class LocationMessageHelper {
    
    private static final String PREFIX = "[LOCATION]";
    private static final String SEP_COORD = ",";
    private static final String SEP_ADDR = "|";
    
    /**
     * 构建位置消息内容
     * 格式: [LOCATION]lat,lng|address
     */
    public static String buildContent(double latitude, double longitude, String address) {
        if (address == null || address.isEmpty()) {
            address = String.format("%.6f,%.6f", latitude, longitude);
        }
        return PREFIX + latitude + SEP_COORD + longitude + SEP_ADDR + address;
    }
    
    /**
     * 判断是否为位置消息
     */
    public static boolean isLocationContent(String content) {
        return content != null && content.startsWith(PREFIX);
    }
    
    /**
     * 兼容旧方法名
     */
    public static boolean isLocationMessage(String content) {
        return isLocationContent(content);
    }
    
    /**
     * 解析位置消息，返回 [lat, lng]，解析失败返回 null
     */
    public static double[] parseLatLng(String content) {
        if (!isLocationContent(content)) return null;
        
        String rest = content.substring(PREFIX.length());
        int i = rest.indexOf(SEP_ADDR);
        if (i < 0) return null;
        
        String coordStr = rest.substring(0, i);
        String[] parts = coordStr.split(SEP_COORD);
        if (parts.length != 2) return null;
        
        try {
            double lat = Double.parseDouble(parts[0]);
            double lng = Double.parseDouble(parts[1]);
            return new double[]{lat, lng};
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 解析地址
     */
    public static String parseAddress(String content) {
        if (!isLocationContent(content)) return null;
        
        int i = content.indexOf(SEP_ADDR);
        if (i < 0) return "";
        
        String address = content.substring(i + SEP_ADDR.length());
        return address.isEmpty() ? null : address;
    }
    
    /**
     * 从消息内容中提取纬度
     */
    public static double extractLatitude(String content) {
        double[] latLng = parseLatLng(content);
        return latLng != null ? latLng[0] : 0;
    }
    
    /**
     * 从消息内容中提取经度
     */
    public static double extractLongitude(String content) {
        double[] latLng = parseLatLng(content);
        return latLng != null ? latLng[1] : 0;
    }
    
    /**
     * 从消息内容中提取地址
     */
    public static String extractAddress(String content) {
        String addr = parseAddress(content);
        return addr != null ? addr : "";
    }
}
