package com.masterwok.simplevlcplayer.services;

import android.net.Uri;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.SurfaceView;

import com.masterwok.simplevlcplayer.contracts.MediaPlayer;
import com.masterwok.simplevlcplayer.contracts.VlcMediaPlayer;
import com.masterwok.simplevlcplayer.fragments.LocalPlayerFragment;
import com.masterwok.simplevlcplayer.observables.RendererItemObservable;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.RendererItem;

import java.lang.ref.WeakReference;

public final class MediaPlayerServiceBinder extends android.os.Binder {

    private final WeakReference<MediaPlayerService> serviceWeakReference;

    public MediaPlayerServiceBinder(MediaPlayerService service) {
        serviceWeakReference = new WeakReference<>(service);
    }

    public RendererItemObservable getRendererItemObservable() {
        return serviceWeakReference
                .get()
                .rendererItemObservable;
    }

    public void setSelectedRendererItem(RendererItem rendererItem) {
        getPlayer().setRendererItem(rendererItem);

        serviceWeakReference
                .get()
                .sendRendererSelectedBroadcast(rendererItem);
    }

    public IVLCVout getVout() {
        return getPlayer().getVout();
    }

    public void setMedia(Uri mediaUri) {
        getPlayer().setMedia(mediaUri);
    }

    public void play() {
        getPlayer().play();
    }

    public void stop() {
        getPlayer().stop();
    }

    public void setCallback(MediaPlayer.Callback callback) {
        serviceWeakReference
                .get()
                .callback = callback;
    }

    public MediaSessionCompat getMediaSession() {
        return serviceWeakReference
                .get()
                .mediaSession;
    }

    public void setTime(long time) {
        getPlayer().setTime(time);
    }

    private VlcMediaPlayer getPlayer() {
        return serviceWeakReference
                .get()
                .player;
    }

    public void setProgress(int progress) {
        final VlcMediaPlayer player = getPlayer();

        player.setTime((long) ((float) progress / 100 * player.getLength()));
    }

    public void togglePlayback() {
        final VlcMediaPlayer player = getPlayer();

        if (player.isPlaying()) {
            player.pause();
            return;
        }

        player.play();
    }

    public void pause() {
        getPlayer().pause();
    }

    public void setAspectRatio(String aspectRatio) {
        getPlayer().setAspectRatio(aspectRatio);
    }

    public void setScale(float scale) {
        getPlayer().setScale(scale);
    }

    public Media.VideoTrack getCurrentVideoTrack() {
        return getPlayer().getCurrentVideoTrack();
    }

    public void attachSurfaces(
            SurfaceView surfaceMedia,
            SurfaceView surfaceSubtitle,
            LocalPlayerFragment localPlayerFragment
    ) {
        getPlayer().attachSurfaces(
                surfaceMedia,
                surfaceSubtitle,
                localPlayerFragment
        );
    }

    public void detachSurfaces() {
        getPlayer().detachSurfaces();
    }

    public RendererItem getSelectedRendererItem() {
        return getPlayer().getSelectedRendererItem();
    }
}
