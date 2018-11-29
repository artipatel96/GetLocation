package com.example.art.getlocation;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.art.getlocation.CustomAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Calendar;

import static android.location.Location.distanceBetween;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final String PACKAGE_NAME = "com.example.art.getlocation";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";
    public static Location location;
    public static double longitude;
    public static double latitude;
    private String mAddressOutput;
    private TextView locationTv;
    private TextView addressTv;
    private GoogleApiClient googleApiClient;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private LocationRequest locationRequest;
    private static final long UPDATE_INTERVAL = 5000, FASTEST_INTERVAL = 5000;
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    private static final int ALL_PERMISSIONS_RESULT = 1011;
    ResultReceiver mResultReceiver;
    EditText custom_title;
    public static  DBHelper checkin_db;
    Button checkin_b;
    Button button_map;
    ListView listview;
    public static CustomAdapter adapter;
    public static  ArrayList<String> addressList;
    int count = 0;
    Switch switch1;
    int timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationTv = findViewById(R.id.location);
        addressTv = findViewById(R.id.address);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissionsToRequest = permissionsToRequest(permissions);
        checkin_db = new DBHelper(this);
        checkin_b = (Button)findViewById(R.id.button);
        custom_title = (EditText)findViewById(R.id.custom_label);
        listview = (ListView)findViewById(R.id.list_view);
        addressList = new ArrayList<>();
        adapter = new CustomAdapter(addressList,this);
        listview.setAdapter(adapter);
        button_map = (Button)findViewById(R.id.button_map);
        switch1 = (Switch)findViewById(R.id.switch1);
        timer = 0;


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(permissionsToRequest.size() > 0){
                requestPermissions(permissionsToRequest.
                toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
            }
        }

        googleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();

        Checkin5();
        viewAddressList();

        checkin_b.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                String time = calendar.getTime().toString();
                String title = custom_title.getText().toString();
                Checkin(title, time);
            }
        });

        button_map.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                intent.putExtra("latitude", location.getLatitude());
                intent.putExtra("longitude",location.getLongitude());
                startActivity(intent);
            }
        });

