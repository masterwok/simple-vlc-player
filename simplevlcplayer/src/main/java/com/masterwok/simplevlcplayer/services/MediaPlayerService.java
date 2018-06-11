package com.masterwok.simplevlcplayer.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaControllerCompat;

import com.masterwok.simplevlcplayer.sessions.VlcMediaPlayerSession;

public class MediaPlayerService extends Service {

    private static final String vlcMediaPlayerSessionTag = "tag.vlcmediaplayersession";

    private final MediaPlayerServiceBinder mediaPlayerServiceBinder
            = new MediaPlayerServiceBinder();

    private MediaControllerCompat mediaController;
    private VlcMediaPlayerSession vlcMediaPlayerSession;

    public class MediaPlayerServiceBinder extends Binder {
        public VlcMediaPlayerSession getMediaPlayerSession() {
            return vlcMediaPlayerSession;
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
