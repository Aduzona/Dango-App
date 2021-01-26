package com.example.trackingapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MessagingService extends FirebaseMessagingService {

    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification().getBody() != null) {
            Log.e("FIREBASE", "Message Notification Body: " + remoteMessage.getNotification().getBody());
            sendNotification(remoteMessage);
        }
    }

    private void sendNotification(RemoteMessage remoteMessage) {
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this).setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.d).setWhen(System.currentTimeMillis())
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getBody())
                .setAutoCancel(true)
                .setSound(defaultSoundUri).setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);


        String channelId = "Default";

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Default channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Dango");
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.setVibrationPattern(new long []{0,1000,500,1000});
            notificationManager.createNotificationChannel(channel);

        }
        notificationManager.notify(new Random().nextInt(), notificationBuilder.build());
    }


}
