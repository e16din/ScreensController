package com.e16din.sc.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.Debug
import android.os.IBinder
import android.support.v7.view.ContextThemeWrapper
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import com.e16din.sc.INVALID_VALUE
import com.e16din.sc.ScreensController
import com.e16din.sc.Utils
import com.e16din.sc.sc


class LockScreenService : Service() {

    private var controller = ScreensController.instance()

    private var layout: Int = INVALID_VALUE
    private var lockTheme: Int = INVALID_VALUE

    private lateinit var wm: WindowManager
    private var view: ViewGroup? = null

    companion object {
        lateinit var params: WindowManager.LayoutParams
    }


    private lateinit var vDecor: ViewGroup

    fun setContentView(layout: Int) {
        this.layout = layout
    }

    fun setLockTheme(theme: Int) {
        this.lockTheme = theme
    }


    override fun onCreate() {
        super.onCreate()

        if (Debug.isDebuggerConnected()) {
            Debug.waitForDebugger()
        }

        Utils.disableSystemLockScreens(this)

        controller.onBindLockScreenService(this)

        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(metrics)

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2)
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            else
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        }

        val flags = (//WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        // Keeps the button presses from going to the background window
                        or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        // Enables the notification to recieve touch events
                        or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        // Draws over status bar
                        or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)

        params = WindowManager.LayoutParams(layoutFlag, flags, PixelFormat.TRANSLUCENT)

        params.screenOrientation = orientation()

        params.gravity = Gravity.CENTER
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.MATCH_PARENT

        vDecor = FrameLayout(this)

        Utils.hideNavigation(vDecor)
        wm.addView(vDecor, params)

        val v = startView(layout)!!
        onViewCreated(v)
    }

    fun onViewCreated(view: View) {
        controller.bindView(view, vDecor)
        controller.onShowView(null)
    }

    fun startView(layoutId: Int): View? {
        val contextThemeWrapper = ContextThemeWrapper(this, theme)
        val newView = LayoutInflater.from(contextThemeWrapper).inflate(layoutId, null) as ViewGroup

        vDecor.addView(newView, params)
        view = newView

        return view
    }

    fun removeView(view: View) {
        Log.d("debug", "hide service decor view")
        sc().onHideView(null)
        vDecor.removeView(view)
        if (view == this.view) {
            this.view = null
        }
    }

    fun orientation() = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

    override fun onDestroy() {
        if (view != null) {
            removeView(view!!)
        }
        super.onDestroy()
    }

    fun exit() {
        stopSelf()
        System.exit(0)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

}
