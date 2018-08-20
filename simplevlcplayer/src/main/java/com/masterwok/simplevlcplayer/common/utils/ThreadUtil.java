package com.masterwok.simplevlcplayer.common.utils;


import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executors;


/**
 * This class provides static convenience methods for threads.
 */
public class ThreadUtil {

    private ThreadUtil() {
    }

    /**
     * Execute the provided runnable on a background thread.
     *
     * @param runnable The runnable instance.
     */
    public static void onBackground(Runnable runnable) {
        Executors.newSingleThreadExecutor().execute(runnable);
    }

    /**
     * Execute the provided runnable on a main thread.
     *
     * @param runnable The runnable instance.
     */
    public static void onMain(Runnable runnable) {
        Looper mainLooper = Looper.getMainLooper();

        // Already on main thread, execute and return.
        if (Thread.currentThread() == mainLooper.getThread()) {
            runnable.run();
            return;
        }

        // Push work to main thread and execute.
        new Handler(mainLooper).post(runnable);
    }
}
