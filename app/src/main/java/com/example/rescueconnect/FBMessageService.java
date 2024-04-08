package com.example.rescueconnect;

import android.app.NotificationManager;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FBMessageService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {

        super.onMessageReceived(message);

        Instant notificationInstant = Instant.ofEpochSecond(message.getSentTime());

        ZonedDateTime notificationTime = notificationInstant.atZone(ZoneId.of("UTC+05:30"));

        String day,date,time;
        boolean isPM=false;
        int hour;

        day = notificationTime.getDayOfWeek().toString();

        date = notificationTime.getDayOfMonth() + " "+ notificationTime.getMonth().toString() + " "+ notificationTime.getYear();

        if((hour=notificationTime.getHour())>=12) {
            isPM = true;
            if(hour!=12)
                hour-=12;
        }

        time = hour + " : " + notificationTime.getMinute() + (isPM?" PM":" AM");

        Map<String,String> notification = new HashMap<>();
        notification.put("Title",message.getNotification().getTitle());
        notification.put("Body",message.getNotification().getBody());
        notification.put("Day",day);
        notification.put("Date",date);
        notification.put("Time",time);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notifications").add(notification);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "ALERT")
                .setContentText(Objects.requireNonNull(message.getNotification()).getBody())
                .setContentTitle(message.getNotification().getTitle())
                .setAutoCancel(true);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        NotificationManagerCompat.from(this).notify(1, notificationBuilder.build());
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(1,null);
    }
}
