package com.masterwok.simplevlcplayer.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.SurfaceView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.masterwok.simplevlcplayer.R;
import com.masterwok.simplevlcplayer.services.MediaPlayerService;

import org.videolan.libvlc.IVLCVout;

public class MediaPlayerActivity
        extends AppCompatActivity
        implements IVLCVout.OnNewVideoLayoutListener {

    private MediaPlayerService.MediaPlayerServiceBinder mediaPlayerServiceBinder;
    private boolean mediaPlayerServiceIsBound = false;

    private AppCompatImageButton imageButtonPlayPause;
    private RelativeLayout relativeLayoutRoot;
    private SurfaceView surfaceViewSubtitles;
    private SurfaceView surfaceViewMedia;
    private Toolbar toolbarHeader;
    private Toolbar toolbarFooter;
    private TextView textViewPosition;
    private TextView textViewLength;
    private SeekBar seekBarPosition;

    private final ServiceConnection mediaPlayerServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(
                ComponentName componentName,
                IBinder iBinder
        ) {
            mediaPlayerServiceBinder = (MediaPlayerService.MediaPlayerServiceBinder) iBinder;
            mediaPlayerServiceIsBound = true;

            playMediaLocally();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mediaPlayerServiceIsBound = false;
            mediaPlayerServiceBinder = null;
        }
    };

    private void playMediaLocally() {
        if (!mediaPlayerServiceIsBound) {
            return;
        }

        mediaPlayerServiceBinder.setRenderer(
                surfaceViewMedia,
                surfaceViewSubtitles,
                this
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);

        startAndBindMediaPlayerService();
        bindViewComponents();
        subscribeToViewComponents();

        setSupportActionBar(toolbarHeader);
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

    private void subscribeToViewComponents() {
        // TODO: Actually subscribe to views..
    }

    @Override
    protected void onStart() {
        super.onStart();

        startAndBindMediaPlayerService();
    }

    @Override
    protected void onStop() {
        super.onStop();

        unbindService(mediaPlayerServiceConnection);
        mediaPlayerServiceIsBound = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.media_player, menu);

        return true;
    }

    private void startAndBindMediaPlayerService() {
        Intent intent = new Intent(this, MediaPlayerService.class);

        startService(intent);

        bindService(
                intent,
                mediaPlayerServiceConnection,
                Context.BIND_AUTO_CREATE
        );
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

    }
}
