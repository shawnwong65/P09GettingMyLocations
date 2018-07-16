package com.example.a16022774.p09_gettingmylocations;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class MainActivity extends AppCompatActivity {

    private GoogleMap map;
    Button btnStart, btnStop, btnCheck;
    TextView tvLocation;
    SupportMapFragment mapFragment;
    FusedLocationProviderClient client;
    String folderLocation;
    File folder;
    Double lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLocation = (TextView) findViewById(R.id.tvLastLocation);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);
        btnCheck = (Button) findViewById(R.id.btnCheckRecords);

        folderLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/locationFolder";
        folder = new File(folderLocation);

        client = LocationServices.getFusedLocationProviderClient(this);


        FragmentManager fm = getSupportFragmentManager();
        mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;

                if (checkPermission() == true) {
                    Task<Location> task = client.getLastLocation();
                    task.addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {

                                lat = location.getLatitude();
                                lng = location.getLongitude();
                                tvLocation.setText("Last known when this activity started\nLatitude: " + lat + "\nLongitude: " + lng);
                                LatLng lastLocation = new LatLng(lat, lng);
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, 11));

                                Marker marker = map.addMarker(new
                                        MarkerOptions()
                                        .position(lastLocation)
                                        .title("")
                                        .snippet("")
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                                UiSettings uiSettings = map.getUiSettings();
                                uiSettings.setZoomControlsEnabled(true);

                            } else {
                                String msg = "No last known location found.";
                                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }else {
                    String msg = "Permission not granted to retrieve location info";
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                }


            }
        });


        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkPermission() == true) {
                    Intent i = new Intent(MainActivity.this, MyService.class);
                    startService(i);
                }else{
                    String msg = "Permission not granted to retrieve location info";
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MyService.class);
                stopService(i);
            }
        });
        
        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File targetFile = new File(folderLocation, "locations.txt");
                if (targetFile.exists() == true){
                    String data = "";
                    try{

                        FileReader reader = new FileReader(targetFile);
                        BufferedReader br = new BufferedReader(reader);

                        String line = br.readLine();
                        while(line != null){
                            data += line + "\n";
                            line = br.readLine();
                        }

                        Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();

                        br.close();
                        reader.close();

                    }catch (Exception e){
                        Toast.makeText(MainActivity.this, "Failed to read!", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    Log.i("Content", data);
                }
            }
        });


    }

    private boolean checkPermission() {
        int permissionCheck_Coarse = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck_Fine = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck_Coarse == PermissionChecker.PERMISSION_GRANTED
                || permissionCheck_Fine == PermissionChecker.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }
}
