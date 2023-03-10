package com.ethan.FamiCare;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class alarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String event = intent.getStringExtra("event");
        String time = intent.getStringExtra("time");

        if(!event.equals("") && !time.equals("")) {
            FCMsend.pushNotification(
                    context,
                    "APA91bEg-xO9Rlyb72AGxpt3wNoyKAYsA-9-fdbWKSNxyaG8qxz2syGfiwWVXoHLwZ2EIygaygZXGF19Ge1lL9h40NDhimvwoYJXJc37P2X3gWZDn7O0cA4",
                    event,
                    time
            );
        }

        NotificationHelper notificationHelper=new NotificationHelper(context);
        NotificationCompat.Builder nb=notificationHelper.notificationChannelBuild(event,time);
        PendingIntent contextIntent=PendingIntent.getActivity(context,0,new Intent(context,GroupCalendar.class),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        nb.setContentIntent(contextIntent);

        notificationHelper.getManager().notify(1,nb.build());

    }
}