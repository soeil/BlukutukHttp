package com.robinyonathan.blukutukhttp;

public interface ProgressListener {
    void update(long bytesRead, long contentLength, boolean done);
}
