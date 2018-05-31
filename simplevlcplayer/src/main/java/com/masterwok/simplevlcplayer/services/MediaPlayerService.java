package com.masterwok.simplevlcplayer.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.SurfaceView;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.RendererItem;

public class MediaPlayerService extends Service {

    private final MediaPlayerServiceBinder mediaPlayerServiceBinder
            = new MediaPlayerServiceBinder();

    public class MediaPlayerServiceBinder extends Binder {

        /**
         * Set renderer to VLC render item. Invoke this method to cast media.
         *
         * @param renderItem The render item to play on.
         */
        public void setRenderer(RendererItem renderItem) {
            // TODO: Set player to bind to render item.
        }

        /**
         * Play media locally using the provided surface views.
         *
         * @param mediaSurfaceView    The SurfaceView to render the media in.
         * @param subtitleSurfaceView The SurfaceView to render the subtitles in.
         * @param layoutListener      A layout listener for responding to layout changes.
         */
        public void setRenderer(
                SurfaceView mediaSurfaceView,
                SurfaceView subtitleSurfaceView,
                IVLCVout.OnNewVideoLayoutListener layoutListener
        ) {
            // TODO: Set player to play locally.
        }
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mediaPlayerServiceBinder;
    }

}
