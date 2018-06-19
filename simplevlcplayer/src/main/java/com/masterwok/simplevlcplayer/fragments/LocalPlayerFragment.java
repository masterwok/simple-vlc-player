package com.masterwok.simplevlcplayer.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.masterwok.simplevlcplayer.R;
import com.masterwok.simplevlcplayer.components.PlayerComponent;
import com.masterwok.simplevlcplayer.contracts.MediaPlayer;

import javax.inject.Inject;

public class LocalPlayerFragment
        extends InjectableFragment {

    @Inject
    public MediaPlayer mediaPlayer;

    private PlayerComponent componentPlayer;
    private SurfaceView surfaceSubtitle;
    private SurfaceView surfaceMedia;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_player_local, container, false);
    }

    // TODO: Should this be delegated to another class?
    private void togglePlayback() {
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        bindViewComponents(view);

        componentPlayer.init(this::togglePlayback);
    }

    private void bindViewComponents(View view) {
        componentPlayer = view.findViewById(R.id.component_player);
        surfaceMedia = view.findViewById(R.id.surface_media);
        surfaceSubtitle = view.findViewById(R.id.surface_subtitle);
    }
}
