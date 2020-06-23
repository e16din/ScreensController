package com.e16din.sc;


import android.app.Activity;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

final class ScreenViewUtils {

    static void initToolbar(Activity activity, boolean withBack) {
        setToolbarTitle(activity, activity.getTitle());

        if (withBack/*!activity.isTaskRoot()*/) {

            if (activity instanceof AppCompatActivity) {
                ActionBar vSupportActionBar = ((AppCompatActivity) activity).getSupportActionBar();
                if (vSupportActionBar != null) {
                    vSupportActionBar.setDisplayHomeAsUpEnabled(true);
                    vSupportActionBar.setDisplayShowHomeEnabled(true);
                }
            }

            android.app.ActionBar vActionBar = activity.getActionBar();
            if (vActionBar != null) {
                vActionBar.setDisplayHomeAsUpEnabled(true);
                vActionBar.setDisplayShowHomeEnabled(true);
            }
        }
    }

    static View getContentView(Activity activity) {
        return activity.findViewById(android.R.id.content);
    }

    static Toolbar setToolbarTitle(Activity activity, CharSequence text) {
        activity.setTitle(text);

        View view = getContentView(activity);

        final Toolbar[] vCompatToolbar = {null};
        Utils.recursiveLoopChildren(view, (view1, viewGroup, deep) -> {
            View v = view1 != null ? view1 : viewGroup;
            if (v instanceof Toolbar) {
                vCompatToolbar[0] = (Toolbar) v;
            }
        });

        if (vCompatToolbar[0] != null) {
            vCompatToolbar[0].setTitle(text);
            try {
                ((AppCompatActivity) activity).setSupportActionBar(vCompatToolbar[0]);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            ((AppCompatActivity) activity).getSupportActionBar().setTitle(text);

            return vCompatToolbar[0];
        }

        return null;
    }
}
