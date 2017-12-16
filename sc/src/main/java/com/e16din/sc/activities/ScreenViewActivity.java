package com.e16din.sc.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.e16din.sc.ScreensController;

import static com.e16din.sc.UtilsExtKt.INVALID_VALUE;

public abstract class ScreenViewActivity extends AppCompatActivity {

    private String activityName;
    private String screenName;

    protected abstract ScreensController controller();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        controller().beforeBindActivity(this);
        super.onCreate(savedInstanceState);
        controller().onBindActivity(this);

        activityName = getClass().getSimpleName();
        screenName = controller().getCurrentScreen().getClass().getSimpleName();

        log("start");
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        // log("restart");
    }

    @Override
    protected void onStart() {
        super.onStart();
        log("show");
        controller().onShow(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        log("hide");
        controller().onHide(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        log("resume");
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
        controller().onShow(this);
        controller().onActivityResult(requestCode, resultCode, data);
    }
}
