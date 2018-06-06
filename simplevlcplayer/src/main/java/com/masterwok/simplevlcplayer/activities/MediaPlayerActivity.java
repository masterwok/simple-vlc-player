package com.masterwok.simplevlcplayer.activities;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.masterwok.simplevlcplayer.R;
import com.masterwok.simplevlcplayer.callbacks.SeekBarListener;
import com.masterwok.simplevlcplayer.fragments.RendererItemDialogFragment;
import com.masterwok.simplevlcplayer.services.MediaPlayerService;
import com.masterwok.simplevlcplayer.sessions.VlcMediaPlayerSession;
import com.masterwok.simplevlcplayer.utils.ThreadUtil;
import com.masterwok.simplevlcplayer.utils.TimeUtil;
import com.masterwok.simplevlcplayer.utils.ViewUtil;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.RendererItem;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class MediaPlayerActivity
        extends AppCompatActivity
        implements IVLCVout.OnNewVideoLayoutListener,
        RendererItemDialogFragment.RendererItemSelectionListener {

    public static final String VideoPathExtra = "extra.videopath";
    public static final String SubtitlePathExtra = "extra.subtitlepath";
    public static final String RequestCodeExtra = "extra.requestcode";
    public static final String PlaybackPositionExtra = "extra.playbackposition";
    public static final String VlcOptions = "extra.vlcoptions";
    public static final String RendererItemDialogTag = "tag.dialogrendereritem";

    private MediaControllerCompat mediaController;
    private MediaControllerCompat.TransportControls transportControls;

    private MediaPlayerService.MediaPlayerServiceBinder mediaPlayerServiceBinder;
    private boolean mediaPlayerServiceIsBound = false;

    private String subtitleFilePath;
    private String videoFilePath;
    private long playbackPosition;
    private int requestCode;

    private int videoWidth;
    private int videoHeight;

    private AppCompatImageButton imageButtonPlayPause;
    private RelativeLayout relativeLayoutRoot;
    private SurfaceView surfaceViewSubtitles;
    private SurfaceView surfaceViewMedia;
    private SurfaceHolder surfaceHolderMedia;
    private Toolbar toolbarHeader;
    private Toolbar toolbarFooter;
    private TextView textViewPosition;
    private TextView textViewLength;
    private SeekBar seekBarPosition;

    private SeekBarListener seekBarListener;

    private boolean toolbarsAreVisible = true;
    private Timer toolbarHideTimer;

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

    /**
     * Update the display state.
     *
     * @param state The current playback state.
     */
    private void configure(PlaybackStateCompat state) {
        if (state == null
                || state.getExtras() == null
                || seekBarListener.isTrackingTouch()) {
            return;
        }

        Bundle extras = state.getExtras();

        long length = extras.getLong(VlcMediaPlayerSession.LengthExtra);
        long time = extras.getLong(VlcMediaPlayerSession.TimeExtra);

        setSeekBarState(time, length);
    }


    /**
     * Set the seek bar display state.
     *
     * @param time   The current playback position.
     * @param length The length of the media.
     */
    private void setSeekBarState(long time, long length) {
        String lengthText = TimeUtil.getTimeString(length);
        String positionText = TimeUtil.getTimeString(time);
        int progress = (int) (((float) time / length) * 100);

        ThreadUtil.onMain(() -> {
            seekBarPosition.setProgress(progress);

            textViewPosition.setText(positionText);
            textViewLength.setText(lengthText);
        });
    }

    private final ServiceConnection mediaPlayerServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(
                ComponentName componentName,
                IBinder iBinder
        ) {
            mediaPlayerServiceBinder = (MediaPlayerService.MediaPlayerServiceBinder) iBinder;
            mediaPlayerServiceIsBound = true;

            mediaController = mediaPlayerServiceBinder.getMediaController();
            transportControls = mediaController.getTransportControls();

            mediaController.registerCallback(mediaControllerCallback);

            MediaControllerCompat.setMediaController(MediaPlayerActivity.this, mediaController);

            seekBarListener = new SeekBarListener(
                    mediaController,
                    textViewPosition
            );

            seekBarPosition.setOnSeekBarChangeListener(seekBarListener);

            setRenderer(mediaPlayerServiceBinder.getSelectedRendererItem());
            prepareMedia(videoFilePath, subtitleFilePath);
            transportControls.play();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mediaPlayerServiceIsBound = false;
            mediaPlayerServiceBinder = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);

        readIntent();

        bindViewComponents();
        subscribeToViewComponents();

        configureMediaSurfaceViewHolder(surfaceViewMedia);
        configureSubtitlesSurfaceView(surfaceViewSubtitles);

        setSupportActionBar(toolbarHeader);
    }

    private void readIntent() {
        Intent intent = getIntent();

        videoFilePath = intent.getStringExtra(VideoPathExtra);
        subtitleFilePath = intent.getStringExtra(SubtitlePathExtra);
        requestCode = intent.getIntExtra(RequestCodeExtra, Integer.MIN_VALUE);
        playbackPosition = intent.getLongExtra(PlaybackPositionExtra, 0L);
    }

    private void bindViewComponents() {
        relativeLayoutRoot = findViewById(R.id.relative_layout_root);

        toolbarHeader = findViewById(R.id.toolbar_header);
        toolbarFooter = findViewById(R.id.toolbar_footer);

        surfaceViewMedia = findViewById(R.id.surface_media);
        surfaceViewSubtitles = findViewById(R.id.surface_subtitles);

        seekBarPosition = toolbarFooter.findViewById(R.id.seekbar_position);
        textViewPosition = toolbarFooter.findViewById(R.id.textview_position);
        textViewLength = toolbarFooter.findViewById(R.id.textview_length);
        imageButtonPlayPause = toolbarFooter.findViewById(R.id.imagebutton_play_pause);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void subscribeToViewComponents() {
        imageButtonPlayPause.setOnClickListener(view -> onPlayPauseImageButtonClick());
        relativeLayoutRoot.setOnTouchListener(this::onRootViewTouch);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != R.id.menu_item_cast) {
            return false;
        }

        RendererItemDialogFragment rendererItemDialog = new RendererItemDialogFragment();

        rendererItemDialog.show(
                getSupportFragmentManager(),
                RendererItemDialogTag
        );

        return true;
    }

    private boolean onRootViewTouch(
            View view,
            MotionEvent motionEvent
    ) {
        // Only start animation on down press.
        if (motionEvent.getActionMasked() != MotionEvent.ACTION_DOWN) {
            return false;
        }

        toolbarHideTimer.cancel();

        toggleToolbarVisibility();

        return false;
    }

    /**
     * Toggle the visibility of the toolbars by slide animating them.
     */
    private void toggleToolbarVisibility() {
        // User is sliding seek bar, do not modify visibility.
        if (seekBarListener.isTrackingTouch()) {
            return;
        }

        if (toolbarsAreVisible) {
            hideToolbars();
            return;
        }

        showToolbars();
    }

    /**
     * Hide header and footer toolbars by translating them off the screen vertically.
     */
    private void hideToolbars() {
        // Already hidden, do nothing.
        if (!toolbarsAreVisible) {
            return;
        }

        toolbarsAreVisible = false;

        ThreadUtil.onMain(() -> {
            ViewUtil.slideViewAboveOrBelowParent(toolbarHeader, true);
            ViewUtil.slideViewAboveOrBelowParent(toolbarFooter, false);
        });
    }

    /**
     * Show header and footer toolbars by translating them vertically.
     */
    private void showToolbars() {
        // Already shown, do nothing.
        if (toolbarsAreVisible) {
            return;
        }

        ThreadUtil.onMain(() -> {
            toolbarsAreVisible = true;
            ViewUtil.resetVerticalTranslation(toolbarHeader);
            ViewUtil.resetVerticalTranslation(toolbarFooter);
        });
    }

    /**
     * Play or pause the playback of the media. This method is invoked when the pause/play
     * button is tapped.
     */
    private void onPlayPauseImageButtonClick() {
        int playbackState = mediaController
                .getPlaybackState()
                .getState();

        // Currently playing, update image button and pause media.
        if (playbackState == PlaybackStateCompat.STATE_PLAYING) {
            imageButtonPlayPause.setImageResource(R.drawable.ic_play_arrow_white_36dp);
            transportControls.pause();
            return;
        }

        // Currently paused, update image button and play media.
        imageButtonPlayPause.setImageResource(R.drawable.ic_pause_white_36dp);
        transportControls.play();
    }

    private void startToolbarHideTimer() {
        toolbarHideTimer = new Timer();

        int timerDelay = getResources()
                .getInteger(R.integer.media_player_toolbar_hide_timeout);

        toolbarHideTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                toggleToolbarVisibility();
            }
        }, timerDelay);
    }

    @Override
    protected void onStop() {
        // Playing locally, stop playback.
        if (mediaPlayerServiceIsBound
                && mediaPlayerServiceBinder.getSelectedRendererItem() == null) {
            transportControls.pause();
        }

        unbindService(mediaPlayerServiceConnection);

        mediaPlayerServiceIsBound = false;

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        transportControls.stop();

        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();

        startAndBindMediaPlayerService();

        showToolbars();
        startToolbarHideTimer();
    }

    @Override
    protected void onPause() {
        transportControls.pause();

        super.onPause();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.media_player, menu);

        return true;
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Set the size of the media surface views when the layout configuration changes
        setSize(videoWidth, videoHeight);
    }

    @Override
    public void onNewVideoLayout(
            IVLCVout vlcVideoOut,
            int width,
            int height,
            int visibleWidth,
            int visibleHeight,
            int sarNum,
            int sarDen
    ) {
        // If either video with or video height are 0, do nothing.
        if (width * height <= 1) {
            return;
        }

        // Set the size of the media surface views when the layout changes.
        setSize(width, height);
    }

    /* Set the size of the media surface views.
     *
     * @param width  The new surface view width.
     * @param height The new surface view height.
     */
    private void setSize(int width, int height) {
        videoWidth = width;
        videoHeight = height;

        // No views to set the size on, do nothing.
        if (surfaceViewMedia == null
                || surfaceViewSubtitles == null) {
            return;
        }

        Pair<Integer, Integer> videoDimensions = getVideoDimensions(
                videoWidth,
                videoHeight,
                getWindow()
                        .getDecorView()
                        .getWidth(),
                getWindow()
                        .getDecorView()
                        .getHeight(),
                getResources()
                        .getConfiguration()
                        .orientation == Configuration.ORIENTATION_PORTRAIT
        );

        configureMediaSurfaceViewHolder(surfaceViewMedia);

        // force surface buffer size
        surfaceHolderMedia.setFixedSize(
                videoWidth,
                videoHeight
        );

        // set display size
        ViewGroup.LayoutParams lp = surfaceViewMedia.getLayoutParams();

        //noinspection ConstantConditions
        lp.width = videoDimensions.first;
        //noinspection ConstantConditions
        lp.height = videoDimensions.second;

        surfaceViewMedia.setLayoutParams(lp);
        surfaceViewSubtitles.setLayoutParams(lp);

        surfaceViewMedia.invalidate();
        surfaceViewSubtitles.invalidate();
    }

    /**
     * Configure the media surface view.
     *
     * @param surfaceView The video surface view.
     */
    private void configureMediaSurfaceViewHolder(SurfaceView surfaceView) {
        surfaceHolderMedia = surfaceView.getHolder();
        surfaceHolderMedia.setKeepScreenOn(true);
    }

    /**
     * Configure the subtitles surface view.
     *
     * @param surfaceView The subtitle surface view.
     */
    private void configureSubtitlesSurfaceView(SurfaceView surfaceView) {
        surfaceView.setZOrderMediaOverlay(true);
        surfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
    }

    /**
     * Calculate the new video dimensions using the provided parameters.
     *
     * @param width        The new width of the media surface view.
     * @param height       The new height of the media surface view.
     * @param screenWidth  The device screen width.
     * @param screenHeight The device screen height.
     * @param isPortrait   Whether or not the device is displayed in portrait mode.
     * @return A pair instance of width by height.
     */
    public static Pair<Integer, Integer> getVideoDimensions(
            int width,
            int height,
            int screenWidth,
            int screenHeight,
            boolean isPortrait
    ) {
        if (screenWidth > screenHeight && isPortrait || screenWidth < screenHeight && !isPortrait) {
            int tmp = screenWidth;
            //noinspection SuspiciousNameCombination
            screenWidth = screenHeight;
            screenHeight = tmp;
        }

        float videoAr = (float) width / (float) height;
        float screenAr = (float) screenWidth / (float) screenHeight;

        if (screenAr < videoAr) {
            screenHeight = (int) (screenWidth / videoAr);
        } else {
            screenWidth = (int) (screenHeight * videoAr);
        }

        return new Pair<>(screenWidth, screenHeight);
    }

    @Override
    public void onRendererUpdate(RendererItem rendererItem) {
        transportControls.stop();

        setRenderer(rendererItem);
        prepareMedia(videoFilePath, subtitleFilePath);
        transportControls.play();
    }

    private void setRenderer(RendererItem rendererItem) {
        // No renderer selected, set local renderer.
        if (rendererItem == null) {
            mediaPlayerServiceBinder.setRenderer(
                    surfaceViewMedia,
                    surfaceViewSubtitles,
                    this
            );

            return;
        }

        mediaPlayerServiceBinder.setRenderer(rendererItem);
    }

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

}
