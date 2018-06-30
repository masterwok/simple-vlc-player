package com.masterwok.simplevlcplayer.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.masterwok.simplevlcplayer.R;
import com.masterwok.simplevlcplayer.components.PlayerControlComponent;
import com.masterwok.simplevlcplayer.services.MediaPlayerService;

public class RendererPlayerFragment
        extends BasePlayerFragment {

    private PlayerControlComponent componentControls;

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
    }

    private void subscribeToViewComponents() {
        componentControls.registerCallback(this);
    }

    private void bindViewComponents(View view) {
        componentControls = view.findViewById(R.id.component_player);
    }

    private void startPlayback() {
        if (serviceBinder == null) {
            return;
        }

        serviceBinder.setMedia(mediaUri);
        serviceBinder.play();
    }

}
