package com.masterwok.simplevlcplayer.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.masterwok.simplevlcplayer.R;
import com.masterwok.simplevlcplayer.components.PlayerControlComponent;
import com.masterwok.simplevlcplayer.contracts.MediaPlayer;
import com.masterwok.simplevlcplayer.dagger.injectors.InjectableFragment;
import com.masterwok.simplevlcplayer.services.MediaPlayerService;
import com.masterwok.simplevlcplayer.utils.ResourceUtil;
import com.masterwok.simplevlcplayer.utils.ThreadUtil;


public abstract class BasePlayerFragment
        extends InjectableFragment
        implements PlayerControlComponent.Callback
        , MediaPlayer.Callback {

    private MediaControllerCompat mediaController;
    private MediaPlayerService.Binder serviceBinder;
    private ProgressBar progressBar;

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

            serviceBinder.setCallback(BasePlayerFragment.this);

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
        serviceBinder.setCallback(null);
        mediaController.unregisterCallback(controllerCallback);
        unbindMediaPlayerService();

        super.onStop();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        initProgressBar();
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

        if (activity == null || serviceBinder == null) {
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
    public void onPlayPauseButtonClicked() {
        if (serviceBinder == null) {
            return;
        }

        if (isProgressBarVisible()) {
            ThreadUtil.onMain(this::hideProgressBar);
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
    public void onProgressChangeStarted() {
        if (serviceBinder == null) {
            return;
        }

        serviceBinder.pause();
    }

    @Override
    public void onProgressChanged(int progress) {
        if (serviceBinder == null) {
            return;
        }

        ThreadUtil.onMain(this::showProgressBar);

        serviceBinder.setProgress(progress);
        serviceBinder.play();
    }

    @SuppressWarnings("ConstantConditions")
    private void initProgressBar() {
        progressBar = new ProgressBar(
                getContext(),
                null,
                android.R.attr.progressBarStyleLarge
        );

        progressBar.setVisibility(View.GONE);
        progressBar.setIndeterminateTintList(
                ColorStateList.valueOf(
                        ResourceUtil.getColor(
                                getContext(),
                                R.color.player_spinner
                        )));

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ResourceUtil.getDimenDp(getContext(), R.dimen.player_spinner_width),
                ResourceUtil.getDimenDp(getContext(), R.dimen.player_spinner_height)
        );

        params.addRule(RelativeLayout.CENTER_IN_PARENT);

        ((ViewGroup) getView()).addView(progressBar, params);
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }


    protected boolean isProgressBarVisible() {
        return progressBar.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onPlayerOpening() {
        ThreadUtil.onMain(this::showProgressBar);
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
        // Ensure the progress bar is hidden if the time changes.
        if (isProgressBarVisible()) {
            ThreadUtil.onMain(this::hideProgressBar);
        }
    }

    @Override
    public void onPlayerPositionChange(float positionChanged) {

    }
}
