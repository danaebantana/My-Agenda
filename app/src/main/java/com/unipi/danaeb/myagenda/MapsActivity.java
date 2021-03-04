package com.unipi.danaeb.myagenda;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

import java.util.ArrayList;
import java.util.Arrays;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient locationProviderClient;

    private SQLiteDatabase db;

    private Button selectBtn;
    private TextView lat , lon;
    private EditText name;
    private Spinner location_spinner;

    private Double curr_lon, curr_lat = 0.0, latitude, longitude;
    private String text;
    private ArrayList<String> locations;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        lat = findViewById((R.id.latTextView));
        lon = findViewById(R.id.longTextView);
        selectBtn = findViewById(R.id.saveLocatioBtn);
        name = findViewById(R.id.editTextName);

        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLocationGPS();

        db = openOrCreateDatabase("LocationsDB", Context.MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS Locations(name TEXT, lat DOUBLE, long DOUBLE)");

        //Create locations list from SQLite database and load it in location_spinner
        location_spinner = findViewById(R.id.location_spinner);
        Cursor cursor = db.rawQuery("SELECT * FROM Locations",null);
        locations = new ArrayList<String>(Arrays.asList(new String[] {}));
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                StringBuilder builder = new StringBuilder();
                builder.append(cursor.getString(0)).append(": ").append(cursor.getDouble(1)).append(", ").append(cursor.getDouble(2));
                locations.add(builder.toString());
            }
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, locations); // Create an adapter to describe how the items are displayed.
        location_spinner.setAdapter(adapter);  // Set the spinners adapter to the previously created one.

        location_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Object item = location_spinner.getItemAtPosition(position);
                String[] obj = item.toString().split(":");
                String text = obj[0];
                name.setText(text);
                String[] obj1 = obj[1].split(",");
                String latitude = obj1[0];
                lat.setText(latitude);
                String longitude = obj1[1];
                lon.setText(longitude);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }

        });

        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = name.getText().toString();
                if (text.equals("")) {
                    Toast.makeText(getApplicationContext(), "Please add name for location", Toast.LENGTH_LONG).show();
                } else {
                    latitude = Double.valueOf(lat.getText().toString());
                    longitude = Double.valueOf(lon.getText().toString());
                    if (!checkIfAlreadyInDatabase(text, latitude, longitude)) {
                        db.execSQL("INSERT INTO Locations VALUES('" + text + "','" + latitude + "', '" + longitude +"')");
                    }
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("latitude", latitude);
                    returnIntent.putExtra("longitude", longitude);
                    returnIntent.putExtra("location", text);
                    setResult(123, returnIntent);
                    finish();
                }
            }
        });
    }

    // Save location to database and print in spinner
    public void saveLocation(View view) {
        Spinner location_spinner = findViewById(R.id.location_spinner);
        text = name.getText().toString();
        if (text.equals("")) {
            Toast.makeText(getApplicationContext(), "Please add name for location", Toast.LENGTH_LONG).show();
        } else {
            latitude = Double.valueOf(lat.getText().toString());
            longitude = Double.valueOf(lon.getText().toString());
            if (!checkIfAlreadyInDatabase(text, latitude, longitude)) {
                db.execSQL("INSERT INTO Locations VALUES('" + text + "','" + latitude + "', '" + longitude + "')");
            }
        }
        StringBuilder builder = new StringBuilder();
        builder.append(text).append(": ").append(latitude).append(", ").append(longitude);
        adapter.add(builder.toString());
        adapter.notifyDataSetChanged();
        location_spinner.setSelection(adapter.getPosition(builder.toString()));
        Toast.makeText(MapsActivity.this, R.string.toast_LocationSave, Toast.LENGTH_LONG).show();
    }

    // Delete location from database and spinner
    public void deleteLocation(View view) {
        Object item = location_spinner.getSelectedItem();
        String[] obj = item.toString().split(":");
        String text = obj[0];
        String[] obj1 = obj[1].split(",");
        String latitude = obj1[0].substring(1);
        String longitude = obj1[1].substring(1);

        AlertDialog.Builder alert = new AlertDialog.Builder(MapsActivity.this);
        alert.setTitle("Delete entry");
        alert.setMessage("Are you sure you want to delete this location?");
        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                db.delete("Locations","name=? AND lat=? AND long=?",new String[]{text, String.valueOf(latitude), String.valueOf(longitude)});
                StringBuilder builder = new StringBuilder();
                builder.append(text).append(": ").append(latitude).append(", ").append(longitude);
                adapter.remove(builder.toString());
                adapter.notifyDataSetChanged();
                name.setText("");
                Toast.makeText(MapsActivity.this, R.string.toast_LocationDelete , Toast.LENGTH_LONG).show();
            }
        });
        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // close dialog
                dialog.cancel();
            }
        });
        alert.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Current location and move the camera
        if(curr_lat != 0){
            LatLng curr = new LatLng(curr_lat, curr_lon);
            mMap.addMarker(new MarkerOptions().position(curr).title("Marker in Current Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curr,10));

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
                // Clear edittext name and spinner
                name.setText("");
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
        locationProviderClient.getLastLocation().addOnSuccessListener(MapsActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    curr_lat = location.getLatitude();
                    curr_lon = location.getLongitude();
                    lat.setText(String.valueOf(curr_lat));
                    lon.setText((String.valueOf(curr_lon)));

                } else {
                    curr_lat = 0.0;
                    curr_lon = 0.0;
                }
                // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(MapsActivity.this);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("latitude",0.0);
        returnIntent.putExtra("longitude",0.0);
        returnIntent.putExtra("location","None");
        setResult(123,returnIntent);
        finish();
    }

    //If the given location is already in the database then the function returns true. Otherwise it returns false.
    private boolean checkIfAlreadyInDatabase(String n, Double lat, Double lon) {
        boolean flag = false;
        Cursor cursor = db.rawQuery("SELECT * FROM Locations",null);
        if (cursor.getCount()>0) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                Double latitude = cursor.getDouble(1);
                Double longitude = cursor.getDouble(2);
                if(n.equals(name) && lat.equals(latitude) && lon.equals(longitude)) {
                    flag = true;
                }
            }
        }
        return flag;
    }
}