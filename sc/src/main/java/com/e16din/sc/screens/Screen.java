package com.e16din.sc.screens;

import android.support.annotation.IntRange;

import java.io.Serializable;

public abstract class Screen implements Serializable {

    public static final int INVALID_VALUE = -1;

    private int layout = INVALID_VALUE;
    private int theme = INVALID_VALUE;
    private int menu = INVALID_VALUE;

    private String title;

    private long refreshRateMs = 0;

    private int orientation = INVALID_VALUE;

    private boolean isFullScreen = false;
    private boolean withDoneAction = false;
    private boolean withBackButton = false;
    private boolean isNoHistory = false;

    private Screen prevScreen;

    private String linkToView;// use https://github.com/airbnb/DeepLinkDispatch


    public Screen() {
    }

    public Screen(String title) {
        this.title = title;
    }

    public boolean isFullScreen() {
        return isFullScreen;
    }

    public void setFullScreen(boolean fullScreen) {
        isFullScreen = fullScreen;
    }

    public boolean withDoneAction() {
        return withDoneAction;
    }

    public void setWithDoneAction(boolean withDoneAction) {
        this.withDoneAction = withDoneAction;
    }

    public boolean withBackButton() {
        return withBackButton;
    }

    public void setWithBackButton(boolean withBackButton) {
        this.withBackButton = withBackButton;
    }

    public long getRefreshRateMs() {
        return refreshRateMs;
    }

    /**
     * @param refreshRateMs 1 minute is minimal value
     */
    public void setRefreshRateMs(@IntRange(from = 60 * 1000, to = 60 * 60 * 1000)
                                         long refreshRateMs) {
        this.refreshRateMs = refreshRateMs;
    }

    public int getLayout() {
        return layout;
    }

    public void setLayout(int layout) {
        this.layout = layout;
    }

    public Screen getPrevScreen() {
        return prevScreen;
    }

    public void setPrevScreen(Screen prevScreen) {
        this.prevScreen = prevScreen;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getMenu() {
        return menu;
    }

    public void setMenu(int menu) {
        this.menu = menu;
    }

    public int getTheme() {
        return theme;
    }

    /**
     * @param theme for example R.style.AppTheme_NoActionBar
     */
    public void setTheme(int theme) {
        this.theme = theme;
    }

    public int getOrientation() {
        return orientation;
    }

    /**
     * @param orientation for example ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
     */
    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public boolean isNoHistory() {
        return isNoHistory;
    }

    public void setNoHistory(boolean noHistory) {
        this.isNoHistory = noHistory;
    }
}
