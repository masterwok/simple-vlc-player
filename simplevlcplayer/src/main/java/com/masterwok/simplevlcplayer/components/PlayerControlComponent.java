package com.masterwok.simplevlcplayer.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.masterwok.simplevlcplayer.R;
import com.masterwok.simplevlcplayer.common.utils.ThreadUtil;
import com.masterwok.simplevlcplayer.common.utils.TimeUtil;
import com.masterwok.simplevlcplayer.common.utils.ViewUtil;

public class PlayerControlComponent
        extends RelativeLayout
        implements SeekBar.OnSeekBarChangeListener {

    private final Handler handler = new Handler();

    private Toolbar toolbarHeader;
    private Toolbar toolbarFooter;
    private SeekBar seekBarPosition;
    private AppCompatTextView textViewPosition;
    private AppCompatTextView textViewLength;
    private AppCompatImageButton imageButtonPlayPause;
    private boolean toolbarsAreVisible = true;
    private boolean isTrackingTouch;
    private Callback callback;
    private boolean hasSelectedRenderer;
    private boolean showSubtitleMenuItem;

    public PlayerControlComponent(Context context) {
        super(context);
        inflate(context);
    }

    public PlayerControlComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        readStyleAttributes(context, attrs);
        inflate(context);
    }

    public PlayerControlComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        readStyleAttributes(context, attrs);
        inflate(context);
    }

    public PlayerControlComponent(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        readStyleAttributes(context, attrs);
        inflate(context);
    }

    private void inflate(Context context) {
        inflate(context, R.layout.component_player_control, this);
    }

    public interface Callback {
        void onPlayPauseButtonClicked();

        void onCastButtonClicked();

        void onProgressChanged(int progress);

        void onProgressChangeStarted();

        void onSubtitlesButtonClicked();
    }

    public void registerCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        bindViewComponents();
        subscribeToViewComponents();
        startToolbarHideTimer();

        if (!showSubtitleMenuItem) {
            toolbarHeader.inflateMenu(R.menu.media_player_no_subtitle_item);
            return;
        }

        if (hasSelectedRenderer) {
            toolbarHeader.inflateMenu(R.menu.media_player_renderer);
        } else {
            toolbarHeader.inflateMenu(R.menu.media_player);
        }
    }

    private void readStyleAttributes(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }

        TypedArray styledAttributes = context.obtainStyledAttributes(
                attrs,
                R.styleable.PlayerControlComponent,
                0,
                0
        );

        hasSelectedRenderer = styledAttributes.getBoolean(
                R.styleable.PlayerControlComponent_showSubtitleMenuItem,
                false
        );

        showSubtitleMenuItem = styledAttributes.getBoolean(
                R.styleable.PlayerControlComponent_showSubtitleMenuItem,
                true
        );

        styledAttributes.recycle();
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

        imageButtonPlayPause.setOnClickListener(view -> callback.onPlayPauseButtonClicked());

        toolbarHeader.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.menu_item_cast) {
                callback.onCastButtonClicked();
            } else if (itemId == R.id.menu_item_subtitles) {
                callback.onSubtitlesButtonClicked();
            }

            return true;
        });

        setOnClickListener((view) -> {
            handler.removeCallbacksAndMessages(null);

            toggleToolbarVisibility();
        });
    }

    /**
     * Start the timer that hides the toolbars after a predefined amount of time.
     */
    private void startToolbarHideTimer() {
        int timerDelay = getResources()
                .getInteger(R.integer.player_toolbar_hide_timeout);

        handler.postDelayed(this::hideToolbars, timerDelay);
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


    public void configure(
            boolean isPlaying,
            long time,
            long length
    ) {
        String lengthText = TimeUtil.getTimeString(length);
        String positionText = TimeUtil.getTimeString(time);
        int progress = (int) (((float) time / length) * 100);

        ThreadUtil.onMain(() -> {
            imageButtonPlayPause.setImageResource(
                    getPlayPauseDrawableResourceId(isPlaying)
            );

            if (time < 0 || length < 0) {
                seekBarPosition.setProgress(0);
                textViewPosition.setText(null);
                textViewLength.setText(null);
                return;
            }

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

        callback.onProgressChangeStarted();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isTrackingTouch = false;

        callback.onProgressChanged(seekBar.getProgress());
    }

}
