package com.mapbox.vision.examples;

import android.os.Bundle;
import android.app.Application;
import com.mapbox.vision.VisionManager;


public class MyApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
//        VisionManager.init(this, getString(R.string.mapbox_access_token));
    }
}
