package com.masterwok.simplevlcplayer.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaControllerCompat;
import android.view.SurfaceView;

import com.masterwok.simplevlcplayer.callbacks.RendererItemListener;
import com.masterwok.simplevlcplayer.sessions.VlcMediaPlayerSession;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.RendererItem;

public class MediaPlayerService extends Service {

    private static final String vlcMediaPlayerSessionTag = "tag.vlcmediaplayersession";

    private final MediaPlayerServiceBinder mediaPlayerServiceBinder
            = new MediaPlayerServiceBinder();

    private MediaControllerCompat mediaController;
    private VlcMediaPlayerSession vlcMediaPlayerSession;

    public class MediaPlayerServiceBinder extends Binder {

        public RendererItem getSelectedRendererItem() {
            return vlcMediaPlayerSession.getSelectedRendererItem();
        }

        /**
         * Get an observable of renderer items.
         *
         * @return A list of renderer items.
         */
        public RendererItemListener getRenderItemObservable() {
            return vlcMediaPlayerSession.getRenderItemObservable();
        }

        /**
         * Set renderer to VLC render item. Invoke this method to cast media.
         *
         * @param renderItem The render item to play on.
         */
        public void setRenderer(RendererItem renderItem) {

            // TODO: Need to become foreground service..

            vlcMediaPlayerSession.setRenderer(renderItem);
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
            // TODO: If foreground service, stop being in foreground..

            vlcMediaPlayerSession.setRenderer(
                    mediaSurfaceView,
                    subtitleSurfaceView,
                    layoutListener
            );
        }

        public MediaControllerCompat getMediaController() {
            return mediaController;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        vlcMediaPlayerSession = new VlcMediaPlayerSession(
                getApplicationContext(),
                vlcMediaPlayerSessionTag
        );

        mediaController = new MediaControllerCompat(
                getApplicationContext(),
                vlcMediaPlayerSession
        );
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mediaPlayerServiceBinder;
    }

}
