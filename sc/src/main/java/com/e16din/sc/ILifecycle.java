package com.e16din.sc;

import com.e16din.sc.activities.ScreenViewActivity;

public interface ILifecycle {

    void beforeBindActivity(ScreenViewActivity activity);

    void onBindActivity(ScreenViewActivity activity);

    void onShow(ScreenViewActivity activity);

    void onHide(ScreenViewActivity activity);

    void onRefresh();
}