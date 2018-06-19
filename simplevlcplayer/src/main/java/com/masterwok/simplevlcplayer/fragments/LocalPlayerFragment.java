package com.masterwok.simplevlcplayer.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.masterwok.simplevlcplayer.R;
import com.masterwok.simplevlcplayer.components.PlayerControlComponent;
import com.masterwok.simplevlcplayer.contracts.MediaPlayer;
import com.masterwok.simplevlcplayer.contracts.SurfaceMediaPlayer;

import javax.inject.Inject;

public class LocalPlayerFragment
        extends BasePlayerFragment {

    private static final String SAMPLE_URL = "http://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_640x360.m4v";

    @Inject
    public SurfaceMediaPlayer player;

    private View.OnLayoutChangeListener surfaceLayoutListener;

    private PlayerControlComponent componentControls;
    private SurfaceView surfaceSubtitle;
    private SurfaceView surfaceMedia;


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
        registerSurfaceLayoutListener();
    }


    @Override
    public void onDestroyView() {
        unregisterSurfaceLayoutListener();

        super.onDestroyView();
    }

    private void unregisterSurfaceLayoutListener() {
        surfaceMedia.removeOnLayoutChangeListener(surfaceLayoutListener);
    }

    private void registerSurfaceLayoutListener() {
        surfaceLayoutListener = (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (left != oldLeft || top != oldTop || right != oldRight && bottom != oldBottom) {
                player.onSurfaceChanged(v.getWidth(), v.getHeight());
            }
        };

        surfaceMedia.addOnLayoutChangeListener(surfaceLayoutListener);
    }

    @Override
    public void onStart() {
        super.onStart();

        player.attachSurfaces(
                surfaceMedia,
                surfaceSubtitle
        );
    }

    @Override
    public void onResume() {
        super.onResume();

        player.setMedia(Uri.parse(SAMPLE_URL));
        player.play();
    }

    @Override
    public void onStop() {
        player.detachSurfaces();

        super.onStop();
    }

    private void bindViewComponents(View view) {
        componentControls = view.findViewById(R.id.component_player);
        surfaceMedia = view.findViewById(R.id.surface_media);
        surfaceSubtitle = view.findViewById(R.id.surface_subtitle);
    }

    @Override
    protected MediaPlayer getPlayer() {
        return player;
    }

    @Override
    protected PlayerControlComponent getControls() {
        return componentControls;
    }
}
