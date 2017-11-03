package com.e16din.sc.activities;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.e16din.sc.ScreensController;
import com.e16din.sc.screens.Screen;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("debug", "start!");
        Screen screen = ScreensController.firstScreen();
        startActivity(new Intent(this, getActivityCls(screen.getOrientation())));
        ActivityCompat.finishAffinity(this);
        super.onCreate(savedInstanceState);
    }

    @NonNull
    public static Class<? extends Activity> getActivityCls(int orientation) {
        switch (orientation) {
            case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                return PortraitHolderActivity.class;
            case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                return LandscapeHolderActivity.class;
        }

        return ScreenHolderActivity.class;
    }

}
