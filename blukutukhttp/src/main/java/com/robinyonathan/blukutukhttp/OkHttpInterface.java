package com.robinyonathan.blukutukhttp;

import android.net.Uri;

import java.io.File;
import java.util.HashMap;

import okhttp3.RequestBody;

public interface OkHttpInterface {
    Boolean isAcceptAllCertificate();

    File downloadPath();

    int getBodyType();

    RequestBody requestBody();

    String downloadFileName();

    String paternCertificate();

    String pinCertificate();

    String url();

    Uri.Builder builder();

    HashMap<Integer, Integer> getConnectionTimeOut();

    void before();

    void progress(int progress);

    void status(int code, String message);

    void after(Object o);
}
