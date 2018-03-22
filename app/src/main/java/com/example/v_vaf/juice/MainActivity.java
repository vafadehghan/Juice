package com.example.v_vaf.juice;

import android.Manifest;
import android.content.Intent;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    final String TAG = "MainActivity";
    EditText mNameEdit;
    EditText mIPAddressEdit;
    EditText mPortEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);


        Button mConnectBtn = findViewById(R.id.connectBtn);
        mNameEdit = findViewById(R.id.nameEdit);
        mIPAddressEdit = findViewById(R.id.ipEdit);
        mPortEdit = findViewById(R.id.portEdit);


        mConnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    private void attemptLogin() {

        // Reset errors.
        mNameEdit.setError(null);
        mIPAddressEdit.setError(null);
        mPortEdit.setError(null);


        // Store values at the time of the login attempt.
        String IP = mIPAddressEdit.getText().toString();
        String port = mPortEdit.getText().toString();
        String name = mNameEdit.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid IP
        if (TextUtils.isEmpty(IP)) {
            mIPAddressEdit.setError(getString(R.string.requiredText));
            focusView = mIPAddressEdit;
            cancel = true;
        }

        // Check for a valid port.
        if (TextUtils.isEmpty(port)) {
            mPortEdit.setError(getString(R.string.requiredText));
            focusView = mPortEdit;
            cancel = true;

        }
        //Check for valid name
        if (TextUtils.isEmpty(name)) {
//            mNameEdit.setError(getString(R.string.requiredText));
//            focusView = mNameEdit;
//            cancel = true;

        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
//            connectToServer(IP, Integer.parseInt(port));
            Log.d(TAG, "attemptLogin: " + IP);
            Log.d(TAG, "attemptLogin: " + port);
            Intent i = new Intent(MainActivity.this, LocationService.class);
            i.putExtra("Name", name);
            i.putExtra("IP", IP);
            i.putExtra("Port", port);
            startService(i);
            Snackbar mSuccessSnackbar = Snackbar.make(findViewById(R.id.constraintLayout), R.string.snackbarSuccess, Snackbar.LENGTH_SHORT);
            mSuccessSnackbar.show();

        }

    }
}

