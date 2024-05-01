package com.example.rescueconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

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



    ////////////////////////////////////////////////////
    //  modifications on 29-04-2024                   //
    ////////////////////////////////////////////////////

    // DatabaseReference for Firebase Realtime Database
    private DatabaseReference databaseReference;

    // HashMap to store markers for hospitals
    private HashMap<String, Marker> hospitalMarkers = new HashMap<>();

    private GoogleMap googleMapVar; // Define the GoogleMap object




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

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getSupportActionBar()).hide();
        createNotificationChannel();
        Resources resources = getResources();
        Drawable tickIcon;
        try {
            tickIcon = Drawable.createFromXml(resources, resources.getXml(R.drawable.done));
        } catch (IOException | XmlPullParserException e) {
            throw new RuntimeException(e);
        }

        /////////////////////////////////////////////////////////////
        // Initialize Firebase Realtime Database reference
        //databaseReference = FirebaseDatabase.getInstance().getReference().child("hospital1");
        //////////////////////////////////////////////////////////



        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            MainActivity.this.finish();
        } else {

            AtomicReference<String> token = new AtomicReference<>();
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (task.isSuccessful())
                    token.set(task.getResult());
            });


            FirebaseMessaging.getInstance().unsubscribeFromTopic("test");
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("tokens").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot d : task.getResult().getDocuments()) {
                        if (Objects.equals(d.get("token"), token.get()))
                            return;
                    }
                    Map<String, String> tokenMap = new HashMap<>();
                    tokenMap.put("token", token.get());
                    db.collection("tokens").add(tokenMap);
                }
                else{
                    Log.d("Nimish","DATabase unreachable");
                }
            });
            requestLocation();
            requestNotificationPermission();

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestLocation();
            }


            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isLocationEnabled()) {
                Toast.makeText(this, "Please enable location services", Toast.LENGTH_LONG).show();
                this.finish();
            }


            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                        // Further operations with mapFragment
                        assert mapFragment != null;
                        mapFragment.getMapAsync(MainActivity.this);

                        try {
                            String locality = new Geocoder(MainActivity.this).getFromLocation(currentLocation.latitude, currentLocation.longitude, 5).get(0).getSubLocality();
                            String temp = ((TextView) findViewById(R.id.locality)).getText().toString();
                            ((TextView) findViewById(R.id.locality)).setText(String.format("%s%s", temp, locality));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        // Handle the case where location is null, maybe show an error message or retry obtaining the location
                    }

                }
            });

            setContentView(R.layout.activity_main);


