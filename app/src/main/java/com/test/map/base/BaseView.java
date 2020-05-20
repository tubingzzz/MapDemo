package com.test.map.base;

public interface BaseView {
    void showLoading();

    void hideLoading();

    void onError(Throwable throwable);
}
