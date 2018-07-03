package com.masterwok.simplevlcplayer.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.KeyEvent;

import com.masterwok.simplevlcplayer.R;
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

        mediaSession.setActive(true);

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

        mediaSession.setActive(false);

        stopForeground(true);

        if (callback != null) {
            callback.onPlayerStopped();
        }
    }

    @Override
    public void onPlayerEndReached() {
        updatePlaybackState();

        mediaSession.setActive(false);

        if (callback != null) {
            callback.onPlayerEndReached();
        }
    }

    @Override
    public void onPlayerError() {
        updatePlaybackState();

        mediaSession.setActive(false);

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
        return buildPlaybackNotification(
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

        updateNotification(
                getApplicationContext(),
                MediaPlayerServiceNotificationId,
                buildNotification(
                        player.getMedia(),
                        mediaBitmap
                )
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
        mediaSession.setMediaButtonReceiver(null);
        mediaSession.setCallback(new PlayerSessionCallback());
        mediaSession.setPlaybackState(stateBuilder.build());
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                        | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Pass notification button intents to the media session callback.
        MediaButtonReceiver.handleIntent(mediaSession, intent);

        return super.onStartCommand(intent, flags, startId);
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

        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            final String action = mediaButtonEvent.getAction();

            if (action == null
                    || !action.equals(Intent.ACTION_MEDIA_BUTTON)) {
                return false;
            }

            KeyEvent keyEvent = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    player.play();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    player.pause();
                    break;
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    player.stop();
                    break;
            }

            return true;
        }
    }


    private static NotificationCompat.Action getPauseAction(Context context) {
        return new NotificationCompat.Action(
                R.drawable.ic_pause_black_36dp,
                "Pause",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,
                        PlaybackStateCompat.ACTION_PAUSE)
        );
    }

    private static NotificationCompat.Action getPlayAction(Context context) {
        return new NotificationCompat.Action(
                R.drawable.ic_play_arrow_black_36dp,
                "Play",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,
                        PlaybackStateCompat.ACTION_PLAY)
        );
    }

    private static NotificationCompat.Action getStopAction(Context context) {
        return new NotificationCompat.Action(
                R.drawable.ic_clear_black_36dp,
                "Stop",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,
                        PlaybackStateCompat.ACTION_STOP)
        );
    }

    public Notification buildPlaybackNotification(
            Context context,
            MediaSessionCompat.Token token,
            String channelId,
            String title,
            String description,
            Bitmap cover,
            boolean isPlaying
    ) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context,
                channelId
        );

        builder.setSmallIcon(R.drawable.ic_play_arrow_black_36dp)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(title)
                .setContentText(description)
                .setLargeIcon(cover)
                .setTicker(title)
                .setAutoCancel(false)
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_SERVICE);

        if (isPlaying) {
            builder.addAction(getPauseAction(context));
        } else {
            builder.addAction(getPlayAction(context));
        }

        builder.addAction(getStopAction(context));

        builder.setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(token)
                .setShowActionsInCompactView(0, 1)
        );

        return builder.build();
    }

    public void updateNotification(
            Context context,
            int notificationId,
            Notification notification
    ) {
        if (player.getSelectedRendererItem() == null) {
            return;
        }

        final NotificationManager notificationManager = NotificationUtil.getNotificationManager(context);

        notificationManager.notify(
                notificationId,
                notification
        );
    }


}
