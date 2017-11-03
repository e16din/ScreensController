package com.e16din.sc.example.screens.main

import com.e16din.sc.example.R
import com.e16din.sc.screens.Screen

class MainScreen : Screen() {
    init {
        layout = R.layout.screen_main
        theme = R.style.AppTheme_NoActionBar
        title = "Main Screen"
        menu = R.menu.main
        //orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
}