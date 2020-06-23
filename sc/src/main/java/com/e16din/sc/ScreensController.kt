package com.e16din.sc

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.res.Resources
import android.util.Log
import android.view.*
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.e16din.sc.screens.LockScreen
import com.e16din.sc.screens.Screen
import com.e16din.sc_bosscontrol.activities.ScreenViewActivity
import com.e16din.sc_bosscontrol.activities.StartActivity
import com.e16din.sc_bosscontrol.services.LockScreenService
import com.e16din.topactivity.TopActivity
import java.io.Serializable
import java.lang.ref.WeakReference
import java.util.*


abstract class ScreensController(var app: Application) : IScreensController {

    private var contentViewLink: WeakReference<View>? = null
    private var decorViewLink: WeakReference<ViewGroup>? = null // for lock screen service

    val screenStack = Stack<Screen>()
    private var onMenuItemClickListener: MenuItem.OnMenuItemClickListener? = null
    protected var isHistoryEnabled = false

    protected var startViewControllers = mutableListOf<String>()
    protected var currentViewControllers = mutableListOf<Any>()

    private val dataMap = hashMapOf<String, Any?>() // <Screen, ViewState>

    // key is viewController class name
    private val actionsMap = hashMapOf<String, MutableList<Runnable>>()
    private val backActions = mutableListOf<Runnable>()
    private val replaceBackActions = mutableListOf<Runnable>()

    val currentScreen: Screen?
        get() = if (screenStack.empty()) null else screenStack.peek()

    val resources: Resources
        get() = activity!!.resources

    val contentView: View
        get() = contentViewLink?.get()!!

    val decorView: ViewGroup?
        get() = decorViewLink!!.get()

    val currentData: Any?
        get() {
            val screenName = currentScreen!!.javaClass.name
            return getData(screenName)
        }

    override val activity = com.e16din.topactivity.activity()

    init {
        TopActivity.init(app, null)
    }


    protected fun getViewController(name: String): Any? {
        for (vc in currentViewControllers) {
            if (Utils.getClassDefaultName(vc) == name) {
                return vc
            }
        }
        return null
    }

    override fun beforeBindActivity(activity: Activity) {
        bindScreen()

        val currentScreen = screenStack.peek()
        if (currentScreen.theme != INVALID_VALUE) {
            activity.setTheme(currentScreen.theme)
        }

        if (currentScreen.isFullScreen) {
            activity.requestWindowFeature(Window.FEATURE_NO_TITLE)
            activity.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }

        if (currentScreen.title != null) {
            activity.title = currentScreen.title
        }
    }

    private fun bindScreen() {
        if (isHistoryEnabled) {
            val lastScreenName: String? = null//todo: add logic
        }

        if (screenStack.isEmpty()) {
            screenStack.push(firstScreen)
            val currentScreen = screenStack.peek()
            dataMap[currentScreen.javaClass.name] = firstData!!
        }
    }

    fun onBindLockScreenService(service: LockScreenService) {
        bindScreen()
        val currentScreen = screenStack.peek()
        service.setContentView(currentScreen.layout)
        service.setTheme(currentScreen.theme)
    }

    override fun onBindActivity(activity: Activity) {
        val currentScreen = screenStack.peek()
        activity.setContentView(currentScreen.layout)

        ScreenViewUtils.initToolbar(activity, currentScreen.withBackButton())
    }

    override// todo: refactor it
    fun runAction(viewController: Any, action: Runnable) {
        val name = Utils.getClassDefaultName(viewController)

        if (decorView != null) {
            for (vc in currentViewControllers) {
                if (Utils.getClassDefaultName(vc) == name) {
                    action.run()
                    return
                }
            }
        } // else {

        saveAction(name, action)
    }

    private fun saveAction(viewControllerName: String, action: Runnable) {
        var actions: MutableList<Runnable>? = actionsMap[viewControllerName]
        if (actions == null) {
            actions = ArrayList()
        }

        actions.add(action)
        actionsMap[viewControllerName] = actions
    }


    private fun bindView(screen: Screen, view: View, vLockContainer: ViewGroup) {
        if (screen is LockScreen) {
            decorViewLink = WeakReference(vLockContainer)
        }
        contentViewLink = WeakReference(view)

        if (currentViewControllers != null) {
            currentViewControllers.clear()
        }
        replaceBackActions.clear()
        backActions.clear()

        val name = screen.javaClass.name
        Log.i("debug", "bindView: screen name: $name")

        addViewControllers(screen, view, ON_BIND)
    }

    override fun onBindView(view: View, vDecor: ViewGroup) {
        val currentScreen = screenStack.peek()
        bindView(currentScreen, view, vDecor)
    }

    override fun onShowView(view: View?) {
        showViewControllers()
    }

