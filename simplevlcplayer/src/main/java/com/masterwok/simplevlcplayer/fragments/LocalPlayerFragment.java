package com.masterwok.simplevlcplayer.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.masterwok.simplevlcplayer.R;
import com.masterwok.simplevlcplayer.components.PlayerControlComponent;
import com.masterwok.simplevlcplayer.services.MediaPlayerService;

public class LocalPlayerFragment
        extends BasePlayerFragment {

    private static final String SAMPLE_URL = "http://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_640x360.m4v";
    private static final String IsPlayingKey = "bundle.isplaying";
    private static final String LengthKey = "bundle.length";
    private static final String TimeKey = "bundle.time";

    protected boolean resumeIsPlaying = true;
    protected long resumeLength = 0;
    protected long resumeTime = 0;


    private View.OnLayoutChangeListener surfaceLayoutListener;

    private PlayerControlComponent componentControls;
    private SurfaceView surfaceSubtitle;
    private SurfaceView surfaceMedia;

    private MediaPlayerService.Binder serviceBinder;

    @Override
    protected void configure(
            boolean isPlaying,
            long time,
            long length
    ) {
        componentControls.configure(
                isPlaying,
                time,
                length
        );
    }

    @Override
    protected void onConnected(MediaPlayerService.Binder binder) {
        this.serviceBinder = binder;

        startPlayback();
    }

    @Override
    protected void onDisconnected() {
        this.serviceBinder = null;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_player_local,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        bindViewComponents(view);
        subscribeToViewComponents();
        registerSurfaceLayoutListener();
    }

    @Override
    public void onStop() {
        stopPlayback();

        super.onStop();
    }

    private void subscribeToViewComponents() {
        componentControls.registerCallback(this);
    }

    @Override
    public void onDestroyView() {
        stopPlayback();
        unregisterSurfaceLayoutListener();

        super.onDestroyView();
    }

    private void bindViewComponents(View view) {
        componentControls = view.findViewById(R.id.component_player);
        surfaceMedia = view.findViewById(R.id.surface_media);
        surfaceSubtitle = view.findViewById(R.id.surface_subtitle);
    }

    private void unregisterSurfaceLayoutListener() {
        surfaceMedia.removeOnLayoutChangeListener(surfaceLayoutListener);
    }

    private void registerSurfaceLayoutListener() {
        surfaceLayoutListener = (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (serviceBinder != null && (left != oldLeft || top != oldTop || right != oldRight && bottom != oldBottom)) {
                serviceBinder.onSurfaceChanged(v.getWidth(), v.getHeight());
            }
        };

        surfaceMedia.addOnLayoutChangeListener(surfaceLayoutListener);
    }

    private void startPlayback() {
        if (serviceBinder == null) {
            return;
        }

        serviceBinder.setCallback(this);
        serviceBinder.attachSurfaceViews(
                surfaceMedia,
                surfaceSubtitle
        );

        serviceBinder.setMedia(Uri.parse(SAMPLE_URL));

        if (resumeIsPlaying) {
            serviceBinder.play();
        }
    }

    private void stopPlayback() {
        if (serviceBinder == null) {
            return;
        }

        serviceBinder.stop();
        serviceBinder.detachSurfaceViews();
    }


    @Override
    public void onPlayerSeekStateChange(boolean canSeek) {
        super.onPlayerSeekStateChange(canSeek);

        if (!canSeek
                || serviceBinder == null) {
            return;
        }

        serviceBinder.setTime(resumeTime);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState == null) {
            return;
        }

        resumeIsPlaying = savedInstanceState.getBoolean(IsPlayingKey, true);
        resumeTime = savedInstanceState.getLong(TimeKey, 0);
        resumeLength = savedInstanceState.getLong(LengthKey, 0);

        configure(
                resumeIsPlaying,
                resumeTime,
                resumeLength
        );
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        final PlaybackStateCompat playbackState = getPlaybackState();

        outState.putBoolean(IsPlayingKey, playbackState.getState() == PlaybackStateCompat.STATE_PLAYING);
        outState.putLong(TimeKey, playbackState.getPosition());
        outState.putLong(LengthKey, playbackState.getBufferedPosition());

        super.onSaveInstanceState(outState);
    }
}
