package com.masterwok.simplevlcplayer.services;

import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.SurfaceView;

import com.masterwok.simplevlcplayer.contracts.MediaPlayer;
import com.masterwok.simplevlcplayer.contracts.VlcMediaPlayer;
import com.masterwok.simplevlcplayer.dagger.injectors.InjectableService;
import com.masterwok.simplevlcplayer.observables.RendererItemObservable;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.RendererItem;

import javax.inject.Inject;

public class MediaPlayerService
        extends InjectableService
        implements MediaPlayer.Callback {

    public static final String RendererClearedAction = "action.rendererclearedaction";
    public static final String RendererSelectionAction = "action.rendererselectionaction";

    private static final String SimpleVlcSessionTag = "tag.simplevlcsession";

    @Inject
    public LibVLC libVlc;

    @Inject
    public VlcMediaPlayer player;

    private final Binder binder = new Binder();

    private RendererItemObservable rendererItemObservable;

    private RendererItem rendererItem;

    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder stateBuilder;

    private MediaPlayer.Callback callback;

    @Override
    public void onPlayerOpening() {
        updatePlaybackState();

        if (callback != null) {
            callback.onPlayerOpening();
        }
    }

    @Override
    public void onPlayerSeekStateChange(boolean canSeek) {
        updatePlaybackState();

        if (callback != null) {
            callback.onPlayerSeekStateChange(canSeek);
        }
    }

    @Override
    public void onPlayerPlaying() {
        updatePlaybackState();

        if (callback != null) {
            callback.onPlayerPlaying();
        }
    }

    @Override
    public void onPlayerPaused() {
        updatePlaybackState();

        if (callback != null) {
            callback.onPlayerPaused();
        }
    }

    @Override
    public void onPlayerStopped() {
        updatePlaybackState();

        if (callback != null) {
            callback.onPlayerStopped();
        }
    }

    @Override
    public void onPlayerEndReached() {
        updatePlaybackState();

        if (callback != null) {
            callback.onPlayerEndReached();
        }
    }

    @Override
    public void onPlayerError() {
        updatePlaybackState();

        if (callback != null) {
            callback.onPlayerError();
        }
    }

    @Override
    public void onPlayerTimeChange(long timeChanged) {
        updatePlaybackState();

        if (callback != null) {
            callback.onPlayerTimeChange(timeChanged);
        }
    }

    @Override
    public void onPlayerPositionChange(float positionChanged) {
        updatePlaybackState();

        if (callback != null) {
            callback.onPlayerPositionChange(positionChanged);
        }
    }

    @Override
    public void onBuffering(float buffering) {
        updatePlaybackState();

        if (callback != null) {
            callback.onBuffering(buffering);
        }
    }

    public void updatePlaybackState() {
        stateBuilder.setBufferedPosition(player.getLength());
        stateBuilder.setState(
                player.isPlaying()
                        ? PlaybackStateCompat.STATE_PLAYING
                        : PlaybackStateCompat.STATE_PAUSED,
                player.getTime(),
                1
        );

        mediaSession.setPlaybackState(stateBuilder.build());
    }

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

        public void onSurfaceChanged(int width, int height) {
            player.onSurfaceChanged(width, height);
        }

        public void attachSurfaceViews(
                SurfaceView surfaceMedia,
                SurfaceView surfaceSubtitle
        ) {
            player.attachSurfaces(
                    surfaceMedia,
                    surfaceSubtitle
            );
        }

        public void detachSurfaceViews() {
            player.detachSurfaces();
        }

        public void setMedia(Uri mediaUri) {
            player.setMedia(mediaUri);
        }

        public void play() {
            player.play();
        }

        public void stop() {
            player.stop();
        }

        public void setCallback(MediaPlayer.Callback callback) {
            MediaPlayerService.this.callback = callback;
        }

        public MediaSessionCompat getMediaSession() {
            return mediaSession;
        }

        public void setTime(long time) {
            player.setTime(time);
        }

        public void setProgress(int progress) {
            player.setTime((long) ((float) progress / 100 * player.getLength()));
        }

        public void togglePlayback() {
            if (player.isPlaying()) {
                player.pause();
                return;
            }

            player.play();
        }

        public void pause() {
            player.pause();
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

        stateBuilder = new PlaybackStateCompat
                .Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SEEK_TO)
                .setState(PlaybackStateCompat.STATE_PAUSED, 0L, 1);

        createMediaSession();

        player.setCallback(this);

        rendererItemObservable = new RendererItemObservable(libVlc);
        rendererItemObservable.start();
    }

    private void createMediaSession() {
        mediaSession = new MediaSessionCompat(this, SimpleVlcSessionTag);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setMediaButtonReceiver(null);
        mediaSession.setCallback(new PlayerSessionCallback());
        mediaSession.setPlaybackState(stateBuilder.build());
    }

    private class PlayerSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            player.play();
        }

        @Override
        public void onPause() {
            player.pause();
        }
    }


    @Override
    public void onDestroy() {
        mediaSession.release();
        rendererItemObservable.stop();

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

}
