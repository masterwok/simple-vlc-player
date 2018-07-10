package com.masterwok.simplevlcplayer.services.binders;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.SurfaceView;

import com.masterwok.simplevlcplayer.contracts.MediaPlayer;
import com.masterwok.simplevlcplayer.contracts.VlcMediaPlayer;
import com.masterwok.simplevlcplayer.fragments.LocalPlayerFragment;
import com.masterwok.simplevlcplayer.observables.RendererItemObservable;
import com.masterwok.simplevlcplayer.services.MediaPlayerService;
import com.masterwok.simplevlcplayer.utils.AudioUtil;
import com.masterwok.simplevlcplayer.utils.FileUtil;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.RendererItem;

import java.io.FileDescriptor;
import java.lang.ref.WeakReference;


/**
 * This is the binder for the media player service. It was created as a separate
 * class to avoid an implicit reference to the service. The service is referenced
 * through a weak reference to prevent memory leaks.
 */
@SuppressWarnings("WeakerAccess")
public final class MediaPlayerServiceBinder extends android.os.Binder {

    private final WeakReference<MediaPlayerService> serviceWeakReference;
    private final WeakReference<AudioManager.OnAudioFocusChangeListener> audioFocusChangeListener;

    private final AudioManager audioManager;

    public MediaPlayerServiceBinder(MediaPlayerService service) {
        serviceWeakReference = new WeakReference<>(service);
        audioFocusChangeListener = new WeakReference<>(createAudioFocusListener());

        audioManager = AudioUtil.getAudioManager(service.getApplicationContext());
    }

    private AudioManager.OnAudioFocusChangeListener createAudioFocusListener() {
        return focusChange -> {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    setVolume(100);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    // Lower volume, continue playing.
                    setVolume(50);
                    break;
            }
        };
    }

    public RendererItemObservable getRendererItemObservable() {
        return serviceWeakReference
                .get()
                .rendererItemObservable;
    }

    public void setSelectedRendererItem(RendererItem rendererItem) {
        final VlcMediaPlayer player = getPlayer();

        // No need for local audio focus, abandon it.
        if (rendererItem != null) {
            abandonAudioFocus();
        }

        player.detachSurfaces();
        player.setRendererItem(rendererItem);

        serviceWeakReference
                .get()
                .sendRendererSelectedBroadcast(rendererItem);
    }

    public IVLCVout getVout() {
        return getPlayer().getVout();
    }

    public void setMedia(Context context, Uri mediaUri) {
        if (context == null || mediaUri == null) {
            return;
        }

        final VlcMediaPlayer player = getPlayer();
        final String schema = mediaUri.getScheme();

        // Use file descriptor when dealing with content schemas.
        if (schema != null && schema.equals(ContentResolver.SCHEME_CONTENT)) {
            player.setMedia(FileUtil.getUriFileDescriptor(
                    context.getApplicationContext(),
                    mediaUri,
                    "r"
            ));

            return;
        }

        player.setMedia(mediaUri);
    }

    public void setMedia(FileDescriptor fileDescriptor) {
        if (fileDescriptor == null) {
            return;
        }

        getPlayer().setMedia(fileDescriptor);
    }

    public void setSubtitle(Uri subtitleUri) {
        if (subtitleUri == null) {
            return;
        }

        getPlayer().setSubtitle(subtitleUri);
    }

    public void play() {
        gainAudioFocus();

        getPlayer().play();
    }

    public void stop() {
        abandonAudioFocus();

        getPlayer().stop();
    }

    private void gainAudioFocus() {
        // Only gain audio focus when playing locally.
        if (getPlayer().getSelectedRendererItem() != null) {
            return;
        }

        AudioUtil.requestAudioFocus(
                audioManager,
                audioFocusChangeListener.get()
        );
    }

    private void abandonAudioFocus() {
        audioManager.abandonAudioFocus(audioFocusChangeListener.get());
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
        if (getPlayer().isPlaying()) {
            pause();
            return;
        }

        play();
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

    public boolean isPlaying() {
        return getPlayer()
                .isPlaying();
    }

    public void setVolume(int volume) {
        getPlayer().setVolume(volume);
    }
}
