package com.unipi.danaeb.myagenda;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.location.Location;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class MapsActivity2 extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Double curr_lon, curr_lat = 0.0;
    Button selectBtn;
    TextView lat , lon;
    EditText name;
    private FusedLocationProviderClient locationProviderClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        lat = findViewById((R.id.latTextView));
        lon = findViewById(R.id.longTextView);
        selectBtn = findViewById(R.id.saveLocatioBtn);
        name = findViewById(R.id.editTextName);
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLocationGPS();

        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = name.getText().toString();
                if (text.equals("")) {
                    Toast.makeText(getApplicationContext(), "Please add name for location", Toast.LENGTH_LONG).show();
                } else {
                    double longitude = Double.valueOf(lon.getText().toString());
                    double latitude = Double.valueOf(lat.getText().toString());
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("latitude", latitude);
                    returnIntent.putExtra("longitude", longitude);
                    returnIntent.putExtra("name", text);
                    setResult(123, returnIntent);
                    finish();
                }

            }
        });
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
        // Add a marker in Current location and move the camera
        if(curr_lat != 0){
            LatLng curr = new LatLng(curr_lat, curr_lon);
            mMap.addMarker(new MarkerOptions().position(curr).title("Marker in Current Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(curr));
        }


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //Create marker
                MarkerOptions markerOptions = new MarkerOptions();
                //Set marker position
                markerOptions.position(latLng);
                //Set lat and long on marker
                markerOptions.title((latLng.latitude + " : " + latLng.longitude));
                //Set lat and long on textViews
                lat.setText(String.valueOf(latLng.latitude));
                lon.setText(String.valueOf(latLng.longitude));
                //Clear the previously clicked position
                mMap.clear();
                //Zoom the Marker
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                //Add marker on map
                mMap.addMarker((markerOptions));

            }
        });
    }

    //Gets last GPS location. That would be longitude and latitude
    private void getLocationGPS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationProviderClient.getLastLocation().addOnSuccessListener(MapsActivity2.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                String currentLocation = "";
                if (location != null) {
                    curr_lat = location.getLatitude();
                    curr_lon = location.getLongitude();

                } else {
                    curr_lat = 0.0;
                    curr_lon = 0.0;
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("latitude",0.0);
        returnIntent.putExtra("longitude",0.0);
        returnIntent.putExtra("name","None");
        setResult(123,returnIntent);
        finish();
    }
}