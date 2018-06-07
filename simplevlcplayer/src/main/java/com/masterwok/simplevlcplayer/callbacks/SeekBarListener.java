package com.masterwok.simplevlcplayer.callbacks;

import android.widget.SeekBar;

import com.masterwok.simplevlcplayer.interfaces.ParamRunnable;


/**
 * This listener is responsible for listener to SeekBar changes and updating the
 * provided text view to the correct time.
 */
public class SeekBarListener implements SeekBar.OnSeekBarChangeListener {
    private final ParamRunnable<Float> onSeekBarUpdate;
    private boolean isTrackingTouch = false;

    public SeekBarListener(
            ParamRunnable<Float> onSeekBarUpdate
    ) {
        this.onSeekBarUpdate = onSeekBarUpdate;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (!isTrackingTouch || onSeekBarUpdate == null) {
            return;
        }

        onSeekBarUpdate.run((float) i / 100);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isTrackingTouch = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isTrackingTouch = false;
    }

//    /**
//     * Get the length of the media from the playback state.
//     *
//     * @return The length of the media in milliseconds.
//     */
//    private long getMediaLength() {
//        PlaybackStateCompat playbackState = mediaController.getPlaybackState();
//        Bundle extras = playbackState.getExtras();
//
//        return extras == null
//                ? 0
//                : extras.getLong(VlcMediaPlayerSession.LengthExtra);
//    }
//
//    @Override
//    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//        if (!isTrackingTouch) {
//            return;
//        }
//
//        long position = (long) (((float) i / 100) * getMediaLength());
//        String timeText = TimeUtil.getTimeString(position);
//
//        ThreadUtil.onMain(() -> textViewPosition.setText(timeText));
//    }
//
//    @Override
//    public void onStopTrackingTouch(SeekBar seekBar) {
//        float position = (float) seekBar.getProgress() / 100;
//
//        transportControls.seekTo((int) (position * getMediaLength()));
//
//        isTrackingTouch = false;
//    }
}
