package com.masterwok.demosimplevlcplayer.helpers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Helper methods for dealing with files.
 */
public class FileHelper {

    /**
     * Copy input stream to destination file.
     *
     * @param in          The input stream.
     * @param destination The destination file.
     * @throws IOException Thrown when problem create file output stream.
     */
    public static void copy(
            InputStream in,
            File destination
    ) throws IOException {
        OutputStream out = new FileOutputStream(destination);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
    }

}