//        boolean switch_on = switch1.isChecked();
//        Location currLoc = location;
//        while(switch_on){
//
//        }

    }

    private ArrayList<String> permissionsToRequest(ArrayList<String> wantedPermissions){
        ArrayList<String> result = new ArrayList<>();

        for(String perm: wantedPermissions){
            if(!hasPermission(perm)){
                result.add(perm);
            }
        }

        Log.d("msg2", "Permissions to Request");

        return result;
    }

    private boolean hasPermission(String permission){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }

        Log.d("hasPermission", "Permissions to Request");

        return true;
    }

    @Override
    protected void onStart(){
        super.onStart();

        Log.d("onStart", "Permissions to Request");

        if(googleApiClient != null){
            googleApiClient.connect();
        }

    }

    @Override
    protected void onResume(){
        super.onResume();

        if(!checkPlayServices()){
            locationTv.setText("Install Google Play Services");
        }
    }

    @Override
    protected void onPause(){
        super.onPause();

        if(googleApiClient != null && googleApiClient.isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    private boolean checkPlayServices(){
        Log.d("checkservices", "Permissions to Request");
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if(resultCode != ConnectionResult.SUCCESS){
            if(apiAvailability.isUserResolvableError(resultCode)){
                apiAvailability.getErrorDialog(this,resultCode,PLAY_SERVICES_RESOLUTION_REQUEST);
            }else{
                finish();
            }
            return false;
        }

        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Log.d("on connected", "Permissions to Request");

        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }

        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if(location != null){
            Log.d("settingLocation", "Permissions to Request");
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            String txt = "Latitude: " + latitude + "\nLongitude: " + longitude;
            locationTv.setText(txt);

            if(!Geocoder.isPresent()){
                Toast.makeText(MainActivity.this,"No Geocoder present", Toast.LENGTH_SHORT).show();
                return;
            }

            startIntentService();

        }

        startLocationUpdates();
    }

    private void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "Enable Permissions to display Location", Toast.LENGTH_SHORT).show();
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }
    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        timer = timer + 5;
        if(location != null){
            this.location = location;
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            String txt = "Latitude: " + latitude + "\nLongitude: " + longitude;
            locationTv.setText(txt);
            startIntentService();

            Cursor c = checkin_db.getAllData();
            do {
                Double lat1 = Double.parseDouble(c.getString(2));
                Double long1 = Double.parseDouble(c.getString(3));
                float[] res = new float[1];
                distanceBetween(lat1,long1,latitude,longitude,res);
                if(res[0]<30 && res[0]!=0){
                    Toast.makeText(this, "You are within 30m of "+c.getString(1)+"\n Last Checkin: "+c.getString(5),Toast.LENGTH_LONG).show();
                }
                String time = Calendar.getInstance().getTime().toString();
                if(switch1.isChecked() && timer == 300){ // 5 min have passed
                    Checkin("Autocheckedin",time);
                    timer = 0;
                }
            }while(c.moveToNext());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestcode, @NonNull String[] permissions, @NonNull int[] grantResults){
        switch(requestcode){
            case ALL_PERMISSIONS_RESULT:
                for(String perm: permissionsToRequest){
                    if(!hasPermission(perm)){
                        permissionsRejected.add(perm);
                    }
                }

                if(permissionsRejected.size() > 0){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        if(shouldShowRequestPermissionRationale(permissionsRejected.get(0))){
                            new AlertDialog.Builder(MainActivity.this).
                                    setMessage("These permissions are mandatory to get your location. Allow them").
                                    setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]),
                                                ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    }).
                                    setNegativeButton("Cancel", null).create().show();
                            return;
                        }
                    }
                } else{
                    if(googleApiClient != null){
                        googleApiClient.connect();
                    }
                }
                break;
        }
    }

    private class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler){
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            mAddressOutput = resultData.getString(RESULT_DATA_KEY);
            displayAddressOutput();
        }

        private void displayAddressOutput() {
            addressTv.setText("Address: " + mAddressOutput);

        }
    }

    public void startIntentService(){
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        mResultReceiver = new AddressResultReceiver(new Handler());
        intent.putExtra(RECEIVER, mResultReceiver);
        intent.putExtra(LOCATION_DATA_EXTRA, location);
        startService(intent);
    }

    public boolean Checkin(String name, String time){
        Log.i("Checkin", name);
        Log.i("Checkin", latitude+"");
        Log.i("Checkin", longitude+"");
        Log.i("Checkin", mAddressOutput);
        Log.i("Checkin", time);

        Cursor c = checkin_db.getAllData();
        do {
            Double lat1 = Double.parseDouble(c.getString(2));
            Double long1 = Double.parseDouble(c.getString(3));
            float[] res = new float[1];
            distanceBetween(lat1,long1,latitude,longitude,res);
            if(res[0]<30 && res[0]!=0){
                Toast.makeText(this,"Location is within 30m of Existing Location: "+c.getString(1),Toast.LENGTH_SHORT).show();
                boolean i = checkin_db.insertData2(c.getInt(0),name,latitude+"",longitude+"",time);
                return i;
            }
        }while(c.moveToNext());

        boolean in = checkin_db.insertData(name,latitude+"",longitude+"",mAddressOutput,time);
        Log.i("Checkin",in+"");
        if(in==true){
            Toast.makeText(this,"Checked In at "+time, Toast.LENGTH_SHORT).show();
            viewAddressList();
        }
        return in;
    }

    private void Checkin5(){
        if(count == 0) {
            boolean b = checkin_db.insertData("Busch Student Center", "40.523411", "-74.458717", "604 Bartholomew Rd, Piscataway Township, NJ 08854, USA", "");
            boolean c = checkin_db.insertData("Sonny Werblin Recreation Center", "40.520390", "-74.457542", "656 Bartholomew Rd, Piscataway Township, NJ 08854, USA", "");
            boolean a = checkin_db.insertData("Buell Apartments", "40.522360", "-74.456010", "55 Bevier Rd, Piscataway Township, NJ 08854, USA", "");
            boolean d = checkin_db.insertData("Library of Science and Medicine", "40.525810", "-74.465050", "165 Bevier Rd, Piscataway Township, NJ 08854, USA", "");
            boolean e = checkin_db.insertData("Busch Engineering Science and Technology Hall", "40.522180", "-74.455920", "50 Bevier Rd, Piscataway Township, NJ 08854, USA", "");
        }
        count ++;
    }

    public static void viewAddressList(){
            addressList.clear();
            Cursor c = checkin_db.viewData();
            if(c.getString(0) != null) {
                do {
                    String name = c.getString(1);
                    String lat = c.getString(2);
                    String longi = c.getString(3);
                    String address = c.getString(4);
                    String time = c.getString(5);
                    String whole = name + "\n" + time + "\nLatitude: " + lat + "\nLongitude: " + longi + "\nAddress: " + address;
                    addressList.add(whole);
                } while (c.moveToNext());
                adapter.notifyDataSetChanged();
            }
        }
}
