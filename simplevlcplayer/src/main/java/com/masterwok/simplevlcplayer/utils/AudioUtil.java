package com.masterwok.simplevlcplayer.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;


/**
 * This class provides static convenience methods for working with audio.
 */
@SuppressWarnings("UnusedReturnValue")
public class AudioUtil {

    /**
     * Attempt to request audio focus for the application.
     *
     * @param context The context to resolve the audio manager system service from.
     * @return If audio manager resolved, true. Else, false.
     */
    public static boolean requestAudioFocus(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (audioManager == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                    .build();

            AudioFocusRequest audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(audioAttributes)
                    .setAcceptsDelayedFocusGain(false)
                    .build();

            audioManager.requestAudioFocus(audioFocusRequest);

            return true;
        }

        //noinspection deprecation
        audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
        );

        return true;
    }
}
