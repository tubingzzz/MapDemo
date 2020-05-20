package com.test.map;

import android.app.Application;

public class MyApplication extends Application {
    private static MyApplication INSTANCE =null;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE=this;
    }

   public static Application getApplication() {
        return INSTANCE;
    }
}
