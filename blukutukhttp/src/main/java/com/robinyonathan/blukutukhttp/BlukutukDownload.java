package com.robinyonathan.blukutukhttp;

public interface BlukutukDownload {
    void failed(int errorCode, String errorMessage);

    void success();
}
