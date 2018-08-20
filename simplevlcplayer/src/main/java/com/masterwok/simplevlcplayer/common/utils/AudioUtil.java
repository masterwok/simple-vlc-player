package com.masterwok.simplevlcplayer.common.utils;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;

/**
 * This class provides static convenience methods for Android audio.
 */
@SuppressWarnings("UnusedReturnValue")
public class AudioUtil {

    /**
     * Get the audio manager system service.
     *
     * @param context The context to resolve the service in.
     * @return The audio manager service.
     */
    public static AudioManager getAudioManager(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(AudioManager.class);
        }

        return (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * Request permanent audio focus.
     *
     * @param audioManager The audio manager used to create the request.
     * @param listener     The listener of the audio focus request.
     * @return Whether or not audio focus was granted.
     */
    public static boolean requestAudioFocus(
            AudioManager audioManager,
            AudioManager.OnAudioFocusChangeListener listener
    ) {
        final int result = audioManager.requestAudioFocus(
                listener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
        );

        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

}
