package com.masterwok.simplevlcplayer.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
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

    private MediaControllerCompat mediaController;
    private MediaPlayerService.Binder serviceBinder;

    protected abstract void configure(
            boolean isPlaying,
            long time,
            long length
    );

    protected abstract void onConnected(MediaPlayerService.Binder binder);

    protected abstract void onDisconnected();


    private ServiceConnection mediaPlayerServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BasePlayerFragment.this.serviceBinder = (MediaPlayerService.Binder) iBinder;

            onConnected(serviceBinder);

            registerMediaController(serviceBinder);
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

    protected PlaybackStateCompat getPlaybackState() {
        return mediaController.getPlaybackState();
    }

    @Override
    public void onPlayPauseButtonClicked() {
        if (serviceBinder == null) {
            return;
        }

        serviceBinder.togglePlayback();
    }

    @Override
    public void onCastButtonClicked() {
        FragmentManager fragmentManager = getFragmentManager();

        if (fragmentManager == null) {
            return;
        }

        new RendererItemDialogFragment().show(
                fragmentManager,
                RendererItemDialogFragment.Tag
        );
    }

    @Override
    public void onProgressChanged(int progress) {
        if (serviceBinder == null) {
            return;
        }

        serviceBinder.setProgress(progress);
    }

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
