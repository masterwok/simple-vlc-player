package com.masterwok.simplevlcplayer.contracts;

import android.net.Uri;

/**
 * This contract provides a way to interact with a media player.
 */
public interface MediaPlayer {

    void play();

    void pause();

    void togglePlayback();

    void stop();

    void setMedia(Uri uri);

    void setSubtitle(Uri uri);

    void setCallback(com.masterwok.simplevlcplayer.MediaPlayer.Callback callback);

    long getTime();

    void setTime(long time);


    interface Callback {
        void onOpening();

        void onSeekStateChange(boolean canSeek);

        void onPlaying();

        void onPaused();

        void onStopped();

        void onEndReached();

        void onError();

        void onTimeChange(long timeChanged);

        void onPositionChange(float positionChanged);
    }
}
