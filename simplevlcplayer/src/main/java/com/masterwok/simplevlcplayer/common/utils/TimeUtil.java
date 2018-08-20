package com.masterwok.simplevlcplayer.common.utils;

import java.util.Locale;
import java.util.concurrent.TimeUnit;


/**
 * This class provides static convenience methods for time.
 */
@SuppressWarnings("WeakerAccess")
public class TimeUtil {

    private TimeUtil() {
    }

    /**
     * Get the number of hours from milliseconds.
     *
     * @param milliseconds Time in milliseconds.
     * @return Time in hours.
     */
    public static long getHours(long milliseconds) {
        return TimeUnit.MILLISECONDS.toHours(milliseconds);
    }

    /**
     * Get the number of minutes from milliseconds.
     *
     * @param milliseconds Time in milliseconds.
     * @return Time in minutes.
     */
    public static long getMinutes(long milliseconds) {
        return TimeUnit.MILLISECONDS.toMinutes(milliseconds);
    }

    /**
     * Get the number of seconds from milliseconds.
     *
     * @param milliseconds Time in milliseconds.
     * @return Time in seconds.
     */
    public static long getSeconds(long milliseconds) {
        return TimeUnit.MILLISECONDS.toSeconds(milliseconds);
    }


    /**
     * Get a formatted timestamp from milliseconds.
     *
     * @param milliseconds Time in milliseconds.
     * @return A formatted timestamp.
     */
    public static String getTimeString(long milliseconds) {
        if (milliseconds < 0) {
            return "0";
        }

        long hours = getHours(milliseconds);

        milliseconds -= TimeUnit.HOURS.toMillis(hours);

        long minutes = getMinutes(milliseconds);

        milliseconds -= TimeUnit.MINUTES.toMillis(minutes);

        long seconds = getSeconds(milliseconds);

        if (hours == 0 && minutes == 0) {
            return String.format(
                    Locale.US,
                    "%d",
                    seconds
            );
        }

        if (hours == 0) {
            return String.format(
                    Locale.US,
                    "%d:%02d",
                    minutes,
                    seconds
            );
        }

        return String.format(
                Locale.US,
                "%d:%02d:%02d",
                hours,
                minutes,
                seconds
        );
    }
}