package com.masterwok.simplevlcplayer.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;

import com.masterwok.simplevlcplayer.R;
import com.masterwok.simplevlcplayer.components.MediaPlayerComponent;
import com.masterwok.simplevlcplayer.fragments.RendererItemDialogFragment;
import com.masterwok.simplevlcplayer.services.MediaPlayerService;
import com.masterwok.simplevlcplayer.sessions.VlcMediaPlayerSession;

import org.videolan.libvlc.RendererItem;

import java.io.File;

public class MediaPlayerActivity
        extends AppCompatActivity
        implements RendererItemDialogFragment.RendererItemSelectionListener {

    public static final String VideoPathExtra = "extra.videopath";
    public static final String SubtitlePathExtra = "extra.subtitlepath";
    public static final String RequestCodeExtra = "extra.requestcode";
    public static final String PlaybackPositionExtra = "extra.playbackposition";
    public static final String VlcOptions = "extra.vlcoptions";
    public static final String RendererItemDialogTag = "tag.dialogrendereritem";

    private String subtitleFilePath;
    private String videoFilePath;
    private long playbackPosition;
    private int requestCode;

    private MediaPlayerComponent mediaPlayerComponent;

    private MediaPlayerService.MediaPlayerServiceBinder mediaPlayerServiceBinder;
    private MediaControllerCompat.TransportControls transportControls;
    private VlcMediaPlayerSession mediaPlayerSession;
    private MediaControllerCompat mediaController;
    private boolean mediaPlayerServiceIsBound;

    private final ServiceConnection mediaPlayerServiceConnection = new ServiceConnection() {


        @Override
        public void onServiceConnected(
                ComponentName componentName,
                IBinder iBinder
        ) {
            mediaPlayerServiceBinder = (MediaPlayerService.MediaPlayerServiceBinder) iBinder;
            mediaPlayerServiceIsBound = true;

            mediaPlayerSession = mediaPlayerServiceBinder.getMediaPlayerSession();
            mediaController = mediaPlayerServiceBinder.getMediaController();
            transportControls = mediaController.getTransportControls();

            mediaController.registerCallback(mediaControllerCallback);

            setRenderer(mediaPlayerSession.getSelectedRendererItem());
            prepareMedia(videoFilePath, subtitleFilePath);
            transportControls.play();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mediaPlayerServiceIsBound = false;
            mediaPlayerServiceBinder = null;
            mediaPlayerSession = null;
            transportControls = null;
            mediaController = null;
        }
    };

    private final MediaControllerCompat.Callback mediaControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            // End of media reached, finish the activity.
            if (state.getState() == PlaybackStateCompat.STATE_STOPPED
                    && state.getPosition() == 0) {
                finish();
            }

            // Media is buffering, seek to the current playback position.
            if (state.getState() == PlaybackStateCompat.STATE_BUFFERING) {
                transportControls.seekTo(playbackPosition);
            }

            // Always update display state when playback state changes.
            configure(state);
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);

        bindViewComponents();
        readIntent();

        startAndBindMediaPlayerService();
    }

    @Override
    protected void onDestroy() {
        if (!mediaPlayerServiceIsBound) {
            return;
        }

        transportControls.stop();
        unbindService(mediaPlayerServiceConnection);
        mediaPlayerServiceIsBound = false;

        super.onDestroy();
    }

    /**
     * Activity is finishing, add playback position to result if activity started
     * with result code.
     */
    @Override
    public void finish() {
        // Activity wasn't started with result, do nothing.
        if (requestCode < 0) {
            super.finish();
            return;
        }

        Intent intent = new Intent();

        // Add playback position to result.
        intent.putExtra(
                PlaybackPositionExtra,
                mediaController
                        .getPlaybackState()
                        .getPosition()
        );

        setResult(requestCode, intent);

        super.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mediaPlayerServiceIsBound) {
            return;
        }

        // TODO: Fix this, renderer shouldn't be going black on pause.
        if (mediaPlayerSession.getSelectedRendererItem() == null) {
            setLocalRenderer();
        }
    }

    @Override
    protected void onPause() {
        transportControls.pause();

        super.onPause();
    }

    private void readIntent() {
        Intent intent = getIntent();

        videoFilePath = intent.getStringExtra(VideoPathExtra);
        subtitleFilePath = intent.getStringExtra(SubtitlePathExtra);
        requestCode = intent.getIntExtra(RequestCodeExtra, Integer.MIN_VALUE);
        playbackPosition = intent.getLongExtra(PlaybackPositionExtra, 0L);
    }

    private void bindViewComponents() {
        mediaPlayerComponent = findViewById(R.id.component_media_player);
        mediaPlayerComponent.init(
                this::seekToPosition,
                this::showRendererItemDialogFragment,
                this::togglePlayback
        );
    }

    private void startAndBindMediaPlayerService() {
        Intent intent = new Intent(
                getApplicationContext(),
                MediaPlayerService.class
        );

        startService(intent);

        bindService(
                intent,
                mediaPlayerServiceConnection,
                Context.BIND_AUTO_CREATE
        );
    }

    /**
     * Update the display state.
     *
     * @param state The current playback state.
     */
    private void configure(PlaybackStateCompat state) {
        if (state == null
                || state.getExtras() == null
                || mediaPlayerComponent.isTrackingTouch()) {
            return;
        }

        Bundle extras = state.getExtras();

        long length = extras.getLong(VlcMediaPlayerSession.LengthExtra);
        long time = extras.getLong(VlcMediaPlayerSession.TimeExtra);

        mediaPlayerComponent.configure(
                length,
                time,
                getPlaybackStateCompat() == PlaybackStateCompat.STATE_PLAYING
        );
    }

    /**
     * Toggle the playback of the media by pausing or playing the media.
     */
    private void togglePlayback() {
        if (getPlaybackStateCompat() == PlaybackStateCompat.STATE_PLAYING) {
            transportControls.pause();
            return;
        }

        transportControls.play();
    }

    /**
     * Show the renderer item dialog fragment.
     */
    private void showRendererItemDialogFragment() {
        RendererItemDialogFragment rendererItemDialog = new RendererItemDialogFragment();

        rendererItemDialog.show(
                getSupportFragmentManager(),
                RendererItemDialogTag
        );
    }

    /**
     * Seek to a position that is a percentage of the total length of the media.
     *
     * @param position The percentage value to seek to.
     */
    private void seekToPosition(Float position) {
        transportControls.seekTo((long) (position * getMediaLength()));
    }

    /**
     * Get the playback state (i.e. PlaybackStateCompat.STATE_PLAYING) of the media.
     *
     * @return The playback state.
     */
    private int getPlaybackStateCompat() {
        return mediaController
                .getPlaybackState()
                .getState();
    }

    /**
     * Get the length of the media from the playback state.
     *
     * @return The length of the media in milliseconds.
     */
    private long getMediaLength() {
        PlaybackStateCompat playbackState = mediaController.getPlaybackState();
        Bundle extras = playbackState.getExtras();

        return extras == null
                ? 0
                : extras.getLong(VlcMediaPlayerSession.LengthExtra);
    }

    /**
     * Prepare the media for the provided video and subtitle files.
     *
     * @param videoFilePath    The path to the video file.
     * @param subtitleFilePath The path to the subtitle file.
     */
    private void prepareMedia(
            String videoFilePath,
            String subtitleFilePath
    ) {
        if (transportControls == null) {
            return;
        }

        Bundle bundle = new Bundle();

        bundle.putString(
                MediaPlayerActivity.SubtitlePathExtra,
                subtitleFilePath
        );

        transportControls.prepareFromUri(
                Uri.fromFile(new File(videoFilePath)),
                bundle
        );
    }

    @Override
    public void onRendererUpdate(RendererItem rendererItem) {
        setRenderer(rendererItem);
    }

    private void setLocalRenderer() {
        mediaPlayerSession.setRenderer(
                mediaPlayerComponent.getMediaSurfaceView(),
                mediaPlayerComponent.getSubtitleSurfaceView(),
                mediaPlayerComponent
        );
    }


    /**
     * Set the current renderer item. If the renderer is null, then
     * local playback is used.
     *
     * @param rendererItem The renderer item.
     */
    private void setRenderer(RendererItem rendererItem) {
        // No renderer selected, set local renderer.
        if (rendererItem == null) {
            setLocalRenderer();
            return;
        }

        mediaPlayerSession.setRenderer(rendererItem);
    }


}
