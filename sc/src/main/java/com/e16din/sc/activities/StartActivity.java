package com.e16din.sc.activities;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.e16din.sc.ScreensController;
import com.e16din.sc.screens.LockScreen;
import com.e16din.sc.screens.Screen;

import static com.e16din.sc.UtilsExtKt.INVALID_VALUE;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        Log.e("debug", "startScreen StartActivity");
        Screen screen = ScreensController.firstScreen();

        final int delayMs = ScreensController.getSplashDelayMs();
        if (delayMs <= 0) {
            Log.i("debug", "delay: " + delayMs);
            startNextActivity(screen);
            super.onCreate(savedInstanceState);
            return;
        } else {
            new Handler().postDelayed(() -> startNextActivity(screen), delayMs);
        }

        super.onCreate(savedInstanceState);

        final int layoutId = ScreensController.getSplashLayoutRes();
        if (layoutId != INVALID_VALUE) {
            setContentView(layoutId);
        }
    }

    private void startNextActivity(Screen screen) {
        if (ScreensController.isStartOnRequest()) {
            return;
        }
        startActivity(new Intent(this, getActivityCls(screen)));
        ActivityCompat.finishAffinity(this);
    }

    @NonNull
    public static Class<? extends Activity> getActivityCls(Screen screen) {
        if (screen instanceof LockScreen) {
            return LockScreenHolderActivity.class;
        } // else {

        switch (screen.getOrientation()) {
            case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                return PortraitHolderActivity.class;
            case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                return LandscapeHolderActivity.class;
        } // else {

        return ScreenHolderActivity.class;
    }

}
