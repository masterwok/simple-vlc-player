package com.masterwok.simplevlcplayer.contracts;

import android.view.SurfaceView;

public interface SurfaceMediaPlayer
        extends MediaPlayer {

    void attachSurfaces(
            SurfaceView surfaceMedia,
            SurfaceView surfaceSubtitle
    );

    void detachSurfaces();
}
