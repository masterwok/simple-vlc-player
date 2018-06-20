package com.masterwok.simplevlcplayer.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.masterwok.simplevlcplayer.R;
import com.masterwok.simplevlcplayer.components.PlayerControlComponent;
import com.masterwok.simplevlcplayer.contracts.MediaPlayer;
import com.masterwok.simplevlcplayer.contracts.RendererItemMediaPlayer;
import com.masterwok.simplevlcplayer.services.MediaPlayerService;

import javax.inject.Inject;

public class RendererPlayerFragment
        extends BasePlayerFragment {

    private PlayerControlComponent componentControls;

    @Inject
    public RendererItemMediaPlayer player;

    private MediaPlayerService.Binder serviceBinder;

    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(
                ComponentName componentName,
                IBinder iBinder
        ) {
            serviceBinder = (MediaPlayerService.Binder) iBinder;

            serviceBinder.setRendererMediaPlayer(player);
            player.setRendererItem(serviceBinder.getSelectedRendererItem());
            player.setMedia(Uri.parse(SAMPLE_URL));
            player.play();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            serviceBinder = null;
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        bindMediaPlayerService();
    }

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        final Activity activity = getActivity();

        if (activity == null) {
            return;
        }

        activity.unbindService(serviceConnection);
    }

    private void bindViewComponents(View view) {
        componentControls = view.findViewById(R.id.component_player);
    }

    private void bindMediaPlayerService() {
        final Activity activity = getActivity();

        if (activity == null) {
            return;
        }

        getActivity().bindService(
                new Intent(
                        activity.getApplicationContext(),
                        MediaPlayerService.class
                ),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );
    }
}
