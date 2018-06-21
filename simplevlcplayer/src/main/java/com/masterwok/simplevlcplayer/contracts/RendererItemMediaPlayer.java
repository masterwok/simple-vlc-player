package com.masterwok.simplevlcplayer.contracts;


import org.videolan.libvlc.RendererItem;

public interface RendererItemMediaPlayer
        extends MediaPlayer {

    void setRendererItem(RendererItem rendererItem);
}
