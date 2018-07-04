package com.masterwok.simplevlcplayer.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * This class provides static convenience methods for bitmaps.
 */
public class BitmapUtil {

    /**
     * Convert the provided drawable image to a bitmap.
     *
     * @param drawable The drawable to convert.
     * @return The bitmap representation of the drawable.
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
        );

        final Canvas canvas = new Canvas(bitmap);

        drawable.setBounds(
                0,
                0,
                canvas.getWidth(),
                canvas.getHeight()
        );

        drawable.draw(canvas);

        return bitmap;
    }
}
