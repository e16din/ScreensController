package com.e16din.sc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.e16din.sc.activities.ScreenViewActivity;
import com.e16din.sc.activities.StartActivity;
import com.e16din.sc.screens.LockScreen;
import com.e16din.sc.screens.Screen;
import com.e16din.sc.services.LockScreenService;
import com.e16din.topactivity.TopActivity;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static com.e16din.sc.UtilsExtKt.INVALID_VALUE;
import static com.e16din.topactivity.TopActivityKt.activity;


public abstract class ScreensController implements IScreensController {

    private static final int ON_BIND = 1;
    private static final int ON_SHOW = 2;

    private static ScreensController sc;

    public static Screen getFirstScreen() {
        return firstScreen;
    }

    private static Screen firstScreen;
    private static Object firstData;
    private static boolean startOnRequest;

    private static int splashDelayMs = 0;

    @LayoutRes
    private static int splashLayoutRes = INVALID_VALUE;

    public ScreensController(Application app) {
        TopActivity.init(app, null);
    }

    public static void set(@NonNull ScreensController screenController, Screen firstScreen, Object data) {
        ScreensController.sc = screenController;
        ScreensController.firstScreen = firstScreen;
        ScreensController.firstData = data;
    }

    public static ScreensController instance() {
        return sc;
    }

    public static void startOnRequest(boolean startOnRequest) {
        ScreensController.startOnRequest = startOnRequest;
    }

    public static boolean isStartOnRequest() {
        return startOnRequest;
    }

    public static void startFirstScreen() {
        startOnRequest = false;

        final Activity activity = instance().getActivity();
        activity.startActivity(new Intent(activity, StartActivity.class));
        ActivityCompat.finishAffinity(activity);
    }

    private static void setSplash(@LayoutRes int layoutRes, int delayMs) {
        ScreensController.splashLayoutRes = layoutRes;
        ScreensController.splashDelayMs = delayMs;
    }

    public static void setSplashDelay(int delayMs) {
        setSplash(INVALID_VALUE, delayMs);
    }

    public static void setSplashLayout(@LayoutRes int layoutRes, int delayMs) {
        setSplash(layoutRes, delayMs);
    }

    public static int getSplashDelayMs() {
        return splashDelayMs;
    }


    private WeakReference<View> contentViewLink;
    private WeakReference<ViewGroup> decorViewLink; // for lock screen service

    private Stack<Screen> screenStack = new Stack<>();
    private MenuItem.OnMenuItemClickListener onMenuItemClickListener;
    private boolean historyEnabled = false;

    protected List<String> startViewControllers = new ArrayList<>();
    protected List<Object> currentViewControllers = new ArrayList<>();

    // key is a Screen class name, value is a ViewState object
    private Map<String, Object> dataMap = new HashMap<>();

    // key is viewController class name
    private Map<String, List<Runnable>> actionsMap = new HashMap<>();
    private List<Runnable> backActions = new ArrayList<>();
    private final List<Runnable> replaceBackActions = new ArrayList<>();


