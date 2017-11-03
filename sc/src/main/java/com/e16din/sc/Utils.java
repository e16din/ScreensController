package com.e16din.sc;


import android.view.View;
import android.view.ViewGroup;

class Utils {

    static void recursiveLoopChildren(View view, LoopChildrenCallback callback) {
        recursiveLoopChildren(view, callback, 0);
    }

    private static void recursiveLoopChildren(View view, LoopChildrenCallback callback, int deep) {
        if (view == null) return;

        deep += 1;

        if (view instanceof ViewGroup) {
            final ViewGroup viewGroup = (ViewGroup) view;
            callback.onChild(null, viewGroup, deep);

            for (int i = viewGroup.getChildCount() - 1; i >= 0; i--) {
                recursiveLoopChildren(viewGroup.getChildAt(i), callback);
            }
        } else {//if !ViewGroup
            callback.onChild(view, null, deep);
        }
    }

    public interface LoopChildrenCallback {
        void onChild(View view, ViewGroup viewGroup, int deep);
    }
}
