package com.e16din.sc

import android.content.Intent
import com.e16din.sc.screens.Screen
import java.io.Serializable
import kotlin.reflect.KClass


const val INVALID_VALUE = -1

fun sc() = ScreensController.instance()!!

fun <T : Serializable> Intent.get() = this.getSerializableExtra(IScreensController.KEY_DATA) as T

fun startScreen(screen: Screen, data: Any?, finishCurrent: Boolean) {
    sc().startScreen(screen, data, finishCurrent)
}

fun backToScreen(screenCls: KClass<out Screen>, data: Any?) {
    sc().backToScreen(screenCls.java, data)
}

fun finishScreen() {
    sc().finishScreen()
}

