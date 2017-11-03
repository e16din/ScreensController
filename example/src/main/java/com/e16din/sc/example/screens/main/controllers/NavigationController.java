package com.e16din.sc.example.screens.main.controllers;


import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.e16din.sc.ScreensController;
import com.e16din.sc.annotations.ViewController;
import com.e16din.sc.example.R;
import com.e16din.sc.example.screens.main.MainScreen;


@ViewController(screen = MainScreen.class)
public class NavigationController implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout vDrawer;

    public NavigationController(final ScreensController sc, View view, Object data) {
        Log.e("debug", "NavigationController!!!");

        Toolbar vToolbar = view.findViewById(R.id.vToolbar);
        NavigationView vNavigation = view.findViewById(R.id.vNavigation);
        vDrawer = view.findViewById(R.id.vDrawer);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(sc.getActivity(), vDrawer, vToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        vDrawer.addDrawerListener(toggle);
        toggle.syncState();

        vNavigation.setNavigationItemSelectedListener(this);

        sc.addOnBackAction(new Runnable() {
            @Override
            public void run() {
                if (vDrawer.isDrawerOpen(GravityCompat.START)) {
                    vDrawer.closeDrawer(GravityCompat.START);
                } else {
                    sc.finishScreen();
                }
            }
        }, false);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_camera:
                break;
            case R.id.nav_gallery:
                break;
            case R.id.nav_slideshow:
                break;
            case R.id.nav_manage:
                break;
            case R.id.nav_share:
                break;
            case R.id.nav_send:
                break;
        }

        vDrawer.closeDrawer(GravityCompat.START);
        return true;
    }
}