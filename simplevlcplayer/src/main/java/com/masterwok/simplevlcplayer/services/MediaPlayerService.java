package com.masterwok.simplevlcplayer.services;

import android.app.Notification;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.masterwok.simplevlcplayer.contracts.MediaPlayer;
import com.masterwok.simplevlcplayer.contracts.VlcMediaPlayer;
import com.masterwok.simplevlcplayer.dagger.injectors.InjectableService;
import com.masterwok.simplevlcplayer.observables.RendererItemObservable;
import com.masterwok.simplevlcplayer.services.binders.MediaPlayerServiceBinder;
import com.masterwok.simplevlcplayer.utils.NotificationUtil;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.RendererItem;

import javax.inject.Inject;

public final class MediaPlayerService
        extends InjectableService
        implements MediaPlayer.Callback {

    public static final String RendererClearedAction = "action.rendererclearedaction";
    public static final String RendererSelectionAction = "action.rendererselectionaction";

    private static final String MediaPlayerServiceChannelName = "Media Player Service";
    private static final String MediaPlayerServiceChannelId = "channel.mediaplayerservice";
    private static final String SimpleVlcSessionTag = "tag.simplevlcsession";
    private static final int MediaPlayerServiceNotificationId = 32106;

    @Inject
    public LibVLC libVlc;

    @Inject
    public VlcMediaPlayer player;

    private MediaPlayerServiceBinder binder;

    private PlaybackStateCompat.Builder stateBuilder;

    public RendererItemObservable rendererItemObservable;
    public MediaSessionCompat mediaSession;
    public MediaPlayer.Callback callback;

    private Bitmap mediaBitmap;

    @Override
    public void onCreate() {
        super.onCreate();

        binder = new MediaPlayerServiceBinder(this);

        stateBuilder = new PlaybackStateCompat
                .Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SEEK_TO)
                .setState(PlaybackStateCompat.STATE_PAUSED, 0L, 1);

        createMediaSession();
        createNotificationChannel();

        player.setCallback(this);

        rendererItemObservable = new RendererItemObservable(libVlc);
        rendererItemObservable.start();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        player.stop();
        player.release();
        libVlc.release();
        mediaSession.release();
        rendererItemObservable.stop();
        binder = null;
        player = null;
        libVlc = null;
        mediaSession = null;
        rendererItemObservable = null;

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

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

        if (player.getSelectedRendererItem() != null) {
            enterForeground();
        }

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

        stopForeground(true);

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

    private void enterForeground() {
        final Media media = player.getMedia();

        mediaBitmap = ThumbnailUtils.createVideoThumbnail(
                media.getUri().getPath(),
                MediaStore.Images.Thumbnails.MINI_KIND
        );

        mediaSession.setMetadata(getMediaMetadata(mediaBitmap));

        startForeground(
                MediaPlayerServiceNotificationId,
                buildNotification(
                        media,
                        mediaBitmap
                )
        );
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationUtil.createNotificationChannel(
                getApplicationContext(),
                MediaPlayerServiceChannelId,
                MediaPlayerServiceChannelName,
                false,
                false,
                false
        );
    }

    private Notification buildNotification(
            Media media,
            Bitmap bitmap
    ) {
        return NotificationUtil.buildPlaybackNotification(
                getApplicationContext(),
                mediaSession.getSessionToken(),
                MediaPlayerServiceChannelId,
                media.getUri().getLastPathSegment(),
                null,
                bitmap,
                player.isPlaying()
        );
    }

    private MediaMetadataCompat getMediaMetadata(Bitmap bitmap) {
        final MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();

        builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap);

        return builder.build();
    }

    private void updatePlaybackState() {
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

    public void sendRendererSelectedBroadcast(RendererItem rendererItem) {
        Intent intent = rendererItem == null
                ? new Intent(RendererClearedAction)
                : new Intent(RendererSelectionAction);

        LocalBroadcastManager
                .getInstance(getApplicationContext())
                .sendBroadcast(intent);
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

}
