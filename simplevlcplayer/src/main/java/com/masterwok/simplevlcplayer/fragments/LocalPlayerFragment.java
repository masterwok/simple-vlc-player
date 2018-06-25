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
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.masterwok.simplevlcplayer.R;
import com.masterwok.simplevlcplayer.components.PlayerControlComponent;
import com.masterwok.simplevlcplayer.dagger.injectors.InjectableFragment;
import com.masterwok.simplevlcplayer.services.MediaPlayerService;

public class LocalPlayerFragment
        extends InjectableFragment implements PlayerControlComponent.Callback {

    private static final String SAMPLE_URL = "http://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_640x360.m4v";

    private View.OnLayoutChangeListener surfaceLayoutListener;
    private MediaPlayerService.Binder serviceBinder;

    private PlayerControlComponent componentControls;
    private SurfaceView surfaceSubtitle;
    private SurfaceView surfaceMedia;

    private MediaControllerCompat mediaController;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onStart() {
        super.onStart();

        bindMediaPlayerService();
    }

    @Override
    public void onStop() {
        unbindMediaPlayerService();

        super.onStop();
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
        registerSurfaceLayoutListener();
    }

    private void subscribeToViewComponents() {
        componentControls.registerCallback(this);
    }

    @Override
    public void onDestroyView() {
        stopPlayback();
        unregisterSurfaceLayoutListener();

        super.onDestroyView();
    }

    private void bindViewComponents(View view) {
        componentControls = view.findViewById(R.id.component_player);
        surfaceMedia = view.findViewById(R.id.surface_media);
        surfaceSubtitle = view.findViewById(R.id.surface_subtitle);
    }


    private void unbindMediaPlayerService() {
        Activity activity = getActivity();

        if (activity == null) {
            return;
        }

        activity.unbindService(mediaPlayerServiceConnection);
    }

    private void bindMediaPlayerService() {
        Activity activity = getActivity();

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


    private void unregisterSurfaceLayoutListener() {
        surfaceMedia.removeOnLayoutChangeListener(surfaceLayoutListener);
    }

    private void registerSurfaceLayoutListener() {
        surfaceLayoutListener = (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (serviceBinder != null
                    && (left != oldLeft
                    || top != oldTop
                    || right != oldRight
                    && bottom != oldBottom)) {
                serviceBinder.onSurfaceChanged(v.getWidth(), v.getHeight());
            }
        };

        surfaceMedia.addOnLayoutChangeListener(surfaceLayoutListener);
    }

    private void startPlayback() {
        if (serviceBinder == null) {
            return;
        }

        registerMediaController();
        serviceBinder.attachSurfaceViews(
                surfaceMedia,
                surfaceSubtitle
        );

        serviceBinder.setMedia(Uri.parse(SAMPLE_URL));
        serviceBinder.play();
    }

    private void stopPlayback() {
        if (serviceBinder == null) {
            return;
        }

        mediaController.unregisterCallback(controllerCallback);
        serviceBinder.detachSurfaceViews();
        serviceBinder.stop();
    }

    private ServiceConnection mediaPlayerServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            serviceBinder = (MediaPlayerService.Binder) iBinder;
            startPlayback();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            serviceBinder = null;
        }
    };


    private MediaControllerCompat.Callback controllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            if (state.getBufferedPosition() <= 0) {
                return;
            }

            componentControls.configure(state);
        }
    };

    public void registerMediaController() {
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
    public void onPlayPauseButtonClicked() {
        if (serviceBinder == null) {
            return;
        }

        serviceBinder.togglePlayback();
    }

    @Override
    public void onCastButtonClicked() {
        // TODO: Show renderer item dialog fragment.
    }

    @Override
    public void onProgressChanged(int progress) {
        if (serviceBinder == null) {
            return;
        }

        serviceBinder.setProgress(progress);
    }
}
