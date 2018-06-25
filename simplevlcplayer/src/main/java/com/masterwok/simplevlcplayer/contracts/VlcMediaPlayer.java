package com.masterwok.simplevlcplayer.contracts;

import android.view.SurfaceView;

import org.videolan.libvlc.RendererItem;

public interface VlcMediaPlayer
        extends MediaPlayer {

    void setRendererItem(RendererItem rendererItem);

    void onSurfaceChanged(int width, int height);

    void attachSurfaces(
            SurfaceView surfaceMedia,
            SurfaceView surfaceSubtitle
    );

    void detachSurfaces();
}
