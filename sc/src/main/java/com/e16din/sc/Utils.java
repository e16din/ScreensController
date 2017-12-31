package com.e16din.sc;


import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;


public class Utils {

    @SuppressLint("WakelockTimeout")
    public static void disableSystemLockScreens(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.ON_AFTER_RELEASE, "INFO");
        wl.acquire();
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("name");
        kl.disableKeyguard();
    }

    public static void hideNavigation(View vDecor) {
        vDecor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_IMMERSIVE);

        vDecor.setFitsSystemWindows(false);
    }

    static void recursiveLoopChildren(View view, LoopChildrenCallback callback) {
        recursiveLoopChildren(view, callback, 0);
    }

    private static void recursiveLoopChildren(View view, LoopChildrenCallback callback, int deep) {
        if (view == null) return;

        deep += 1;

        if (view instanceof ViewGroup) {
            final ViewGroup viewGroup = (ViewGroup) view;
            callback.onChild(null, viewGroup, deep);

            for (int i = viewGroup.getChildCount() - 1; i >= 0; i--) {
                recursiveLoopChildren(viewGroup.getChildAt(i), callback);
            }
        } else {//if !ViewGroup
            callback.onChild(view, null, deep);
        }
    }

    public static String getClassDefaultName(@NonNull Object name) {
        return name.getClass().getName().split("\\$")[0];
    }

    public interface LoopChildrenCallback {
        void onChild(View view, ViewGroup viewGroup, int deep);
    }
}
