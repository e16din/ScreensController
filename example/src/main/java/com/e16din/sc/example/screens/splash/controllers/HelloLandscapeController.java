package com.e16din.sc.example.screens.splash.controllers;


import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.e16din.sc.ScreensController;
import com.e16din.sc.annotations.OnBind;
import com.e16din.sc.annotations.ViewController;
import com.e16din.sc.example.R;
import com.e16din.sc.example.screens.splash.SplashScreen;


@ViewController(screen = SplashScreen.class)
public class HelloLandscapeController {

    @OnBind
    public void onBindView(ScreensController sc, View view, Object data) {
        Log.e("debug", "HelloLandscapeController");
        TextView vSplashLabel = view.findViewById(R.id.vSplashLabel);
        vSplashLabel.setText(sc.getString(R.string.app_name_landscape));
    }

//    @Override
//    public boolean enabled(ScreensController sc) {
//        int orientation = sc.getCurrentScreen().getOrientation();
//        return InclusionConditions.isOnLandscapeOrientation(orientation);
//    }
}
