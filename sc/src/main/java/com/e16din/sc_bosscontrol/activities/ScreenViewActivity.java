package com.e16din.sc_bosscontrol.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.e16din.sc.ScreensController;
import com.e16din.sc.screens.LockScreen;

import static com.e16din.sc.UtilsExtKt.INVALID_VALUE;

public abstract class ScreenViewActivity extends AppCompatActivity {

    private String activityName;
    private String screenName;

    protected abstract ScreensController controller();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        log("startScreen ScreenViewActivity");

        controller().beforeBindActivity(this);
        super.onCreate(savedInstanceState);
        controller().onBindActivity(this);

        activityName = getClass().getSimpleName();
        screenName = controller().getCurrentScreen().getClass().getSimpleName();
    }

    private View getContentView() {
        return findViewById(android.R.id.content);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        log("restart");
    }

    @Override
    protected void onStart() {
        super.onStart();
        log("show");

        controller().onBindView(getContentView(), (ViewGroup) getWindow().getDecorView());
    }

    @Override
    protected void onStop() {
        super.onStop();
        log("hide");
        controller().onHideView(getContentView());
    }

    @Override
    protected void onResume() {
        super.onResume();
        log("resume");
        controller().onShowView(getContentView());
    }

    @Override
    protected void onPause() {
        log("pause");
        super.onPause();
    }

    @Override
    public void finish() { // also called after onBackPressed
        log("back");
        controller().onBack();
    }

    public void superFinish() {
        log("finish");
        super.finish();
    }

    private void log(String action) {
        Log.i("debug", action + " activity: " + activityName + " screen: " + screenName);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return controller().clickMenuItem(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int menuId = controller().getCurrentScreen().getMenu();
        if (menuId != INVALID_VALUE) {
            getMenuInflater().inflate(menuId, menu);
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final ScreensController controller = controller();
        if (controller.getCurrentScreen() instanceof LockScreen) return; // else {

        controller.onBindView(getContentView(),(ViewGroup) getWindow().getDecorView());
        controller.onActivityResult(requestCode, resultCode, data);
    }
}
