package com.masterwok.simplevlcplayer.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.masterwok.simplevlcplayer.observables.RendererItemObservable;

import dagger.android.DaggerService;

public class MediaPlayerService extends Service {

    private final MediaPlayerServiceBinder mediaPlayerServiceBinder
            = new MediaPlayerServiceBinder();



    public class MediaPlayerServiceBinder extends Binder {
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mediaPlayerServiceBinder;
    }

}
