package com.e16din.sc_bosscontrol.activities


import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import com.e16din.sc.INVALID_VALUE
import com.e16din.sc.ScreensController
import com.e16din.sc.screens.LockScreen
import com.e16din.sc.screens.Screen

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        Log.e("debug", "startScreen StartActivity")
        val screen = ScreensController.firstScreen()

        val delayMs = ScreensController.splashDelayMs
        if (delayMs <= 0) {
            Log.i("debug", "delay: $delayMs")
            startNextActivity(screen)
            super.onCreate(savedInstanceState)
            return
        }

        Handler().postDelayed({ startNextActivity(screen) }, delayMs.toLong())
        super.onCreate(savedInstanceState)

        val layoutId = ScreensController.splashLayoutRes
        if (layoutId != INVALID_VALUE) {
            setContentView(layoutId)
        }
    }

    private fun startNextActivity(screen: Screen?) {
        startActivity(Intent(this, getActivityCls(screen)))
        ActivityCompat.finishAffinity(this)
    }

    companion object {

        fun getActivityCls(screen: Screen?): Class<out Activity> {
            if (screen is LockScreen) {
                return LockScreenHolderActivity::class.java
            } // else {

            when (screen!!.orientation) {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> {
                    return PortraitHolderActivity::class.java
                }
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> {
                    return LandscapeHolderActivity::class.java
                }
            } // else {

            return ScreenHolderActivity::class.java
        }
    }

}
