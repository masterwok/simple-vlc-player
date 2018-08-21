package com.masterwok.simplevlcplayer;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;

import org.videolan.libvlc.util.AndroidUtil;
import org.videolan.libvlc.util.VLCUtil;

import java.io.File;
import java.util.ArrayList;

/**
 * This singleton class is used to provide options when initializing LibVlc.
 */
@SuppressWarnings("unused")
public final class VlcOptionsProvider {

    // Some magical java way of guaranteeing thread safe singleton initialization..
    private static class InstanceHolder {
        private static final VlcOptionsProvider instance = new VlcOptionsProvider();
    }

    private ArrayList<String> options = null;

    private VlcOptionsProvider() {
        // Help, I'm trapped!
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
     * This builder class can be used to build a list of VLC options. Non-set
     * options use their default values when built. This class aims to have the
     * same default values as the VLCOptions class in the vlc-android project.
     * <p>
     *
     * @see <a href="https://code.videolan.org/videolan/vlc-android/blob/master/vlc-android/src/org/videolan/vlc/util/VLCOptions.java">VLCOptions.java</a>
     */
    @SuppressWarnings("SpellCheckingInspection")
    public static class Builder {
        private static int AudioTrackSessionId = 0;

        private final File keyStoreFile;

        private boolean withChromecastAudioPassthrough;
        private int chromecastConversionQuality = 2;

        private String subtitleEncoding = "";
        private boolean hasSubtitleBackground;
        private boolean isSubtitleBold;
        private int subtitleBackgroundOpacity = 128;
        private int subtitleColor = 16777215;
        private int subtitleSize = 16;

        private String chroma = "";
        private boolean timeStretching = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        private boolean withFrameSkip;
        private int networkCaching = 0;
        private int deblocking = -1;
        private int openGl = -1;

        private boolean isVerbose;

        public Builder(Context context) {
            keyStoreFile = new File(
                    context.getDir("keystore", Context.MODE_PRIVATE),
                    "file"
            );

            if (Build.VERSION.SDK_INT < 21) {
                return;
            }

            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

            if (audioManager == null) {
                return;
            }

            AudioTrackSessionId = audioManager.generateAudioSessionId();
        }

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
            hasSubtitleBackground = true;

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

        public Builder withChromecastAudioPassthrough(boolean withPassthrough) {
            withChromecastAudioPassthrough = withPassthrough;
            return this;
        }

        public Builder withChromecastConversionQuality(int quality) {
            chromecastConversionQuality = quality;
            return this;
        }

        public Builder withTimeStretching(boolean withTimeStretching) {
            this.timeStretching = withTimeStretching;
            return this;
        }

        public Builder withNetworkCaching(int networkCaching) {
            if (networkCaching > 60000) {
                networkCaching = 60000;
            } else if (networkCaching < 0) {
                networkCaching = 0;
            }

            this.networkCaching = networkCaching;

            return this;
        }

        public Builder withDeblocking(int deblocking) {
            this.deblocking = deblocking;
            return this;
        }

        public Builder withFrameSkip(boolean frameSkip) {
            this.withFrameSkip = frameSkip;
            return this;
        }

        public Builder setVerbose(boolean verbose) {
            isVerbose = verbose;
            return this;
        }

        public Builder withSubtitleEncoding(String subtitleEncoding) {
            this.subtitleEncoding = subtitleEncoding;
            return this;
        }

        public Builder withChroma(String chroma) {
            chroma = chroma.equals("YV12")
                    ? ""
                    : chroma;

            this.chroma = chroma;
            return this;
        }

        public Builder withOpenGl(int openGl) {
            this.openGl = openGl;
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

            if (withChromecastAudioPassthrough) {
                options.add("--sout-chromecast-audio-passthrough");
            } else {
                options.add("--no-sout-chromecast-audio-passthrough");
            }

            options.add("--sout-chromecast-conversion-quality=" + chromecastConversionQuality);
            options.add("--sout-keep");

            options.add(
                    timeStretching
                            ? "--audio-time-stretch"
                            : "--no-audio-time-stretch"
            );

            if (networkCaching > 0) {
                options.add("--network-caching=" + networkCaching);
            }

            if (openGl == 1) {
                options.add("--vOut=gles2,none");
            } else if (openGl == 0) {
                options.add("--vOut=android_display,none");
            }

            options.add("--avcodec-skiploopfilter");
            options.add("" + getDeblocking(deblocking));
            options.add("--avcodec-skip-frame");
            options.add(withFrameSkip ? "2" : "0");
            options.add("--avcodec-skip-idct");
            options.add(withFrameSkip ? "2" : "0");
            options.add("--subsdec-encoding");
            options.add(subtitleEncoding);
            options.add("--stats");
            options.add("--android-display-chroma");
            options.add(chroma);
            options.add("--audio-resampler");
            options.add(getResampler());
            options.add("--audiotrack-session-id=" + AudioTrackSessionId);

            options.add("--keystore");

            if (AndroidUtil.isMarshMallowOrLater) {
                options.add("file_crypt,none");
            } else {
                options.add("file_plaintext,none");
            }

            options.add("--keystore-file");
            options.add(keyStoreFile.getAbsolutePath());

            return options;
        }

        private static String getResampler() {
            final VLCUtil.MachineSpecs m = VLCUtil.getMachineSpecs();
            return (m == null || m.processors > 2) ? "soxr" : "ugly";
        }

        private static int getDeblocking(int deblocking) {
            int ret = deblocking;

            if (deblocking > 4) {
                return 3;
            }

            if (deblocking > 0) {
                return deblocking;
            }

            VLCUtil.MachineSpecs machineSpecs = VLCUtil.getMachineSpecs();

            if (machineSpecs == null) {
                return ret;
            }

            // Set some reasonable sDeblocking defaults:
            //
            // Skip all (4) for armv6 and MIPS by default
            // Skip non-ref (1) for all armv7 more than 1.2 Ghz and more than 2 cores
            // Skip non-key (3) for all devices that don't meet anything above
            if ((machineSpecs.hasArmV6 && !(machineSpecs.hasArmV7)) || machineSpecs.hasMips) {
                ret = 4;
            } else if (machineSpecs.frequency >= 1200 && machineSpecs.processors > 2) {
                ret = 1;
            } else if (machineSpecs.bogoMIPS >= 1200 && machineSpecs.processors > 2) {
                ret = 1;
            } else {
                ret = 3;
            }

            return ret;
        }

    }

}
