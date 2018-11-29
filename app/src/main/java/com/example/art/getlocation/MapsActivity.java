package com.example.art.getlocation;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.SyncStateContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.example.art.getlocation.MainActivity.checkin_db;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final String PACKAGE_NAME = "com.example.art.getlocation";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";
    String mAddressOutput;
    private GoogleMap mMap;
    private Marker marker;
    private MarkerOptions markerOptions;
    private UiSettings mUiSettings;
    String title;
    ResultReceiver mResultReceiver;
    Location location;
    LatLng myCoordinates;
    LocationManager locationManager;
    ScheduledExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mUiSettings = mMap.getUiSettings();
        mMap.setMyLocationEnabled(true);
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setMyLocationButtonEnabled(true);
        mUiSettings.setScrollGesturesEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setTiltGesturesEnabled(true);
        mUiSettings.setRotateGesturesEnabled(true);
        mUiSettings.setCompassEnabled(true);
        location = new Location("");
        mAddressOutput = "";

        addMarkers();
        gotoCurrLocation();

        mMap.setOnMapClickListener(latLng-> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Input name for Location");

// Set up the input
            EditText input = new EditText(this);
            builder.setView(input);

// Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                   title = input.getText().toString();
                   location.setLatitude(latLng.latitude);
                   location.setLongitude(latLng.longitude);
                   //startIntentService();
                   String time = Calendar.getInstance().getTime().toString();
                   List<Address> addresses = null;

                    Geocoder g = new Geocoder(MapsActivity.this,Locale.getDefault());
                    try {
                        addresses = g.getFromLocation(latLng.latitude,latLng.longitude,1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(addresses!=null || addresses.size() != 0){
                        Address address = addresses.get(0);
                        ArrayList<String> addressFragments = new ArrayList<String>();
                        // get and join address and send to main activity
                        for(int i =0; i <= address.getMaxAddressLineIndex(); i++){
                            addressFragments.add(address.getAddressLine(i));
                        }
                        String x = TextUtils.join(System.getProperty("line.separator"),addressFragments);
                        Log.i("add",x);
                        MainActivity.checkin_db.insertData(title,latLng.latitude+"",latLng.longitude+"",x,time);
                    }

                    MainActivity.viewAddressList();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
            mMap.addMarker(new MarkerOptions().position(latLng).title(title));
        });

//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    public void gotoCurrLocation(){
        LatLng p = new LatLng(MainActivity.latitude,MainActivity.longitude);
        if(markerOptions == null){
            markerOptions = new MarkerOptions();
            markerOptions.title("Current");
            markerOptions.snippet("");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            markerOptions.position(new LatLng(MainActivity.latitude,MainActivity.longitude));
            marker = mMap.addMarker(markerOptions);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(p,50));
        }

        marker.setPosition(p);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(p,50));

    }

    public void addMarkers(){
        Cursor y = checkin_db.getAllData();
        while(y.moveToNext()){
            String name = y.getString(1);
            Double lat = Double.parseDouble(y.getString(2));
            Double longi = Double.parseDouble(y.getString(3));
            LatLng loc = new LatLng(lat,longi);
            mMap.addMarker(new MarkerOptions().position(loc).title(name));
        }
    }

//    public void currLocMarker(){
//        if(markerOptions == null){
//            markerOptions = new MarkerOptions();
//            markerOptions.title("Current");
//            markerOptions.snippet("");
//            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
//            markerOptions.position(new LatLng(MainActivity.latitude,MainActivity.longitude));
//            marker = mMap.addMarker(markerOptions);
//        }
//
//        marker.setPosition(new LatLng(MainActivity.latitude,MainActivity.longitude));
//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(MainActivity.latitude,MainActivity.longitude),50));
//    }

    Runnable getLoc = new Runnable() {
        @Override
        public void run() {
            LatLng p = new LatLng(MainActivity.latitude,MainActivity.longitude);
            mMap.addMarker(new MarkerOptions().position(p).title("current"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(p,50));
        }
    };

}
