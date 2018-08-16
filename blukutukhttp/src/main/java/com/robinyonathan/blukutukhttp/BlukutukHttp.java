package com.robinyonathan.blukutukhttp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.google.gson.internal.Primitives;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public class BlukutukHttp {
    private Activity activity;

    private BlukutukFail blukutukFail;
    private BlukutukJsonObject blukutukJsonObject;
    private BlukutukJsonArray blukutukJsonArray;
    private BlukutukModel blukutukModel;
    private BlukutukUploadProgress blukutukUploadProgress;
    private BlukutukDownload blukutukDownload;
    private ProgressBar progressBar;

    private static boolean isAcceptAllCertificate = false;

    private static String paternCertificate = "";
    private static String pinCertificate = "";
    private static String downloadPath = "";
    private static String downloadFileName = "";

    private static int responseCode = 200;

    private ProgressDialog progressDialog;

    private static RequestBody requestBody;

    private static String responseMessage = "";

    private static Uri.Builder builder;

    private Class<Object> model;

    public BlukutukHttp(Activity activity, Uri.Builder builder, RequestBody requestBody) {
        this.activity = activity;
        this.builder = builder;
        this.requestBody = requestBody;
    }

    public void useProgressDialog(String message) {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
    }

    public void setProgressDialog(ProgressDialog progressDialog) {
        this.progressDialog = progressDialog;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public void setModel(Class model) {
        this.model = model;
    }

    public void setAcceptAllCertificate(boolean isAcceptAllCertificate) {
        isAcceptAllCertificate = isAcceptAllCertificate;
    }

    public void setCertificate(String paternCertificate, String pinCertificate) {
        this.paternCertificate = paternCertificate;
        this.pinCertificate = pinCertificate;
    }

    public void setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    public void setDownloadFileName(String downloadFileName) {
        this.downloadFileName = downloadFileName;
    }

    public void setFailedListener(BlukutukFail blukutukFail) {
        this.blukutukFail = blukutukFail;
    }

    public void setJsonObjectResultListener(BlukutukJsonObject blukutukJsonObject) {
        this.blukutukJsonObject = blukutukJsonObject;
    }

    public void setJsonArrayResultListener(BlukutukJsonArray blukutukJsonArray) {
        this.blukutukJsonArray = blukutukJsonArray;
    }

    public void setModelResultListener(BlukutukModel blukutukModel) {
        this.blukutukModel = blukutukModel;
    }

    public void setUploadProgressListener(BlukutukUploadProgress blukutukUploadProgress) {
        this.blukutukUploadProgress = blukutukUploadProgress;
    }

    public void setDownloadListener(BlukutukDownload blukutukDownload) {
        this.blukutukDownload = blukutukDownload;
    }

    private void processResult(Object o) {
        if (responseMessage.length() == 0) {
            if (blukutukJsonObject != null) {
                Boolean failedJsonTest = false;

                JSONObject result = null;
                try {
                    result = new JSONObject((String) o);
                } catch (JSONException e) {
                    try {
                        new JSONArray((String) o);
                    } catch (JSONException e1) {
                        failedJsonTest = true;
                    }
                }

                if (failedJsonTest) {
                    responseCode = 999;
                    responseMessage = code("" + 999);

                    blukutukFail.result(responseCode, responseMessage);
                } else {
                    blukutukJsonObject.result(result);
                }
            }

            if (blukutukJsonArray != null) {
                Boolean failedJsonTest = false;

                JSONArray result = null;
                try {
                    result = new JSONArray((String) o);
                } catch (JSONException e) {
                    try {
                        new JSONArray((String) o);
                    } catch (JSONException e1) {
                        failedJsonTest = true;
                    }
                }

                if (failedJsonTest) {
                    responseCode = 999;
                    responseMessage = code("" + 999);

                    blukutukFail.result(responseCode, responseMessage);
                } else {
                    blukutukJsonArray.result(result);
                }
            }

            if (blukutukModel != null && model != null) {
                Boolean failedJsonTest = false;

                Gson gson = new Gson();
                Object modelResult = gson.fromJson((String) o, (Type) model);

                blukutukModel.result(Primitives.wrap(model).cast(modelResult));
            }

        } else {
            if (blukutukFail != null) {
                blukutukFail.result(responseCode, responseMessage);
            }
        }
    }

    public Boolean jsonObjectTest(String o) {
        Boolean failedJsonTest = false;

        JSONObject result = null;
        try {
            result = new JSONObject(o);
        } catch (JSONException e) {
            try {
                new JSONArray(o);
            } catch (JSONException e1) {
                failedJsonTest = true;
            }
        }

        return failedJsonTest;
    }

    public Boolean jsonArrayTest(String o) {
        Boolean failedJsonTest = false;

        JSONArray result = null;
        try {
            result = new JSONArray(o);
        } catch (JSONException e) {
            try {
                new JSONArray(o);
            } catch (JSONException e1) {
                failedJsonTest = true;
            }
        }

        return failedJsonTest;
    }

    public void execute() {
        OkHttp okHttp = new OkHttp();
        okHttp.setOkHttpInterface(new OkHttpInterface() {
            @Override
            public void before() {
                if (!activity.isDestroyed()) {
                    if (progressDialog != null) {
                        progressDialog.show();
                    }

                    if (progressBar != null) {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void progress(int progress) {
                if (blukutukUploadProgress != null && !activity.isDestroyed()) {
                    blukutukUploadProgress.result(progress);
                }
            }

            @Override
            public void after(Object o) {
                if (!activity.isDestroyed()) {
                    processResult(o);

                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }

                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }
        });

        okHttp.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void download() {
        OkHttpDownload okHttp = new OkHttpDownload();
        okHttp.setOkHttpInterface(new OkHttpInterface() {
            @Override
            public void before() {
                if (!activity.isDestroyed()) {
                    if (progressDialog != null) {
                        progressDialog.show();
                    }

                    if (progressBar != null) {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void progress(int progress) {
                if (blukutukUploadProgress != null && !activity.isDestroyed()) {
                    blukutukUploadProgress.result(progress);
                }
            }

            @Override
            public void after(Object o) {
                if (!activity.isDestroyed()) {
                    if (blukutukDownload != null) {
                        if (o.equals("1")) {
                            blukutukDownload.success();
                        } else {
                            blukutukDownload.failed(responseCode, responseMessage);
                        }
                    }

                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }

                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }
        });

        okHttp.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    static class OkHttp extends AsyncTask {

        OkHttpInterface okHttpInterface;

        void setOkHttpInterface(OkHttpInterface okHttpInterface) {
            this.okHttpInterface = okHttpInterface;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            okHttpInterface.before();
        }

        @Override
        protected Object doInBackground(final Object[] objects) {
            OkHttpClient.Builder builderOkhttp = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS);

            if (paternCertificate.length() > 0 && pinCertificate.length() > 0) {
                builderOkhttp.certificatePinner(new CertificatePinner.Builder().add(paternCertificate, pinCertificate).build());
            }

            if (isAcceptAllCertificate) {
                final TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                            }

                            @Override
                            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                            }

                            @Override
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return new java.security.cert.X509Certificate[]{};
                            }
                        }
                };
                try {
                    SSLContext sslContext = SSLContext.getInstance("SSL");
                    sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

                    SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

                    builderOkhttp.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
                } catch (NoSuchAlgorithmException e) {
                    responseCode = 904;
                    responseMessage = e.getMessage();
                    return "";
                } catch (KeyManagementException e) {
                    responseCode = 905;
                    responseMessage = e.getMessage();
                    return "";
                }
            }
            OkHttpClient client = builderOkhttp.build();

            Request request = new Request.Builder()
                    .url(builder.toString())
//                    .post((RequestBody) objects[1])
                    .post(new ProgressRequestBody(requestBody, progress -> okHttpInterface.progress(progress)))
                    .build();
            try {
                Response response = client.newCall(request).execute();

                ResponseBody responseBody = response.body();

                if (!response.isSuccessful() && responseBody != null) {
                    responseCode = 900;

                    return responseBody.string();
                } else if (responseBody != null) {
                    responseCode = response.code();

                    return responseBody.string();
                } else {
                    responseCode = 900;

                    return "";
                }
            } catch (Exception e) {
                responseCode = 900;
                responseMessage = e.getMessage();

                return "";
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            okHttpInterface.after(o);
        }
    }

    static class OkHttpDownload extends AsyncTask {

        OkHttpInterface okHttpInterface;

        void setOkHttpInterface(OkHttpInterface okHttpInterface) {
            this.okHttpInterface = okHttpInterface;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            okHttpInterface.before();
        }

        @Override
        protected Object doInBackground(final Object[] objects) {
            OkHttpClient.Builder builderOkhttp = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS);

            if (paternCertificate.length() > 0 && pinCertificate.length() > 0) {
                builderOkhttp.certificatePinner(new CertificatePinner.Builder().add(paternCertificate, pinCertificate).build());
            }

            if (isAcceptAllCertificate) {
                final TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                            }

                            @Override
                            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                            }

                            @Override
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return new java.security.cert.X509Certificate[]{};
                            }
                        }
                };
                try {
                    SSLContext sslContext = SSLContext.getInstance("SSL");
                    sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

                    SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

                    builderOkhttp.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
                } catch (NoSuchAlgorithmException e) {
                    responseCode = 904;
                    responseMessage = e.getMessage();
                    return "";
                } catch (KeyManagementException e) {
                    responseCode = 905;
                    responseMessage = e.getMessage();
                    return "";
                }
            }
            OkHttpClient client = builderOkhttp.build();

            Request request = new Request.Builder()
                    .url(builder.toString())
//                    .post((RequestBody) objects[1])
                    .post(new ProgressRequestBody(requestBody, progress -> okHttpInterface.progress(progress)))
                    .build();
            try {
                Response response = client.newCall(request).execute();

                if (!response.isSuccessful()) {
                    responseCode = 901;
                    return "";
                } else {
                    responseCode = response.code();
                    try {
                        File file = new File(downloadPath, downloadFileName);
                        BufferedSink sink = Okio.buffer(Okio.sink(file));
                        BufferedSource bufferedSource = response.body().source();
                        if (bufferedSource != null) {
                            sink.writeAll(bufferedSource);
                            sink.close();
                            return "1";
                        } else {
                            return "";
                        }
                    } catch (FileNotFoundException e) {
                        return "";
                    } catch (IOException e) {
                        return "";
                    }
                }
            } catch (Exception e) {
                responseCode = 900;
                responseMessage = e.getMessage();

                return "";
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            okHttpInterface.after(o);
        }
    }

    public static String code(String code) {
        String result = "";
        if (code.startsWith("5")) {
            result += "Server error.\nPlease contact customer support and describe your issue.\n";
        } else if (code.startsWith("4")) {
            result += "Connection interrupted.\nPlease contact customer support and describe your issue.\n";
        }
        switch (code) {
            case "505":
                result += "HTTP Version Not Supported";
                break;
            case "504":
                result += "Gateway Timeout";
                break;
            case "503":
                result += "Service Unavailable";
                break;
            case "502":
                result += "Bad Gateway";
                break;
            case "501":
                result += "Not Implemented";
                break;
            case "500":
                result += "Internal Server Error";
                break;
            case "417":
                result += "Expectation Failed";
                break;
            case "416":
                result += "Requested Range Not Satisfiable";
                break;
            case "415":
                result += "Unsupported Media Type";
                break;
            case "414":
                result += "Request-URI Too Long";
                break;
            case "413":
                result += "Request Entity Too Large";
                break;
            case "412":
                result += "Precondition Failed";
                break;
            case "411":
                result += "Length Required";
                break;
            case "410":
                result += "Gone";
                break;
            case "409":
                result += "Conflict";
                break;
            case "408":
                result += "Request Timeout";
                break;
            case "407":
                result += "Proxy Authentication Required";
                break;
            case "406":
                result += "Not Acceptable";
                break;
            case "405":
                result += "Method Not Allowed";
                break;
            case "404":
                result += "Not Found";
                break;
            case "403":
                result += "Forbidden";
                break;
            case "402":
                result += "Payment Required";
                break;
            case "401":
                result += "Unauthorized";
                break;
            case "400":
                result += "Bad Request";
                break;
            case "900":
                result = "No Internet Connection Available";
                break;
            default:
                result = "Unknown Connection Problem";
                break;
        }
        result += " ( " + code + " ).";
        return result;
    }
}

