package com.e16din.sc.example.screens.splash.controllers;


import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.e16din.sc.ScreensController;
import com.e16din.sc.annotations.OnBind;
import com.e16din.sc.annotations.ViewController;
import com.e16din.sc.example.screens.splash.SplashScreen;
import com.e16din.sc.example.screens.users.UsersScreen;


@ViewController(screen = SplashScreen.class, startOnce = true)
public class SplashController {

    @OnBind
    public void onBindView(final ScreensController sc, View view, Object data) {
        Log.e("debug", "SplashController!!!: ");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sc.start(new UsersScreen(), null, true);
            }
        }, 2000);
    }
}
