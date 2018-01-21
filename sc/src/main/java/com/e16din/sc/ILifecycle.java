package com.e16din.sc;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

public interface ILifecycle {

    void beforeBindActivity(Activity activity);

    void onBindActivity(Activity activity);

    void onBindView(View view, ViewGroup vDecor);

    void onShowView(View view);

    void onHideView(View view);

    void onRefresh();
}