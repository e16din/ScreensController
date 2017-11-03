package com.e16din.sc.example.screens.splash

import android.content.pm.ActivityInfo
import com.e16din.sc.example.R
import com.e16din.sc.screens.Screen

class SplashScreen : Screen() {
    init {
        layout = R.layout.screen_splash
        isFullScreen = true
        theme = R.style.AppTheme_NoActionBar
        orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
}
