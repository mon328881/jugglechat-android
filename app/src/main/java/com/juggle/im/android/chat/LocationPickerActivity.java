package com.juggle.im.android.chat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.juggle.im.android.R;
import com.juggle.im.android.chat.utils.CoordinateUtils;
import com.tencent.lbssearch.TencentSearch;
import com.tencent.tencentmap.mapsdk.maps.TencentMapInitializer;
import com.tencent.lbssearch.httpresponse.BaseObject;
import com.tencent.lbssearch.httpresponse.HttpResponseListener;
import com.tencent.lbssearch.httpresponse.Poi;
import com.tencent.lbssearch.object.param.Geo2AddressParam;
import com.tencent.lbssearch.object.result.Geo2AddressResultObject;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.maps.LocationSource;
import com.tencent.tencentmap.mapsdk.maps.MapView;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory;
import com.tencent.tencentmap.mapsdk.maps.model.CameraPosition;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.Marker;
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions;
import com.tencent.tencentmap.mapsdk.maps.model.MyLocationStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 地图选点页面
 */
public class LocationPickerActivity extends AppCompatActivity {

    public static final String EXTRA_LAT = "extra_lat";
    public static final String EXTRA_LNG = "extra_lng";
    public static final String EXTRA_ADDRESS = "extra_address";
    private static final int REQ_LOCATION_PERMISSION = 21001;
    private static final long LOCATION_MIN_TIME_MS = 2000L;
    private static final float LOCATION_MIN_DISTANCE_M = 5f;

    private MapView mapView;
    private TencentMap tencentMap;
    private LatLng selectedLatLng;
    private Marker selectedMarker;
    private LocationManager locationManager;
    private LocationSource.OnLocationChangedListener mapLocationListener;
    private boolean hasMovedCameraToUser;
    private volatile Location lastReportedLocation;
    private volatile boolean ignoreNextCameraIdleCallback;

    private RecyclerView recyclerLocationList;
    private LocationListAdapter locationListAdapter;
    private final List<LocationPoiItem> locationItems = new ArrayList<>();
    private String selectedAddressForResult = "";
    private TencentSearch tencentSearch;
    private EditText edtSearchPlace;
    private final ExecutorService geocoderExecutor = Executors.newSingleThreadExecutor();
    private volatile boolean isShowingSearchResults;
    private final OkHttpClient httpClient = new OkHttpClient();

    private final LocationSource locationSource = new LocationSource() {
        @Override
        public void activate(@NonNull LocationSource.OnLocationChangedListener listener) {
            mapLocationListener = listener;
            if (ContextCompat.checkSelfPermission(LocationPickerActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            startLocationUpdates();
            tryApplyLastKnownLocation();
        }

        @Override
        public void deactivate() {
            mapLocationListener = null;
            stopLocationUpdates();
        }
    };

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            Location gcj = CoordinateUtils.toGcj02Location(location);
            if (gcj == null) gcj = location;
            lastReportedLocation = gcj;
            final Location locationForCallback = gcj;
            if (mapLocationListener != null) {
                runOnUiThread(() -> {
                    if (mapLocationListener != null) {
                        mapLocationListener.onLocationChanged(locationForCallback);
                    }
                    if (!hasMovedCameraToUser && tencentMap != null) {
                        hasMovedCameraToUser = true;
                        LatLng latLng = new LatLng(locationForCallback.getLatitude(), locationForCallback.getLongitude());
                        selectedLatLng = latLng;
                        updateSelectedMarker(latLng);
                        tencentMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition(latLng, 15f, 0, 0)));
                        requestReverseGeocodeAndRefreshList(latLng);
                    }
                });
            }
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {}

        @Override
        public void onProviderDisabled(@NonNull String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 确保隐私协议在地图初始化前设置（再次确认）
        try {
            TencentMapInitializer.setAgreePrivacy(true);
        } catch (Throwable ignored) {
            // 如果地图SDK类不可用，不要崩溃
        }
        
        setContentView(R.layout.activity_location_picker);

        mapView = findViewById(R.id.map_view);
        tencentMap = mapView.getMap();

        if (tencentMap == null) {
            Toast.makeText(this, "地图初始化失败，请检查 Key 配置", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        try {
            tencentSearch = new TencentSearch(getApplicationContext());
        } catch (Throwable ignored) {
            tencentSearch = null;
        }

        tencentMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition(new LatLng(39.908823, 116.397470), 12f, 0, 0)));

        try {
            MyLocationStyle style = new MyLocationStyle();
            style.fillColor(0x332F80ED);
            style.strokeColor(0xFF2F80ED);
            style.strokeWidth(2);
            style.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER);
            tencentMap.setMyLocationStyle(style);
        } catch (Throwable ignored) {}

