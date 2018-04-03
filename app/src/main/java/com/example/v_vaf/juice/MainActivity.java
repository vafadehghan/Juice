package com.example.v_vaf.juice;
import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**------------------------------------------------------------------------------------------------------------------
 -- SOURCE FILE:	MainActivity.java - The main entry point of the program.
 --
 -- PROGRAM:		Friend Locator
 --
 -- FUNCTIONS:		protected void onCreate(Bundle savedInstanceState)
 --					private void attemptLogin()
 --
 --
 --
 -- DATE:			April 1,2018
 --
 --
 -- DESIGNER:		Vafa Dehghan Saei
 --
 -- PROGRAMMER:		Vafa Dehghan Saei
 --
 -- NOTES:			This class is the entry point of Locator app the creates the main activity.
 --					From here the user can connect to the server.
 ----------------------------------------------------------------------------------------------------------------------*/
public class MainActivity extends AppCompatActivity {

    final String TAG = "MainActivity";
    EditText mNameEdit;
    EditText mIPAddressEdit;
    EditText mPortEdit;

/**------------------------------------------------------------------------------------------------------------------
 -- FUNCTION:	onCreate
 --
 -- DATE:		April 1,2018
 --
 --
 -- DESIGNER:	Vafa Dehghan Saei
 --
 -- PROGRAMMER:	Vafa Dehghan Saei
 --
 -- INTERFACE:	 protected void onCreate(Bundle savedInstanceState)
 --					Bundle savedInstanceState: a reference to a Bundle object, contains the state of the program on execution of method
 --
 --
 --
 -- RETURNS:	N/A
 --
 -- NOTES:		This function creates the main activity and is the application entry point.
 --
 ----------------------------------------------------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);


        getSupportActionBar().hide();


        Button mDisconnectBtn = findViewById(R.id.disconnectBtn);
        Button mConnectBtn = findViewById(R.id.connectBtn);
        mNameEdit = findViewById(R.id.nameEdit);
        mIPAddressEdit = findViewById(R.id.ipEdit);
        mPortEdit = findViewById(R.id.portEdit);


        mDisconnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Log.d(TAG, "onClick: DISCONNECT");
                Toast.makeText(MainActivity.this, "Service Destroyed", Toast.LENGTH_SHORT).show();
                stopService(new Intent(MainActivity.this, LocationService.class));
            }
        });
        mConnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

/**------------------------------------------------------------------------------------------------------------------
-- FUNCTION:	attemptLogin
--
-- DATE:		April 1,2018
--
--
-- DESIGNER:	Vafa Dehghan Saei
--
-- PROGRAMMER:	Vafa Dehghan Saei
--
-- INTERFACE:	private void attemptLogin()
--
--
--
-- RETURNS:     N/A
--
-- NOTES:		This function will check if the text fields are empty, and if they're not it will start the service.
--
----------------------------------------------------------------------------------------------------------------------*/
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
            mNameEdit.setError(getString(R.string.requiredText));
            focusView = mNameEdit;
            cancel = true;

        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
//            Log.d(TAG, "attemptLogin: " + IP);
//            Log.d(TAG, "attemptLogin: " + port);
            Intent i = new Intent(MainActivity.this, LocationService.class);
            i.putExtra("Name", name);
            i.putExtra("IP", IP);
            i.putExtra("Port", port);
            startService(i);

        }

    }
}

