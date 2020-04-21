package com.example.golecdriverbigbasket;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.Locale;

public class ShowSlots extends AppCompatActivity {
    private TextView distance_from_warehouse, change_language;
    private Button slot1, slot2;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_slots);
        distance_from_warehouse = findViewById(R.id.textView);
        slot1 = findViewById(R.id.button2);
        slot2 = findViewById(R.id.button3);
        change_language = findViewById(R.id.textView6);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("drivers");

        sharedPreferences = getSharedPreferences("com.example.golecdriverbigbasket", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        String language = sharedPreferences.getString("set_language", "en");
        setAppLocale(language);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        checkLocationPermission();

        getWareHouseLocation();

        slot1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String distance_from_warehouse = sharedPreferences.getString("distance_from_warehouse", "1");
                if(Float.parseFloat(distance_from_warehouse)<=0.5){
                    Intent intent = new Intent(ShowSlots.this, DeliveredOrders.class);
                    intent.putExtra("slot", "slot1");
                    startActivity(intent);
                }
                else{
                    Toast.makeText(ShowSlots.this, "You are not in the area yet", Toast.LENGTH_SHORT).show();
                }
            }
        });

        slot2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String distance_from_warehouse = sharedPreferences.getString("distance_from_warehouse", "1");
                if(Float.parseFloat(distance_from_warehouse)<=0.5){
                    Intent intent = new Intent(ShowSlots.this, DeliveredOrders.class);
                    intent.putExtra("slot", "slot2");
                    startActivity(intent);
                }
                else{
                    Toast.makeText(ShowSlots.this, "You are not in the area yet", Toast.LENGTH_SHORT).show();
                }
            }
        });

        change_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAlertDialogBox();
            }
        });
    }


    private void getWareHouseLocation() {
        String driver_phone_number = sharedPreferences.getString("driver_phone_number", "+910");
        databaseReference.child(driver_phone_number).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String warehouse_latitude = dataSnapshot.child("Warehouse Latitude").getValue().toString();
                String warehouse_longitude = dataSnapshot.child("Warehouse Longitude").getValue().toString();
                calculateDistanceFromWarehouse(warehouse_latitude, warehouse_longitude);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void calculateDistanceFromWarehouse(String warehouse_lat, String warehouse_long) {
        Double driver_latitude = Double.parseDouble(sharedPreferences.getString("latitude", "0"));
        Double driver_longitude = Double.parseDouble(sharedPreferences.getString("longitude", "0"));
        Double warehouse_latitude = Double.parseDouble(warehouse_lat);
        Double warehouse_longitude = Double.parseDouble(warehouse_long);
        float results[] = new float[1];
        Location.distanceBetween(driver_latitude, driver_longitude,
                warehouse_latitude, warehouse_longitude, results);
        float distance_res = results[0]/1000;
        String reduced_distance = String.format("%.1f", distance_res);
        editor.putString("distance_from_warehouse", reduced_distance);
        editor.commit();
        distance_from_warehouse.setText("Distance from warehouse: "+reduced_distance+"km");
        refresh(2000);
    }

    private void refresh(int milliseconds) {
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                getWareHouseLocation();
            }
        };
        handler.postDelayed(runnable, milliseconds);
    }

    private void checkLocationPermission() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        Task<Location> task = fusedLocationProviderClient.getLastLocation();
                        task.addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if(location != null){
                                    currentLocation = location;
                                    editor.putString("latitude", Double.toString(currentLocation.getLatitude()));
                                    editor.putString("longitude", Double.toString(currentLocation.getLongitude()));

                                    editor.commit();
                                }
                            }
                        });
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
    }

    private void openAlertDialogBox() {
        final String[] supported_langs = {"English", "हिंदी", "मराठी"};
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(ShowSlots.this);
        mBuilder.setTitle("Select Language");
        mBuilder.setSingleChoiceItems(supported_langs, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0){
                    setAppLocale("en");
                    editor.putString("set_language", "en");
                    editor.commit();
                    recreate();
                }
                else if(which == 1){
                    setAppLocale("hi");
                    editor.putString("set_language", "hi");
                    editor.commit();
                    recreate();
                }
                else if(which == 2){
                    setAppLocale("mr");
                    editor.putString("set_language", "mr");
                    editor.commit();
                    recreate();
                }
                dialog.dismiss();
            }
        });
        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }
    //setting app language.
    private void setAppLocale(String localeCode) {
        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            configuration.setLocale(new Locale(localeCode.toLowerCase()));
        }
        else {
            configuration.locale = new Locale(localeCode.toLowerCase());
        }
        resources.updateConfiguration(configuration, displayMetrics);
    }
}
