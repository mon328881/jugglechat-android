package com.juggle.im.android.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

/**
 * 网络状态管理器
 * 监听网络连接状态变化，在网络恢复时自动重连
 */
public class NetworkStateManager {
    private static final String TAG = "NetworkStateManager";
    private final Context context;
    private final ConnectivityManager connectivityManager;
    private final ConnectivityManager.NetworkCallback networkCallback;
    private final Callback callback;
    private boolean isRegistered = false;

    public interface Callback {
        void onNetworkAvailable();
        void onNetworkLost();
    }

    public NetworkStateManager(Context context, Callback callback) {
        this.context = context;
        this.callback = callback;
        this.connectivityManager = 
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        this.networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                Log.i(TAG, "网络已连接");
                callback.onNetworkAvailable();
            }
            
            @Override
            public void onLost(Network network) {
                Log.i(TAG, "网络已断开");
                callback.onNetworkLost();
            }
            
            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities capabilities) {
                Log.i(TAG, "网络能力已改变");
            }
        };
    }

    public void register() {
        if (isRegistered) {
            Log.w(TAG, "网络监听已注册，无需重复注册");
            return;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                NetworkRequest networkRequest = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build();
                connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
                isRegistered = true;
                Log.i(TAG, "网络监听已注册");
            } catch (Exception e) {
                Log.e(TAG, "注册网络监听失败", e);
            }
        }
    }

    public void unregister() {
        if (!isRegistered) {
            return;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
                isRegistered = false;
                Log.i(TAG, "网络监听已注销");
            } catch (Exception e) {
                Log.e(TAG, "注销网络监听失败", e);
            }
        }
    }

    public boolean isRegistered() {
        return isRegistered;
    }
}