        tencentMap.setLocationSource(locationSource);
        tencentMap.setMyLocationEnabled(true);

        tencentMap.setOnMapClickListener(latLng -> {
            selectedLatLng = latLng;
            updateSelectedMarker(latLng);
            tencentMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        });

        tencentMap.setOnCameraChangeListener(new TencentMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {}

            @Override
            public void onCameraChangeFinished(CameraPosition cameraPosition) {
                if (ignoreNextCameraIdleCallback) {
                    ignoreNextCameraIdleCallback = false;
                    return;
                }
                if (cameraPosition == null || cameraPosition.target == null) return;
                LatLng target = cameraPosition.target;
                selectedLatLng = target;
                updateSelectedMarker(target);
                requestReverseGeocodeAndRefreshList(target);
            }
        });

        View btnCancel = findViewById(R.id.btn_cancel);
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> finish());
        }

        recyclerLocationList = findViewById(R.id.recycler_location_list);
        if (recyclerLocationList != null) {
            recyclerLocationList.setLayoutManager(new LinearLayoutManager(this));
            locationListAdapter = new LocationListAdapter(locationItems, (item, position) -> {
                ignoreNextCameraIdleCallback = true;
                selectedLatLng = new LatLng(item.lat, item.lng);
                updateSelectedMarker(selectedLatLng);
                tencentMap.moveCamera(CameraUpdateFactory.newLatLng(selectedLatLng));
                selectedAddressForResult = (item.title != null && !item.title.isEmpty())
                        ? item.title
                        : (item.address != null ? item.address : "");
                locationListAdapter.setSelectedIndex(position);
            });
            recyclerLocationList.setAdapter(locationListAdapter);
        }

        edtSearchPlace = findViewById(R.id.edt_search_place);
        if (edtSearchPlace != null) {
            edtSearchPlace.setOnEditorActionListener((v, actionId, event) -> {
                boolean isSearchAction = actionId == EditorInfo.IME_ACTION_SEARCH
                        || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
                if (!isSearchAction) return false;
                String keyword = v.getText() != null ? v.getText().toString().trim() : "";
                if (keyword.isEmpty()) {
                    Toast.makeText(LocationPickerActivity.this,
                            R.string.location_toast_search_empty, Toast.LENGTH_SHORT).show();
                    return true;
                }
                searchPlace(keyword);
                return true;
            });

            edtSearchPlace.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if (s == null || s.toString().trim().isEmpty()) {
                        if (isShowingSearchResults) {
                            isShowingSearchResults = false;
                            if (selectedLatLng != null) requestReverseGeocodeAndRefreshList(selectedLatLng);
                        }
                    }
                }
            });
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQ_LOCATION_PERMISSION);
        }

        View btnRelocate = findViewById(R.id.btn_relocate);
        if (btnRelocate != null) {
            btnRelocate.setOnClickListener(v -> moveCameraToMyLocation());
        }

        Button btnSend = findViewById(R.id.btn_send_location);
        if (btnSend != null) {
            btnSend.setOnClickListener(v -> {
                if (selectedLatLng == null) {
                    Toast.makeText(LocationPickerActivity.this,
                            R.string.location_toast_select_first, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_CANCELED);
                } else {
                    Intent data = new Intent();
                    data.putExtra(EXTRA_LAT, selectedLatLng.latitude);
                    data.putExtra(EXTRA_LNG, selectedLatLng.longitude);
                    String addr = selectedAddressForResult != null && !selectedAddressForResult.isEmpty()
                            ? selectedAddressForResult
                            : (selectedLatLng.latitude + "," + selectedLatLng.longitude);
                    data.putExtra(EXTRA_ADDRESS, addr);
                    setResult(RESULT_OK, data);
                    android.util.Log.d("LocationPicker", "Returning location: lat=" + selectedLatLng.latitude + 
                            ", lng=" + selectedLatLng.longitude + ", addr=" + addr);
                }
                finish();
            });
        } else {
            android.util.Log.e("LocationPicker", "btn_send_location not found!");
        }
    }

    private void requestReverseGeocodeAndRefreshList(LatLng latLng) {
        if (latLng == null) return;
        if (tencentSearch == null) {
            fillListWithFallback(latLng);
            return;
        }

        Geo2AddressParam param = new Geo2AddressParam(latLng)
                .getPoi(true)
                .setPoiOptions(new Geo2AddressParam.PoiOptions()
                        .setRadius(1000)
                        .setPageSize(10));

        tencentSearch.geo2address(param, new HttpResponseListener<BaseObject>() {
            @Override
            public void onSuccess(int statusCode, BaseObject baseObject) {
                if (!(baseObject instanceof Geo2AddressResultObject)) {
                    fillListWithFallback(latLng);
                    return;
                }
                Geo2AddressResultObject obj = (Geo2AddressResultObject) baseObject;
                Geo2AddressResultObject.ReverseAddressResult res = obj.result;
                runOnUiThread(() -> {
                    locationItems.clear();
                    if (res != null) {
                        String addr = res.address;
                        String title = (addr != null && !addr.isEmpty())
                                ? addr
                                : String.format("%.6f, %.6f", latLng.latitude, latLng.longitude);
                        LocationPoiItem current = new LocationPoiItem(
                                title, "", latLng.latitude, latLng.longitude
                        );
                        current.address = addr;
                        locationItems.add(current);

                        if (res.pois != null) {
                            for (int i = 0; i < res.pois.size() && i < 10; i++) {
                                Poi p = res.pois.get(i);
                                if (p == null) continue;
                                String poiTitle = p.title;
                                if (poiTitle == null || poiTitle.isEmpty()) {
                                    poiTitle = p.address;
                                }
                                String distanceText;
                                if (p._distance >= 1000f) {
                                    distanceText = String.format("%.1f km", p._distance / 1000f);
                                } else {
                                    distanceText = String.format("%d m", (int) p._distance);
                                }
                                String subtitle = distanceText;
                                if (p.address != null && !p.address.isEmpty()) {
                                    subtitle = subtitle + " | " + p.address;
                                }
                                LatLng poiLatLng = p.latLng != null ? p.latLng : latLng;
                                LocationPoiItem item = new LocationPoiItem(
                                        poiTitle, subtitle, poiLatLng.latitude, poiLatLng.longitude
                                );
                                item.address = p.address;
                                locationItems.add(item);
                            }
                        }
                    } else {
                        fillListWithFallback(latLng);
                        return;
                    }

                    if (!locationItems.isEmpty()) {
                        selectedAddressForResult = locationItems.get(0).title;
                    }
                    if (locationListAdapter != null) {
                        locationListAdapter.setList(locationItems);
                        locationListAdapter.setSelectedIndex(0);
                    }
                });
            }

            @Override
            public void onFailure(int statusCode, String responseString, Throwable throwable) {
                runOnUiThread(() -> fillListWithFallback(latLng));
            }
        });
    }

    private void fillListWithFallback(LatLng latLng) {
        locationItems.clear();
        LocationPoiItem current = new LocationPoiItem(
                String.format("%.6f, %.6f", latLng.latitude, latLng.longitude),
                "", latLng.latitude, latLng.longitude
        );
        current.address = current.title;
        locationItems.add(current);
        selectedAddressForResult = current.title;
        if (locationListAdapter != null) {
            locationListAdapter.setList(locationItems);
            locationListAdapter.setSelectedIndex(0);
        }
    }

    private void startLocationUpdates() {
        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        }
        if (locationManager == null || mapLocationListener == null) return;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;
        try {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                try {
                    locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, Looper.getMainLooper());
                } catch (Throwable ignored) {}
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        LOCATION_MIN_TIME_MS, LOCATION_MIN_DISTANCE_M, locationListener, Looper.getMainLooper());
            }
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        LOCATION_MIN_TIME_MS, LOCATION_MIN_DISTANCE_M, locationListener, Looper.getMainLooper());
            }
        } catch (SecurityException ignored) {}
    }

    private void stopLocationUpdates() {
        if (locationManager != null && locationListener != null) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (SecurityException ignored) {}
        }
    }

    private void tryApplyLastKnownLocation() {
        if (locationManager == null || mapLocationListener == null || hasMovedCameraToUser) return;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;
        Location loc = null;
        try {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (loc == null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        } catch (SecurityException ignored) {}
        if (loc != null) {
            Location gcj = CoordinateUtils.toGcj02Location(loc);
            if (gcj == null) gcj = loc;
            lastReportedLocation = gcj;
            mapLocationListener.onLocationChanged(gcj);
            if (!hasMovedCameraToUser && tencentMap != null) {
                hasMovedCameraToUser = true;
                LatLng latLng = new LatLng(gcj.getLatitude(), gcj.getLongitude());
                selectedLatLng = latLng;
                updateSelectedMarker(latLng);
                tencentMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                        new CameraPosition(latLng, 15f, 0, 0)));
                requestReverseGeocodeAndRefreshList(latLng);
            }
        } else {
            Toast.makeText(this, "正在获取位置…，可先在地图上选点", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSelectedMarker(LatLng latLng) {
        if (tencentMap == null) return;
        if (selectedMarker != null) {
            selectedMarker.remove();
            selectedMarker = null;
        }
        try {
            selectedMarker = tencentMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_pin_green))
                    .anchor(0.5f, 1f)
                    .title("已选位置"));
        } catch (Throwable t) {
            selectedMarker = tencentMap.addMarker(new MarkerOptions(latLng).title("已选位置"));
        }
    }

    private void moveCameraToMyLocation() {
        if (lastReportedLocation != null && tencentMap != null) {
            LatLng latLng = new LatLng(lastReportedLocation.getLatitude(), lastReportedLocation.getLongitude());
            tencentMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition(latLng, 15f, 0, 0)));
        } else {
            Toast.makeText(this, "尚未获取到当前位置，请稍候或在地图上选点", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mapLocationListener != null) {
                    startLocationUpdates();
                    tryApplyLastKnownLocation();
                }
            } else {
                Toast.makeText(this, "未授予定位权限，可在地图上手动选点", Toast.LENGTH_SHORT).show();
            }
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
    protected void onDestroy() {
        stopLocationUpdates();
        try { geocoderExecutor.shutdownNow(); } catch (Throwable ignored) {}
        super.onDestroy();
        if (mapView != null) mapView.onDestroy();
    }

    private void searchPlace(String keyword) {
        String key = getTencentMapKey();
        if (key == null || key.trim().isEmpty()) {
            Toast.makeText(this, "地图 Key 未配置", Toast.LENGTH_SHORT).show();
            return;
        }
        searchPlaceByTencentSuggestion(keyword, key.trim());
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

    private void searchPlaceByTencentSuggestion(String keyword, String key) {
        final LatLng anchor = selectedLatLng;
        isShowingSearchResults = true;
        geocoderExecutor.execute(() -> {
            try {
                HttpUrl.Builder urlBuilder = HttpUrl.parse("https://apis.map.qq.com/ws/place/v1/suggestion")
                        .newBuilder()
                        .addQueryParameter("keyword", keyword)
                        .addQueryParameter("key", key)
                        .addQueryParameter("page_size", "10");
                if (anchor != null) {
                    urlBuilder.addQueryParameter("location", anchor.latitude + "," + anchor.longitude);
                    urlBuilder.addQueryParameter("policy", "1");
                }
                Request req = new Request.Builder().url(urlBuilder.build()).get().build();
                try (Response resp = httpClient.newCall(req).execute()) {
                    if (!resp.isSuccessful() || resp.body() == null) {
                        throw new RuntimeException("http " + resp.code());
                    }
                    String body = resp.body().string();
                    JsonObject root = new JsonParser().parse(body).getAsJsonObject();
                    int status = root.has("status") ? root.get("status").getAsInt() : -1;
                    if (status != 0) {
                        throw new RuntimeException("api status=" + status);
                    }
                    JsonArray data = root.has("data") && root.get("data").isJsonArray()
                            ? root.getAsJsonArray("data")
                            : new JsonArray();

                    List<LocationPoiItem> results = new ArrayList<>();
                    for (JsonElement el : data) {
                        if (el == null || !el.isJsonObject()) continue;
                        JsonObject o = el.getAsJsonObject();
                        String title = o.has("title") ? safeAsString(o.get("title")) : "";
                        String addr = o.has("address") ? safeAsString(o.get("address")) : "";

                        double lat = 0, lng = 0;
                        if (o.has("location") && o.get("location").isJsonObject()) {
                            JsonObject loc = o.getAsJsonObject("location");
                            if (loc.has("lat")) lat = loc.get("lat").getAsDouble();
                            if (loc.has("lng")) lng = loc.get("lng").getAsDouble();
                        }
                        if (lat == 0 && lng == 0) continue;

                        String showTitle = (title != null && !title.trim().isEmpty()) ? title.trim() : keyword;
                        LocationPoiItem item = new LocationPoiItem(showTitle, addr, lat, lng);
                        item.address = (addr != null && !addr.trim().isEmpty()) ? addr.trim() : showTitle;
                        results.add(item);
                        if (results.size() >= 10) break;
                    }

                    runOnUiThread(() -> {
                        if (results.isEmpty()) {
                            Toast.makeText(LocationPickerActivity.this,
                                    R.string.location_toast_no_result, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        locationItems.clear();
                        locationItems.addAll(results);
                        if (locationListAdapter != null) {
                            locationListAdapter.setList(locationItems);
                            locationListAdapter.setSelectedIndex(0);
                        }
                        LocationPoiItem first = results.get(0);
                        ignoreNextCameraIdleCallback = true;
                        selectedLatLng = new LatLng(first.lat, first.lng);
                        updateSelectedMarker(selectedLatLng);
                        if (tencentMap != null) {
                            tencentMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                                    new CameraPosition(selectedLatLng, 15f, 0, 0)));
                        }
                        selectedAddressForResult = (first.title != null && !first.title.isEmpty())
                                ? first.title
                                : (first.address != null ? first.address : "");
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(LocationPickerActivity.this,
                        "搜索失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private static String safeAsString(JsonElement el) {
        try {
            if (el == null || el.isJsonNull()) return "";
            return el.getAsString();
        } catch (Exception e) {
            return "";
        }
    }

    // 内部类：POI 项
    static class LocationPoiItem {
        String title;
        String subtitle;
        double lat;
        double lng;
        String address;

        LocationPoiItem(String title, String subtitle, double lat, double lng) {
            this.title = title;
            this.subtitle = subtitle;
            this.lat = lat;
            this.lng = lng;
        }
    }

    // 内部类：列表适配器
    static class LocationListAdapter extends RecyclerView.Adapter<LocationListAdapter.VH> {
        private final List<LocationPoiItem> list = new ArrayList<>();
        private final OnItemClickListener listener;
        private int selectedIndex = 0;

        interface OnItemClickListener {
            void onItemClick(LocationPoiItem item, int position);
        }

        LocationListAdapter(List<LocationPoiItem> initial, OnItemClickListener listener) {
            this.listener = listener;
            if (initial != null) list.addAll(initial);
        }

        void setList(List<LocationPoiItem> newList) {
            list.clear();
            if (newList != null) list.addAll(newList);
            notifyDataSetChanged();
        }

        void setSelectedIndex(int index) {
            int old = selectedIndex;
            selectedIndex = index;
            if (old >= 0 && old < list.size()) notifyItemChanged(old);
            if (selectedIndex >= 0 && selectedIndex < list.size()) notifyItemChanged(selectedIndex);
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location_picker, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            LocationPoiItem item = list.get(position);
            holder.title.setText(item.title != null ? item.title : "");
            holder.subtitle.setText(item.subtitle != null ? item.subtitle : "");
            holder.check.setVisibility(position == selectedIndex ? View.VISIBLE : View.GONE);
            if (holder.divider != null) {
                holder.divider.setVisibility(position == 0 ? View.GONE : View.VISIBLE);
            }
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item, position);
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView title, subtitle;
            ImageView check;
            View divider;

            VH(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.item_location_title);
                subtitle = itemView.findViewById(R.id.item_location_subtitle);
                check = itemView.findViewById(R.id.item_location_check);
                divider = itemView.findViewById(R.id.item_location_divider);
            }
        }
    }
}
