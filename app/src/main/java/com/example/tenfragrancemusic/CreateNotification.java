package com.example.tenfragrancemusic;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class CreateNotification {
    public static final String CHANNEL_ID = "channel1";
    public static final String ACTION_PREVIOUS = "actionprevious";
    public static final String ACTION_NEXT = "actionnext";
    public static final String ACTION_PLAY = "actionplay";
    public static Notification notification;

    public static void createNotification(Context context, Activity activity, Song song, int pos, int size) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(context, "tag");
            Bitmap icon = song.getAlbum_art();

            PendingIntent pendingIntentPrevious;
            int drawPrevious = R.drawable.baseline_skip_previous_124;
            if(pos==0){
                pendingIntentPrevious = null;
            }else{
                Intent intentPrevious = new Intent(context, NotificationBroadcastReceiver.class).setAction(ACTION_PREVIOUS);
                pendingIntentPrevious = PendingIntent.getBroadcast(context, 0, intentPrevious, PendingIntent.FLAG_IMMUTABLE);
            }

            Intent intentPlay = new Intent(context, NotificationBroadcastReceiver.class).setAction(ACTION_PLAY);
            PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(context, 0, intentPlay, PendingIntent.FLAG_IMMUTABLE);
            int drawPlay = 0;
            if(MainActivity.musicControl.playing){
                drawPlay = R.drawable.baseline_pause_124;
            }else{
                drawPlay = R.drawable.baseline_play_arrow_124;
            }

            PendingIntent pendingIntentNext;
            int drawNext = R.drawable.baseline_skip_next_124;
            if(pos==size-1){
                pendingIntentNext = null;
            }else{
                Intent intentNext = new Intent(context, NotificationBroadcastReceiver.class).setAction(ACTION_NEXT);
                pendingIntentNext = PendingIntent.getBroadcast(context, 0, intentNext, PendingIntent.FLAG_IMMUTABLE);
            }

            notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.baseline_music_note_24)
                    .setContentTitle(song.getName())
                    .setContentText(song.getArtist())
                    .setLargeIcon(icon)
                    .setOnlyAlertOnce(true)
                    .setShowWhen(false)
                    .addAction(drawPrevious, "Previous", pendingIntentPrevious)
                    .addAction(drawPlay, "Play", pendingIntentPlay)
                    .addAction(drawNext, "Next", pendingIntentNext)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0, 1, 2)
                            .setMediaSession(mediaSessionCompat.getSessionToken()))
                    .setColorized(true)
                    .setSilent(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build();

            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.POST_NOTIFICATIONS},70);
            }else{notificationManagerCompat.notify(1, notification);}

        }
    }

}