    private fun showViewControllers() {
        for (vc in currentViewControllers) {
            onShowViewController(vc)

            runAllActions(vc)
        }
    }

    private fun addViewControllers(screen: Screen, vContent: View, method: Int) {
        val screenName = screen.javaClass.name
        val data = getData(screenName)

        val viewControllers = buildViewControllers(screenName)

        for (vc in viewControllers) {
            if (vc is MenuItem.OnMenuItemClickListener) {
                onMenuItemClickListener = vc
            }

            val controllerName = Utils.getClassDefaultName(vc)

            if (once(controllerName)) {
                if (!startViewControllers.contains(controllerName)) {
                    startViewControllers.add(controllerName)

                    when (method) {
                        ON_BIND -> onBindViewController(vc, vContent, data)
                        ON_SHOW -> onShowViewController(vc)
                    }
                }

            } else {
                when (method) {
                    ON_BIND -> onBindViewController(vc, vContent, data)
                    ON_SHOW -> onShowViewController(vc)
                }
                addViewController(vc)
            }
            runAllActions(vc)
        }
    }

    private fun getData(screenName: String): Any? {
        return dataMap[screenName]
    }

    private fun runAllActions(vc: Any) {
        val vcName = Utils.getClassDefaultName(vc)
        val actions = actionsMap[vcName]
        if (actions != null) {
            for (action in actions) {
                action.run()
            }
            actionsMap.remove(vcName)
        }
    }

    override fun onBack() {
        if (replaceBackActions.size > 0) {
            backActions.clear()

            for (action in replaceBackActions) {
                action.run()
            }

        } else if (backActions.size > 0) {
            for (action in backActions) {
                action.run()
            }
            finishScreen()

        } else {
            finishScreen()
        }
    }

    @JvmOverloads
    fun finishScreen(result: Serializable? = null) {
        finishScreen(false, result)
    }

    fun finishScreen(affinity: Boolean, result: Serializable?) {
        backActions.clear()
        replaceBackActions.clear()
        startViewControllers.clear()

        if (result != null) {
            setResult(result)
        }

        val currentScreen = screenStack.pop()
        dataMap.remove(currentScreen.javaClass.name)
        if (!screenStack.empty()) {
            val prevScreen = screenStack.peek()

            if (prevScreen is LockScreen) {

                startLockScreen(prevScreen)
                return
            }

        } else {
            unlockScreenAndExit()
        } // else {

        contentViewLink = null

// else {

        if (affinity) {
            ActivityCompat.finishAffinity(activity!!)
        } else {
            if (activity is ScreenViewActivity)
                (activity as ScreenViewActivity).superFinish()
            else
                activity?.finish()
        }
    }

    fun setResult(result: Serializable) {// check this on lock screen
        val data = Intent()
        data.putExtra(IScreensController.KEY_DATA, result)
        activity?.setResult(Activity.RESULT_OK, data)
    }

    override fun onHideView(view: View?) {
        hideViewControllers()
    }

    private fun hideViewControllers() {
        for (vc in currentViewControllers) {
            onHideViewController(vc)

            runAllActions(vc)
        }
    }

    fun clickMenuItem(item: MenuItem): Boolean {
        for (vc in currentViewControllers) {
            val r = onMenuItemClick(vc, item)
            if (r) return true
        }

        for (vc in startViewControllers) {
            val r = onMenuItemClick(vc, item)
            if (r) return true
        }

        return false
    }

