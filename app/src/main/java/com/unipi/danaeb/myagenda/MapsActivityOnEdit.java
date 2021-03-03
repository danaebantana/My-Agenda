package com.unipi.danaeb.myagenda;

import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Arrays;

public class MapsActivityOnEdit extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Double lat,lon;
    String location;
    TextView textView_lat,textView_lon;
    EditText name;
    Button submit;
    private SQLiteDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_on_edit);
        name = findViewById(R.id.editTextName);
        submit = findViewById(R.id.saveLocatioBtn);
        textView_lat = findViewById(R.id.latTextView);
        textView_lon = findViewById(R.id.longTextView);

        db = openOrCreateDatabase("LocationsDB", Context.MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS Locations(name TEXT, lat DOUBLE, long DOUBLE)");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Create locations list from SQLite database and load it in location_spinner
        Spinner location_spinner = (Spinner) findViewById(R.id.location_spinner);
        Cursor cursor = db.rawQuery("SELECT * FROM Locations",null);
        StringBuilder builder = new StringBuilder();
        ArrayList<String> locations = new ArrayList<String>(Arrays.asList(new String[] {}));
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                builder.append(cursor.getString(0)).append(": ").append(cursor.getDouble(1)).append(", ").append(cursor.getDouble(2));
                locations.add(builder.toString());
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, locations); // Create an adapter to describe how the items are displayed.
        location_spinner.setAdapter(adapter);  // Set the spinners adapter to the previously created one.

        location_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Object item = location_spinner.getItemAtPosition(position);
                String[] obj = item.toString().split(":");
                String text = obj[0];
                name.setText(text);
                String latitude = obj[1].substring(1);
                textView_lat.setText(latitude);
                String longitude = obj[1].substring(2);
                textView_lon.setText(longitude);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }

        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = name.getText().toString();
                if (text.equals("")) {
                    Toast.makeText(getApplicationContext(), "Please add name for location", Toast.LENGTH_LONG).show();
                } else {
                    double longitude = Double.valueOf(textView_lon.getText().toString());
                    double latitude = Double.valueOf(textView_lat.getText().toString());
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


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Intent fromEditIntent = getIntent();
        lat = fromEditIntent.getDoubleExtra("latitude",0.0);
        lon = fromEditIntent.getDoubleExtra("longitude",0.0);
        location = fromEditIntent.getStringExtra("location");

        mMap = googleMap;

        LatLng marker = new LatLng(lat, lon);
        mMap.addMarker(new MarkerOptions().position(marker).title("Marker in Meeting Place"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker));

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
                textView_lat.setText(String.valueOf(latLng.latitude));
                textView_lon.setText(String.valueOf(latLng.longitude));
                //Clear the previously clicked position
                mMap.clear();
                //Zoom the Marker
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                //Add marker on map
                mMap.addMarker((markerOptions));

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