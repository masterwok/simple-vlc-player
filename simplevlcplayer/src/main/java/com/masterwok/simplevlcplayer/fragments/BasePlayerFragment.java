package com.masterwok.simplevlcplayer.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.masterwok.simplevlcplayer.components.PlayerControlComponent;
import com.masterwok.simplevlcplayer.contracts.MediaPlayer;
import com.masterwok.simplevlcplayer.dagger.injectors.InjectableFragment;
import com.masterwok.simplevlcplayer.services.MediaPlayerService;

public abstract class BasePlayerFragment
        extends InjectableFragment
        implements PlayerControlComponent.Callback
        , MediaPlayer.Callback {

    private static final String IsPlayingKey = "bundle.isplaying";
    private static final String LengthKey = "bundle.length";
    private static final String TimeKey = "bundle.time";

    private MediaControllerCompat mediaController;

    protected boolean resumeIsPlaying = true;
    protected long resumeLength = 0;
    protected long resumeTime = 0;

    protected abstract void configure(
            boolean isPlaying,
            long time,
            long length
    );

    protected abstract void onConnected(MediaPlayerService.Binder binder);

    protected abstract void onDisconnected();


    @Override
    public void onStart() {
        super.onStart();

        bindMediaPlayerService();
    }

    @Override
    public void onStop() {
        unbindMediaPlayerService();
        mediaController.unregisterCallback(controllerCallback);

        super.onStop();
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
        final PlaybackStateCompat playbackState = mediaController.getPlaybackState();

        outState.putBoolean(IsPlayingKey, playbackState.getState() == PlaybackStateCompat.STATE_PLAYING);
        outState.putLong(TimeKey, playbackState.getPosition());
        outState.putLong(LengthKey, playbackState.getBufferedPosition());

        super.onSaveInstanceState(outState);
    }

    private void unbindMediaPlayerService() {
        final Activity activity = getActivity();

        if (activity == null) {
            return;
        }

        activity.unbindService(mediaPlayerServiceConnection);
    }

    private void bindMediaPlayerService() {
        final Activity activity = getActivity();

        if (activity == null) {
            return;
        }

        activity.bindService(
                new Intent(
                        activity.getApplicationContext(),
                        MediaPlayerService.class
                ),
                mediaPlayerServiceConnection,
                Context.BIND_AUTO_CREATE
        );
    }


    private ServiceConnection mediaPlayerServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            final MediaPlayerService.Binder serviceBinder = (MediaPlayerService.Binder) iBinder;

            onConnected(serviceBinder);
            registerMediaController(serviceBinder);
        }

        private void registerMediaController(MediaPlayerService.Binder serviceBinder) {
            final Activity activity = getActivity();

            if (activity == null) {
                return;
            }

            mediaController = new MediaControllerCompat(
                    activity,
                    serviceBinder.getMediaSession()
            );

            mediaController.registerCallback(controllerCallback);

            MediaControllerCompat.setMediaController(activity, mediaController);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            onDisconnected();
        }
    };

    private MediaControllerCompat.Callback controllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            if (state.getBufferedPosition() <= 0) {
                return;
            }

            configure(
                    state.getState() == PlaybackStateCompat.STATE_PLAYING,
                    state.getPosition(),
                    state.getBufferedPosition()
            );
        }
    };

    @Override
    public void onPlayerOpening() {

    }

    @Override
    public void onPlayerSeekStateChange(boolean canSeek) {

    }

    @Override
    public void onPlayerPlaying() {

    }

    @Override
    public void onPlayerPaused() {

    }

    @Override
    public void onPlayerStopped() {

    }

    @Override
    public void onPlayerEndReached() {

    }

    @Override
    public void onPlayerError() {

    }

    @Override
    public void onPlayerTimeChange(long timeChanged) {

    }

    @Override
    public void onPlayerPositionChange(float positionChanged) {

    }
}
