package com.example.rescueconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int TYPES_OF_AGENCIES = 4;
    private static boolean[] searchFor = new boolean[TYPES_OF_AGENCIES];
    private static final String LOCATION_PERMISSION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String NOTIFICATION_PERMISSION = Manifest.permission.POST_NOTIFICATIONS;
    private static final int LOCATION_PERMISSION_REQ_CODE = 69;
    private static final int NOTIFICATION_PERMISSION_REQ_CODE = 96;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private LatLng currentLocation = new LatLng(0,0);

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("Nimish", "location granted");
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, LOCATION_PERMISSION)) {
            AlertDialog.Builder builder = new MaterialAlertDialogBuilder(this);
            builder.setMessage("Location permissions are required to show the rescue agencies near you.")
                    .setTitle("Permission Required")
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, which) -> {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{LOCATION_PERMISSION}, LOCATION_PERMISSION_REQ_CODE);
                    })
                    .setNegativeButton("EXIT", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MainActivity.this.finish();
                        }
                    });
            builder.show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{LOCATION_PERMISSION}, LOCATION_PERMISSION_REQ_CODE);
        }

        Log.d("Nimish", "I did nothing.");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannel();
        Resources resources = getResources();
        Drawable tickIcon;
        try {
            tickIcon = Drawable.createFromXml(resources,resources.getXml(R.drawable.done));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        }


        firebaseAuth = FirebaseAuth.getInstance();
        if ((user = firebaseAuth.getCurrentUser()) == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            MainActivity.this.finish();
        }


        requestLocation();
        requestNotificationPermission();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocation();
        }


        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isLocationEnabled()) {
            Toast.makeText(this,"Please enable location services",Toast.LENGTH_LONG).show();
            this.finish();
        }


        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                assert mapFragment != null;
                mapFragment.getMapAsync(MainActivity.this);
                ((TextView) findViewById(R.id.usernameview)).setText("Hello " + user.getDisplayName() + "!");
            }
        });

        setContentView(R.layout.activity_main);

        ((MaterialButton)(findViewById(R.id.hospitalButton))).addOnCheckedChangeListener(new MaterialButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(MaterialButton button, boolean isChecked) {
                searchFor[0] = !searchFor[0];
                if(isChecked){
                    button.setIcon(tickIcon);
                }
                else{
                    button.setIcon(null);
                }
            }
        });
        ((MaterialButton)(findViewById(R.id.fireButton))).addOnCheckedChangeListener(new MaterialButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(MaterialButton button, boolean isChecked) {
                searchFor[1] = !searchFor[1];

                if(isChecked){
                    button.setIcon(tickIcon);
                }
                else{
                    button.setIcon(null);
                }
            }
        });
        ((MaterialButton)(findViewById(R.id.waterButton))).addOnCheckedChangeListener(new MaterialButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(MaterialButton button, boolean isChecked) {
                searchFor[2] = !searchFor[2];

                if(isChecked){
                    button.setIcon(tickIcon);
                }
                else{
                    button.setIcon(null);
                }
            }
        });
        ((MaterialButton)(findViewById(R.id.animalButton))).addOnCheckedChangeListener(new MaterialButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(MaterialButton button, boolean isChecked) {
                searchFor[3] = !searchFor[3];

                if(isChecked){
                    button.setIcon(tickIcon);
                }
                else{
                    button.setIcon(null);
                }
            }
        });
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel("ALERT","Emergency Alerts", NotificationManager.IMPORTANCE_DEFAULT);
        (getSystemService(NotificationManager.class)).createNotificationChannel(channel);
    }

    private void requestNotificationPermission() {
        if(ActivityCompat.checkSelfPermission(this,NOTIFICATION_PERMISSION)==PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this,new String[]{NOTIFICATION_PERMISSION},NOTIFICATION_PERMISSION_REQ_CODE);
        else if (ActivityCompat.shouldShowRequestPermissionRationale(this, NOTIFICATION_PERMISSION))
            Toast.makeText(this,"Please enable notification permission",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode== LOCATION_PERMISSION_REQ_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location granted", Toast.LENGTH_LONG).show();
            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, LOCATION_PERMISSION)) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
                builder.setMessage("Location permissions are required to show the rescue agencies near you.")
                        .setTitle("Permission Required")
                        .setNegativeButton("EXIT", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        MainActivity.this.finish();
                                    }
                                }
                        )
                        .setCancelable(false);
                builder.show();
            } else {
                requestLocation();
            }
        }
    }

    public void signOut(View v){
        firebaseAuth.signOut();
        startActivity(new Intent(MainActivity.this,LoginActivity.class));
        MainActivity.this.finish();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        googleMap.addMarker(new MarkerOptions().position(currentLocation).title("Your location"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(16));
    }
}