package com.e16din.sc.example.screens.splash.controllers;


import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.e16din.sc.IEnabled;
import com.e16din.sc.IViewController;
import com.e16din.sc.InclusionConditions;
import com.e16din.sc.ScreensController;
import com.e16din.sc.annotations.ViewController;
import com.e16din.sc.example.R;
import com.e16din.sc.example.screens.splash.SplashScreen;


@ViewController(screen = SplashScreen.class)
public class HelloPortraitController implements IViewController, IEnabled {

    @Override
    public void onBindView(ScreensController sc, View view, Object data) {
        Log.e("debug", "HelloPortraitController");
        TextView vSplashLabel = view.findViewById(R.id.vSplashLabel);
        vSplashLabel.setText(sc.getString(R.string.app_name_portrait));
        vSplashLabel.setBackgroundColor(sc.getColor(android.R.color.holo_orange_dark));
    }

    @Override
    public boolean enabled(ScreensController sc) {
        int orientation = sc.getCurrentScreen().getOrientation();
        return InclusionConditions.isOnPortraitOrientation(orientation);
    }
}
