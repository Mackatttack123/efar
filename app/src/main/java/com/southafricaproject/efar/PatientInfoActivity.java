package com.southafricaproject.efar;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Log;
import android.content.SharedPreferences;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneNumberFormattingTextWatcher;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PatientInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_info);

        GPSTracker gps = new GPSTracker(this);
        // check if GPS is avalible
        if(!gps.canGetLocation()){
            gps.showSettingsAlert();
        }

        TextView userInfoScreenUpdate = (TextView) findViewById(R.id.user_update_info_screen);
        userInfoScreenUpdate.setText("An EFAR will be contacted once you fill in this inforamtion:");

        Button infoSumbitButton = (Button)findViewById(R.id.patient_info_sumbmit_button);
        final EditText patient_phone_number = (EditText) findViewById(R.id.patient_phone_number);
        patient_phone_number.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        final EditText patient_other_info = (EditText) findViewById(R.id.patient_other_info);

        infoSumbitButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {

                        final String phone_number = patient_phone_number.getText().toString();
                        final String other_info = patient_other_info.getText().toString();

                        add_emergency(phone_number, other_info);

                        // go to main screen
                        finish();
                    }
                }
        );

    }


    private void add_emergency(String phone_number, String other_info) {
        // Create new emergency in the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference emergency_ref = database.getReference("emergencies");
        DatabaseReference emergency_key = emergency_ref.push();
        //TODO: send cordinates too
        emergency_key.child("phone_number").setValue(phone_number);
        emergency_key.child("other_info").setValue(other_info);

        GPSTracker gps = new GPSTracker(this);
        emergency_key.child("latitude").setValue(gps.getLatitude()); // latitude
        emergency_key.child("longitude").setValue(gps.getLongitude()); // longitude

        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String key = emergency_key.getKey().toString();
        editor.putString("emergency_key", key);
        editor.commit();
        Log.wtf("Patient Info", "Creating New Emergency!");
    }

}



