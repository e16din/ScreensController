package com.e16din.sc;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;

import com.e16din.sc.screens.Screen;


public interface IScreensController extends ILifecycle {

    String KEY_DATA = "com.e16din.sc.screens.data";


    Activity getActivity();

    void onBack();

    /**
     * @param data model for view
     */
    void startScreen(Screen screen, Object data, boolean finishCurrent);

    void addViewController(Object vc);

    void removeViewController(Object vc);

    boolean onMenuItemClick(Object vc, MenuItem item);

    void runAction(Object vc, Runnable action);

    void onActivityResult(int requestCode, int resultCode, Intent data);

    Object[] buildViewControllers(String screenName);

    void onBindViewController(Object vc, View view, Object data);

    void onShowViewController(Object vc);

    void onHideViewController(Object vc);

    boolean once(String vcName);

    boolean enabled(Object vc);
}