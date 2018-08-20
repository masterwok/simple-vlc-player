package com.masterwok.simplevlcplayer.common.utils;

import android.content.Context;
import android.net.Uri;

import java.io.FileDescriptor;

/**
 * This class provides static convenience methods for working with files.
 */
public class FileUtil {

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