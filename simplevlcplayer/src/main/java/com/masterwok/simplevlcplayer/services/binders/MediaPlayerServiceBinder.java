package com.masterwok.simplevlcplayer.services.binders;

import android.content.Context;
import android.net.Uri;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.SurfaceView;

import com.masterwok.simplevlcplayer.contracts.MediaPlayer;
import com.masterwok.simplevlcplayer.observables.RendererItemObservable;
import com.masterwok.simplevlcplayer.services.MediaPlayerService;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.RendererItem;

import java.lang.ref.WeakReference;


/**
 * This is the binder for the media player service. It was created as a separate
 * class to avoid an implicit reference to the service. The service is referenced
 * through a weak reference to prevent memory leaks.
 */
@SuppressWarnings("WeakerAccess")
public final class MediaPlayerServiceBinder extends android.os.Binder {

    private final WeakReference<MediaPlayerService> serviceWeakReference;

    public MediaPlayerServiceBinder(MediaPlayerService service) {
        serviceWeakReference = new WeakReference<>(service);
    }

    public MediaPlayerService getMediaPlayerService() {
        return serviceWeakReference.get();
    }

    public RendererItemObservable getRendererItemObservable() {
        return getMediaPlayerService().rendererItemObservable;
    }


    public void setSelectedRendererItem(RendererItem rendererItem) {
        getMediaPlayerService().setSelectedRendererItem(rendererItem);
    }

    public IVLCVout getVout() {
        return getMediaPlayerService()
                .getVout();
    }

    public void setMedia(Context context, Uri mediaUri) {
        getMediaPlayerService().setMedia(
                context,
                mediaUri
        );
    }

    public void setSubtitle(Uri subtitleUri) {
        getMediaPlayerService().setSubtitle(subtitleUri);
    }

    public void play() {
        getMediaPlayerService().play();
    }

    public void stop() {
        getMediaPlayerService().stop();
    }

    public void setCallback(MediaPlayer.Callback callback) {
        getMediaPlayerService().callback = callback;
    }

    public MediaSessionCompat getMediaSession() {
        return getMediaPlayerService().mediaSession;
    }

    public void setTime(long time) {
        getMediaPlayerService().setTime(time);
    }

    public void setProgress(int progress) {
        getMediaPlayerService().setProgress(progress);
    }

    public void togglePlayback() {
        getMediaPlayerService().togglePlayback();
    }

    public void pause() {
        getMediaPlayerService().pause();
    }

    public void setAspectRatio(String aspectRatio) {
        getMediaPlayerService().setAspectRatio(aspectRatio);
    }

    public void setScale(float scale) {
        getMediaPlayerService().setScale(scale);
    }

    public Media.VideoTrack getCurrentVideoTrack() {
        return getMediaPlayerService().getCurrentVideoTrack();
    }

    public void attachSurfaces(
            SurfaceView surfaceMedia,
            SurfaceView surfaceSubtitle,
            IVLCVout.OnNewVideoLayoutListener listener
    ) {
        getMediaPlayerService().attachSurfaces(
                surfaceMedia,
                surfaceSubtitle,
                listener
        );
    }

    public void detachSurfaces() {
        getMediaPlayerService().detachSurfaces();
    }

    public RendererItem getSelectedRendererItem() {
        return getMediaPlayerService().getSelectedRendererItem();
    }

    public boolean isPlaying() {
        return getMediaPlayerService().isPlaying();
    }

    public void setVolume(int volume) {
        getMediaPlayerService().setVolume(volume);
    }
}
