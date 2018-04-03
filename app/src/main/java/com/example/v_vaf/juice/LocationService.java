package com.example.v_vaf.juice;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * ------------------------------------------------------------------------------------------------------------------
 * -- SOURCE FILE:		LocationService.java - The service that will connect to the server and transmit user location
 * --
 * -- PROGRAM:			Friend Locator
 * --
 * -- FUNCTIONS:		public int onStartCommand(Intent intent, int flags, int startId)
 * --					public void onCreate()
 * --                  public IBinder onBind(Intent intent)
 * --
 * -- INNER CLASSES:   sendDataThread
 * --                  connectThread
 * --
 * -- DATE:			April 1,2018
 * --
 * --
 * -- DESIGNER:		Vafa Dehghan Saei
 * --
 * -- PROGRAMMER:		Vafa Dehghan Saei
 * --
 * -- NOTES:			This service is the meat of the program. It will connect to the server and send the users location every 5 seconds.
 * ----------------------------------------------------------------------------------------------------------------------
 */
public class LocationService extends Service {
    private static final String TAG = "LocationService";
    LocationRequest mLocationRequest;
    String ip;
    int port;
    String name;
    Socket server;
    FusedLocationProviderClient mFusedLocationClient;
    LocationCallback mLocationCallback;


    /**
     * ------------------------------------------------------------------------------------------------------------------
     * -- FUNCTION:	onDestroy
     * --
     * -- DATE:		April 1,2018
     * --
     * --
     * -- DESIGNER:	    Vafa Dehghan Saei
     * --
     * -- PROGRAMMER:   Vafa Dehghan Saei
     * --
     * -- INTERFACE:	public void onDestroy()
     * --
     * --
     * --
     * -- RETURNS:		N/A
     * --
     * -- NOTES:		This function is called automatically when the service is destroyed. The method will destroy the socket and stop location updates.
     * --
     * ----------------------------------------------------------------------------------------------------------------------
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy: Destroy");
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
        if (server != null) {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------
     * -- FUNCTION:	onStartCommand
     * --
     * -- DATE:		April 1,2018
     * --
     * --
     * -- DESIGNER:	Vafa Dehghan Saei
     * --
     * -- PROGRAMMER:	Vafa Dehghan Saei
     * --
     * -- INTERFACE:	public int onStartCommand(Intent intent, int flags, int startId)
     * --					Intent intent: The intent that called this service
     * --					int flags: Additional data about this start request
     * --					int startId: A unique integer representing this specific start request
     * --
     * --
     * -- RETURNS:		The return value indicates what semantics the system should use for the service's current started state.
     * --
     * -- NOTES:		This function is called automatically when the server is called. It will pull the Ip, user name, and port from the calling Intent.
     * --
     * ----------------------------------------------------------------------------------------------------------------------
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Bundle extras = intent.getExtras();
            ip = (String) extras.get("IP");
            name = (String) extras.get("Name");
            port = Integer.parseInt((String) extras.get("Port"));
            new connectThread().execute(ip, port);
        }
        return super.onStartCommand(intent, flags, startId);

    }

    /**
     * ------------------------------------------------------------------------------------------------------------------
     * -- FUNCTION:	onCreate
     * --
     * -- DATE:		April 1,2018
     * --
     * --
     * -- DESIGNER:	Vafa Dehghan Saei
     * --
     * -- PROGRAMMER:	Vafa Dehghan Saei
     * --
     * -- INTERFACE:	public void onCreate()
     * --
     * --
     * -- RETURNS:		N/A
     * --
     * -- NOTES:		This function will create the LocationRequest object and start to receive user location and send to the sendDataThread.
     * --
     * ----------------------------------------------------------------------------------------------------------------------
     */
    @Override
    public void onCreate() {
        super.onCreate();
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View myView = inflater.inflate(R.layout.activity_main, null);
        Button mDisconectBtn = myView.findViewById(R.id.disconnectBtn);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5000);

