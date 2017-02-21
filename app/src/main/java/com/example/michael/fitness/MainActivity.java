package com.example.michael.fitness;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import static com.google.android.gms.location.Geofence.NEVER_EXPIRE;

/**
 * Main screen that shows all of the user's statistics
 */
public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private final int REQUEST_PERMISSION_ACCES_LOCATION = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private TextView mName;
    private TextView mTotalDistance;
    private TextView mDistanceToday;
    private TextView mTotalFeedback;
    private TextView mDailyFeedback;
    private Location mLastLocation;
    private TextView mLatitude;
    private TextView mLongitude;
    private TextView mLastUpdatedTime;
    private User user;
    private int mDay;
    private String mEmail;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private List mGeofenceList = new ArrayList();
    private PendingIntent mGeofencePendingIntent;
    String geoFenceId = "office";
    double latitude = 40.7302407;
    double longitude = -73.9999568;
    float geofenceRadius = 100;
    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        //get the email key for the logged in user
        Intent intent = getIntent();
        mEmail = intent.getStringExtra("email");
        SharedPreferences pref = getSharedPreferences(mEmail,this.MODE_PRIVATE);

        //create a new user from sharedPreferences using the email key
        user = new User(
                mEmail,
                pref.getString("password", ""),
                pref.getString("name", ""),
                pref.getInt("totalDistance", 0),
                pref.getInt("dailyDistance", 0),
                pref.getInt("milestone", 1000),
                pref.getInt("currentDay", 0)
        );

        mName = (TextView) findViewById(R.id.helloTextView);
        mTotalDistance = (TextView) findViewById(R.id.totalDistanceValue);
        mDistanceToday = (TextView) findViewById(R.id.distanceTodayValue);
        mTotalFeedback = (TextView) findViewById(R.id.totalFeedback);
        mDailyFeedback = (TextView) findViewById(R.id.dailyFeedback);
        mLatitude = (TextView) findViewById(R.id.latitude);
        mLongitude = (TextView) findViewById(R.id.longitude);
        mLastUpdatedTime = (TextView) findViewById(R.id.locationTime);

        //Determine if it's a new day
        if (user.getCurrentDay() != Calendar.getInstance().DAY_OF_YEAR) {
            user.setDistanceWalkedToday(0);
            user.setCurrentDay(Calendar.getInstance().DAY_OF_YEAR);
        }

        updateDisplay();

//        String geoFenceId = "office";
//        double latitude = 40.7302407;
//        double longitude = -73.9999568;
//        float geofenceRadius = 100;

