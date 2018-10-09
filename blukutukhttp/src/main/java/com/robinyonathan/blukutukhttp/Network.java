package com.robinyonathan.blukutukhttp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

class Network {
    static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean connected = false;

        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                connected = activeNetworkInfo.isConnected();
            }
        }

        return connected;
    }
}