//            ((MaterialButton) (findViewById(R.id.hospitalButton))).addOnCheckedChangeListener((button, isChecked) -> {
//                searchFor[0] = !searchFor[0];
//                if (isChecked) {
//                    button.setIcon(tickIcon);
//                } else {
//                    button.setIcon(null);
//                }
//            });

            ((MaterialButton) findViewById(R.id.hospitalButton)).addOnCheckedChangeListener((button, isChecked) -> {
                searchFor[0] = isChecked;

                if (isChecked) {
                    button.setIcon(tickIcon);


                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("hospitals");

                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot hospitalSnapshot : dataSnapshot.getChildren()) {
                                String hospitalName = hospitalSnapshot.child("name").getValue(String.class);
                                double latitude = hospitalSnapshot.child("latitude").getValue(Double.class);
                                double longitude = hospitalSnapshot.child("longitude").getValue(Double.class);

                                Log.d("FirebaseData", "Hospital Name: " + hospitalName);
                                Log.d("FirebaseData", "Latitude: " + latitude);
                                Log.d("FirebaseData", "Longitude: " + longitude);

                                // Add hospital marker to the map
                                Marker marker = googleMapVar.addMarker(new MarkerOptions()
                                        .position(new LatLng(latitude, longitude))
                                        .title(hospitalName));
                                hospitalMarkers.put(dataSnapshot.getKey(), marker);
                                googleMapVar.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
                                googleMapVar.moveCamera(CameraUpdateFactory.zoomTo(16));

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("FirebaseData", "Error fetching data", databaseError.toException());

                        }

                    });

                } else {
                    button.setIcon(null);
                    // Clear hospital markers from the map
                    // not working. Used clear() method instead
//                    for (Marker marker : hospitalMarkers.values()) {
//                        marker.remove();
//                    }
                    // Clear the hospitalMarkers HashMap
                    googleMapVar.clear();
                    //Focus camera back to user's current location
                    googleMapVar.addMarker(new MarkerOptions().position(currentLocation).title("Your location"));
                    googleMapVar.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
                    googleMapVar.moveCamera(CameraUpdateFactory.zoomTo(16));

                    hospitalMarkers.clear();
                }
            });







            // Find the report button by its ID
            MaterialButton reportButton = findViewById(R.id.reportButton);

            // Set a click listener for the report button
            reportButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Call a method to handle the report action
                    handleReport();
                }
            });


            ///////////////////////////////////////////


            ((MaterialButton) (findViewById(R.id.fireButton))).addOnCheckedChangeListener((button, isChecked) -> {
                searchFor[1] = !searchFor[1];

                if (isChecked) {
                    button.setIcon(tickIcon);
                } else {
                    button.setIcon(null);
                }
            });
            ((MaterialButton) (findViewById(R.id.waterButton))).addOnCheckedChangeListener((button, isChecked) -> {
                searchFor[2] = !searchFor[2];

                if (isChecked) {
                    button.setIcon(tickIcon);
                } else {
                    button.setIcon(null);
                }
            });
            ((MaterialButton) (findViewById(R.id.animalButton))).addOnCheckedChangeListener((button, isChecked) -> {
                searchFor[3] = !searchFor[3];

                if (isChecked) {
                    button.setIcon(tickIcon);
                } else {
                    button.setIcon(null);
                }
            });

            ((MaterialToolbar) findViewById(R.id.toolbar)).setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.alerts_button) {
                    Intent i = new Intent(MainActivity.this, NotificationHistory.class);
                    startActivity(i);
                    return true;
                }
                return false;
            });

            ((TextView) findViewById(R.id.usernameview)).setText(String.format("Hello %s!", user.getDisplayName()));
        }
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

        googleMapVar = googleMap;
    }









    //////////////////////////////////////////////////////////////

    private void handleReport() {
        // Get the current location
        double latitude = currentLocation.latitude;
        double longitude = currentLocation.longitude;

        // Create an array of options for the user to choose from
        final String[] reportOptions = {"Fire", "Water", "Animal", "Accident", "Other"};

        // Create an AlertDialog with the list of options
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Select report type");
        builder.setItems(reportOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String reportType = reportOptions[which];
                addReportToDatabase(latitude, longitude, reportType);
            }
        });
        builder.show();
    }

    private void addReportToDatabase(double latitude, double longitude, String reportType) {
        // Get a reference to the "reports" child in the database
        DatabaseReference reportsRef = FirebaseDatabase.getInstance().getReference().child("reports");

        // Create a new entry for the report
        String reportId = reportsRef.push().getKey();
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("latitude", latitude);
        reportData.put("longitude", longitude);
        reportData.put("type", reportType);

        // Add the new report to the database
        reportsRef.child(reportId).setValue(reportData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Report added successfully
                        Toast.makeText(MainActivity.this, "Report added successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to add report
                        Toast.makeText(MainActivity.this, "Failed to add report!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleReportTest() {
        // Get the current location
        double latitude = currentLocation.latitude;
        double longitude = currentLocation.longitude;

        // You can set the type of report based on your application logic
        String reportType = "Type of Report";

        // Get a reference to the "reports" child in the database
        DatabaseReference reportsRef = FirebaseDatabase.getInstance().getReference().child("reports");

        // Create a new entry for the report
        String reportId = reportsRef.push().getKey();
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("latitude", latitude);
        reportData.put("longitude", longitude);
        reportData.put("type", reportType);

        // Add the new report to the database
        reportsRef.child(reportId).setValue(reportData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Report added successfully
                        Toast.makeText(MainActivity.this, "Report added successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to add report
                        Toast.makeText(MainActivity.this, "Failed to add report!", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    //////////////////////////////////////////////////////////////


}