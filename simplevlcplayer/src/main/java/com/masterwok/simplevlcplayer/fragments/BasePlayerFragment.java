package com.masterwok.simplevlcplayer.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;

import com.masterwok.simplevlcplayer.PlayerViewBinder;
import com.masterwok.simplevlcplayer.components.PlayerControlComponent;
import com.masterwok.simplevlcplayer.contracts.MediaPlayer;
import com.masterwok.simplevlcplayer.contracts.PlayerView;
import com.masterwok.simplevlcplayer.dagger.injectors.InjectableFragment;

public abstract class BasePlayerFragment
        extends InjectableFragment
        implements PlayerView {

    protected static final String SAMPLE_URL = "http://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_640x360.m4v";

    public static final String SimpleVlcSessionTag = "tag.simplevlcsession";

    protected abstract MediaPlayer getPlayer();

    protected abstract PlayerControlComponent getControls();

    private PlayerViewBinder playerViewBinder;
    private PlaybackStateCompat.Builder stateBuilder;
    private MediaControllerCompat mediaController;
    private MediaSessionCompat mediaSession;


    private MediaControllerCompat.Callback controllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            if (state.getBufferedPosition() <= 0) {
                return;
            }

            getControls().configure(state);
        }
    };

    private class PlayerSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            getPlayer().play();
        }

        @Override
        public void onPause() {
            getPlayer().pause();
        }

        @Override
        public void onSeekTo(final long pos) {
            getPlayer().setTime(pos);
        }
    }


    @Override
    public void registerCallback(Callback callback) {
        getControls().registerCallback(new PlayerControlComponent.Callback() {
            @Override

            public void onPlayPauseButtonClicked() {
                callback.togglePlayback();
            }

            @Override
            public void onCastButtonClicked() {
                new RendererItemDialogFragment().show(
                        getFragmentManager(),
                        RendererItemDialogFragment.Tag
                );
            }

            @Override
            public void onProgressChanged(int progress) {
                callback.onProgressChanged(progress);
            }
        });
    }

    @Override
    public void updatePlaybackState(
            boolean isPlaying,
            long length,
            long time
    ) {
        stateBuilder.setBufferedPosition(length);
        stateBuilder.setState(
                isPlaying
                        ? PlaybackStateCompat.STATE_PLAYING
                        : PlaybackStateCompat.STATE_PAUSED,
                time,
                1
        );

        mediaSession.setPlaybackState(stateBuilder.build());
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        stateBuilder = new PlaybackStateCompat
                .Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SEEK_TO)
                .setState(PlaybackStateCompat.STATE_PAUSED, 0L, 1);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        playerViewBinder = new PlayerViewBinder(
                getPlayer(),
                this
        );
    }

    @Override
    public void onStart() {
        super.onStart();

        attachMediaSession();
    }

    @Override
    public void onStop() {
        super.onStop();

        detachMediaSession();
    }

    public void detachMediaSession() {
        mediaSession.release();
        mediaController.unregisterCallback(controllerCallback);
    }

    public void attachMediaSession() {
        final Activity activity = getActivity();

        if (activity == null) {
            return;
        }

        mediaSession = new MediaSessionCompat(activity, SimpleVlcSessionTag);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setMediaButtonReceiver(null);
        mediaSession.setCallback(new PlayerSessionCallback());
        mediaSession.setPlaybackState(stateBuilder.build());

        mediaController = new MediaControllerCompat(activity, mediaSession);
        mediaController.registerCallback(controllerCallback);

        MediaControllerCompat.setMediaController(activity, mediaController);
    }

}
