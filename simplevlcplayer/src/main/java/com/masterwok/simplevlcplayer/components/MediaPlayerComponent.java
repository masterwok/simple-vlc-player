package com.masterwok.simplevlcplayer.components;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.masterwok.simplevlcplayer.R;
import com.masterwok.simplevlcplayer.callbacks.SeekBarListener;
import com.masterwok.simplevlcplayer.interfaces.ParamRunnable;
import com.masterwok.simplevlcplayer.utils.ThreadUtil;
import com.masterwok.simplevlcplayer.utils.TimeUtil;

public class MediaPlayerComponent
        extends RelativeLayout {

    private AppCompatImageButton imageButtonPlayPause;
    private SurfaceView surfaceViewSubtitle;
    private SurfaceView surfaceViewMedia;
    private SurfaceHolder surfaceHolderMedia;
    private Toolbar toolbarHeader;
    private Toolbar toolbarFooter;
    private TextView textViewPosition;
    private TextView textViewLength;
    private SeekBar seekBarPosition;

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

    public void init(
            ParamRunnable<Float> onSeekBarPositionUpdate,
            Runnable onCastButtonTap
    ) {
        toolbarHeader.setOnMenuItemClickListener(item -> {
            onCastButtonTap.run();
            return true;
        });

        seekBarPosition.setOnSeekBarChangeListener(new SeekBarListener(onSeekBarPositionUpdate::run));
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        bindViewComponents();

        toolbarHeader.inflateMenu(R.menu.media_player);
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
}
