package com.e16din.sc;


import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.location.Location;

import com.e16din.sc.screens.Screen;


public class InclusionConditions {

    public static boolean isOnLandscapeOrientation(int orientation) {
        return orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    }

    public static boolean isOnPortraitOrientation(int orientation) {
        return orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }


    //todo: replace all with static methods
    /**
     * Включается всегда, независимо от остальных параметров.
     */
    private boolean always;

    /**
     * Включается если был возврат с экрана типа resultScreenType.
     */
    private boolean onResultFromScreen;
    private Class<? extends Screen> resultScreenType;

    /**
     * Включается если количество показов экрана за текущую сессию использования
     * равно countOfShows.
     */
    private boolean onCountOfShows;
    private int countOfShows;

    /**
     * Включается если количество показов экрана за все время использования приложения
     * равно countOfShows.
     */
    private boolean onCountOfAllShows;
    private int countOfAllShows;

    /**
     * Включается если экран открыт в один из заданных интервалов времени.
     */
    private boolean inTheTimeIntervals;
    private Point[] timeIntervals;

    /**
     * Включается если с момента установки приложения прошло passedTimeSinceTheInstallation времени.
     */
    private boolean onPassedTimeSinceTheInstallation;
    private long passedTimeSinceTheInstallation;

    /**
     * Включается если с момента последнего открытия экрана прошло passedTimeSinceScreenLastOpening времени.
     */
    private boolean onPassedTimeSinceScreenLastOpening;
    private long passedTimeSinceScreenLastOpening;

    /**
     * Включается если с момента последнего открытия приложения прошло passedTimeSinceAppLastOpening времени.
     */
    private boolean onPassedTimeSinceAppLastOpening;
    private long passedTimeSinceAppLastOpening;

    /**
     * Включается если экран в Landscape ориентации.
     */
    private boolean onLandscapeOrientation;

    /**
     * Включается если экран в Portrait ориентации.
     */
    private boolean onPortraitOrientation;

    /**
     * Включается если телефон находится в определенной области на карте.
     */
    private boolean inLocationArea;
    private Location location;
    private int radius;

    public boolean isAlways() {
        return always;
    }

    public void setAlways(boolean always) {
        this.always = always;
    }

    public boolean isOnResultFromScreen() {
        return onResultFromScreen;
    }

    public void setOnResultFromScreen(boolean onResultFromScreen) {
        this.onResultFromScreen = onResultFromScreen;
    }

    public boolean isOnCountOfShows() {
        return onCountOfShows;
    }

    public void setOnCountOfShows(boolean onCountOfShows) {
        this.onCountOfShows = onCountOfShows;
    }

    public boolean isOnCountOfAllShows() {
        return onCountOfAllShows;
    }

    public void setOnCountOfAllShows(boolean onCountOfAllShows) {
        this.onCountOfAllShows = onCountOfAllShows;
    }

    public boolean isInTheTimeIntervals() {
        return inTheTimeIntervals;
    }

    public void setInTheTimeIntervals(boolean inTheTimeIntervals) {
        this.inTheTimeIntervals = inTheTimeIntervals;
    }

    public boolean isOnPassedTimeSinceTheInstallation() {
        return onPassedTimeSinceTheInstallation;
    }

    public void setOnPassedTimeSinceTheInstallation(boolean onPassedTimeSinceTheInstallation) {
        this.onPassedTimeSinceTheInstallation = onPassedTimeSinceTheInstallation;
    }

    public boolean isOnPassedTimeSinceScreenLastOpening() {
        return onPassedTimeSinceScreenLastOpening;
    }

    public void setOnPassedTimeSinceScreenLastOpening(boolean onPassedTimeSinceScreenLastOpening) {
        this.onPassedTimeSinceScreenLastOpening = onPassedTimeSinceScreenLastOpening;
    }

    public boolean isOnPassedTimeSinceAppLastOpening() {
        return onPassedTimeSinceAppLastOpening;
    }

    public void setOnPassedTimeSinceAppLastOpening(boolean onPassedTimeSinceAppLastOpening) {
        this.onPassedTimeSinceAppLastOpening = onPassedTimeSinceAppLastOpening;
    }

    public boolean isOnLandscapeOrientation() {
        return onLandscapeOrientation;
    }

    public void setOnLandscapeOrientation(boolean onLandscapeOrientation) {
        this.onLandscapeOrientation = onLandscapeOrientation;
    }

    public boolean isOnPortraitOrientation() {
        return onPortraitOrientation;
    }

    public void setOnPortraitOrientation(boolean onPortraitOrientation) {
        this.onPortraitOrientation = onPortraitOrientation;
    }
}
