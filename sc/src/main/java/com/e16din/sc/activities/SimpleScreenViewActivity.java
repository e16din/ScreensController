package com.e16din.sc.activities;

import com.e16din.sc.ScreensController;

public abstract class SimpleScreenViewActivity extends ScreenViewActivity {

    private ScreensController controller = ScreensController.instance();

    protected ScreensController controller() {
        return controller;
    }
}
