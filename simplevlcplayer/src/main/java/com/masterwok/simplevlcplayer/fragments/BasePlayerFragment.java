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
import android.support.v4.app.FragmentManager;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.provider.DocumentFile;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.masterwok.simplevlcplayer.R;
import com.masterwok.simplevlcplayer.common.utils.ResourceUtil;
import com.masterwok.simplevlcplayer.common.utils.ThreadUtil;
import com.masterwok.simplevlcplayer.common.utils.ViewUtil;
import com.masterwok.simplevlcplayer.components.PlayerControlComponent;
import com.masterwok.simplevlcplayer.contracts.MediaPlayer;
import com.masterwok.simplevlcplayer.dagger.injectors.InjectableFragment;
import com.masterwok.simplevlcplayer.services.MediaPlayerService;
import com.masterwok.simplevlcplayer.services.binders.MediaPlayerServiceBinder;


public abstract class BasePlayerFragment
        extends InjectableFragment
        implements PlayerControlComponent.Callback
        , MediaPlayer.Callback {

    public static final String MediaUri = "extra.mediauri";
    public static final String SubtitleUri = "extra.subtitleuri";
    public static final String OpenSubtitlesUserAgent = "extra.useragent";
    public static final String SubtitleDestinationUri = "extra.subtitledestinationuri";
    public static final String SubtitleLanguageCode = "extra.subtitlelanguagecode";

    protected MediaPlayerServiceBinder serviceBinder;
    private Uri subtitleDestinationUri;
    protected Uri subtitleUri;
    protected Uri mediaUri;
    private String openSubtitlesUserAgent;
    private String subtitleLanguageCode;

    private MediaControllerCompat mediaController;
    private ProgressBar progressBar;

    protected abstract void configure(
            boolean isPlaying,
            long time,
            long length
    );

    protected abstract void onConnected();

    protected abstract void onDisconnected();

    private ServiceConnection mediaPlayerServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BasePlayerFragment.this.serviceBinder = (MediaPlayerServiceBinder) iBinder;

            serviceBinder.setCallback(BasePlayerFragment.this);

            onConnected();

            registerMediaController(serviceBinder);
        }


        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            serviceBinder = null;

            onDisconnected();
        }
    };

    private MediaControllerCompat.Callback controllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
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
    public void onAttach(Context context) {
        super.onAttach(context);

        readIntent();
    }

    private void readIntent() {
        Intent intent = getActivity().getIntent();

        mediaUri = intent.getParcelableExtra(MediaUri);
        subtitleUri = intent.getParcelableExtra(SubtitleUri);
        subtitleDestinationUri = intent.getParcelableExtra(SubtitleDestinationUri);
        openSubtitlesUserAgent = intent.getStringExtra(OpenSubtitlesUserAgent);
        subtitleLanguageCode = intent.getStringExtra(SubtitleLanguageCode);
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
        serviceBinder = null;
    }

    private void bindMediaPlayerService() {
        getActivity().bindService(
                getMediaPlayerServiceIntent(),
                mediaPlayerServiceConnection,
                Context.BIND_AUTO_CREATE
        );
    }

    private Intent getMediaPlayerServiceIntent() {
        return new Intent(
                getActivity(),
                MediaPlayerService.class
        );
    }

    private void registerMediaController(MediaPlayerServiceBinder serviceBinder) {
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
    public void onSubtitlesButtonClicked() {
        FragmentManager fragmentManager = getFragmentManager();

        if (fragmentManager == null) {
            return;
        }

        String mediaName = DocumentFile
                .fromSingleUri(getContext(), mediaUri)
                .getName();

        SubtitlesDialogFragment
                .createInstance(
                        mediaName
                        , openSubtitlesUserAgent
                        , subtitleLanguageCode
                        , subtitleDestinationUri
                ).show(fragmentManager, SubtitlesDialogFragment.Tag);
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

        serviceBinder.setProgress(progress);
        serviceBinder.play();
    }

    @SuppressWarnings("ConstantConditions")
    private void initProgressBar() {
        final Context context = getContext();

        progressBar = new ProgressBar(
                context,
                null,
                android.R.attr.progressBarStyleLarge
        );

        progressBar.setVisibility(View.GONE);

        ViewUtil.setProgressBarColor(
                context,
                progressBar,
                R.color.progress_bar_spinner
        );

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ResourceUtil.getDimenDp(context, R.dimen.player_spinner_width),
                ResourceUtil.getDimenDp(context, R.dimen.player_spinner_height)
        );

        params.gravity = Gravity.CENTER;

        ((ViewGroup) getView()).addView(progressBar, params);
    }

    @Override
    public void onPlayerOpening() {
    }

    @Override
    public void onBuffering(float buffering) {
        if (buffering == 100f) {
            ThreadUtil.onMain(() -> progressBar.setVisibility(View.GONE));
            return;
        }

        if (progressBar.getVisibility() == View.VISIBLE) {
            return;
        }

        ThreadUtil.onMain(() -> progressBar.setVisibility(View.VISIBLE));
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
        //noinspection ConstantConditions
        getActivity().finish();
    }

    @Override
    public void onPlayerError() {
    }

    @Override
    public void onPlayerTimeChange(long timeChanged) {
    }

    @Override
    public void onPlayerPositionChanged(float positionChanged) {
    }

    @Override
    public void onSubtitlesCleared() {
    }

}
