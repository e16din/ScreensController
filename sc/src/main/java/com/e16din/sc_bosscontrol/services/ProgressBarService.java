package com.e16din.sc_bosscontrol.services;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Debug;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.e16din.sc_bosscontrol.R;

import androidx.annotation.Nullable;


public class ProgressBarService extends Service {

    private static final String TAG = "ProgressBarService";

    private ViewGroup view;
    private WindowManager wm;


    @Override
    public void onCreate() {
        super.onCreate();

        if (Debug.isDebuggerConnected()) {
            Debug.waitForDebugger();
        }
        Log.i(TAG, "onCreate: "+TAG);

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        final DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);

        params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;

        params.gravity = Gravity.CENTER;
        int windowWidthPx = metrics.widthPixels;
        params.width = windowWidthPx;
        int windowHeightPx = metrics.heightPixels;
        params.height = windowHeightPx;

        view = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.layout_progress_bar, null);

        wm.addView(view, params);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void collapseView() {
        wm.removeView(view);
    }
}
