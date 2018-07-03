package com.masterwok.simplevlcplayer.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.masterwok.simplevlcplayer.R;

@SuppressWarnings("WeakerAccess")
public class NotificationUtil {

    /**
     * Get the notification manager system service.
     *
     * @param context The context used to get the service.
     * @return The notification manager system service.
     */
    public static NotificationManager getNotificationManager(Context context) {
        return context.getSystemService(NotificationManager.class);
    }

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

    public static Notification buildPlaybackNotification(
            Context context,
            MediaSessionCompat.Token token,
            String channelId,
            String title,
            String description,
            Bitmap cover,
            boolean isPlaying
    ) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context,
                channelId
        );

        builder.setSmallIcon(R.drawable.ic_play_arrow_black_36dp)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(title)
                .setContentText(description)
                .setLargeIcon(cover)
                .setTicker(title)
                .setAutoCancel(false)
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_SERVICE);

        if (isPlaying) {
            builder.addAction(R.drawable.ic_pause_black_36dp, "Pause", null);
        } else {
            builder.addAction(R.drawable.ic_play_arrow_black_36dp, "Play", null);
        }

        builder.addAction(R.drawable.ic_clear_black_36dp, "Stop", null);

        builder.setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(token)
                .setShowActionsInCompactView(0, 1)
        );

        return builder.build();
    }

}
