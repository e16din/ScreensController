package com.e16din.sc

import android.app.Activity
import android.view.View
import android.view.ViewGroup

interface ILifecycle {

    fun beforeBindActivity(activity: Activity)

    fun onBindActivity(activity: Activity)

    fun onBindView(view: View, vDecor: ViewGroup)

    fun onShowView(view: View?)

    fun onHideView(view: View?)
}