package com.masterwok.simplevlcplayer.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.masterwok.simplevlcplayer.R;
import com.masterwok.simplevlcplayer.contracts.MediaPlayer;
import com.masterwok.simplevlcplayer.contracts.VlcMediaPlayer;
import com.masterwok.simplevlcplayer.dagger.injectors.InjectableService;
import com.masterwok.simplevlcplayer.observables.RendererItemObservable;
import com.masterwok.simplevlcplayer.services.binders.MediaPlayerServiceBinder;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.RendererItem;

import javax.inject.Inject;

public final class MediaPlayerService
        extends InjectableService
        implements MediaPlayer.Callback {

    public static final String RendererClearedAction = "action.rendererclearedaction";
    public static final String RendererSelectionAction = "action.rendererselectionaction";

    private static final String MediaPlayerServiceName = "Media Player Service";
    private static final String MediaPlayerServiceChannelId = "channel.mediaplayerservice";
    private static final String SimpleVlcSessionTag = "tag.simplevlcsession";

    @Inject
    public LibVLC libVlc;

    @Inject
    public VlcMediaPlayer player;

    private MediaPlayerServiceBinder binder;

    private PlaybackStateCompat.Builder stateBuilder;

    public RendererItemObservable rendererItemObservable;
    public MediaSessionCompat mediaSession;
    public MediaPlayer.Callback callback;

    @Override
    public void onCreate() {
        super.onCreate();

        binder = new MediaPlayerServiceBinder(this);

        stateBuilder = new PlaybackStateCompat
                .Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SEEK_TO)
                .setState(PlaybackStateCompat.STATE_PAUSED, 0L, 1);

        createMediaSession();

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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        NotificationChannel channel = new NotificationChannel(
                MediaPlayerServiceChannelId,
                MediaPlayerServiceName,
                NotificationManager.IMPORTANCE_DEFAULT
        );

        channel.setSound(null, null);
        channel.enableLights(false);
        channel.enableVibration(false);

        //noinspection ConstantConditions
        notificationManager.createNotificationChannel(channel);

        startForeground(1, buildNotification());
    }

    private Notification buildNotification() {
        final Media media = player.getMedia();

        return new NotificationCompat.Builder(this, MediaPlayerServiceChannelId)
                .setSmallIcon(R.drawable.ic_cast_connected_white_24dp)
                .setContentTitle("Example Title")
                .setContentText(media.getUri().getPath())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
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
