package com.aics.bscs.vehiclegpstrackingapp;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.StringBuilderPrinter;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference policeAref = database.getReference("PoliceLocation/PoliceA");
    DatabaseReference policeBref = database.getReference("PoliceLocation/PoliceB");
    DatabaseReference policeCref = database.getReference("PoliceLocation/PoliceC");
    TextView txtPoliceA, txtPoliceB, txtPoliceC;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtPoliceA = (TextView) findViewById(R.id.txt_policeA);
        txtPoliceB = (TextView) findViewById(R.id.txt_policeB);
        txtPoliceC = (TextView) findViewById(R.id.txt_policeC);

        showPoliceInfo();
        showEmergencyStatus();
    }


    public void onClick(View view)
    {
        switch(view.getId()) {
            case R.id.btn_emergency_info: {
                Intent emergencyScreen = new Intent(MainActivity.this, EmergencyAlertActivity.class);
                startActivity(emergencyScreen);
                break;
            }
//            case R.id.btn_edit_police_info:
//            {
//
//            }
        }

    }


    private void showPoliceInfo()
    {

        policeAref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                txtPoliceA.setText(value);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        policeBref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                txtPoliceB.setText(value);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        policeCref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                txtPoliceC.setText(value);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showEmergencyStatus()
    {
        final ImageView img_Alert = (ImageView) findViewById(R.id.img_alert);
        final TextView txt_Alert = (TextView) findViewById(R.id.lbl_emergency_alert);
        final LinearLayout layout_BG = (LinearLayout) findViewById(R.id.layout_red_bg);
        final Button btn_show_info = (Button) findViewById(R.id.btn_emergency_info);

        DatabaseReference emergencyStatus = database.getReference("InterruptEmergency/EmergencyStatus");

        emergencyStatus.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                int status = dataSnapshot.getValue(Integer.class);

                if(status == 1)
                {
                    img_Alert.setVisibility(View.VISIBLE);
                    txt_Alert.setVisibility(View.VISIBLE);
                    layout_BG.setVisibility(View.VISIBLE);
                    btn_show_info.setVisibility(View.VISIBLE);
                }
                else
                {
                    img_Alert.setVisibility(View.INVISIBLE);
                    txt_Alert.setVisibility(View.INVISIBLE);
                    layout_BG.setVisibility(View.INVISIBLE);
                    btn_show_info.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

}