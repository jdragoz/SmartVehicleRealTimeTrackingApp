package com.aics.bscs.vehiclegpstrackingapp;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EmergencyAlertActivity extends AppCompatActivity{

    TextView txtName, txtLocation, txtPlateNum, txtVehicleColor, txtVehicleType;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference nameRef = database.getReference("VehicleInfo/DriverName");
    DatabaseReference locationRef = database.getReference("VehicleInfo/LastLocated");
    DatabaseReference plateNumberRef = database.getReference("VehicleInfo/PlateNumber");
    DatabaseReference vehicleColorRef = database.getReference("VehicleInfo/VehicleColor");
    DatabaseReference vehicleTypeRef = database.getReference("VehicleInfo/VehicleType");
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_alert);

        txtName = (TextView) findViewById(R.id.txt_drivers_name);
        txtLocation = (TextView) findViewById(R.id.txt_last_located);
        txtPlateNum = (TextView) findViewById(R.id.txt_plate_num);
        txtVehicleColor = (TextView) findViewById(R.id.txt_vehicle_color);
        txtVehicleType = (TextView) findViewById(R.id.txt_vehicle_type);


        showVehicleInfo();

    }

    public void onClick(View view)
    {
        switch(view.getId()){
            case R.id.btn_GPS_tracking:
            {
                Intent gpsTrackingScreen = new Intent(EmergencyAlertActivity.this,MapsActivity.class);
                startActivity(gpsTrackingScreen);
                break;
            }
            case R.id.btn_videos:
            {
                Intent videosScreen = new Intent(EmergencyAlertActivity.this,VideoActivity.class);
                startActivity(videosScreen);
                break;
            }
            case R.id.btn_stop_alert:
            {
                Intent goToMainScreen = new Intent(EmergencyAlertActivity.this,MainActivity.class);
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference stop = database.getReference("InterruptEmergency/EmergencyStatus");
                stop.setValue(0);
                startActivity(goToMainScreen);
                break;
            }
        }
    }

    private void showVehicleInfo()
    {

        nameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                txtName.setText(value);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        locationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                txtLocation.setText(value);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        plateNumberRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                txtPlateNum.setText(value);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        vehicleColorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                txtVehicleColor.setText(value);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        vehicleTypeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);

                txtVehicleType.setText(value);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}
