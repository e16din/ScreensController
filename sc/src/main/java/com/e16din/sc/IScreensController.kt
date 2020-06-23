package com.e16din.sc

import android.app.Activity
import android.content.Intent
import android.view.MenuItem
import android.view.View

import com.e16din.sc.screens.Screen


interface IScreensController : ILifecycle {

    companion object {
        val KEY_DATA = "com.e16din.sc.screens.data"
    }

    val activity: Activity?

    fun onBack()

    /**
     * @param data model for view
     */
    fun startScreen(screen: Screen, data: Any?, finishCurrent: Boolean)

    fun addViewController(vc: Any)

    fun removeViewController(vc: Any)

    fun onMenuItemClick(vc: Any, item: MenuItem): Boolean

    fun runAction(vc: Any, action: Runnable)

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent)

    fun buildViewControllers(screenName: String): Array<Any>

    fun onBindViewController(vc: Any, view: View, data: Any?)

    fun onShowViewController(vc: Any)

    fun onHideViewController(vc: Any)

    fun once(vcName: String): Boolean
}