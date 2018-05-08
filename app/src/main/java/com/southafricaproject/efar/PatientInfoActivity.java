package com.southafricaproject.efar;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Log;
import android.content.SharedPreferences;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneNumberFormattingTextWatcher;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Calendar;


import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PatientInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_info);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        //check database connection
        /*DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (!connected) {
                    new AlertDialog.Builder(PatientInfoActivity.this)
                            .setTitle("Connection Error:")
                            .setMessage("Your device is currently unable connect to our services. " +
                                    "Please check your connection or try again later.")
                            .show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Listener was cancelled");
            }
        });*/

        GPSTracker gps = new GPSTracker(this);
        // check if GPS is avalible
        if(!gps.canGetLocation()){
            gps.showSettingsAlert();
        }

        //button to get back to patient screen
        Button backButton = (Button) findViewById(R.id.info_back_button);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchPatientScreen();
                finish();
            }
        });

        //TextView userInfoScreenUpdate = (TextView) findViewById(R.id.user_update_info_screen);
        //userInfoScreenUpdate.setText("An EFAR will be contacted once you fill in this information:");

        Button infoSumbitButton = (Button)findViewById(R.id.patient_info_sumbmit_button);
        final EditText patient_phone_number = (EditText) findViewById(R.id.patient_phone_number);
        patient_phone_number.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        final EditText patient_other_info = (EditText) findViewById(R.id.patient_other_info);

        infoSumbitButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {

                        final String phone_number = patient_phone_number.getText().toString();
                        final String other_info = patient_other_info.getText().toString();

                        try {
                            add_emergency(phone_number, other_info);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // go to main screen
                        finish();
                    }
                }
        );

    }


    private void add_emergency(String phone_number, String other_info) throws JSONException {
        // Create new emergency in the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference emergency_ref = database.getReference("emergencies");
        DatabaseReference emergency_key = emergency_ref.push();

        GPSTracker gps = new GPSTracker(this);

        Map<String, String> data = new HashMap<String, String>();
        data.put("phone_number",phone_number);
        data.put("other_info",other_info);
        data.put("latitude",Double.toString(gps.getLatitude()));
        data.put("longitude",Double.toString(gps.getLongitude()));
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String timestamp = simpleDateFormat.format(currentTime);
        data.put("creation_date",timestamp);
        data.put("state","0");
        String token = FirebaseInstanceId.getInstance().getToken();
        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        boolean efar_logged_in = sharedPreferences.getBoolean("logged_in", false);
        if(efar_logged_in){
            data.put("emergency_made_by_efar_token",token);
        }
        emergency_ref.child(emergency_key.getKey()).setValue(data);

        /*
        emergency_key.child("phone_number").setValue(phone_number);
        emergency_key.child("other_info").setValue(other_info);

        emergency_key.child("latitude").setValue(gps.getLatitude()); // latitude
        emergency_key.child("longitude").setValue(gps.getLongitude()); // longitude
        */

        // put emergency key into the users phone to store for later if needed
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String key = emergency_key.getKey().toString();
        editor.putString("emergency_key", key);
        editor.putString("creation_date", timestamp);
        editor.putString("user_emergency_state", "0");
        editor.commit();
        Log.wtf("Patient Info", "Creating New Emergency!");
    }

    @Override
    public void onBackPressed() {

        return;
    }

    //disables the werid transition beteen activities
    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    // Starts up launchEfarScreen screen
    private void launchPatientScreen() {
        Intent toPatientScreen = new Intent(this, PatientMainActivity.class);
        startActivity(toPatientScreen);
        finish();
    }
}



