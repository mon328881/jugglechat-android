package com.juggle.im.android.chat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.juggle.im.android.R;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.maps.MapView;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.TencentMapInitializer;
import com.tencent.tencentmap.mapsdk.maps.model.CameraPosition;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions;

/**
 * 位置消息预览页面
 */
public class LocationPreviewActivity extends AppCompatActivity {

    public static final String EXTRA_LAT = "extra_lat";
    public static final String EXTRA_LNG = "extra_lng";
    public static final String EXTRA_ADDRESS = "extra_address";

    private MapView mapView;
    private TencentMap tencentMap;
    private double lat;
    private double lng;
    private String address;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 确保隐私协议在地图初始化前设置（再次确认）
        try {
            TencentMapInitializer.setAgreePrivacy(true);
        } catch (Throwable ignored) {
            // 如果地图SDK类不可用，不要崩溃
        }
        
        setContentView(R.layout.activity_location_preview);

        lat = getIntent().getDoubleExtra(EXTRA_LAT, 0);
        lng = getIntent().getDoubleExtra(EXTRA_LNG, 0);
        address = getIntent().getStringExtra(EXTRA_ADDRESS);
        if (address == null) address = "";

        mapView = findViewById(R.id.map_view);
        tencentMap = mapView.getMap();
        if (tencentMap == null) {
            Toast.makeText(this, "地图初始化失败", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        LatLng point = new LatLng(lat, lng);
        tencentMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition(point, 15f, 0, 0)));
        tencentMap.addMarker(new MarkerOptions(point).title(address.isEmpty() ? "位置" : address));

        Button btnOpenMap = findViewById(R.id.btn_open_map);
        Button btnNav = findViewById(R.id.btn_navigate);
        btnOpenMap.setOnClickListener(v -> openInTencentMap());
        btnNav.setOnClickListener(v -> navigate());
    }

    private void openInTencentMap() {
        String url = "https://apis.map.qq.com/uri/v1/marker?marker=coord:" + lat + "," + lng
                + ";title=" + Uri.encode(address.isEmpty() ? "位置" : address);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setPackage("com.tencent.map");
        try {
            startActivity(intent);
        } catch (Exception e) {
            intent.setPackage(null);
            startActivity(Intent.createChooser(intent, getString(R.string.location_open_in_map)));
        }
    }

    private void navigate() {
        String key = getTencentMapKey();
        String toName = address.isEmpty() ? ("位置 " + lat + "," + lng) : address;
        String url = "qqmap://map/routeplan?type=drive"
                + "&from=当前位置&fromcoord=CurrentLocation"
                + "&to=" + Uri.encode(toName)
                + "&tocoord=" + lat + "," + lng
                + (key != null ? "&referer=" + Uri.encode(key) : "");
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setPackage("com.tencent.map");
        try {
            startActivity(intent);
        } catch (Exception e) {
            intent.setPackage(null);
            try {
                startActivity(Intent.createChooser(intent, getString(R.string.location_navigate)));
            } catch (Exception e2) {
                Toast.makeText(this, "未安装可用的地图应用", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getTencentMapKey() {
        try {
            return getPackageManager()
                    .getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA)
                    .metaData.getString("TencentMapSDK");
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onStart() { super.onStart(); if (mapView != null) mapView.onStart(); }
    @Override
    protected void onResume() { super.onResume(); if (mapView != null) mapView.onResume(); }
    @Override
    protected void onPause() { super.onPause(); if (mapView != null) mapView.onPause(); }
    @Override
    protected void onStop() { super.onStop(); if (mapView != null) mapView.onStop(); }
    @Override
    protected void onDestroy() { super.onDestroy(); if (mapView != null) mapView.onDestroy(); }
}
