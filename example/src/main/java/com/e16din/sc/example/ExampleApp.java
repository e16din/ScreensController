package com.e16din.sc.example;


import android.app.Application;

import com.e16din.sc.GeneratedScreensController;
import com.e16din.sc.ScreensController;
import com.e16din.sc.example.screens.splash.SplashScreen;

public class ExampleApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ScreensController.set(new GeneratedScreensController(), new SplashScreen());
    }
}
