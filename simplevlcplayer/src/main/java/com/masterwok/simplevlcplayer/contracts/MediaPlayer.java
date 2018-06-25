package com.masterwok.simplevlcplayer.contracts;

import android.net.Uri;

import com.masterwok.simplevlcplayer.VlcMediaPlayer;

/**
 * This contract provides a way to interact with a media player.
 */
public interface MediaPlayer {

    void release();

    void play();

    void pause();

    void togglePlayback();

    void stop();

    void setMedia(Uri uri);

    void setSubtitle(Uri uri);

    void setCallback(VlcMediaPlayer.Callback callback);

    long getTime();

    void setTime(long time);

    long getLength();

    boolean isPlaying();

    interface Callback {
        void onPlayerOpening();

        void onPlayerSeekStateChange(boolean canSeek);

        void onPlayerPlaying();

        void onPlayerPaused();

        void onPlayerStopped();

        void onPlayerEndReached();

        void onPlayerError();

        void onPlayerTimeChange(long timeChanged);

        void onPlayerPositionChange(float positionChanged);
    }
}
