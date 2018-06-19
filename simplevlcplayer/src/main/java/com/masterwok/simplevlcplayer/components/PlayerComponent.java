package com.masterwok.simplevlcplayer.components;

import android.content.Context;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.masterwok.simplevlcplayer.R;
import com.masterwok.simplevlcplayer.utils.ThreadUtil;
import com.masterwok.simplevlcplayer.utils.TimeUtil;
import com.masterwok.simplevlcplayer.utils.ViewUtil;

import java.util.Timer;
import java.util.TimerTask;

public class PlayerComponent
        extends RelativeLayout
        implements SeekBar.OnSeekBarChangeListener {

    private Toolbar toolbarHeader;
    private Toolbar toolbarFooter;
    private SeekBar seekBarPosition;
    private AppCompatTextView textViewPosition;
    private AppCompatTextView textViewLength;
    private AppCompatImageButton imageButtonPlayPause;
    private Timer toolbarHideTimer;
    private boolean toolbarsAreVisible = true;
    private Runnable onPlaybackButtonTapped;
    private boolean isTrackingTouch;


    public PlayerComponent(Context context) {
        super(context);
        inflate(context);
    }

    public PlayerComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context);
    }

    public PlayerComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context);
    }

    public PlayerComponent(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        inflate(context);
    }

    private void inflate(Context context) {
        inflate(context, R.layout.component_player, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        bindViewComponents();
        subscribeToViewComponents();
        startToolbarHideTimer();
    }

    public void init(
            Runnable onPlaybackButtonTapped
    ) {
        this.onPlaybackButtonTapped = onPlaybackButtonTapped;
    }

    private void bindViewComponents() {
        toolbarHeader = findViewById(R.id.toolbar_header);
        toolbarFooter = findViewById(R.id.toolbar_footer);

        seekBarPosition = toolbarFooter.findViewById(R.id.seekbar_position);
        textViewPosition = toolbarFooter.findViewById(R.id.textview_position);
        textViewLength = toolbarFooter.findViewById(R.id.textview_length);
        imageButtonPlayPause = toolbarFooter.findViewById(R.id.imagebutton_play_pause);
    }

    private void subscribeToViewComponents() {
        seekBarPosition.setOnSeekBarChangeListener(this);

        imageButtonPlayPause.setOnClickListener(view -> onPlaybackButtonTapped.run());

        setOnClickListener((view) -> {
            toolbarHideTimer.cancel();

            toggleToolbarVisibility();
        });
    }

    /**
     * Start the timer that hides the toolbars after a predefined amount of time.
     */
    private void startToolbarHideTimer() {
        toolbarHideTimer = new Timer();

        int timerDelay = getResources()
                .getInteger(R.integer.player_toolbar_hide_timeout);

        toolbarHideTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                hideToolbars();
            }
        }, timerDelay);
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
     * Toggle the visibility of the toolbars by slide animating them.
     */
    private void toggleToolbarVisibility() {
        // User is sliding seek bar, do not modify visibility.
        if (isTrackingTouch) {
            return;
        }

        if (toolbarsAreVisible) {
            hideToolbars();
            return;
        }

        showToolbars();
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


    protected void configure(PlaybackStateCompat state) {
        boolean isPlaying = state.getState() == PlaybackStateCompat.STATE_PLAYING;
        long time = state.getPosition();
        long length = state.getBufferedPosition();

        String lengthText = TimeUtil.getTimeString(length);
        String positionText = TimeUtil.getTimeString(time);
        int progress = (int) (((float) time / length) * 100);

        ThreadUtil.onMain(() -> {
            imageButtonPlayPause.setImageResource(
                    getPlayPauseDrawableResourceId(isPlaying)
            );

            seekBarPosition.setProgress(progress);
            textViewPosition.setText(positionText);
            textViewLength.setText(lengthText);

        });
    }

    private int getPlayPauseDrawableResourceId(boolean isPlaying) {
        return isPlaying
                ? R.drawable.ic_pause_white_36dp
                : R.drawable.ic_play_arrow_white_36dp;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        // Nothing to do..
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isTrackingTouch = true;

//        final int state = mediaController.getPlaybackState().getState();
//
//        if (state != PlaybackStateCompat.STATE_PLAYING) {
//            return;
//        }
//
//        player.pause();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isTrackingTouch = false;
//        player.setTime((long) ((float) seekBar.getProgress() / 100 * player.getLength()));
//        ThreadUtil.onMain(this::showProgressBar);
//
//        player.play();
    }
}
