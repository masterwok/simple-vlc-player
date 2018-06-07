package com.masterwok.simplevlcplayer.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.masterwok.simplevlcplayer.R;
import com.masterwok.simplevlcplayer.components.MediaPlayerComponent;
import com.masterwok.simplevlcplayer.fragments.RendererItemDialogFragment;
import com.masterwok.simplevlcplayer.services.MediaPlayerService;

import org.videolan.libvlc.RendererItem;

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

    private final ServiceConnection mediaPlayerServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(
                ComponentName componentName,
                IBinder iBinder
        ) {
//            mediaPlayerServiceBinder = (MediaPlayerService.MediaPlayerServiceBinder) iBinder;
//            mediaPlayerServiceIsBound = true;
//
//            mediaController = mediaPlayerServiceBinder.getMediaController();
//            transportControls = mediaController.getTransportControls();
//
//            mediaController.registerCallback(mediaControllerCallback);
//
//            seekBarListener = new SeekBarListener(
//                    mediaController,
//                    textViewPosition
//            );
//
//            seekBarPosition.setOnSeekBarChangeListener(seekBarListener);
//
//            setRenderer(mediaPlayerServiceBinder.getSelectedRendererItem());
//            prepareMedia(videoFilePath, subtitleFilePath);
//            transportControls.play();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
//            mediaPlayerServiceIsBound = false;
//            mediaPlayerServiceBinder = null;
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

    private void bindViewComponents() {
        mediaPlayerComponent = findViewById(R.id.component_media_player);
        mediaPlayerComponent.init(
                this::onSeekBarPositionChanged,
                this::onCastButtonTapped,
                this::onPlayPauseButtonTapped
        );
    }

    private void onPlayPauseButtonTapped() {
    }

    private void onCastButtonTapped() {
        RendererItemDialogFragment rendererItemDialog = new RendererItemDialogFragment();

        rendererItemDialog.show(
                getSupportFragmentManager(),
                RendererItemDialogTag
        );
    }

    private void onSeekBarPositionChanged(Float position) {
    }

    private void readIntent() {
        Intent intent = getIntent();

        videoFilePath = intent.getStringExtra(VideoPathExtra);
        subtitleFilePath = intent.getStringExtra(SubtitlePathExtra);
        requestCode = intent.getIntExtra(RequestCodeExtra, Integer.MIN_VALUE);
        playbackPosition = intent.getLongExtra(PlaybackPositionExtra, 0L);
    }

    @Override
    public void onRendererUpdate(RendererItem rendererItem) {

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

    //    private MediaControllerCompat mediaController;
//    private MediaControllerCompat.TransportControls transportControls;
//
//    private MediaPlayerService.MediaPlayerServiceBinder mediaPlayerServiceBinder;
//    private boolean mediaPlayerServiceIsBound = false;
//

//    private SeekBarListener seekBarListener;
//
//    private boolean toolbarsAreVisible = true;
//    private Timer toolbarHideTimer;
//
//    private final MediaControllerCompat.Callback mediaControllerCallback = new MediaControllerCompat.Callback() {
//        @Override
//        public void onPlaybackStateChanged(PlaybackStateCompat state) {
//            // End of media reached, finish the activity.
//            if (state.getState() == PlaybackStateCompat.STATE_STOPPED
//                    && state.getPosition() == 0) {
//                finish();
//            }
//
//            // Media is buffering, seek to the current playback position.
//            if (state.getState() == PlaybackStateCompat.STATE_BUFFERING) {
//                transportControls.seekTo(playbackPosition);
//            }
//
//            // Always update display state when playback state changes.
//            configure(state);
//        }
//    };
//
//    /**
//     * Update the display state.
//     *
//     * @param state The current playback state.
//     */
//    private void configure(PlaybackStateCompat state) {
//        if (state == null
//                || state.getExtras() == null
//                || seekBarListener.isTrackingTouch()) {
//            return;
//        }
//
//        Bundle extras = state.getExtras();
//
//        long length = extras.getLong(VlcMediaPlayerSession.LengthExtra);
//        long time = extras.getLong(VlcMediaPlayerSession.TimeExtra);
//
//        setSeekBarState(time, length);
//    }
//
//
//    /**
//     * Set the seek bar display state.
//     *
//     * @param time   The current playback position.
//     * @param length The length of the media.
//     */
//    private void setSeekBarState(long time, long length) {
//        String lengthText = TimeUtil.getTimeString(length);
//        String positionText = TimeUtil.getTimeString(time);
//        int progress = (int) (((float) time / length) * 100);
//
//        ThreadUtil.onMain(() -> {
//            seekBarPosition.setProgress(progress);
//
//            textViewPosition.setText(positionText);
//            textViewLength.setText(lengthText);
//        });
//    }
//

//    /**
//     * Play or pause the playback of the media. This method is invoked when the pause/play
//     * button is tapped.
//     */
//    private void onPlayPauseImageButtonClick() {
//        int playbackState = mediaController
//                .getPlaybackState()
//                .getState();
//
//        // Currently playing, update image button and pause media.
//        if (playbackState == PlaybackStateCompat.STATE_PLAYING) {
//            imageButtonPlayPause.setImageResource(R.drawable.ic_play_arrow_white_36dp);
//            transportControls.pause();
//            return;
//        }
//
//        // Currently paused, update image button and play media.
//        imageButtonPlayPause.setImageResource(R.drawable.ic_pause_white_36dp);
//        transportControls.play();
//    }
//
//    @Override
//    protected void onStop() {
//        // Playing locally, stop playback.
//        if (mediaPlayerServiceIsBound
//                && mediaPlayerServiceBinder.getSelectedRendererItem() == null) {
//            transportControls.pause();
//        }
//
//        unbindService(mediaPlayerServiceConnection);
//
//        mediaPlayerServiceIsBound = false;
//
//        super.onStop();
//    }
//
//    @Override
//    protected void onDestroy() {
//        transportControls.stop();
//
//        super.onDestroy();
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        startAndBindMediaPlayerService();
//
//        showToolbars();
//        startToolbarHideTimer();
//    }
//
//    @Override
//    protected void onPause() {
//        transportControls.pause();
//
//        super.onPause();
//    }
//
//    /**
//     * Activity is finishing, add playback position to result if activity started
//     * with result code.
//     */
//    @Override
//    public void finish() {
//        // Activity wasn't started with result, do nothing.
//        if (requestCode < 0) {
//            super.finish();
//            return;
//        }
//
//        Intent intent = new Intent();
//
//        // Add playback position to result.
//        intent.putExtra(
//                PlaybackPositionExtra,
//                mediaController
//                        .getPlaybackState()
//                        .getPosition()
//        );
//
//        setResult(requestCode, intent);
//
//        super.finish();
//    }
//
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//
//        // Set the size of the media surface views when the layout configuration changes
//        setSize(videoWidth, videoHeight);
//    }
//
//
//

//
//    @Override
//    public void onRendererUpdate(RendererItem rendererItem) {
//        setRenderer(rendererItem);
//    }
//
//    private void setRenderer(RendererItem rendererItem) {
//        // No renderer selected, set local renderer.
//        if (rendererItem == null) {
//            mediaPlayerServiceBinder.setRenderer(
//                    surfaceViewMedia,
//                    surfaceViewSubtitles,
//                    this
//            );
//
//            return;
//        }
//
//        mediaPlayerServiceBinder.setRenderer(rendererItem);
//    }
//
//    private void prepareMedia(
//            String videoFilePath,
//            String subtitleFilePath
//    ) {
//        if (transportControls == null) {
//            return;
//        }
//
//        Bundle bundle = new Bundle();
//
//        bundle.putString(
//                MediaPlayerActivity.SubtitlePathExtra,
//                subtitleFilePath
//        );
//
//        transportControls.prepareFromUri(
//                Uri.fromFile(new File(videoFilePath)),
//                bundle
//        );
//    }

}
