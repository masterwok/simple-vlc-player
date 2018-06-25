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

public class MediaPlayerService extends InjectableService implements MediaPlayer.Callback {

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

    @Override
    public void onPlayerOpening() {
        updatePlaybackState();
    }

    @Override
    public void onPlayerSeekStateChange(boolean canSeek) {
        updatePlaybackState();
    }

    @Override
    public void onPlayerPlaying() {
        updatePlaybackState();
    }

    @Override
    public void onPlayerPaused() {
        updatePlaybackState();
    }

    @Override
    public void onPlayerStopped() {
        updatePlaybackState();
    }

    @Override
    public void onPlayerEndReached() {
        updatePlaybackState();
    }

    @Override
    public void onPlayerError() {
        updatePlaybackState();
    }

    @Override
    public void onPlayerTimeChange(long timeChanged) {
        updatePlaybackState();
    }

    @Override
    public void onPlayerPositionChange(float positionChanged) {
        updatePlaybackState();
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

        public void pause() {
            player.pause();
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

        player.setCallback(this);

        createMediaSession();

        rendererItemObservable = new RendererItemObservable(libVlc);
        rendererItemObservable.start();
    }

    private void createMediaSession() {
        mediaSession = new MediaSessionCompat(getApplicationContext(), SimpleVlcSessionTag);
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
