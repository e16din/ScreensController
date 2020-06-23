package com.e16din.sc_bosscontrol.activities;

import com.e16din.sc.ScreensController;

public abstract class SimpleScreenViewActivity extends ScreenViewActivity {

    private ScreensController controller = ScreensController.Companion.instance();

    @Override
    protected ScreensController controller() {
        return controller;
    }
}
