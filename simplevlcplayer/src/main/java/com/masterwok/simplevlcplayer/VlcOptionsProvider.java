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


    /**
     * This builder class can be used to build a list of VLC options.
     */
    public static class Builder {
        private boolean hasSubtitleBackground = false;
        private int subtitleBackgroundOpacity = 128;
        private int subtitleColor = 16777215;
        private boolean isSubtitleBold = false;
        private int subtitleSize = 16;

        private boolean isVerbose;

        public Builder withSubtitleBold(boolean isBold) {
            isSubtitleBold = isBold;
            return this;
        }

        public Builder withSubtitleSize(int size) {
            subtitleSize = size;
            return this;
        }

        public Builder withSubtitleColor(int color) {
            subtitleColor = color;
            return this;
        }

        public Builder withSubtitleBackgroundOpacity(int opacity) {
            // Keep in bounds (0-255)
            subtitleBackgroundOpacity = opacity < 0 || opacity > 255
                    ? subtitleBackgroundOpacity
                    : opacity;

            return this;
        }

        public Builder withSubtitleBackground(boolean hasBackground) {
            hasSubtitleBackground = hasBackground;
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

            if (hasSubtitleBackground) {
                options.add("--freetype-background-opacity=" + subtitleBackgroundOpacity);
            } else {
                options.add("--freetype-background-opacity=0");
            }

            if (isSubtitleBold) {
                options.add("--freetype-bold");
            }

            options.add("--freetype-rel-fontsize=" + subtitleSize);
            options.add("--freetype-color=" + subtitleColor);

            return options;
        }


    }

}
