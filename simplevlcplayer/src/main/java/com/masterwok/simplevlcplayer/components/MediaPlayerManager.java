package com.masterwok.simplevlcplayer.components;

import com.masterwok.simplevlcplayer.contracts.MediaPlayer;
import com.masterwok.simplevlcplayer.contracts.PlayerView;


public class MediaPlayerManager
        implements PlayerView.Callback
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
        view.updatePlaybackState();
    }

    @Override
    public void onProgressChanged(int progress) {
        mediaPlayer.setTime((long) ((float) progress / 100 * mediaPlayer.getLength()));
        view.updatePlaybackState();
    }

    @Override
    public void onPlayerOpening() {
        view.updatePlaybackState();
    }

    @Override
    public void onPlayerSeekStateChange(boolean canSeek) {
        view.updatePlaybackState();
    }

    @Override
    public void onPlayerPlaying() {
        view.updatePlaybackState();
    }

    @Override
    public void onPlayerPaused() {
        view.updatePlaybackState();
    }

    @Override
    public void onPlayerStopped() {
        view.updatePlaybackState();
    }

    @Override
    public void onPlayerEndReached() {
        view.updatePlaybackState();
    }

    @Override
    public void onPlayerError() {
        view.updatePlaybackState();
    }

    @Override
    public void onPlayerTimeChange(long timeChanged) {
        view.updatePlaybackState();
    }

    @Override
    public void onPlayerPositionChange(float positionChanged) {
        view.updatePlaybackState();
    }

}
