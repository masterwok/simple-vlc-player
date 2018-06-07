package com.masterwok.simplevlcplayer.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.masterwok.simplevlcplayer.R;
import com.masterwok.simplevlcplayer.callbacks.SeekBarListener;
import com.masterwok.simplevlcplayer.interfaces.ParamRunnable;
import com.masterwok.simplevlcplayer.utils.ThreadUtil;
import com.masterwok.simplevlcplayer.utils.TimeUtil;
import com.masterwok.simplevlcplayer.utils.ViewUtil;

import org.videolan.libvlc.IVLCVout;

import java.util.Timer;
import java.util.TimerTask;


public class MediaPlayerComponent
        extends RelativeLayout
        implements IVLCVout.OnNewVideoLayoutListener {

    private AppCompatImageButton imageButtonPlayPause;
    private SurfaceView surfaceViewSubtitle;
    private SurfaceView surfaceViewMedia;
    private SurfaceHolder surfaceHolderMedia;
    private Toolbar toolbarHeader;
    private Toolbar toolbarFooter;
    private TextView textViewPosition;
    private TextView textViewLength;
    private SeekBar seekBarPosition;

    private int videoWidth;
    private int videoHeight;

    private SeekBarListener seekBarListener;
    private Timer toolbarHideTimer;

    private boolean toolbarsAreVisible = true;

    public MediaPlayerComponent(Context context) {
        super(context);
        inflate();
    }

    public MediaPlayerComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate();
    }

    public MediaPlayerComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate();
    }

    public MediaPlayerComponent(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        inflate();
    }

    private Window getWindow() {
        return ((AppCompatActivity) getContext())
                .getWindow();
    }

    private void inflate() {
        inflate(
                getContext(),
                R.layout.component_media_player,
                this
        );
    }

    @SuppressLint("ClickableViewAccessibility")
    public void init(
            ParamRunnable<Float> onSeekBarPositionUpdate,
            Runnable onCastButtonTap,
            Runnable onPlayPauseButtonTap
    ) {
        toolbarHeader.setOnMenuItemClickListener(item -> {
            onCastButtonTap.run();
            return true;
        });

        this.seekBarListener = new SeekBarListener(onSeekBarPositionUpdate::run);
        seekBarPosition.setOnSeekBarChangeListener(seekBarListener);
        imageButtonPlayPause.setOnClickListener(view -> onPlayPauseButtonTap.run());

        setOnTouchListener((view, motionEvent) -> {
            // Only start animation on down press.
            if (motionEvent.getActionMasked() != MotionEvent.ACTION_DOWN) {
                return false;
            }

            toolbarHideTimer.cancel();

            toggleToolbarVisibility();

            return false;
        });
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        bindViewComponents();

        toolbarHeader.inflateMenu(R.menu.media_player);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        showToolbars();
        startToolbarHideTimer();
    }

    private void bindViewComponents() {
        toolbarHeader = findViewById(R.id.toolbar_header);
        toolbarFooter = findViewById(R.id.toolbar_footer);

        surfaceViewMedia = findViewById(R.id.surface_media);
        surfaceViewSubtitle = findViewById(R.id.surface_subtitle);

        seekBarPosition = toolbarFooter.findViewById(R.id.seekbar_position);
        textViewPosition = toolbarFooter.findViewById(R.id.textview_position);
        textViewLength = toolbarFooter.findViewById(R.id.textview_length);
        imageButtonPlayPause = toolbarFooter.findViewById(R.id.imagebutton_play_pause);
    }

    public SurfaceView getMediaSurfaceView() {
        return surfaceViewMedia;
    }

    public SurfaceView getSubtitileSurfaceView() {
        return surfaceViewSubtitle;
    }

    public void configure(
            long length,
            long time,
            boolean isPlaying
    ) {
        String lengthText = TimeUtil.getTimeString(length);
        String positionText = TimeUtil.getTimeString(time);
        int progress = (int) (((float) time / length) * 100);

        ThreadUtil.onMain(() -> {
            seekBarPosition.setProgress(progress);
            textViewPosition.setText(positionText);
            textViewLength.setText(lengthText);
            imageButtonPlayPause.setImageResource(getPlayPauseDrawableResourceId(isPlaying));
        });
    }

    public int getPlayPauseDrawableResourceId(boolean isPlaying) {
        return isPlaying
                ? R.drawable.ic_pause_white_36dp
                : R.drawable.ic_play_arrow_white_36dp;
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

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Set the size of the media surface views when the layout configuration changes
        setSize(videoWidth, videoHeight);
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
                || surfaceViewSubtitle == null) {
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
        surfaceViewSubtitle.setLayoutParams(lp);

        surfaceViewMedia.invalidate();
        surfaceViewSubtitle.invalidate();
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
}
