package com.e16din.sc;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.e16din.sc.activities.ScreenViewActivity;
import com.e16din.sc.activities.StartActivity;
import com.e16din.sc.screens.Screen;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.e16din.sc.UtilsExtKt.INVALID_VALUE;


public abstract class ScreensController implements IScreensController {

    private static ScreensController sc;
    private static Screen firstScreen;
    private static Object firstViewState;

    private static int splashDelayMs;
    @DrawableRes
    private static int splashRes = INVALID_VALUE;


    public static void set(@NonNull ScreensController screenController, Screen firstScreen,
                           Object data) {
        ScreensController.sc = screenController;
        ScreensController.firstScreen = firstScreen;
        ScreensController.firstViewState = data;
    }

    public static ScreensController instance() {
        return sc;
    }

    public static void setSplash(@DrawableRes int splashRes, int delayMs) {
        ScreensController.splashRes = splashRes;
        ScreensController.splashDelayMs = delayMs;
    }

    public static int getSplashRes() {
        return splashRes;
    }

    public static int getSplashDelayMs() {
        return splashDelayMs;
    }



    private static final int REQ_REFRESH = 0;

    private ScreenViewActivity activity;
    private Screen currentScreen;
    private MenuItem.OnMenuItemClickListener onMenuItemClickListener;
    private boolean historyEnabled = false;

    protected List<String> startViewControllers = new ArrayList<>();
    protected List<Object> currentViewControllers = new ArrayList<>();

    // key is a Screen class name, value is a ViewState object
    private Map<String, Object> dataMap = new HashMap<>();

    // key is viewController class name
    private Map<String, List<Runnable>> actionsMap = new HashMap<>();
    private List<Runnable> backActions = new ArrayList<>();
    private final List<Runnable> replaceBackActions =
            Collections.synchronizedList(new ArrayList<Runnable>());

    @Nullable
    protected Object getViewController(@NonNull String name) {
        for (Object vc : currentViewControllers) {
            if (Utils.getClassDefaultName(vc).equals(name)) {
                return vc;
            }
        }
        return null;
    }

