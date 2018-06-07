package com.masterwok.demosimplevlcplayer.callbacks;

import android.os.SystemClock;
import android.view.View;

import java.util.Map;
import java.util.WeakHashMap;


/**
 * This subclass of View.OnClickListener is responsible for de-bouncing taps
 * on view components.
 */
public abstract class DebouncedOnClickListener implements View.OnClickListener {

    private final long timeout;
    private Map<View, Long> lastClickMap;

    /**
     * This method is the same as onClick(..) but debounced. Debounced click logic
     * should be implemented here.
     *
     * @param v The clicked view.
     */
    public abstract void onDebouncedClick(View v);

    /**
     * Create a new DebouncedOnClickListener instance.
     *
     * @param timeout The debounce timeout.
     */
    public DebouncedOnClickListener(long timeout) {
        this.timeout = timeout;
        this.lastClickMap = new WeakHashMap<>();
    }

    @Override
    public final void onClick(View clickedView) {
        Long previousClickTimestamp = lastClickMap.get(clickedView);
        long currentTimestamp = SystemClock.uptimeMillis();

        lastClickMap.put(clickedView, currentTimestamp);

        if (previousClickTimestamp != null
                && Math.abs(currentTimestamp - previousClickTimestamp) <= timeout) {
            return;
        }

        onDebouncedClick(clickedView);
    }
}