    override fun onMenuItemClick(vc: Any, item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val currentScreen = screenStack.peek()
                if (currentScreen.withBackButton()) {
                    activity?.onBackPressed()
                }
                return true
            }
        }

        return onMenuItemClickListener != null && onMenuItemClickListener!!.onMenuItemClick(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        // override it on generated class
    }

    override fun startScreen(screen: Screen, data: Any?, finishCurrent: Boolean) {
        startScreen(screen, data, finishCurrent, false, 0)
    }

    fun startScreen(screen: Screen, data: Any?, finishCurrent: Boolean, finishAffinity: Boolean, requestCode: Int) {
        val currentScreen = screenStack.peek()
        if (finishCurrent) {
            screenStack.pop()
        }
        val nextScreen = screenStack.push(screen)
        dataMap[nextScreen.javaClass.name] = data

        if (nextScreen is LockScreen) {
            startLockScreen(nextScreen)
        } else {
            startActivityScreen(screen, finishCurrent, requestCode, currentScreen, nextScreen)
        }
    }

    private fun startActivityScreen(screen: Screen, finishCurrent: Boolean, requestCode: Int, currentScreen: Screen, nextScreen: Screen) {
        //todo: check and update this way
        if (currentScreen is LockScreen) {
            hideLockScreen()
            unlockScreen()
        }

        val intent = Intent()
        if (screen.isNoHistory) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        }
        if (screen.needClearTask()) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }

        val activity = activity

        val activityClass = StartActivity.getActivityCls(nextScreen)
        intent.setClass(activity, activityClass)

        activity?.startActivityForResult(intent, requestCode)

        if (finishCurrent) {
            finishScreen()
        }
    }

    private fun startLockScreen(screen: Screen) {
        val decorView = decorView

        @SuppressLint("RestrictedApi")
        val contextThemeWrapper = ContextThemeWrapper(decorView!!.context, screen.theme)
        val newView = LayoutInflater.from(contextThemeWrapper).inflate(screen.layout, decorView, false)

        hideLockScreen()
        decorView.addView(newView, LockScreenService.params)

        bindView(screen, newView, decorView)
        onShowView(null)
    }

    private fun hideLockScreen() {
        decorView!!.removeView(contentView)
        onHideView(null)
    }

    override fun addViewController(vc: Any) {
        currentViewControllers.add(vc)
    }

    override fun removeViewController(vc: Any) {
        currentViewControllers.remove(vc)
    }

    protected fun clearViewControllers() {
        currentViewControllers.clear()
    }

    fun addOnBackAction(action: Runnable) {
        backActions.add(action)
    }

    fun addOnBackAction(action: Runnable, withFinishOnBack: Boolean) {
        if (withFinishOnBack) {
            addOnBackAction(action)

        } else {
            replaceBackActions.add(action)
        }
    }

    fun clearAllOnBackActions() {
        backActions.clear()
        replaceBackActions.clear()
    }

    fun unlockScreenAndExit() {
        unlockScreen()
        System.exit(0)
    }

    private fun unlockScreen() {
        val context = contentView.context.applicationContext
        context.stopService(Intent(context, LockScreenService::class.java))
    }

    fun getString(@StringRes stringId: Int): String {
        return activity!!.getString(stringId)
    }

    fun getColor(colorId: Int): Int {
        return ContextCompat.getColor(activity!!, colorId)
    }

    fun setOnMenuItemClickListener(onMenuItemClickListener: MenuItem.OnMenuItemClickListener) {
        this.onMenuItemClickListener = onMenuItemClickListener
    }

    @JvmOverloads
    fun <T> notifyViewController(cls: Class<*>, key: Int, data: T? = null) {
        val vc = getViewController(cls.name)

        (vc as IOnNotify<T>).onNotify(key, data)
    }

    @JvmOverloads
    fun <T> notifyViewController(cls: Class<*>, data: T? = null) {
        notifyViewController(cls, 0, data)
    }

    fun setCurrentViewControllers(currentViewControllers: ArrayList<Any>) {
        this.currentViewControllers = currentViewControllers
    }

    fun setTitle(text: String) {
        ScreenViewUtils.setToolbarTitle(activity, text)
    }

    fun hasScreen(): Boolean {
        return currentScreen != null
    }

    fun backToScreen(screenCls: Class<out Screen>, data: Any?) {
        while (!screenStack.empty() && !screenCls.isInstance(screenStack.peek())) {
            val currentScreen = screenStack.pop()
            dataMap.remove(currentScreen.javaClass.name)
        }
        if (!screenStack.empty()) {
            startScreen(screenStack.peek(), data, true)
        } else {
            throw IllegalArgumentException("not found " + screenCls.simpleName +
                    " class instance in the screens stack")
        }
    }

    companion object {

        private val ON_BIND = 1
        private val ON_SHOW = 2

        private lateinit var sc: ScreensController

        var firstScreen: Screen? = null
            private set
        private var firstData: Any? = null

        var splashDelayMs = 0

        @LayoutRes
        var splashLayoutRes = INVALID_VALUE

        operator fun set(screenController: ScreensController, firstScreen: Screen, data: Any) {
            ScreensController.sc = screenController
            ScreensController.firstScreen = firstScreen
            ScreensController.firstData = data
        }

        fun instance() = sc

        fun startFirstScreen() {
            instance().activity?.let {
                val intent = Intent(it, StartActivity::class.java)
                it.startActivity(intent)

                ActivityCompat.finishAffinity(it)
            }
        }

        private fun setSplash(@LayoutRes layoutRes: Int, delayMs: Int) {
            ScreensController.splashLayoutRes = layoutRes
            ScreensController.splashDelayMs = delayMs
        }

        fun setSplashDelay(delayMs: Int) {
            setSplash(INVALID_VALUE, delayMs)
        }

        fun setSplashLayout(@LayoutRes layoutRes: Int, delayMs: Int) {
            setSplash(layoutRes, delayMs)
        }

        fun firstScreen(): Screen? {
            return firstScreen
        }
    }
}
