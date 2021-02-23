package com.unipi.danaeb.myagenda;

import androidx.fragment.app.FragmentActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity2 extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Button selectBtn;
    TextView lat,lon;
    EditText name;
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
        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = name.getText().toString();
                if(text.equals("")){
                    Toast.makeText(getApplicationContext(), "Please add name for location", Toast.LENGTH_LONG).show();
                }else{
                    double longitude = Double.valueOf(lon.getText().toString());
                    double latitude = Double.valueOf(lat.getText().toString());
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("latitude",latitude);
                    returnIntent.putExtra("longitude",longitude);
                    returnIntent.putExtra("name",text);
                    setResult(123,returnIntent);
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
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //Create marker
                MarkerOptions markerOptions = new MarkerOptions();
                //Set marker position
                markerOptions.position(latLng);
                //Set lat and long on marker
                markerOptions.title((latLng.latitude+" : "+latLng.longitude));
                //Set lat and long on textViews
                lat.setText(String.valueOf(latLng.latitude));
                lon.setText(String.valueOf(latLng.longitude));
                //Clear the previously clicked position
                mMap.clear();
                //Zoom the Marker
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));
                //Add marker on map
                mMap.addMarker((markerOptions));

            }
        });
    }
}