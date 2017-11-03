package com.e16din.sc.example.screens.splash.controllers;


import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.e16din.sc.IViewController;
import com.e16din.sc.ScreensController;
import com.e16din.sc.annotations.ViewController;
import com.e16din.sc.example.screens.main.MainScreen;
import com.e16din.sc.example.screens.splash.SplashScreen;


@ViewController(screen = SplashScreen.class, startOnce = true)
public class SplashController implements IViewController {

    @Override
    public void onBindView(final ScreensController sc, View view, Object data) {
        Log.e("debug", "SplashController!!!: ");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sc.start(new MainScreen(), null, true);
            }
        }, 2000);
    }
}