    public static int getSplashLayoutRes() {
        return splashLayoutRes;
    }


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
    public void beforeBindActivity(Activity activity) {
        bindScreen();

        final Screen currentScreen = screenStack.peek();
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

    private void bindScreen() {
        if (isHistoryEnabled()) {
            String lastScreenName = null;//todo: add logic
            if (lastScreenName != null) {
                screenStack.pop();
            }
        }

        if (screenStack.isEmpty()) {
            screenStack.push(firstScreen);
            final Screen currentScreen = screenStack.peek();
            dataMap.put(currentScreen.getClass().getName(), firstData);
        }
    }

    public void onBindLockScreenService(LockScreenService service) {
        bindScreen();
        final Screen currentScreen = screenStack.peek();
        service.setContentView(currentScreen.getLayout());
        service.setTheme(currentScreen.getTheme());
    }

    @Override
    public void onBindActivity(Activity activity) {
        final Screen currentScreen = screenStack.peek();
        activity.setContentView(currentScreen.getLayout());

        ScreenViewUtils.initToolbar(activity, currentScreen.withBackButton());
    }

    @Override // todo: refactor it
    public void runAction(Object viewController, Runnable action) {
        String name = Utils.getClassDefaultName(viewController);

        if (getDecorView() != null) {
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


    private void bindView(View view, ViewGroup vLockContainer) {
        final Screen currentScreen = screenStack.peek();

        if (currentScreen instanceof LockScreen) {
            decorViewLink = new WeakReference<>(vLockContainer);
        }
        contentViewLink = new WeakReference<>(view);

        if (currentViewControllers != null) {
            currentViewControllers.clear();
        }
        replaceBackActions.clear();
        backActions.clear();

        final String name = currentScreen.getClass().getName();
        Log.i("debug", "bindView: screen name: " + name);

        addViewControllers(view, ON_BIND);
    }

    @Override
    public void onBindView(View view, ViewGroup vDecor) {
        bindView(view, vDecor);
    }

    @Override
    public void onShowView(View view) {
        final Screen currentScreen = screenStack.peek();
        final String name = currentScreen.getClass().getName();
        Log.i("debug", "onShowView: screen name: " + name);
        showViewControllers();
    }

    private void showViewControllers() {
        for (Object vc : currentViewControllers) {
            final String controllerName = Utils.getClassDefaultName(vc);
            boolean enabled = enabled(controllerName);

            if (enabled) {
                onShowViewController(vc);
            }

            runAllActions(vc);
        }
    }

    private void addViewControllers(View vContent, int method) {
        final Screen currentScreen = screenStack.peek();
        final String screenName = currentScreen.getClass().getName();
        final Object data = getData(screenName);

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
                        switch (method) {
                            case ON_BIND:
                                onBindViewController(vc, vContent, data);
                                break;
                            case ON_SHOW:
                                onShowViewController(vc);
                                break;
                        }

                    }
                }

            } else {
                if (enabled) {
                    switch (method) {
                        case ON_BIND:
                            onBindViewController(vc, vContent, data);
                            break;
                        case ON_SHOW:
                            onShowViewController(vc);
                            break;
                    }
                }
                addViewController(vc);
            }
            runAllActions(vc);
        }
    }

    private Object getData(String screenName) {
        return dataMap.get(screenName);
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

        final Screen currentScreen = screenStack.pop();

        if (result != null) {
            setResult(result);
        }

        if (screenStack.size() > 0) {
            final Screen prevScreen = screenStack.peek();
            if (prevScreen instanceof LockScreen) {
                final String screenName = prevScreen.getClass().getName();
                final Object data = getData(screenName);
                dataMap.remove(currentScreen.getClass().getName());

                startScreen(prevScreen, data, true);
                screenStack.pop();
                return;
            }
        } // else if (!LockScreen) {

        dataMap.remove(currentScreen.getClass().getName());
        contentViewLink = null;

        final Activity activity = getActivity();
        if (activity == null) return; // else {

        if (affinity) {
            ActivityCompat.finishAffinity(activity);
        } else {
            if (activity instanceof ScreenViewActivity)
                ((ScreenViewActivity) activity).superFinish();
            else
                activity.finish();
        }
    }

    public void setResult(Serializable result) {// check this on lock screen
        final Intent data = new Intent();
        data.putExtra(KEY_DATA, result);
        getActivity().setResult(Activity.RESULT_OK, data);
    }

    @Override
    public void onHideView(View view) {
        final Screen currentScreen = screenStack.peek();
        Log.i("debug", "onHideView:");
        if (currentScreen != null) {
            final String name = currentScreen.getClass().getName();
            Log.i("debug", "screen name: " + name);
        }

        hideViewControllers();
    }

    private void hideViewControllers() {
        for (Object vc : currentViewControllers) {
            final String controllerName = Utils.getClassDefaultName(vc);
            boolean enabled = enabled(controllerName);

            if (enabled) {
                onHideViewController(vc);
            }

            runAllActions(vc);
        }
    }

    @Override
    public void onRefresh() {
        for (Object c : currentViewControllers) {
            if (c instanceof IOnRefresh) {
                final Activity activity = getActivity();
                final Bundle extras = activity.getIntent().getExtras();
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
                final Screen currentScreen = screenStack.peek();
                if (currentScreen.withBackButton()) {
                    getActivity().onBackPressed();
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
    public void startScreen(Screen screen, Object data, boolean finishCurrent) {
        startScreen(screen, data, finishCurrent, false, 0);
    }

    public void startScreen(Screen screen, Object data, boolean finishCurrent, boolean finishAffinity, int requestCode) {
        final Screen currentScreen = screenStack.peek();

        final Screen nextScreen = screenStack.push(screen);
        dataMap.put(nextScreen.getClass().getName(), data);

        if (nextScreen instanceof LockScreen) {
            final ViewGroup decorView = getDecorView();

            @SuppressLint("RestrictedApi")
            ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(decorView.getContext(), nextScreen.getTheme());
            View newView = LayoutInflater.from(contextThemeWrapper).inflate(nextScreen.getLayout(), decorView, false);

            hideLockScreen();
            decorView.addView(newView, LockScreenService.params);

            bindView(newView, decorView);
            onShowView(null);

            return;
        } // else {

        if (currentScreen instanceof LockScreen) {
            hideLockScreen();
            unlockScreen();
        }

        Intent intent = new Intent();
        if (screen.isNoHistory()) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        }
        if (screen.needClearTask()) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }

        final Activity activity = getActivity();

        Class<? extends Activity> activityClass =
                StartActivity.getActivityCls(nextScreen);
        intent.setClass(activity, activityClass);

        activity.startActivityForResult(intent, requestCode);

        if (finishCurrent) {
            finishScreen();
        }
    }

    private void hideLockScreen() {
        getDecorView().removeView(getContentView());
        onHideView(null);
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

    public Screen getCurrentScreen() {
        return screenStack.empty() ? null : screenStack.peek();
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

    public void unlockScreenAndExit() {
        unlockScreen();
        System.exit(0);
    }

    private void unlockScreen() {
        final Context context = getContentView().getContext().getApplicationContext();
        context.stopService(new Intent(context, LockScreenService.class));
    }

    @Override
    public Activity getActivity() {
        return activity();
    }

    public String getString(@StringRes int stringId) {
        return getActivity().getString(stringId);
    }

    public Resources getResources() {
        return getActivity().getResources();
    }

    public int getColor(int colorId) {
        return ContextCompat.getColor(getActivity(), colorId);
    }

    public void setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener onMenuItemClickListener) {
        this.onMenuItemClickListener = onMenuItemClickListener;
    }

    public View getContentView() {
        return contentViewLink.get();
    }

    public ViewGroup getDecorView() {
        return decorViewLink.get();
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
        ScreenViewUtils.setToolbarTitle(getActivity(), text);
    }

    public Stack<Screen> getScreenStack() {
        return screenStack;
    }

    public Object getCurrentData() {
        final String screenName = getCurrentScreen().getClass().getName();
        return getData(screenName);
    }
}
