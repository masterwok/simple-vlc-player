package com.masterwok.simplevlcplayer.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.widget.Toast;

import com.masterwok.simplevlcplayer.R;
import com.masterwok.simplevlcplayer.contracts.MediaPlayer;
import com.masterwok.simplevlcplayer.contracts.VlcMediaPlayer;
import com.masterwok.simplevlcplayer.dagger.injectors.InjectableService;
import com.masterwok.simplevlcplayer.observables.RendererItemObservable;
import com.masterwok.simplevlcplayer.services.binders.MediaPlayerServiceBinder;
import com.masterwok.simplevlcplayer.utils.AudioUtil;
import com.masterwok.simplevlcplayer.utils.BitmapUtil;
import com.masterwok.simplevlcplayer.utils.FileUtil;
import com.masterwok.simplevlcplayer.utils.NotificationUtil;
import com.masterwok.simplevlcplayer.utils.ResourceUtil;

import org.videolan.libvlc.Dialog;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.RendererItem;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

public final class MediaPlayerService
        extends InjectableService
        implements MediaPlayer.Callback
        , Dialog.Callbacks {

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

    public RendererItemObservable rendererItemObservable;
    public MediaSessionCompat mediaSession;
    public MediaPlayer.Callback callback;

    private WeakReference<AudioManager.OnAudioFocusChangeListener> audioFocusChangeListener;
    private AudioManager audioManager;
    private NotificationManager notificationManager;
    private PlaybackStateCompat.Builder stateBuilder;
    private MediaPlayerServiceBinder binder;
    private Bitmap mediaBitmap;
    private Bitmap defaultBitmap;

    private long lastUpdateTime = 0L;

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


    @Override
    public void onCreate() {
        super.onCreate();

        audioFocusChangeListener = new WeakReference<>(createAudioFocusListener());
        audioManager = AudioUtil.getAudioManager(getApplicationContext());

        Dialog.setCallbacks(libVlc, this);

        defaultBitmap = BitmapUtil.drawableToBitmap(ResourceUtil.getDrawable(
                getApplicationContext(),
                R.drawable.ic_stream_cover
        ));

        binder = new MediaPlayerServiceBinder(this);

        notificationManager = NotificationUtil.getNotificationManager(
                getApplicationContext()
        );

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
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Pass notification button intents to the media session callback.
        MediaButtonReceiver.handleIntent(mediaSession, intent);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        Dialog.setCallbacks(libVlc, null);
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
        lastUpdateTime = 0L;

        updatePlaybackState();
        updateNotification();

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
        updateNotification();

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
        updateNotification();

        if (callback != null) {
            callback.onPlayerPaused();
        }
    }

    @Override
    public void onPlayerStopped() {
        updatePlaybackState();
        updateNotification();

        mediaSession.setActive(false);

        stopForeground(true);

        if (callback != null) {
            callback.onPlayerStopped();
        }
    }

    @Override
    public void onPlayerEndReached() {
        updatePlaybackState();
        updateNotification();

        mediaSession.setActive(false);

        if (callback != null) {
            callback.onPlayerEndReached();
        }
    }

    @Override
    public void onPlayerError() {
        updatePlaybackState();
        updateNotification();

        mediaSession.setActive(false);

        if (callback != null) {
            callback.onPlayerError();
        }
    }

    @Override
    public void onPlayerTimeChange(long timeChanged) {
        final long time = player.getTime() / 1000L;

        // At least one second has elapsed, update playback state.
        if (time >= lastUpdateTime + 1) {
            updatePlaybackState();
            lastUpdateTime = time;
        }

        if (callback != null) {
            callback.onPlayerTimeChange(timeChanged);
        }
    }

    @Override
    public void onBuffering(float buffering) {
        updatePlaybackState();

        if (callback != null) {
            callback.onBuffering(buffering);
        }
    }

    @Override
    public void onPlayerPositionChanged(float positionChanged) {
        if (callback != null) {
            callback.onPlayerPositionChanged(positionChanged);
        }
    }

    @Override
    public void onDisplay(Dialog.ErrorMessage errorMessage) {

    }

    @Override
    public void onDisplay(Dialog.LoginDialog loginDialog) {

    }

    @Override
    public void onDisplay(Dialog.QuestionDialog questionDialog) {
        final String dialogTitle = questionDialog.getTitle();

        if (dialogTitle.equals("Insecure site")) {
            if (questionDialog.getAction1Text().equals("View certificate")) {
                questionDialog.postAction(1);
            } else if (questionDialog.getAction2Text().equals("Accept permanently")) {
                questionDialog.postAction(2);
            }

            questionDialog.dismiss();
            return;
        }

        // Ignore non-performance warning dialogs.
        if (!questionDialog.getTitle().equals("Performance warning")) {
            return;
        }

        // Let the user know casting will eat their battery.
        Toast.makeText(
                getApplicationContext(),
                R.string.toast_casting_performance_warning,
                Toast.LENGTH_LONG
        ).show();

        // Accept and dismiss performance warning dialog.
        questionDialog.postAction(1);
        questionDialog.dismiss();
    }

    @Override
    public void onDisplay(Dialog.ProgressDialog progressDialog) {

    }

    @Override
    public void onCanceled(Dialog dialog) {

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

    private void enterForeground() {
        mediaSession.setMetadata(
                getMediaMetadata(mediaBitmap)
        );

        startForeground(
                MediaPlayerServiceNotificationId,
                buildPlaybackNotification()
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

    private void updateNotification() {
        if (player.getSelectedRendererItem() == null) {
            return;
        }

        notificationManager.notify(
                MediaPlayerService.MediaPlayerServiceNotificationId,
                buildPlaybackNotification()
        );

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
    public void onProgressUpdate(Dialog.ProgressDialog progressDialog) {

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

    private Notification buildPlaybackNotification() {
        final Context context = getApplicationContext();

        final String title = player
                .getMedia()
                .getUri()
                .getLastPathSegment();

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context,
                MediaPlayerService.MediaPlayerServiceChannelId
        );

        builder.setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setSmallIcon(R.drawable.ic_play_arrow_black_36dp)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setLargeIcon(mediaBitmap)
                .setContentTitle(title)
                .setContentText(null)
                .setTicker(title)
                .setAutoCancel(false)
                .setOngoing(true);

        if (player.isPlaying()) {
            builder.addAction(getPauseAction(context));
        } else {
            builder.addAction(getPlayAction(context));
        }

        builder.addAction(getStopAction(context));

        builder.setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.getSessionToken())
                .setShowActionsInCompactView(0, 1)
        );

        return builder.build();
    }

    public RendererItemObservable getRendererItemObservable() {
        return rendererItemObservable;
    }

    public void setSelectedRendererItem(RendererItem rendererItem) {
        // No need for local audio focus, abandon it.
        if (rendererItem != null) {
            abandonAudioFocus();
        }

        player.detachSurfaces();
        player.setRendererItem(rendererItem);

        sendRendererSelectedBroadcast(rendererItem);
    }

    public IVLCVout getVout() {
        return player.getVout();
    }

    private Bitmap setMediaBitmap(Uri mediaUri) {
        if (mediaBitmap != null) {
            return mediaBitmap;
        }

        mediaBitmap = BitmapUtil.getBitmap(
                getApplicationContext(),
                mediaUri
        );

        return mediaBitmap = mediaBitmap == null
                // Unable to get bitmap, use fallback bitmap.
                ? defaultBitmap
                : mediaBitmap;
    }


    public void setMedia(
            Context context,
            Uri mediaUri
    ) {
        if (context == null || mediaUri == null) {
            return;
        }

        final String schema = mediaUri.getScheme();

        setMediaBitmap(mediaUri);

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

    public void setSubtitle(Uri subtitleUri) {
        if (subtitleUri == null) {
            return;
        }

        player.setSubtitle(subtitleUri);
    }

    public void play() {
        gainAudioFocus();

        player.play();
    }

    public void stop() {
        abandonAudioFocus();

        player.stop();
    }

    private void gainAudioFocus() {
        // Only gain audio focus when playing locally.
        if (player.getSelectedRendererItem() != null) {
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
            pause();
            return;
        }

        play();
    }

    public void pause() {
        player.pause();
    }

    public void setAspectRatio(String aspectRatio) {
        player.setAspectRatio(aspectRatio);
    }

    public void setScale(float scale) {
        player.setScale(scale);
    }

    public Media.VideoTrack getCurrentVideoTrack() {
        return player.getCurrentVideoTrack();
    }

    public void attachSurfaces(
            SurfaceView surfaceMedia,
            SurfaceView surfaceSubtitle,
            IVLCVout.OnNewVideoLayoutListener listener
    ) {
        player.attachSurfaces(
                surfaceMedia,
                surfaceSubtitle,
                listener
        );
    }

    public void detachSurfaces() {
        player.detachSurfaces();
    }

    public RendererItem getSelectedRendererItem() {
        return player.getSelectedRendererItem();
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public void setVolume(int volume) {
        player.setVolume(volume);
    }
}
