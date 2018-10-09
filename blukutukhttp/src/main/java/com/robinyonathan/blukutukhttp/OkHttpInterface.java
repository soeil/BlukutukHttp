package com.robinyonathan.blukutukhttp;

import android.net.Uri;

import java.io.File;

import okhttp3.RequestBody;

public interface OkHttpInterface {
    Boolean isAcceptAllCertificate();

    File downloadPath();

    RequestBody requestBody();

    String downloadFileName();

    String paternCertificate();

    String pinCertificate();

    String url();

    Uri.Builder builder();

    void before();

    void progress(int progress);

    void status(int code, String message);

    void after(Object o);
}
