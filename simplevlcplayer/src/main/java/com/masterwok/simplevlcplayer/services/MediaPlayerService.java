package com.masterwok.simplevlcplayer.services;

import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.masterwok.simplevlcplayer.contracts.RendererItemMediaPlayer;
import com.masterwok.simplevlcplayer.dagger.injectors.InjectableService;
import com.masterwok.simplevlcplayer.observables.RendererItemObservable;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.RendererItem;

import javax.inject.Inject;

public class MediaPlayerService extends InjectableService {

    public static final String RendererClearedAction = "action.rendererclearedaction";
    public static final String RendererSelectionAction = "action.rendererselectionaction";

    @Inject
    public LibVLC libVlc;

    private final Binder binder = new Binder();

    private RendererItemObservable rendererItemObservable;

    private RendererItem rendererItem;
    private RendererItemMediaPlayer player;


    public class Binder extends android.os.Binder {

        public RendererItemObservable getRendererItemObservable() {
            return rendererItemObservable;
        }

        public void setSelectedRendererItem(RendererItem rendererItem) {
            MediaPlayerService.this.rendererItem = rendererItem;
            sendRendererSelectedBroadcast(rendererItem);
        }

        public RendererItem getSelectedRendererItem() {
            return rendererItem;
        }

        public void setRendererMediaPlayer(RendererItemMediaPlayer player) {
            MediaPlayerService.this.player = player;
        }
    }

    private void sendRendererSelectedBroadcast(RendererItem rendererItem) {
        Intent intent = rendererItem == null
                ? new Intent(RendererClearedAction)
                : new Intent(RendererSelectionAction);

        LocalBroadcastManager
                .getInstance(getApplicationContext())
                .sendBroadcast(intent);
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
