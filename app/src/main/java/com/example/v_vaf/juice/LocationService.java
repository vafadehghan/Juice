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
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.Socket;


public class LocationService extends Service {
    private static final String TAG = "LocationService";
    private FusedLocationProviderClient mFusedLocationClient;
    LocationRequest mLocationRequest;
    String ip;
    int port;
    String name;
    Socket server;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();
        ip = (String) extras.get("IP");
        name = (String) extras.get("Name");
        port = Integer.parseInt((String) extras.get("Port"));

        new connectThread().execute(ip, port);

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
                    new sendDataThread().execute(ip, port, location.getLatitude(), location.getLongitude(), name);
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

    class sendDataThread extends AsyncTask<Object, Void, Void> {
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Object... param) {
            try {
                OutputStream outToServer = server.getOutputStream();
                DataOutputStream out = new DataOutputStream(outToServer);
                out.writeBytes(param[4] + "_" + param[2] + "_" + param[3] + "_");
            } catch (Exception e) {
                e.printStackTrace();

            }

            return null;
        }
    }

    class connectThread extends AsyncTask<Object, Void, Object> {
        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Object doInBackground(Object... objects) {
            try {
                server = new Socket((String) objects[0], (int) objects[1]);
                Log.d(TAG, "connectToServer: " + server.getRemoteSocketAddress());
            } catch (Exception e) {
                e.printStackTrace();

            }
            return null;
        }
    }

}