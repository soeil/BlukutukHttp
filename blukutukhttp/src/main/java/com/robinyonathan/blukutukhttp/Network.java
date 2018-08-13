package com.robinyonathan.blukutukhttp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Network {
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean connected = false;

        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                connected = activeNetworkInfo.isConnected();
            } else {
                connected = false;
            }
        }

        return connected;
    }
}
