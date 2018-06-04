package com.masterwok.simplevlcplayer.utils;

import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

/**
 * This class provides static convenience methods for views.
 */
public class ViewUtil {

    /**
     * Translate the provided view above or below its parent. If translating above,
     * then the bottom of the child view will be aligned at the top of the parent view.
     *
     * @param view       The view to hide.
     * @param slideAbove If true, translate up. Else, translate down.
     */
    public static void slideViewAboveOrBelowParent(
            View view,
            boolean slideAbove
    ) {
        view
                .animate()
                .translationY(slideAbove
                        ? -view.getBottom()
                        : view.getBottom()
                )
                .setInterpolator(new AccelerateInterpolator()).start();
    }

    /**
     * Translate the provided view back to its original position.
     *
     * @param view The view to translate.
     */
    public static void resetVerticalTranslation(View view) {
        view.animate()
                .translationY(0)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /**
     * Set the transparency dim amount on a window.
     *
     * @param window The window to set the dim for.
     * @param amount The dim amount.
     */
    public static void setDimAmount(Window window, float amount) {
        if (window == null) {
            return;
        }

        window.setDimAmount(amount);
    }
}
