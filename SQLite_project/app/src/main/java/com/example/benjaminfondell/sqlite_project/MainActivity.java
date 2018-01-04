package com.example.benjaminfondell.sqlite_project;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.location.Location;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.database.Cursor;
import android.content.ContentValues;
import android.util.Log;
import android.app.Dialog;
import android.view.Menu;
import android.view.MenuItem;



import com.example.benjaminfondell.sqlite_project.DatabaseContract.*;
import com.example.benjaminfondell.sqlite_project.DatabaseHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.Console;


public class MainActivity extends AppCompatActivity implements LocationListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private GoogleApiClient mApiClient;
    private LocationRequest mLocationRequest;
    private Location mBufferLocation = new Location("");
    private double nullLastLocation = 0.0;
    private Location osuLocation = new Location("44.502 -123.291");
    private static final int REQUEST_LOCATION = 1;
    private final String myTag = "Main Activity:";

    private LocationAdapter mLocationAdapter;
    private LocationListener mLocationListener;

    private SQLiteDatabase mLocationDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        if (mApiClient == null) {
            mApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5000);


        DatabaseHelper DatabaseHelper = new DatabaseHelper(this);
        mLocationDB = DatabaseHelper.getWritableDatabase();
        Cursor locationCursor = getAllLocations();

        RecyclerView locationRecyclerView;
        locationRecyclerView = (RecyclerView) this.findViewById(R.id.LocationList);
        mLocationAdapter = new LocationAdapter(this, locationCursor);
        locationRecyclerView.setAdapter(mLocationAdapter);
        locationRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],int[] grantResults){
        switch(requestCode){

            case REQUEST_LOCATION:{

                if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    getLocationPermission();
                }
                else {
                }
                return;
            }

            //more cases may be added here
        }
    }

    private void getLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient, mLocationRequest, this);
    }

    public void addLocation(){

        addLocationToDatabase(mBufferLocation);
        mLocationAdapter.IncrementCursor(getAllLocations());
    }

    private long addLocationToDatabase(Location location){

        EditText inputNameView = (EditText) findViewById(R.id.editText);
        String inName = inputNameView.getText().toString();

        if (location == null) return 0;
        double checkLatitude = location.getLatitude();
        if (location.getLatitude() == nullLastLocation)
        {
            ContentValues values = new ContentValues();
            values.put(LocationDBEntry.COLUMN_NAME,inName);
            values.put(LocationDBEntry.COLUMN_LATITUDE, 44.5);
            values.put(LocationDBEntry.COLUMN_LONGITUDE, -123.2);
            Log.i(myTag, "Default location added to database.");
            return mLocationDB.insert(LocationDBEntry.TABLE_NAME, null, values);

        }
        ContentValues values = new ContentValues();
        values.put(LocationDBEntry.COLUMN_NAME,inName);
        values.put(LocationDBEntry.COLUMN_LATITUDE, location.getLatitude());
        values.put(LocationDBEntry.COLUMN_LONGITUDE, location.getLongitude());
        String tester = String.format("%.3f %.3f", location.getLatitude(), location.getLongitude());
        Log.i(myTag,tester);

        Log.i(myTag, "Location added to database.");
        return mLocationDB.insert(LocationDBEntry.TABLE_NAME, null, values);
    }

    private Cursor getAllLocations(){
        return mLocationDB.query(
                LocationDBEntry.TABLE_NAME, null, null, null, null, null, LocationDBEntry._ID
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem itemClicked) {

        if (itemClicked.getItemId() == R.id.action_locate)
        {
            CheckBox rb1 = (CheckBox) findViewById(R.id.radioButton);
            if(rb1.isChecked())
            {
                addLocation();

            }
            else if    (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                getLocationPermission();
            }
            else addLocation();
        }

        return super.onOptionsItemSelected(itemClicked);
    }


    @Override
    public void onConnected( Bundle bundle) {
        Log.i(myTag, "Google Api Connected.");
    }
    @Override
    public void onConnectionSuspended(int i) { Log.i(myTag, "Google Api Suspended.");}
    @Override
    public void onConnectionFailed( ConnectionResult mResult) {
        Dialog errorDia = GoogleApiAvailability.getInstance().getErrorDialog(this, mResult.getErrorCode(), 0);
        errorDia.show();
    }
    @Override
    public void onLocationChanged(Location location) {

        if (location != null) mBufferLocation.set(location);
        else return;
    }
    @Override
    protected void onStart() {
        mApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mApiClient.disconnect();
        super.onStop();
    }

}


