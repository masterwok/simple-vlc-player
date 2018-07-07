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


    public static class Builder {
        private boolean isSubtitleBold = false;
        private int subtitleSize = 16;
        private boolean isVerbose;

        public Builder setSubtitleBold(boolean isBold) {
            isSubtitleBold = isBold;
            return this;
        }

        public Builder setSubtitleSize(int size) {
            subtitleSize = size;
            return this;
        }

        public Builder setVerbose(boolean verbose) {
            isVerbose = verbose;
            return this;
        }

        @SuppressWarnings("SpellCheckingInspection")
        public ArrayList<String> build() {
            final ArrayList<String> options = new ArrayList<>();

            options.add(isVerbose ? "-vv" : "-v");

            if (isSubtitleBold) {
                options.add("--freetype-bold");
            }

            options.add("--freetype-rel-fontsize=" + subtitleSize);

            return options;
        }


    }

}
