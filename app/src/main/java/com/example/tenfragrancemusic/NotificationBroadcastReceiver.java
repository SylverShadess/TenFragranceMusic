package com.example.tenfragrancemusic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.sendBroadcast(new Intent("TRACKS_TRACKS").putExtra("actionname", intent.getAction()));
    }
}