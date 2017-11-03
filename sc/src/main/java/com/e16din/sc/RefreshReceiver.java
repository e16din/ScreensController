package com.e16din.sc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RefreshReceiver extends BroadcastReceiver {

    private IScreensController controller = ScreensController.get();

    @Override
    public void onReceive(Context context, Intent intent) {
        controller.onRefresh();
    }
}
