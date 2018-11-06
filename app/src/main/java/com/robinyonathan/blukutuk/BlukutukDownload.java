package com.robinyonathan.blukutuk;

public interface BlukutukDownload {
    void failed(int errorCode, String errorMessage);

    void success();
}