//        mGeofenceList.add(new Geofence.Builder()
//                // Set the request ID of the geofence. This is a string to identify this
//                // geofence.
//                .setRequestId("office")
//
//                .setCircularRegion(
//                        latitude,
//                        longitude,
//                        geofenceRadius
//                )
//                .setExpirationDuration(NEVER_EXPIRE)
//                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
//                        Geofence.GEOFENCE_TRANSITION_EXIT)
//                .build());


        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        //add the location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        //check whether the current location settings are satisfied
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                int requestCode = 1;

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        break;

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(MainActivity.this, requestCode);
                        } catch (IntentSender.SendIntentException e) {
                            //Ignore the error
                        }
                        break;

                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_log_out) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Are you sure you want to log out?")
                    .setTitle("Log out?")
                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //user clicked ok
                            saveToPreference();
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();

        }

        return super.onOptionsItemSelected(item);
    }


    private void updateDisplay() {

        mName.setText("Hello, " + user.getName() + "!");
        mTotalDistance.setText(user.getDistanceWalked() + " ft");
        mDistanceToday.setText(user.getDistanceWalkedToday() + " ft");
        mDailyFeedback.setText("Try going for " + user.getMilestone() + " ft!");


    }

    private void updateLocationUi(Location location) {
        mLastUpdatedTime.setText(DateFormat.getTimeInstance().format(new Date()));
        mLatitude.setText(location.getLatitude() + "");
        mLongitude.setText(location.getLongitude() + "");

    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
        saveToPreference();
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        Log.v(TAG, "Connected!");
        locationPermission();

    }

    /**
     * Check for permissions for location services
     */
    private void locationPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Permission was never asked, so ask now
            Log.v(TAG, "Permission was never asked. Now ask!");
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSION_ACCES_LOCATION);
        } else {

            //use mLastLocation as our start point
            if (mLastLocation == null) {
                Log.v(TAG, "Location was null");
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                mLatitude.setText(mLastLocation.getLatitude() + "");
                mLongitude.setText(mLastLocation.getLongitude() + "");
            }

            //Permission has been asked
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
//            startGeofence();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_ACCES_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    locationPermission();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;

            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        saveToPreference();
    }

    /**
     * Save values from the user to sharedPreference
     */
    private void saveToPreference() {
        SharedPreferences sharedPref = this.getSharedPreferences(user.getEmail(), this.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("password", user.getPassword());
        editor.putString("name", user.getName());
        editor.putInt("totalDistance", user.getDistanceWalked());
        int daily = user.getDistanceWalkedToday();
        Log.v(TAG, daily + " is daily");
        editor.putInt("dailyDistance", daily);
        editor.putInt("milestone", user.getMilestone());
        editor.clear();
        editor.commit();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.v(TAG, "Location Changed!");
        //get distance from this location to last updated
        float distanceInMeters = location.distanceTo(mLastLocation);
        int distanceInFeet = (int) (distanceInMeters * 3.28);
        Log.v(TAG, "distance traveled: " + distanceInFeet + "");

        //update total distance and today's distance
        mDay = Calendar.getInstance().DAY_OF_YEAR;
        if (user.getCurrentDay() != mDay) {
            user.setDistanceWalkedToday(0);
            user.setCurrentDay(mDay);
        }

        user.setDistanceWalkedToday(user.getDistanceWalkedToday() + distanceInFeet);
        user.setDistanceWalked(user.getDistanceWalked() + distanceInFeet);
        //update locationUI and Display

        updateLocationUi(location);
        feedback(user.getDistanceWalked());
        updateDisplay();

        //replace last updated location with this location
        mLastLocation = location;

    }

    @Override
    public void onBackPressed() {
    }

    /**
     * Determine milestone feedback based off of distance traveled
     * @param totalDistance is the total distance the user traveled
     */
    public void feedback(int totalDistance) {
        if (totalDistance - user.getMilestone() > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("You just reached " + user.getMilestone() + " ft!")
                    .setTitle("New Milestone!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //user clicked ok
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();

            mTotalFeedback.setText("You just reached " + user.getMilestone() + " ft!");
            user.setMilestone(user.getMilestone()*2);
            mDailyFeedback.setText("Try going for " + user.getMilestone() + " ft!");

        }
    }


//    // Create a Geofence
//    private Geofence createGeofence() {
//        Log.d(TAG, "createGeofence");
//        return new Geofence.Builder()
//                .setRequestId(geoFenceId)
//                .setCircularRegion(latitude, longitude, geofenceRadius)
//                .setExpirationDuration(NEVER_EXPIRE)
//                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
//                        | Geofence.GEOFENCE_TRANSITION_EXIT)
//                .build();
//    }
//
//    // Create a Geofence Request
//    private GeofencingRequest createGeofenceRequest(Geofence geofence) {
//        Log.d(TAG, "createGeofenceRequest");
//        return new GeofencingRequest.Builder()
//                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
//                .addGeofence(geofence)
//                .build();
//    }
//
//    private PendingIntent createGeofencePendingIntent() {
//        Log.d(TAG, "createGeofencePendingIntent");
//        if (geoFencePendingIntent != null)
//            return geoFencePendingIntent;
//
//        Intent intent = new Intent(this, GeofenceTransitionService.class);
//        return PendingIntent.getService(
//                this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//    }
//
//    // Add the created GeofenceRequest to the device's monitoring list
//    private void addGeofence(GeofencingRequest request) {
//        Log.d(TAG, "addGeofence");
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
//                    REQUEST_PERMISSION_ACCES_LOCATION);
//        } else {
//            LocationServices.GeofencingApi.addGeofences(
//                    mGoogleApiClient,
//                    request,
//                    createGeofencePendingIntent()
//            ).setResultCallback((ResultCallback<? super Status>) this);
//        }
//    }
//
//    // Start Geofence creation process
//    private void startGeofence() {
//        Log.i(TAG, "startGeofence()");
//        Geofence geofence = createGeofence();
//        GeofencingRequest geofenceRequest = createGeofenceRequest( geofence );
//        addGeofence( geofenceRequest );
//
//    }


}
