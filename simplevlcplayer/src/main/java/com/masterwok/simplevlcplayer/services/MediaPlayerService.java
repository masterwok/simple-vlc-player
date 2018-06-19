package com.masterwok.simplevlcplayer.services;

import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.masterwok.simplevlcplayer.dagger.injectors.InjectableService;
import com.masterwok.simplevlcplayer.observables.RendererItemObservable;

import org.videolan.libvlc.LibVLC;

import javax.inject.Inject;

public class MediaPlayerService extends InjectableService {

    @Inject
    public LibVLC libVlc;

    private final Binder binder = new Binder();

    private RendererItemObservable rendererItemObservable;


    public class Binder extends android.os.Binder {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        rendererItemObservable = new RendererItemObservable(libVlc);
        rendererItemObservable.start();
    }

    @Override
    public void onDestroy() {
        rendererItemObservable.stop();
        rendererItemObservable = null;

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

}
