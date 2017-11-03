package com.e16din.sc;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.e16din.sc.activities.ScreenViewActivity;
import com.e16din.sc.activities.StartActivity;
import com.e16din.sc.screens.Screen;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class ScreensController implements IScreensController {

    private static ScreensController screenController;
    private static Screen firstScreen;

    public static void set(@NonNull ScreensController screenController, Screen firstScreen) {
        ScreensController.screenController = screenController;
        ScreensController.firstScreen = firstScreen;
    }

    public static ScreensController get() {
        return screenController;
    }

    private static final int REQ_REFRESH = 0;

    private ScreenViewActivity activity;
    private Screen currentScreen;
    private MenuItem.OnMenuItemClickListener onMenuItemClickListener;

    private boolean historyEnabled = false;

    private List<String> startViewControllers = new ArrayList<>();
    private List<Object> viewControllers = new ArrayList<>();
    private Map<String, List<Runnable>> actionsMap = new HashMap<>(); // key is viewController class name
    private List<Runnable> backActions = new ArrayList<>();
    private List<Runnable> replaceBackActions = new ArrayList<>();


    @Override
    public void beforeBindActivity(ScreenViewActivity activity) {
        if (isHistoryEnabled()) {
            String lastScreenName = null;//todo: add logic
            if (lastScreenName != null) {
                currentScreen = null;// todo: deserialize screen from json
            }
        }

        if (currentScreen == null) {
            currentScreen = firstScreen();
        }

        if (currentScreen.getTheme() != Screen.INVALID_VALUE) {
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
    public void saveData(Object data) {
        activity.setIntent(createIntentWithData(data));
    }

    @Override
    public void runAction(Object viewController, Runnable action) {
        String name = getClassDefaultName(viewController);

        if (activity != null) {
            for (Object vc : viewControllers) {
                if (getClassDefaultName(vc).equals(name)) {
                    action.run();
                    return;
                }
            }
        } // else {

        saveAction(name, action);
    }

    private String getClassDefaultName(@NonNull Object name) {
        return name.getClass().getName().split("\\$")[0];
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

    protected abstract IViewController[] buildViewControllers(Object data, View view, String screenName);

    protected abstract Object[] buildSimpleViewControllers(Object data, View view, String screenName);

    protected abstract boolean isStartOnceController(String controllerName);

    @Override
    public void onShow(ScreenViewActivity activity) {
        this.activity = activity;

        Object data = null;
        if (activity.getIntent().getExtras() != null) {
            data = activity.getIntent().getExtras().get(KEY_DATA);
        }

        if (this.viewControllers != null) {
            this.viewControllers.clear();
        }
        replaceBackActions.clear();
        backActions.clear();

        addViewControllers(activity, data);

        stopRefreshing();
        startRefreshing();
    }

    private void addViewControllers(Activity activity, Object data) {
        final View vContent = ScreenViewUtils.getContentView(activity);
        final String screenName = currentScreen.getClass().getName();
        final Object[] simpleViewControllers = buildSimpleViewControllers(data, vContent, screenName);

        if (simpleViewControllers != null) {
            for (Object vc : simpleViewControllers) {
                // always enabled

                if (vc instanceof MenuItem.OnMenuItemClickListener) {
                    onMenuItemClickListener = (MenuItem.OnMenuItemClickListener) vc;
                }
                addViewController(vc);
                runAllActions(vc);
            }
        }

        final IViewController[] viewControllers = buildViewControllers(data, vContent, screenName);

        if (viewControllers != null) {
            for (IViewController vc : viewControllers) {
                boolean enabled = true;
                if (vc instanceof IEnabled) {
                    enabled = ((IEnabled) vc).enabled(this);
                }

                if (!enabled) continue; // else {

                if (vc instanceof MenuItem.OnMenuItemClickListener) {
                    onMenuItemClickListener = (MenuItem.OnMenuItemClickListener) vc;
                }

                final String controllerName = getClassDefaultName(vc);
                if (isStartOnceController(controllerName)) {
                    if (!startViewControllers.contains(controllerName)) {
                        startViewControllers.add(controllerName);
                        vc.onBindView(this, vContent, screenName);
                    }

                } else {
                    vc.onBindView(this, vContent, screenName);
                    addViewController(vc);
                }
                runAllActions(vc);
            }
        }
    }

    private void runAllActions(Object vc) {
        String vcName = getClassDefaultName(vc);
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
        backActions.clear();
        replaceBackActions.clear();
        startViewControllers.clear();

        Screen prevScreen = currentScreen.getPrevScreen();
        currentScreen.setPrevScreen(null);

        currentScreen = prevScreen;

        activity.superFinish();
    }

    @Override
    public void onHide() {
        stopRefreshing();
        this.activity = null;
    }

    private void startRefreshing() {
        if (currentScreen.getRefreshRateMs() == 0) {
            stopRefreshing();
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

    private void stopRefreshing() {
        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(activity, RefreshReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(activity, REQ_REFRESH, intent, 0);

        alarmManager.cancel(sender);
    }

    @Override
    public void onRefresh() {
        for (Object c : viewControllers) {
            if (c instanceof IRefresh) {
                final Bundle extras = getActivity().getIntent().getExtras();
                final Object data = extras != null ? extras.get(KEY_DATA) : null;
                ((IRefresh) c).refresh(this, ScreenViewUtils.getContentView(activity), data);
            }
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
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
    public void start(Screen screen, Object data, boolean finishCurrent) {
        screen.setPrevScreen(currentScreen.getPrevScreen());
        currentScreen = screen;

        Class<? extends Activity> activityClass =
                StartActivity.getActivityCls(currentScreen.getOrientation());

        Intent intent = createIntentWithData(data);
        if (screen.isNoHistory()) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        }
        intent.setClass(getContext(), activityClass);

        getContext().startActivity(intent);

        if (finishCurrent) {
            getActivity().superFinish();
        }
    }

    @NonNull
    private Intent createIntentWithData(Object data) {
        final Intent intent = new Intent();

        if (data != null) {
            if (data instanceof Serializable) {
                intent.putExtra(KEY_DATA, (Serializable) data);
            } else if (data instanceof Parcelable) {
                intent.putExtra(KEY_DATA, (Parcelable) data);
            }
        }
        return intent;
    }

    @Override
    public Context getContext() {
        return activity;
    }

    @Override
    public void addViewController(Object vc) {
        viewControllers.add(vc);
    }

    @Override
    public void removeViewController(Object dc) {
        viewControllers.remove(dc);
    }

    protected void clearViewControllers() {
        viewControllers.clear();
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

    public String getString(@StringRes int stringId) {
        return getContext().getString(stringId);
    }

    public Resources getResources() {
        return getContext().getResources();
    }

    public int getColor(int colorId) {
        return ContextCompat.getColor(getContext(), colorId);
    }

    public void setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener onMenuItemClickListener) {
        this.onMenuItemClickListener = onMenuItemClickListener;
    }

    public View getContentView() {
        return ScreenViewUtils.getContentView(activity);
    }
}
