package com.masterwok.simplevlcplayer.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.masterwok.simplevlcplayer.R;
import com.masterwok.simplevlcplayer.components.PlayerControlComponent;
import com.masterwok.simplevlcplayer.contracts.MediaPlayer;
import com.masterwok.simplevlcplayer.contracts.RendererItemMediaPlayer;

import javax.inject.Inject;

public class RendererPlayerFragment
        extends BasePlayerFragment {

    private PlayerControlComponent componentControls;

    @Inject
    public RendererItemMediaPlayer player;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_player_renderer,
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
    }

    @Override
    protected MediaPlayer getPlayer() {
        return player;
    }

    @Override
    protected PlayerControlComponent getControls() {
        return componentControls;
    }


    private void bindViewComponents(View view) {
        componentControls = view.findViewById(R.id.component_player);
    }

}
