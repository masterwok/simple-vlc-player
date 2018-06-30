package com.masterwok.simplevlcplayer.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.FileDescriptor;

public class FileUtil {

    /**
     * Start open document activity.
     *
     * @param activity    The current activity.
     * @param category    The category of documents to show (e.g. Intent.CATEGORY_OPENABLE).
     * @param type        The type of documents to show (e.g. "image/*").
     * @param requestCode The request code used to receive results.
     */
    public static void startOpenDocumentActivity(
            Activity activity,
            String category,
            String type,
            int requestCode
    ) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        if (category != null) {
            intent.addCategory(category);
        }

        if (type != null) {
            intent.setType(type);
        }

        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * Get a file descriptor for the provided Uri.
     *
     * @param context The context of the content resolver.
     * @param uri     The uri to get a file descriptor for.
     * @param mode    The file descriptor mode.
     * @return If successful, the file descriptor. Else, null.
     */
    public static FileDescriptor getUriFileDescriptor(
            Context context,
            Uri uri,
            String mode
    ) {
        try {
            //noinspection ConstantConditions
            return context
                    .getContentResolver()
                    .openFileDescriptor(uri, mode)
                    .getFileDescriptor();
        } catch (Exception ignored) {
            return null;
        }
    }
}
