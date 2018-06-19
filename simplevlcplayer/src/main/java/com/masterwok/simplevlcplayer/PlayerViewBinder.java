package com.masterwok.simplevlcplayer;

import com.masterwok.simplevlcplayer.contracts.MediaPlayer;
import com.masterwok.simplevlcplayer.contracts.PlayerView;


/**
 * This class is responsible for binding player view callbacks to the
 * media player, and media player callbacks to the view. This means that
 * player view callbacks directly control the media player, and media player
 * events are reflected in the display view.
 */
public class PlayerViewBinder
        implements PlayerView.Callback
        , MediaPlayer.Callback {

    private final MediaPlayer mediaPlayer;
    private final PlayerView view;

    public PlayerViewBinder(
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
    public void onPlayerOpening() {
        updatePlaybackState();
    }

    @Override
    public void onPlayerSeekStateChange(boolean canSeek) {
        updatePlaybackState();
    }

    @Override
    public void onPlayerPlaying() {
        updatePlaybackState();
    }

    @Override
    public void onPlayerPaused() {
        updatePlaybackState();
    }

    @Override
    public void onPlayerStopped() {
        updatePlaybackState();
    }

    @Override
    public void onPlayerEndReached() {
        updatePlaybackState();
    }

    @Override
    public void onPlayerError() {
        updatePlaybackState();
    }

    @Override
    public void onPlayerTimeChange(long timeChanged) {
        updatePlaybackState();
    }

    @Override
    public void onPlayerPositionChange(float positionChanged) {
        updatePlaybackState();
    }

    private void updatePlaybackState() {
        view.updatePlaybackState(
                mediaPlayer.isPlaying(),
                mediaPlayer.getLength(),
                mediaPlayer.getTime()
        );
    }


}
