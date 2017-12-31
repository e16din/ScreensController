package com.e16din.sc;

import android.app.Activity;
import android.view.View;

public interface ILifecycle {

    void beforeBindActivity(Activity activity);

    void onBindActivity(Activity activity);

    void onBindView(View view);

    void onShowView(View view);

    void onHideView(View view);

    void onRefresh();
}