    @Override
    public void beforeBindActivity(ScreenViewActivity activity) {
        if (isHistoryEnabled()) {
            String lastScreenName = null;//todo: add logic
            if (lastScreenName != null) {
                currentScreen = null;// todo: deserialize screen from json
            }
        }

        if (currentScreen == null) {
            currentScreen = firstScreen;
            dataMap.put(currentScreen.getClass().getName(), firstViewState);
        }

        if (currentScreen.getTheme() != INVALID_VALUE) {
            activity.setTheme(currentScreen.getTheme());
        }

        if (currentScreen.isFullScreen()) {
            activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        if (currentScreen.getTitle() != null) {
            activity.setTitle(currentScreen.getTitle());
        }
    }

    @Override
    public void onBindActivity(ScreenViewActivity activity) {
        this.activity = activity;

        activity.setContentView(currentScreen.getLayout());

        ScreenViewUtils.initToolbar(activity, currentScreen.withBackButton());
    }

    @Override
    public void runAction(Object viewController, Runnable action) {
        String name = Utils.getClassDefaultName(viewController);

        if (activity != null) {
            for (Object vc : currentViewControllers) {
                if (Utils.getClassDefaultName(vc).equals(name)) {
                    action.run();
                    return;
                }
            }
        } // else {

        saveAction(name, action);
    }

    private void saveAction(String viewControllerName, Runnable action) {
        List<Runnable> actions = actionsMap.get(viewControllerName);
        if (actions == null) {
            actions = new ArrayList<>();
        }

        actions.add(action);
        actionsMap.put(viewControllerName, actions);
    }

    public static Screen firstScreen() {
        return firstScreen;
    }


    @Override
    public void onShow(ScreenViewActivity activity) {
        this.activity = activity;

        if (currentViewControllers != null) {
            currentViewControllers.clear();
        }
        replaceBackActions.clear();
        backActions.clear();

        final String name = currentScreen.getClass().getName();
        Log.i("debug", "onShow: screen name: " + name);
        addViewControllers(activity);

        stopRefreshing(activity);
        startRefreshing();
    }

    private void addViewControllers(Activity activity) {
        final View vContent = ScreenViewUtils.getContentView(activity);
        final String screenName = currentScreen.getClass().getName();
        final Object data = dataMap.get(screenName);

        final Object[] viewControllers = buildViewControllers(screenName);

        for (Object vc : viewControllers) {
            if (vc instanceof MenuItem.OnMenuItemClickListener) {
                onMenuItemClickListener = (MenuItem.OnMenuItemClickListener) vc;
            }

            final String controllerName = Utils.getClassDefaultName(vc);
            boolean enabled = enabled(controllerName);

            if (once(controllerName)) {
                if (!startViewControllers.contains(controllerName)) {
                    startViewControllers.add(controllerName);

                    if (enabled) {
                        onBindViewController(vc, vContent, data);
                    }
                }

            } else {
                if (enabled) {
                    onBindViewController(vc, vContent, data);
                }
                addViewController(vc);
            }
            runAllActions(vc);
        }
    }

    private void runAllActions(Object vc) {
        String vcName = Utils.getClassDefaultName(vc);
        List<Runnable> actions = actionsMap.get(vcName);
        if (actions != null) {
            for (Runnable action : actions) {
                action.run();
            }
            actionsMap.remove(vcName);
        }
    }

    @Override
    public void onBack() {
        if (replaceBackActions.size() > 0) {
            backActions.clear();

            for (Runnable action : replaceBackActions) {
                action.run();
            }

        } else if (backActions.size() > 0) {
            for (Runnable action : backActions) {
                action.run();
            }
            finishScreen();

        } else {
            finishScreen();
        }
    }

    public void finishScreen() {
        finishScreen(null);
    }

    public void finishScreen(@Nullable Serializable result) {
        finishScreen(false, result);
    }

    public void finishScreen(boolean affinity, @Nullable Serializable result) {
        backActions.clear();
        replaceBackActions.clear();
        startViewControllers.clear();

        dataMap.remove(currentScreen.getClass().getName());

        Screen prevScreen = currentScreen.getPrevScreen();
        currentScreen.setPrevScreen(null);

        currentScreen = prevScreen;

        if (result != null) {
            setResult(result);
        }

        if (affinity) {
            ActivityCompat.finishAffinity(activity);
        } else {
            activity.superFinish();
        }
        activity = null;
    }

    public void setResult(Serializable result) {
        final Intent data = new Intent();
        data.putExtra(KEY_DATA, result);
        activity.setResult(Activity.RESULT_OK, data);
    }

    @Override
    public void onHide(ScreenViewActivity activity) {
        stopRefreshing(activity);
    }

    private void startRefreshing() {
        if (currentScreen.getRefreshRateMs() == 0) {
            stopRefreshing(activity);
            return;
        }

        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(activity, RefreshReceiver.class);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(activity, REQ_REFRESH, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        final long interval = currentScreen.getRefreshRateMs();
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + interval, interval, pendingIntent);
    }

    private void stopRefreshing(ScreenViewActivity activity) {
        AlarmManager alarmManager =
                (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(activity, RefreshReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(activity, REQ_REFRESH, intent, 0);

        alarmManager.cancel(sender);
    }

    @Override
    public void onRefresh() {
        for (Object c : currentViewControllers) {
            if (c instanceof IOnRefresh) {
                final Bundle extras = getActivity().getIntent().getExtras();
                final Object data = extras != null ? extras.get(KEY_DATA) : null;
                ((IOnRefresh) c).onRefresh(this, ScreenViewUtils.getContentView(activity), data);
            }
        }
    }

    public boolean clickMenuItem(MenuItem item) {
        for (Object vc : currentViewControllers) {
            final boolean r = onMenuItemClick(vc, item);
            if (r) return true;
        }

        for (Object vc : startViewControllers) {
            final boolean r = onMenuItemClick(vc, item);
            if (r) return true;
        }

        return false;
    }

    @Override
    public boolean onMenuItemClick(@Nullable Object vc, MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (currentScreen.withBackButton()) {
                    activity.onBackPressed();
                }
                return true;
        }

        return onMenuItemClickListener != null && onMenuItemClickListener.onMenuItemClick(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // override it on generated class
    }

    @Override
    public void start(Screen screen, Object data, boolean finishCurrent) {
        start(screen, data, finishCurrent, false, 0);
    }

    public void start(Screen screen, Object data, boolean finishCurrent, boolean finishAffinity, int requestCode) {
        screen.setPrevScreen(currentScreen);
        currentScreen = screen;

        Class<? extends Activity> activityClass =
                StartActivity.getActivityCls(currentScreen.getOrientation());

        dataMap.put(currentScreen.getClass().getName(), data);

        Intent intent = new Intent();
        if (screen.isNoHistory()) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        }
        if (screen.needClearTask()) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }

        intent.setClass(activity, activityClass);

        activity.startActivityForResult(intent, requestCode);

        if (!finishCurrent) return; // else {

        if (finishAffinity) {
            ActivityCompat.finishAffinity(activity);
        } else {
            activity.superFinish();
        }
    }


    @Override
    public Context getContext() {
        return activity;
    }

    @Override
    public void addViewController(Object vc) {
        currentViewControllers.add(vc);
    }

    @Override
    public void removeViewController(Object vc) {
        currentViewControllers.remove(vc);
    }

    protected void clearViewControllers() {
        currentViewControllers.clear();
    }


    protected void setActivity(ScreenViewActivity activity) {
        this.activity = activity;
    }

    @Override
    public ScreenViewActivity getActivity() {
        return activity;
    }


    protected void setCurrentScreen(Screen currentScreen) {
        this.currentScreen = currentScreen;
    }

    public Screen getCurrentScreen() {
        return currentScreen;
    }


    protected boolean isHistoryEnabled() {
        return historyEnabled;
    }

    protected void setHistoryEnabled(boolean historyEnabled) {
        this.historyEnabled = historyEnabled;
    }

    public void addOnBackAction(Runnable action) {
        backActions.add(action);
    }

    public void addOnBackAction(Runnable action, boolean withFinishOnBack) {
        if (withFinishOnBack) {
            addOnBackAction(action);

        } else {
            replaceBackActions.add(action);
        }
    }

    public void clearAllOnBackActions() {
        backActions.clear();
        replaceBackActions.clear();
    }

    public String getString(@StringRes int stringId) {
        return activity.getString(stringId);
    }

    public Resources getResources() {
        return activity.getResources();
    }

    public int getColor(int colorId) {
        return ContextCompat.getColor(activity, colorId);
    }

    public void setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener onMenuItemClickListener) {
        this.onMenuItemClickListener = onMenuItemClickListener;
    }

    public View getContentView() {
        return ScreenViewUtils.getContentView(activity);
    }

    public <T> void notifyViewController(Class<?> cls, int key, T data) {
        final Object vc = getViewController(cls.getName());
        //noinspection unchecked
        ((IOnNotify<T>) vc).onNotify(key, data);
    }

    public void notifyViewController(Class<?> cls, int key) {
        notifyViewController(cls, key, null);
    }

    public <T> void notifyViewController(Class<?> cls, T data) {
        notifyViewController(cls, 0, data);
    }

    public void notifyViewController(Class<?> cls) {
        notifyViewController(cls, null);
    }

    public void setCurrentViewControllers(ArrayList<Object> currentViewControllers) {
        this.currentViewControllers = currentViewControllers;
    }

    public void setTitle(@NotNull String text) {
        ScreenViewUtils.setToolbarTitle(activity, text);
    }
}
