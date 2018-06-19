package com.masterwok.simplevlcplayer.components;

import com.masterwok.simplevlcplayer.contracts.MediaPlayer;
import com.masterwok.simplevlcplayer.contracts.PlayerView;


public class MediaPlayerManager
        implements com.masterwok.simplevlcplayer.contracts.MediaPlayerManager
        , PlayerView.Callback
        , MediaPlayer.Callback {

    private final MediaPlayer mediaPlayer;
    private final PlayerView view;

    public MediaPlayerManager(
            MediaPlayer mediaPlayer,
            PlayerView view

    ) {
        this.mediaPlayer = mediaPlayer;
        this.view = view;

        mediaPlayer.setCallback(this);
        view.registerCallback(this);
    }

    @Override
    public void togglePlayback() {
        mediaPlayer.togglePlayback();
        updatePlaybackState();
    }

    @Override
    public void onProgressChanged(int progress) {
        mediaPlayer.setTime((long) ((float) progress / 100 * mediaPlayer.getLength()));
        updatePlaybackState();
    }

    @Override
    public void onOpening() {
        updatePlaybackState();
    }

    @Override
    public void onSeekStateChange(boolean canSeek) {
        updatePlaybackState();
    }

    @Override
    public void onPlaying() {
        updatePlaybackState();
    }

    @Override
    public void onPaused() {
        updatePlaybackState();
    }

    @Override
    public void onStopped() {
        updatePlaybackState();
    }

    @Override
    public void onEndReached() {
        updatePlaybackState();
    }

    @Override
    public void onError() {
        updatePlaybackState();
    }

    @Override
    public void onTimeChange(long timeChanged) {
        updatePlaybackState();
    }

    @Override
    public void onPositionChange(float positionChanged) {
        updatePlaybackState();
    }

    @Override
    public void onUpdateSurfaceView(
            int width,
            int height
    ) {
        view.setSurfaceSize(width, height);
    }

    private void updatePlaybackState() {
        view.updatePlaybackState();
    }


    @Override
    public void surfaceChanged(int width, int height) {
        mediaPlayer.onSurfaceChanged(width, height);
    }
}
