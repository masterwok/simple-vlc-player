package com.masterwok.simplevlcplayer.services;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.masterwok.simplevlcplayer.dagger.injectors.InjectableService;

import org.videolan.libvlc.LibVLC;

import javax.inject.Inject;

public class MediaPlayerService extends InjectableService {

    @Inject
    public LibVLC libVlc;


    private final MediaPlayerServiceBinder mediaPlayerServiceBinder
            = new MediaPlayerServiceBinder();


    public class MediaPlayerServiceBinder extends Binder {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mediaPlayerServiceBinder;
    }

}
