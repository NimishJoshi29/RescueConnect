package com.example.rescueconnect;

import android.graphics.Canvas;
import android.os.Bundle;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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
            }
        });



    }
}
