package com.e16din.sc;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;

import com.e16din.sc.activities.ScreenViewActivity;
import com.e16din.sc.screens.Screen;


public interface IScreensController extends ILifecycle {

    String KEY_DATA = "com.e16din.sc.screens.data";


    Context getContext();

    ScreenViewActivity getActivity();

    void onBack();

    /**
     * @param data model for view
     */
    void start(Screen screen, Object data, boolean finishCurrent);

    void addViewController(Object vc);

    void removeViewController(Object vc);

    boolean onMenuItemClick(Object vc, MenuItem item);

    void runAction(Object vc, Runnable action);

    void onActivityResult(int requestCode, int resultCode, Intent data);

    Object[] buildViewControllers(String screenName);

    void onBindViewController(Object vc, View view, Object state);

    boolean once(String vcName);

    boolean enabled(Object vc);
}