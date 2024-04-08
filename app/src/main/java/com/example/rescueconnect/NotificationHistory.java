package com.example.rescueconnect;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.messaging.RemoteMessage;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class NotificationHistory extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_history_activity);
        MaterialCardView notificationCard;
        NestedScrollView nestedScrollView = (NestedScrollView)findViewById(R.id.alerts_view);
        notificationCard = (MaterialCardView) getLayoutInflater().inflate(R.layout.notification_card,null);
        nestedScrollView.addView(notificationCard);
    }
}
