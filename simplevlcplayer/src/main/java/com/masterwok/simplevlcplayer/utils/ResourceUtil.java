package com.masterwok.simplevlcplayer.utils;

import android.content.Context;
import android.os.Build;

/**
 * This class provides static convenience methods for Android resources.
 */
public class ResourceUtil {

    private ResourceUtil() {
    }

    /**
     * Safely get a color resource.
     *
     * @param context The current context.
     * @param id      The id of the color resource.
     * @return The color.
     */
    public static int getColor(Context context, int id) {
        //noinspection deprecation
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? context.getResources().getColor(id, null)
                : context.getResources().getColor(id);
    }

    /**
     * Get a string resource.
     *
     * @param context The current context.
     * @param id      The id of the color resource.
     * @return A string resource.
     */
    public static String getStringResource(Context context, int id) {
        return context.getString(id);
    }
}