        mLocationCallback = new LocationCallback() {
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


    /**
     * ------------------------------------------------------------------------------------------------------------------
     * -- FUNCTION:	onBind
     * --
     * -- DATE:		April 1,2018
     * --
     * --
     * -- DESIGNER:	Vafa Dehghan Saei
     * --
     * -- PROGRAMMER:	Vafa Dehghan Saei
     * --
     * -- INTERFACE:	public IBinder onBind(Intent intent)
     * --					Intent intent: The Intent that was used to bind to this service
     * --
     * --
     * -- RETURNS:	    Return an IBinder through which clients can call on to the service. Always null.
     * --
     * -- NOTES:		This function is called when the service is bound
     * --
     * ----------------------------------------------------------------------------------------------------------------------
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------
     * -- CLASS		    sendDataThread - This class will send the user location to the server
     * --
     * -- PROGRAM:			Friend Locator
     * --
     * -- FUNCTIONS:		protected Void doInBackground(Object... param)
     * --
     * --
     * -- DATE:			April 1,2018
     * --
     * --
     * -- DESIGNER:		Vafa Dehghan Saei
     * --
     * -- PROGRAMMER:		Vafa Dehghan Saei
     * --
     * -- NOTES:			This class is a thread that will run in the background and send user location to the server
     * ----------------------------------------------------------------------------------------------------------------------
     */
    @SuppressLint("StaticFieldLeak")
    class sendDataThread extends AsyncTask<Object, Void, Void> {

        /**
         * ------------------------------------------------------------------------------------------------------------------
         * -- FUNCTION:	doInBackground
         * --
         * -- DATE:		April 1,2018
         * --
         * --
         * -- DESIGNER:	Vafa Dehghan Saei
         * --
         * -- PROGRAMMER:	Vafa Dehghan Saei
         * --
         * -- INTERFACE:  protected Void doInBackground(Object... param)
         * --					Object... param: The data send to this thread from the service
         * --                         param[2]: Latitude
         * --                         param[3]: Longitude
         * --                         param[4]: Name
         * --
         * --
         * -- RETURNS:	N/A
         * --
         * -- NOTES:		This function will continuously send user location to the server.
         * --
         * ----------------------------------------------------------------------------------------------------------------------
         */
        @Override
        protected Void doInBackground(Object... param) {
            try {
                OutputStream outToServer = server.getOutputStream();
                DataOutputStream out = new DataOutputStream(outToServer);
                out.writeBytes(" " + param[4] + "_" + param[2] + "_" + param[3] + "_");
            } catch (Exception e) {
                e.printStackTrace();

            }

            return null;
        }
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------
     * -- CLASS		    connectThread - This class will connect the client to the server
     * --
     * -- PROGRAM:			Friend Locator
     * --
     * -- FUNCTIONS        protected void onPostExecute(Object o)
     * --                  protected Object doInBackground(Object... objects)
     * --
     * --
     * -- DATE:			April 1,2018
     * --
     * --
     * -- DESIGNER:		Vafa Dehghan Saei
     * --
     * -- PROGRAMMER:		Vafa Dehghan Saei
     * --
     * -- NOTES:			This class will connect the client to the server using Java Sockets
     * ----------------------------------------------------------------------------------------------------------------------
     */
    @SuppressLint("StaticFieldLeak")
    class connectThread extends AsyncTask<Object, Void, Object> {
        boolean success = true;

        /**
         * ------------------------------------------------------------------------------------------------------------------
         * -- FUNCTION:	onPostExecute
         * --
         * -- DATE:		April 1,2018
         * --
         * --
         * -- DESIGNER:	Vafa Dehghan Saei
         * --
         * -- PROGRAMMER:	Vafa Dehghan Saei
         * --
         * -- INTERFACE:	protected void onPostExecute(Object o)
         * --					Object o:  The result of the operation computed by doInBackground
         * --
         * --
         * -- RETURNS:		N/A
         * --
         * -- NOTES:		This function will display a toast to the user once the client and server are connected.
         * --
         * ----------------------------------------------------------------------------------------------------------------------
         */
        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (success) {
                Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();


            } else {
                Toast.makeText(getApplicationContext(), "The specified server doesn't exist", Toast.LENGTH_SHORT).show();
                stopSelf();
            }

        }

        /**
         * ------------------------------------------------------------------------------------------------------------------
         * -- FUNCTION:	doInBackground
         * --
         * -- DATE:		April 1,2018
         * --
         * --
         * -- DESIGNER:	Vafa Dehghan Saei
         * --
         * -- PROGRAMMER:	Vafa Dehghan Saei
         * --
         * -- INTERFACE:	protected Object doInBackground(Object... objects)
         * --					Object... objects: The parameters send to this thread by the server
         * --                          objects[0]: IP
         * --                          objects[1]: Port
         * --
         * --
         * -- RETURNS:	    N/A
         * --
         * -- NOTES:		This function will create a socket to connect the client and server together.
         * --
         * ----------------------------------------------------------------------------------------------------------------------
         */
        @Override
        protected Object doInBackground(Object... objects) {
            try {
                server = new Socket((String) objects[0], (int) objects[1]);
                Log.d(TAG, "connectToServer: " + server.getRemoteSocketAddress());
            } catch (Exception e) {
                e.printStackTrace();
                success = false;
            }
            return null;
        }
    }

}