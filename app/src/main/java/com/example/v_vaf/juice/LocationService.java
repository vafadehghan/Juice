package com.example.v_vaf.juice;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


public class LocationService extends Service {
    private static final String TAG = "LocationService";
    private FusedLocationProviderClient mFusedLocationClient;
    LocationRequest mLocationRequest;
    String ip;
    int port;
    String name;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();
        ip = (String) extras.get("IP");
        name = (String) extras.get("Name");
        port = Integer.parseInt((String) extras.get("Port"));
        return super.onStartCommand(intent, flags, startId);

    }

    @Override

    public void onCreate() {
        super.onCreate();


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5000);

        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Log.d(TAG, "onLocationResult Long: " + location.getLongitude());
                    Log.d(TAG, "onLocationResult Lat: " + location.getLatitude());
                    new connectThread().execute(ip, port, location.getLatitude(), location.getLongitude(), name);
                }
            }
        };
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    static class connectThread extends AsyncTask<Object, Void, Void> {
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Object... param) {
            try {
                Log.d(TAG, "doInBackground: " + param[4]);
                Socket server = new Socket((String) param[0], (int) param[1]);
                Log.d(TAG, "connectToServer: " + server.toString());
                Log.d(TAG, "connectToServer: " + server.getRemoteSocketAddress());
                OutputStream outToServer = server.getOutputStream();
                DataOutputStream out = new DataOutputStream(outToServer);
                out.writeBytes(param[4] + "_" + param[2] + "_" + param[3] + "_");
                InputStream inFromServer = server.getInputStream();
            } catch (Exception e) {
                e.printStackTrace();

            }

            return null;
        }
    }

}