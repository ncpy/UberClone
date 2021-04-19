package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PassengerActivity_Map extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private Button btnRequestCar, btnLogOutFromPassenger;
    private TextView tx_2;
    private boolean isUberCancelled = true;
    private boolean isCarReady = false;

    private Timer t;
    private TimerTask tt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger__map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnRequestCar = findViewById(R.id.btnRequestCar);
        btnRequestCar.setBackgroundColor(Color.GREEN);
        btnRequestCar.setOnClickListener(this);
        btnLogOutFromPassenger = findViewById(R.id.btnLogOutFromPassenger);
        btnLogOutFromPassenger.setOnClickListener(this);
        tx_2 = findViewById(R.id.tx_2);


        ParseQuery<ParseObject> carRequestQuery = ParseQuery.getQuery("RequestCar");
        carRequestQuery.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        carRequestQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {
                        isUberCancelled = false;
                        btnRequestCar.setText(R.string.cancel_uber_order);
                        btnRequestCar.setBackgroundColor(Color.YELLOW);

                        getDriverUpdates();
                    }
                } else {
                    Log.e("here", e.getMessage());
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                updateCameraPassengerLocation(location);
                Log.i("here car ready:", isCarReady+"");
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
            }
        };

        if (Build.VERSION.SDK_INT < 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        } else if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(PassengerActivity_Map.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(PassengerActivity_Map.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);

            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location currentPassengerLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateCameraPassengerLocation(currentPassengerLocation);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    private void updateCameraPassengerLocation(Location pLocation) {
        if (!isCarReady) {
            LatLng passengerLocation = new LatLng(pLocation.getLatitude(), pLocation.getLongitude());
            mMap.clear();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(passengerLocation, 15));
            mMap.addMarker(new MarkerOptions().position(passengerLocation).title("You are here!!!"));
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnRequestCar: {
                if (isUberCancelled) {  // means request uber and send my data
                    btnRequestCar.setBackgroundColor(Color.YELLOW);
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                        Location passengerCurrentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        if (passengerCurrentLocation != null) {

                            ParseObject requestCar = new ParseObject("RequestCar");
                            requestCar.put("username", ParseUser.getCurrentUser().getUsername());

                            ParseGeoPoint userLocation = new ParseGeoPoint(passengerCurrentLocation.getLatitude(), passengerCurrentLocation.getLongitude());
                            requestCar.put("passengerLocation", userLocation);

                            requestCar.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        Toast.makeText(PassengerActivity_Map.this, "A car request is sent.", Toast.LENGTH_SHORT).show();
                                        btnRequestCar.setText(R.string.cancel_uber_order);
                                        isUberCancelled = false;
                                        getDriverUpdates();
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(this, "Unknown Error. Something went wrong!!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else { // means cancel the order
                    ParseQuery<ParseObject> carRequestQuery = ParseQuery.getQuery("RequestCar");
                    carRequestQuery.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
                    carRequestQuery.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> requestList, ParseException e) {
                            if (requestList.size() > 0 && e == null) {
                                isUberCancelled = true;
                                isCarReady = false;
                                btnRequestCar.setText(R.string.request_uber);
                                btnRequestCar.setBackgroundColor(Color.GREEN);
                                tx_2.setText("");

                                for (ParseObject uberRequest : requestList) {
                                    uberRequest.deleteInBackground(new DeleteCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {
                                                tt.cancel();
                                                t.cancel();
                                                Toast.makeText(PassengerActivity_Map.this, "Request is deleted!", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });
                }
                break;
            }
            case R.id.btnLogOutFromPassenger: {
                tt.cancel();
                t.cancel();
                Toast.makeText(PassengerActivity_Map.this, ParseUser.getCurrentUser().getUsername() + " is logged out!", Toast.LENGTH_SHORT).show();
                ParseUser.logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            finish();
                        }
                    }
                });
            }
        }
    }

    private void getDriverUpdates() {
        t = new Timer();
        tt = new TimerTask() {
            @Override
            public void run() {
                //Log.v("here", "çalışıyorum");

                ParseQuery<ParseObject> uberRequestQuery = ParseQuery.getQuery("RequestCar");
                uberRequestQuery.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
                uberRequestQuery.whereEqualTo("requestAccepted", true);
                uberRequestQuery.whereExists("driverOfMe");

                uberRequestQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (objects.size() > 0 && e == null) {

                            isCarReady = true;
                            for (ParseObject requestObject : objects) {

                                ParseQuery<ParseUser> driverQuery = ParseUser.getQuery();
                                driverQuery.whereEqualTo("username", requestObject.getString("driverOfMe"));
                                driverQuery.findInBackground(new FindCallback<ParseUser>() {
                                    @Override
                                    public void done(List<ParseUser> drivers, ParseException e) {
                                        if (drivers.size() > 0 && e == null) {
                                            for (ParseUser driverOfRequest : drivers) {
                                                ParseGeoPoint driverOfRequestLocation = driverOfRequest.getParseGeoPoint("driverLocation");
                                                if (ContextCompat.checkSelfPermission(PassengerActivity_Map.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                                    Location passengerLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                                                    ParseGeoPoint pLocationAsParseGeoPoint = new ParseGeoPoint(passengerLocation.getLatitude(), passengerLocation.getLongitude());

                                                    double kilometresDistance = driverOfRequestLocation.distanceInKilometersTo(pLocationAsParseGeoPoint);
                                                    if (kilometresDistance < 0.1) {
                                                        requestObject.deleteInBackground(new DeleteCallback() {
                                                            @Override
                                                            public void done(ParseException e) {
                                                                if (e == null) {
                                                                    isCarReady = false;
                                                                    isUberCancelled = true;
                                                                    btnRequestCar.setText("You can order a new uber now!");
                                                                    btnRequestCar.setBackgroundColor(Color.GREEN);
                                                                    tx_2.setText("");

                                                                    AlertDialog.Builder builder = new AlertDialog.Builder(PassengerActivity_Map.this);
                                                                    builder.setTitle("Good News!");
                                                                    builder.setMessage("Your Uber is ready!!!");
                                                                    builder.setPositiveButton("OK", (dialog, which) -> {});
                                                                    builder.show();
                                                                }
                                                            }
                                                        });
                                                    } else {

                                                        float roundedDistance = (float) Math.round(kilometresDistance * 10) / 10;

                                                        //Toast.makeText(PassengerActivity_Map.this, requestObject.get("driverOfMe") + " is " + roundedDistance + " kilometers away from you!- Please wait!!!", Toast.LENGTH_SHORT).show();
                                                        tx_2.setText(requestObject.get("driverOfMe") + " is " + roundedDistance + " kilometers away from you!- Please wait!!!");

                                                        //
                                                        mMap.clear();
                                                        LatLng dLocation = new LatLng(driverOfRequestLocation.getLatitude(), driverOfRequestLocation.getLongitude());
                                                        LatLng pLocation = new LatLng(pLocationAsParseGeoPoint.getLatitude(), passengerLocation.getLongitude()); // pLocationAsParseGeoPoint or passengerLocation

                                                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                                        Marker driverMarker = mMap.addMarker(new MarkerOptions().position(dLocation).title("Driver Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                                        Marker passengerMarker = mMap.addMarker(new MarkerOptions().position(pLocation).title("Passenger Location"));

                                                        ArrayList<Marker> myMarker = new ArrayList<>();
                                                        myMarker.add(driverMarker);
                                                        myMarker.add(passengerMarker);

                                                        for (Marker marker : myMarker) {
                                                            builder.include(marker.getPosition());
                                                        }

                                                        LatLngBounds bounds = builder.build();
                                                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 80);
                                                        mMap.animateCamera(cameraUpdate);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                });
                            }
                        } else
                            isCarReady = false;
                    }
                });
            }
        };

        t.scheduleAtFixedRate(tt, 0, 3000);
    }
}