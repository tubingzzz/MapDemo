package com.test.map.base;

import android.content.Intent;

import androidx.annotation.Nullable;

import com.amap.api.maps.AMap;

public interface MainContract {
    interface Model {

    }

    interface View extends BaseView {
        @Override
        void showLoading();

        @Override
        void hideLoading();

        @Override
        void onError(Throwable throwable);

       AMap getAMap();

        void showPoiInfoView(String name);
        void showRouteView(String des);
        void showNaviResult(int total,long time);

    }

    interface Presenter {
        void goToNavi();
        void goToRouteDetail();

        void getRoutPlan();

        void onActivityResult(int requestCode, int resultCode, @Nullable Intent data);
    }
}
