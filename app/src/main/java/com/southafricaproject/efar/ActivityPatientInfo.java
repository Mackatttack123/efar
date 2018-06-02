package com.southafricaproject.efar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.content.Context;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;
import android.content.SharedPreferences;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.widget.TextView;

import org.json.JSONException;

import java.util.HashMap;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Calendar;


import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ActivityPatientInfo extends AppCompatActivity {

    TextView loadingTextView;
    Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_info);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        final FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();

        //check network connection
        //check if a forced app update is needed
        //check if an logged in on another phone
        CheckFunctions.runAllAppChecks(ActivityPatientInfo.this, this);

        GPSTracker gps = new GPSTracker(this);
        // check if GPS is avalible
        if (!gps.canGetLocation()) {
            gps.showSettingsAlert();
        }

        loadingTextView = (TextView) findViewById(R.id.LoadingTextView);

        //button to get back to patient screen
        backButton = (Button) findViewById(R.id.info_back_button);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchPatientScreen();
                finish();
            }
        });

        final Button infoSumbitButton = (Button) findViewById(R.id.patient_info_sumbmit_button);
        final EditText patient_phone_number = (EditText) findViewById(R.id.patient_phone_number);
        patient_phone_number.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        final EditText patient_other_info = (EditText) findViewById(R.id.patient_other_info);

        infoSumbitButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {

                        loadingTextView.setText("Loading . . .");
                        loadingTextView.setTextColor(Color.argb(255, 0, 0, 0));
                        final String phone_number = patient_phone_number.getText().toString();
                        final String other_info = patient_other_info.getText().toString();
                        infoSumbitButton.setEnabled(false);
                        backButton.setEnabled(false);
                        if(mAuth.getCurrentUser() != null){
                            // Sign in success, update UI with the signed-in user's information
                            try {
                                add_emergency(phone_number, other_info);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            // go to main screen
                            finish();
                        }else{
                            mAuth.signInAnonymously()
                                    .addOnCompleteListener(ActivityPatientInfo.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                // Sign in success, update UI with the signed-in user's information
                                                Log.d("LOGIN", "signInAnonymously:success");
                                                try {
                                                    add_emergency(phone_number, other_info);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                                if (mAuth.getCurrentUser() != null) {
                                                    mAuth.getCurrentUser().delete();
                                                }
                                                // go to main screen
                                                finish();
                                            } else {
                                                // If sign in fails, display a message to the user.
                                                infoSumbitButton.setEnabled(true);
                                                Log.w("LOGIN", "signInAnonymously:failure", task.getException());
                                                loadingTextView.setTextColor(Color.argb(255, 200, 0, 0));
                                                loadingTextView.setText("Failed to call EFARs!");
                                                backButton.setEnabled(true);
                                            }
                                        }

                                    });
                        }
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
        if(!phone_number.equals("")){
            data.put("phone_number",phone_number);
        }else{
            data.put("phone_number","N/A");
        }
        if(!other_info.equals("")){
            data.put("other_info",other_info);
        }else{
            data.put("other_info","N/A");
        }

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
        Intent toPatientScreen = new Intent(this, ActivityPatientMain.class);
        startActivity(toPatientScreen);
        finish();
    }
}



