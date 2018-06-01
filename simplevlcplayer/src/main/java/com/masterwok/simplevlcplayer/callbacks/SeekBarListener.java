package com.masterwok.simplevlcplayer.callbacks;

import android.os.Bundle;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.SeekBar;
import android.widget.TextView;

import com.masterwok.simplevlcplayer.sessions.VlcMediaPlayerSession;
import com.masterwok.simplevlcplayer.utils.ThreadUtil;
import com.masterwok.simplevlcplayer.utils.TimeUtil;


/**
 * This listener is responsible for listener to SeekBar changes and updating the
 * provided text view to the correct time.
 */
public class SeekBarListener implements SeekBar.OnSeekBarChangeListener {
    private final MediaControllerCompat mediaController;
    private final TextView textViewPosition;
    private final MediaControllerCompat.TransportControls transportControls;

    private boolean isTrackingTouch = false;

    /**
     * Get whether or not the user is currently sliding the SeekBar.
     *
     * @return If sliding, true. Else, false.
     */
    public boolean isTrackingTouch() {
        return isTrackingTouch;
    }

    public SeekBarListener(
            MediaControllerCompat mediaController,
            TextView textViewPosition
    ) {
        this.mediaController = mediaController;
        this.textViewPosition = textViewPosition;

        this.transportControls = mediaController.getTransportControls();
    }

    /**
     * Get the length of the media from the playback state.
     *
     * @return The length of the media in milliseconds.
     */
    private long getMediaLength() {
        PlaybackStateCompat playbackState = mediaController.getPlaybackState();
        Bundle extras = playbackState.getExtras();

        return extras == null
                ? 0
                : extras.getLong(VlcMediaPlayerSession.LengthExtra);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (!isTrackingTouch) {
            return;
        }

        long position = (long) (((float) i / 100) * getMediaLength());
        String timeText = TimeUtil.getTimeString(position);

        ThreadUtil.onMain(() -> textViewPosition.setText(timeText));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isTrackingTouch = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        float position = (float) seekBar.getProgress() / 100;

        transportControls.seekTo((int) (position * getMediaLength()));

        isTrackingTouch = false;
    }
}
