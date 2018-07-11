package com.masterwok.simplevlcplayer.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.DocumentsContract;

import java.io.FileNotFoundException;

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

    /**
     * Get a thumbnail for the provided document URI.
     *
     * @param context            The context to resolve the content resolver from.
     * @param uri                The URI of the document.
     * @param width              The width of the thumbnail.
     * @param height             The height of the thumbnail.
     * @param cancellationSignal A cancellation signal.
     * @return If successful, the bitmap. Else, null.
     */
    public static Bitmap getDocumentBitmap(
            Context context,
            Uri uri,
            int width,
            int height,
            android.os.CancellationSignal cancellationSignal
    ) {
        if(width < 0 || height < 0) {
            return null;
        }

        try {
            return DocumentsContract.getDocumentThumbnail(
                    context.getContentResolver(),
                    uri,
                    new Point(width, height),
                    cancellationSignal
            );
        } catch (FileNotFoundException e) {
            return null;
        }
    }

}
