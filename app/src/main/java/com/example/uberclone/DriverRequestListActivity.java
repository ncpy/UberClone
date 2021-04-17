package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class DriverRequestListActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    Button btnGetRequests;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private ListView listView;
    private ArrayList<String> nearbyDriveRequests;
    private ArrayAdapter adapter;

    private ArrayList<Double> passengersLatitudes;
    private ArrayList<Double> passengersLongitudes;
    private ArrayList<String> requestCarUsernames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_request_list);

        setTitle("Driver: " + ParseUser.getCurrentUser().getUsername());

        btnGetRequests = findViewById(R.id.btnGetRequests);
        btnGetRequests.setOnClickListener(this);

        listView = findViewById(R.id.listview_request);
        nearbyDriveRequests = new ArrayList<>();
        passengersLatitudes = new ArrayList<>();
        passengersLongitudes = new ArrayList<>();
        requestCarUsernames = new ArrayList<>();
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, nearbyDriveRequests);
        listView.setAdapter(adapter);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    //updateRequestListView(location);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) { }

                @Override
                public void onProviderEnabled(@NonNull String provider) { }

                @Override
                public void onProviderDisabled(@NonNull String provider) { }

            };

        }

        listView.setOnItemClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_driver, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.item_driverLogOut) {
            ParseUser.logOutInBackground(new LogOutCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        finish();
                    }
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        if (Build.VERSION.SDK_INT < 23) {
            Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            updateRequestListView(currentDriverLocation);

        } else if (Build.VERSION.SDK_INT >= 23) {

            if (ContextCompat.checkSelfPermission(DriverRequestListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(DriverRequestListActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);

            } else {
                // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateRequestListView(currentDriverLocation);
            }
        }

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
//            return;
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
//
//        Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        updateRequestListView(currentDriverLocation);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            //Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            //updateRequestListView(currentDriverLocation);
        }
    }

    private void updateRequestListView(Location dlocation) {
        if (dlocation != null) {

            ParseGeoPoint driverCurrentLocation = new ParseGeoPoint(dlocation.getLatitude(), dlocation.getLongitude());
            ParseQuery<ParseObject> requestCarQuery = ParseQuery.getQuery("RequestCar");
            requestCarQuery.whereNear("passengerLocation", driverCurrentLocation);
            requestCarQuery.whereDoesNotExist("driverOfMe");
            requestCarQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        if (objects.size() > 0 && e == null) {

                            if (nearbyDriveRequests.size() > 0)
                                nearbyDriveRequests.clear();
                            if (passengersLatitudes.size() > 0)
                                passengersLatitudes.clear();
                            if (passengersLongitudes.size() > 0)
                                passengersLongitudes.clear();
                            if (requestCarUsernames.size() > 0)
                                requestCarUsernames.clear();

                            for (ParseObject nearRqst : objects) {

                                ParseGeoPoint pLocation = (ParseGeoPoint) nearRqst.get("passengerLocation");
                                Double kilometersDistanceToPassenger = driverCurrentLocation.distanceInKilometersTo(pLocation);
                                float roundedDistanceValue = (float) Math.round(kilometersDistanceToPassenger * 10) / 10;

                                nearbyDriveRequests.add("There are " + roundedDistanceValue + " kilometers to " + nearRqst.get("username"));

                                passengersLatitudes.add(pLocation.getLatitude());
                                passengersLongitudes.add(pLocation.getLongitude());

                                requestCarUsernames.add(nearRqst.get("username") + "");
                            }
                            adapter.notifyDataSetChanged();

                        } else {
                            Toast.makeText(DriverRequestListActivity.this, "Sorry :( There are no requests yet", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location cdLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (cdLocation != null) {
                Intent intent = new Intent(this, ViewLocationsMapActivity.class);
                intent.putExtra("dLatitude", cdLocation.getLatitude());
                intent.putExtra("dLongitude", cdLocation.getLongitude());
                intent.putExtra("pLatitude", passengersLatitudes.get(position));
                intent.putExtra("pLongitude", passengersLongitudes.get(position));
                intent.putExtra("rUsername", requestCarUsernames.get(position));
                startActivity(intent);
            }
        }
    }
}