package com.masterwok.simplevlcplayer.utils;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;

/**
 * This class provides static convenience methods for Android resources.
 */
@SuppressWarnings("WeakerAccess")
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
    public static int getColor(
            @NonNull Context context,
            int id
    ) {
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
    public static String getString(
            @NonNull Context context,
            int id
    ) {
        return context.getString(id);
    }

    /**
     * Get dimension density dependent resource value.
     *
     * @param context The context to get the resource from.
     * @param id      The id of the dimen resource.
     * @return The density dependent resource value.
     */
    public static float getDimen(
            @NonNull Context context,
            int id
    ) {
        return context
                .getResources()
                .getDimension(id);
    }

    /**
     * Get dimension as density-independent (dp) value.
     *
     * @param context The context to get the resource from.
     * @param id      The id of the dimen resource.
     * @return The density-independent resource value.
     */
    public static int getDimenDp(
            @NonNull Context context,
            int id
    ) {
        final float density = context
                .getResources()
                .getDisplayMetrics()
                .density;

        return (int) (getDimen(context, id) / density);
    }
}