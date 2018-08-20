package com.masterwok.simplevlcplayer.common.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

/**
 * This class provides static convenience methods for Android notifications.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class NotificationUtil {

    /**
     * Get the notification manager system service.
     *
     * @param context The context used to get the service.
     * @return The notification manager system service.
     */
    public static NotificationManager getNotificationManager(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(NotificationManager.class);
        }

        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * Create a notification channel.
     *
     * @param context         The context used to get the notification manager.
     * @param channelId       The channel identifier.
     * @param channelName     The name of the channel.
     * @param enableSound     Whether or not to make a sound when the notification is shown.
     * @param enableLights    Whether or not to show a light when the notification is shown.
     * @param enableVibration Whether or not to enable vibration when the notification is shown.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void createNotificationChannel(
            Context context,
            String channelId,
            String channelName,
            boolean enableSound,
            boolean enableLights,
            boolean enableVibration
    ) {
        NotificationManager notificationManager = getNotificationManager(context);

        NotificationChannel channel = new NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
        );

        if (!enableSound) {
            channel.setSound(null, null);
        }

        channel.enableLights(enableLights);
        channel.enableVibration(enableVibration);

        //noinspection ConstantConditions
        notificationManager.createNotificationChannel(channel);
    }


}
