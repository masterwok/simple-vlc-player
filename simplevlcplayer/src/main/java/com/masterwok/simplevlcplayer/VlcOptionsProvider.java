package com.masterwok.simplevlcplayer;

import java.util.ArrayList;

/**
 * This singleton class is used to provide options when initializing LibVlc.
 */
@SuppressWarnings("unused")
public final class VlcOptionsProvider {

    private static class InstanceHolder {
        private static final VlcOptionsProvider instance = new VlcOptionsProvider();
    }

    private ArrayList<String> options = null;

    private VlcOptionsProvider() {
        // Singleton
    }

    /**
     * Get the singleton instance.
     *
     * @return The options provider instance.
     */
    public static VlcOptionsProvider getInstance() {
        return InstanceHolder.instance;
    }

    /**
     * Set custom options to initialize LibVLC with.
     *
     * @param options A list of options.
     */
    public void setOptions(ArrayList<String> options) {
        this.options = options;
    }

    /**
     * Get the current list of options.
     *
     * @return The list of options.
     */
    public ArrayList<String> getOptions() {
        return options;
    }

}
