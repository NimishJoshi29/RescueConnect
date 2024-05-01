package com.example.rescueconnect;

import android.graphics.Canvas;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NotificationHistory extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_history_activity);



        AtomicReference<LinearLayout> notificationCardLayout = new AtomicReference<>();
        NestedScrollView nestedScrollView = (NestedScrollView)findViewById(R.id.alerts_view);

        LinearLayout notificationsLayout = ((LinearLayout)(nestedScrollView.findViewById(R.id.notifications_layout)));

        LoadingAlertDialog loadingAlertDialog = new LoadingAlertDialog(this,"");
        loadingAlertDialog.startLoading();


        AtomicReference<List<DocumentSnapshot>> alerts = new AtomicReference<>();
        db.collection("alerts").get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                loadingAlertDialog.stopLoading();
                alerts.set(task.getResult().getDocuments());
                for(DocumentSnapshot d : alerts.get()) {
                    notificationCardLayout.set((LinearLayout) getLayoutInflater().inflate(R.layout.notification_card, null));

                    ((TextView)(notificationCardLayout.get().findViewById(R.id.alert_title))).setText((CharSequence) d.get("title"));
                    ((TextView)(notificationCardLayout.get().findViewById(R.id.alert_body))).setText((CharSequence) d.get("body"));
                    ((TextView)(notificationCardLayout.get().findViewById(R.id.alert_timestamp))).setText((CharSequence) d.get("time"));

                    notificationsLayout.addView(notificationCardLayout.get());
                }
                MaterialButton materialButton = new MaterialButton(this);
                materialButton.setText("Send Notification");
                materialButton.setOnClickListener(v -> {
                    NotificationPusher pusher = new NotificationPusher("Tapowan");
                    Thread t = new Thread(pusher);
                    t.start();
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if(pusher.isSuccessful()){
                        Toast.makeText(this,"Notification Sent.",Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(this,"Error.",Toast.LENGTH_SHORT).show();
                });
                notificationsLayout.addView(materialButton);
            }
        });



    }
}
