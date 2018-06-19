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

    void setTime(long time);
}
