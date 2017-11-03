package com.e16din.sc;

import android.content.Context;
import android.view.MenuItem;

import com.e16din.sc.activities.ScreenViewActivity;
import com.e16din.sc.screens.Screen;


public interface IScreensController extends ILifecycle {

    String KEY_DATA = "com.e16din.sc.screens.data";


    Context getContext();

    ScreenViewActivity getActivity();

    void onBack();

    /**
     * @param data parcelable (Bundle for example) or serializable object
     */
    void start(Screen screen, Object data, boolean finishCurrent);

    void addViewController(Object dc);

    void removeViewController(Object dc);

    boolean onMenuItemClick(MenuItem item);

    void saveData(Object data);

    void runAction(Object viewController, Runnable action);
}