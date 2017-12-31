package com.e16din.sc.example;


import android.support.multidex.MultiDexApplication;

import com.e16din.sc.GeneratedScreensController;
import com.e16din.sc.ScreensController;
import com.e16din.sc.example.screens.splash.SplashScreen;

public class ExampleApp extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        ScreensController.set(new GeneratedScreensController(this), new SplashScreen(), null);
    }
}
