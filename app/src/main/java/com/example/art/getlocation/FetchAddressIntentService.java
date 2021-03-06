package com.example.art.getlocation;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class FetchAddressIntentService extends IntentService {

    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME = "com.example.art.getlocation";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";
    protected ResultReceiver mReceiver;
    public FetchAddressIntentService() {
        super("FetchAddressIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String errorMessage = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        Location location = intent.getParcelableExtra(LOCATION_DATA_EXTRA);
        mReceiver = intent.getParcelableExtra(RECEIVER);
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(),1);
        } catch (IOException e) {
            errorMessage = "Service not available.";
            Log.e(TAG, errorMessage, e);
            e.printStackTrace();
        } catch (IllegalArgumentException illegal){
            // catch invalid long or lat
            errorMessage = "Invalid latitude and longitude";
            Log.e(TAG, errorMessage);
        }

        if(addresses == null || addresses.size() == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "No Address found";
                Log.e(TAG, errorMessage);
            }
            deliverResultsToReceiver(FAILURE_RESULT, errorMessage);
        }
        else{
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();
            // get and join address and send to main activity
            for(int i =0; i <= address.getMaxAddressLineIndex(); i++){
                addressFragments.add(address.getAddressLine(i));
            }
            Log.i(TAG,"Address found!");
            deliverResultsToReceiver(SUCCESS_RESULT, TextUtils.join(System.getProperty("line.separator"),addressFragments));
        }


    }

    private void deliverResultsToReceiver(int failureResult, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(RESULT_DATA_KEY, message);
        mReceiver.send(failureResult,bundle);
    }

}
