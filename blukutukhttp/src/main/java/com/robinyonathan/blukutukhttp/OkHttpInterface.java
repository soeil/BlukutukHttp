package com.robinyonathan.blukutukhttp;

public interface OkHttpInterface {
    void before();

    void progress(int progress);

    void after(Object o);
}
