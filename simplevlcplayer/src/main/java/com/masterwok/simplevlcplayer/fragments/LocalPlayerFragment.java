package com.masterwok.simplevlcplayer.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.masterwok.simplevlcplayer.R;
import com.masterwok.simplevlcplayer.components.MediaPlayerManager;
import com.masterwok.simplevlcplayer.components.PlayerControlComponent;
import com.masterwok.simplevlcplayer.contracts.MediaPlayer;
import com.masterwok.simplevlcplayer.contracts.PlayerView;

import javax.inject.Inject;

public class LocalPlayerFragment
        extends InjectableFragment
        implements PlayerView {

    public static final String SimpleVlcSessionTag = "tag.simplevlcsession";

    private PlaybackStateCompat.Builder stateBuilder;
    private MediaControllerCompat mediaController;
    private MediaSessionCompat mediaSession;


    @Inject
    public MediaPlayer mediaPlayer;

    private PlayerControlComponent componentControls;
    private SurfaceView surfaceSubtitle;
    private SurfaceView surfaceMedia;

    private MediaPlayerManager mediaPlayerManager;
    private View.OnLayoutChangeListener surfaceLayoutListener;


    private class PlayerSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            mediaPlayer.play();
        }

        @Override
        public void onPause() {
            mediaPlayer.pause();
        }

        @Override
        public void onSeekTo(final long pos) {
            mediaPlayer.setTime(pos);
        }
    }

    private MediaControllerCompat.Callback controllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            if (state.getBufferedPosition() <= 0) {
                return;
            }

            componentControls.configure(state);
        }
    };

    @Override
    public void registerCallback(Callback callback) {
        componentControls.registerCallback(new PlayerControlComponent.Callback() {
            @Override
            public void onPlayPauseButtonClicked() {
                callback.togglePlayback();
            }

            @Override
            public void onCastButtonClicked() {
                // TODO: Show cast device dialog.
            }

            @Override
            public void onProgressChanged(int progress) {
                callback.onProgressChanged(progress);
            }
        });
    }

    @Override
    public void updatePlaybackState() {
        stateBuilder.setBufferedPosition(mediaPlayer.getLength());
        stateBuilder.setState(
                mediaPlayer.isPlaying()
                        ? PlaybackStateCompat.STATE_PLAYING
                        : PlaybackStateCompat.STATE_PAUSED,
                mediaPlayer.getTime(),
                1
        );

        mediaSession.setPlaybackState(stateBuilder.build());
    }

    @Override
    public void setSurfaceSize(int width, int height) {
        SurfaceHolder holder = surfaceMedia.getHolder();
        holder.setFixedSize(width, height);

        ViewGroup.LayoutParams lp = surfaceMedia.getLayoutParams();
        lp.width = width;
        lp.height = height;
        surfaceMedia.setLayoutParams(lp);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        stateBuilder = new PlaybackStateCompat
                .Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SEEK_TO)
                .setState(PlaybackStateCompat.STATE_PAUSED, 0L, 1);
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

        mediaPlayerManager = new MediaPlayerManager(
                mediaPlayer,
                this
        );

        registerSurfaceLayoutListener();
    }

    @Override
    public void onDestroyView() {
        unregisterSurfaceLayoutListener();

        super.onDestroyView();
    }

    private void unregisterSurfaceLayoutListener() {
        surfaceMedia.removeOnLayoutChangeListener(surfaceLayoutListener);
    }

    private void registerSurfaceLayoutListener() {
        surfaceLayoutListener = (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (left != oldLeft || top != oldTop || right != oldRight && bottom != oldBottom) {
                mediaPlayerManager.surfaceChanged(v.getWidth(), v.getHeight());
            }
        };

        surfaceMedia.addOnLayoutChangeListener(surfaceLayoutListener);
    }

    @Override
    public void onStart() {
        super.onStart();

        attachMediaSession();
    }

    @Override
    public void onStop() {
        detachMediaSession();

        super.onStop();
    }

    private void bindViewComponents(View view) {
        componentControls = view.findViewById(R.id.component_player);
        surfaceMedia = view.findViewById(R.id.surface_media);
        surfaceSubtitle = view.findViewById(R.id.surface_subtitle);
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
