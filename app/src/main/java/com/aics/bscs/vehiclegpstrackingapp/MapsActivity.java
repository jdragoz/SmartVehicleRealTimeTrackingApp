package com.aics.bscs.vehiclegpstrackingapp;

import android.Manifest;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Pack200;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        RoutingListener{

    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currentLocationMarker;
    private Marker currentVehicleMarker;
    public static final int REQUEST_LOCATION_CODE = 99;
    double latVehicle, lonVehicle;
    double latPolA, lonPolA, latPolB, lonPolB, latPolC, lonPolC;
    LatLng currentVehicleLocation, currentDeviceLocation;
    private List<Polyline> polylines;
    TextView txtNearestPolStation;

    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        txtNearestPolStation = (TextView) findViewById(R.id.txt_nearest_police_station);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            checkLocationPermission();
        }

        polylines = new ArrayList<>();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    protected void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (client != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (client != null &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {//Listeners markers and other attributes
        mMap = googleMap;
        //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

//        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
//            buildGoogleApiClient();
//            mMap.setMyLocationEnabled(true);
//        }
        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        addVehicleMarkerToMap(googleMap);

    }

    protected synchronized  void buildGoogleApiClient(){
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        client.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        //locationRequest.setSmallestDisplacement(0.1F); //added
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //changed
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {

            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case REQUEST_LOCATION_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //if permission is granted
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        if(client == null){
                            buildGoogleApiClient();
                        }
                    }
                    mMap.setMyLocationEnabled(true);
                }
                else{ //permission is denied
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
                }
                return;
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
       lastLocation = location;
        if(currentLocationMarker != null){
            currentLocationMarker.remove();
        }

        currentDeviceLocation = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions deviceLocMarker = new MarkerOptions();
        deviceLocMarker.position(currentDeviceLocation).title("Your Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        currentLocationMarker = mMap.addMarker(deviceLocMarker);

        getRouteFromDevice(currentDeviceLocation);
    }

    private void getRouteFromDevice(LatLng currentDeviceLocation) {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(currentDeviceLocation, new LatLng(latVehicle, lonVehicle))
                .build();
        routing.execute();
    }


    public boolean checkLocationPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);

            }
            return false;
        }
        else
            return true;
    }


    private void addVehicleMarkerToMap(final GoogleMap mMap){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference vehicleLocation = database.getReference("VehicleGPS/RealTimeLocation");

        vehicleLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String str = dataSnapshot.getValue(String.class);
                //Toast.makeText(this,str,Toast.LENGTH_LONG).show();
                String[] separated = str.split(",");
                String sLatitude = separated[0].trim();
                String sLongitude = separated[1].trim();

                latVehicle = Double.parseDouble(sLatitude);
                lonVehicle = Double.parseDouble(sLongitude);

//                if(currentVehicleMarker != null){
//                    currentVehicleMarker.remove();
//                }

                currentVehicleLocation = new LatLng(latVehicle, lonVehicle);

                getRouteToVehicleMarker(currentVehicleLocation);
                calculateNearestPoliceStation();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getRouteToVehicleMarker(LatLng currentVehicleLocation) {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), currentVehicleLocation)
                .build();
        routing.execute();
    }


    @Override
    public void onRoutingFailure(RouteException e) {
//        if(e != null) {
//            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
//        }else {
//            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
//        }
    }

    @Override
    public void onRoutingStart() {
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(currentDeviceLocation);
        builder.include(currentVehicleLocation);
        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int padding = (int) (width*0.1);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        mMap.animateCamera(cameraUpdate);
        addMarkersToMap();


        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
                }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            //Toast.makeText(getApplicationContext(),"Distance: "+ ((route.get(i).getDistanceValue())/1000)+" Duration: "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingCancelled() {
    }

    private void eraseDirection(){
        for(Polyline line: polylines){
            line.remove();
        }
        polylines.clear();
    }

    private void addMarkersToMap(){

        if(currentVehicleMarker != null){
            currentVehicleMarker.remove();
        }

        MarkerOptions vehicleLocMarker = new MarkerOptions();
        vehicleLocMarker.position(currentVehicleLocation).title("Vehicle").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentVehicleMarker = mMap.addMarker(vehicleLocMarker);
    }

//    private void calculateNearestPolice()
//    {
//        float distance[] = new float[10];
//        Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(), ddLat, ddLong, distance);
//    }

    private void calculateNearestPoliceStation()
    {
        fetchPoliceLatLngData();
//        final String sNamePolA;
//        final String sNamePolB;
//        final String sNamePolC;
        float distancePolA[] = new float[10];
        float distancePolB[] = new float[10];
        float distancePolC[] = new float[10];

        Location.distanceBetween(latVehicle, lonVehicle, latPolA, lonPolA, distancePolA);
        Location.distanceBetween(latVehicle, lonVehicle, latPolB, lonPolB, distancePolB);
        Location.distanceBetween(latVehicle, lonVehicle, latPolC, lonPolC, distancePolC);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference namePolA = database.getReference("PoliceStationNames/PoliceA");
        final DatabaseReference namePolB = database.getReference("PoliceStationNames/PoliceB");
        final DatabaseReference namePolC = database.getReference("PoliceStationNames/PoliceC");

        if(distancePolA[0]<distancePolB[0] && distancePolA[0]<distancePolB[0])
        {
            namePolA.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String value = dataSnapshot.getValue(String.class);
                    txtNearestPolStation.setText("Nearest: " +value);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }else if(distancePolB[0]<distancePolA[0] && distancePolB[0]<distancePolC[0]){
            namePolB.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String value = dataSnapshot.getValue(String.class);
                    txtNearestPolStation.setText("Nearest: " +value);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }else{
            namePolC.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String value = dataSnapshot.getValue(String.class);
                    txtNearestPolStation.setText("Nearest: " +value);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


    }
    private void fetchPoliceLatLngData(){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference locationPolA = database.getReference("PoliceLocation/PoliceA");
        final DatabaseReference locationPolB = database.getReference("PoliceLocation/PoliceB");
        final DatabaseReference locationPolC = database.getReference("PoliceLocation/PoliceC");

        locationPolA.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String str = dataSnapshot.getValue(String.class);
                String[] separated = str.split(",");
                String sLatitude = separated[0].trim();
                String sLongitude = separated[1].trim();

                latPolA = Double.parseDouble(sLatitude);
                lonPolA = Double.parseDouble(sLongitude);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        locationPolB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String str = dataSnapshot.getValue(String.class);
                String[] separated = str.split(",");
                String sLatitude = separated[0].trim();
                String sLongitude = separated[1].trim();

                latPolB = Double.parseDouble(sLatitude);
                lonPolB = Double.parseDouble(sLongitude);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        locationPolC.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String str = dataSnapshot.getValue(String.class);
                String[] separated = str.split(",");
                String sLatitude = separated[0].trim();
                String sLongitude = separated[1].trim();

                latPolC = Double.parseDouble(sLatitude);
                lonPolC = Double.parseDouble(sLongitude);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

}
