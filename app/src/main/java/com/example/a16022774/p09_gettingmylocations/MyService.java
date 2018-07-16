package com.example.a16022774.p09_gettingmylocations;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileWriter;

public class MyService extends Service {

    boolean started;
    FusedLocationProviderClient client;
    LocationCallback mLocationCallback;
    String msg = "";
    String folderLocation;
    File folder;

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        client = LocationServices.getFusedLocationProviderClient(this);

        folderLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/locationFolder";
        folder = new File(folderLocation);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location data = locationResult.getLastLocation();
                    double lat = data.getLatitude();
                    double lng = data.getLongitude();
                    msg += "Lat: " + lat + " Lng: " + lng;

                    if(checkPermissionWrite() == true) {

                        if (folder.exists() == false) {
                            Boolean result = folder.mkdir();
                            if (result == true) {
                                Log.i("File Read/Write", "Folder created");
                            }
                        }else{
                            try{
                                File targetFile = new File(folderLocation, "locations.txt");
                                FileWriter writer = new FileWriter(targetFile, true);
                                writer.write(msg + "\n");
                                writer.flush();
                                writer.close();

                            }catch (Exception e){
                                Toast.makeText(MyService.this, "Failed to write!", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    }else{
                        Toast.makeText(MyService.this, "No permission", Toast.LENGTH_SHORT).show();

                    }

                    if(checkPermission() == true){
                        LocationRequest mLocationRequest = LocationRequest.create();
                        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        mLocationRequest.setInterval(10000);
                        mLocationRequest.setFastestInterval(5000);
                        mLocationRequest.setSmallestDisplacement(100);

                        client.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                    }else{
                        Toast.makeText(MyService.this, "Permission not granted", Toast.LENGTH_SHORT).show();
                    }
                }
            };
        };
        Log.d("Service", "Service created");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (started == false){
            started = true;

            Log.d("Service", "Service started");
        } else {
            Toast.makeText(this, "Service is running", Toast.LENGTH_SHORT).show();
            Log.d("Service", "Service is still running");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        client.removeLocationUpdates(mLocationCallback);
        Log.d("Service", "Service exited");
        super.onDestroy();
    }

    private boolean checkPermission() {
        int permissionCheck_Coarse = ContextCompat.checkSelfPermission(
                MyService.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck_Fine = ContextCompat.checkSelfPermission(
                MyService.this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck_Coarse == PermissionChecker.PERMISSION_GRANTED
                || permissionCheck_Fine == PermissionChecker.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkPermissionWrite() {
        int permissionCheck_Write = ContextCompat.checkSelfPermission(
                MyService.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck_Write == PermissionChecker.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }
}
