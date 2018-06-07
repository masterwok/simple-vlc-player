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

    public boolean isTrackingTouch() {
        return isTrackingTouch;
    }

}
