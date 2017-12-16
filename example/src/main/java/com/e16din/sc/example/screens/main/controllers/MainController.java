package com.e16din.sc.example.screens.main.controllers;


import android.support.design.widget.Snackbar;
import android.view.MenuItem;
import android.view.View;

import com.e16din.sc.ScreensController;
import com.e16din.sc.annotations.OnBind;
import com.e16din.sc.annotations.ViewController;
import com.e16din.sc.example.R;
import com.e16din.sc.example.screens.main.MainScreen;


@ViewController(screen = MainScreen.class)
public class MainController implements MenuItem.OnMenuItemClickListener {

    private ScreensController sc;

    @OnBind
    public void onBindView(ScreensController sc, final View view, Object data) {
        this.sc = sc;

        View vFab = view.findViewById(R.id.vFab);
        vFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Snackbar.make(sc.getContentView(), "settings", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                return true;
        }

        return false;
    }
}
