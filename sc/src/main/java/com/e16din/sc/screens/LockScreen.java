package com.e16din.sc.screens;

import com.e16din.sc_bosscontrol.R;

import static com.e16din.sc.UtilsExtKt.INVALID_VALUE;

public class LockScreen extends Screen {
    private int holderLayout = INVALID_VALUE;
    private int holderTheme = R.style.Theme_AppCompat_Light_NoActionBar;

    public int getHolderLayout() {
        return holderLayout;
    }

    public void setHolderLayout(int holderLayout) {
        this.holderLayout = holderLayout;
    }

    public int getHolderTheme() {
        return holderTheme;
    }

    public void setHolderTheme(int holderTheme) {
        this.holderTheme = holderTheme;
    }
}
