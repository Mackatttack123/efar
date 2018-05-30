package com.southafricaproject.efar;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.IntentService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.content.Intent;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.preference.PreferenceManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class loginScreen extends AppCompatActivity {

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        //button to get back to patient screen
        Button backButton = (Button) findViewById(R.id.login_back_button);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchPatientScreen();
                finish();
            }
        });

        mAuth = FirebaseAuth.getInstance();

        //check database connection
        /*DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (!connected) {
                    new AlertDialog.Builder(loginScreen.this)
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

        Button submitButton = (Button) findViewById(R.id.login_submit_button);

        final EditText user_name = (EditText) findViewById(R.id.login_name_field);
        final EditText user_id = (EditText) findViewById(R.id.login_id_field);

        final TextView errorText = (TextView) findViewById(R.id.errorLoginText);

        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        String old_id = sharedPreferences.getString("old_id", "");
        String old_name = sharedPreferences.getString("old_name", "");

        if(old_id != "" && old_name != ""){
            user_name.setText(old_name);
            //user_id.setText(old_id);
        }

        // logic for the login submit button
        submitButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {

                        final String name = user_name.getText().toString();
                        final String id = user_id.getText().toString();
                        // make sure there is some data so app doesn't crash
                        if (name.equals("") || id.equals("")) {
                            Log.wtf("Login", "No data input. Cannot attempt login");
                            errorText.setText("ERROR: missing username or id...");
                        } else {
                            // check and validate the user
                            checkUser(name, id);
                        }
                    }
                }
        );

        final CheckBox showPasswordCheckBox = (CheckBox) findViewById(R.id.checkBoxShowPassword);
        showPasswordCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                    if(isChecked){
                        user_id.setTransformationMethod(null);
                    }else{
                        user_id.setTransformationMethod(new PasswordTransformationMethod());
                    }
                }
            }
        );

    }

    // Starts up launchEfarScreen screen
    private void launchEfarScreen() {

        Intent toEfarScreen = new Intent(this, EFARMainActivityTabbed.class);
        finish();
        startActivity(toEfarScreen);
    }

    // Starts up launchEfarScreen screen
    private void launchPatientScreen() {
        Intent toPatientScreen = new Intent(this, PatientMainActivity.class);
        startActivity(toPatientScreen);
        finish();
    }

    // checks to see if  a user exists in the database
    private void checkUser(String user_name, String user_id) {

        final TextView errorText = (TextView) findViewById(R.id.errorLoginText);

        final String name = user_name;
        final String id = user_id;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("users");

        userRef.addListenerForSingleValueEvent(new

        ValueEventListener() {
            @Override
            public void onDataChange (DataSnapshot snapshot){
                // Check if id number is in database

                if (snapshot.hasChild(id)) {
                    // check if name matches id in database
                    String check_name = snapshot.child(id + "/name").getValue().toString();

                    if (check_name.equals(name)) {

                        AlertDialog.Builder alert = new AlertDialog.Builder(loginScreen.this);
                        final EditText edittext = new EditText(loginScreen.this);
                        edittext.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        final ViewGroup.LayoutParams lparams = new ViewGroup.LayoutParams(50, 30);
                        edittext.setLayoutParams(lparams);
                        alert.setTitle("Two Step Verification:");
                        alert.setMessage("A notification will be sent to your phone with a 4 digit code. Please wait and then enter the code below to login.");
                        alert.setCancelable(false);
                        alert.setView(edittext);

                        Random r = new Random();
                        int random_pin = r.nextInt(9999 - 1010) + 1010;

                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference pin_verification_ref = database.getReference("users/" + id + "/verification_pin/pin");
                        DatabaseReference efar_id_verification_ref = database.getReference("users/" + id + "/verification_pin/efar_id");
                        final String verification_pin = Integer.toString(random_pin);
                        pin_verification_ref.setValue(verification_pin);
                        efar_id_verification_ref.setValue(id);

                        alert.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                String entered_verification_code = edittext.getText().toString();
                                if(entered_verification_code.equals(verification_pin)){

                                    new AlertDialog.Builder(loginScreen.this)
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .setTitle("Waiver of Liability")
                                            .setMessage("I, " + name + ", will be participating in the Emergency First Aid Responder (“EFAR”) programme. Being at least eighteen years of age, I do hereby agree to this waiver and release. \n\n" +
                                                    "I recognize that participation in EFAR will involve physical labour and may carry a risk of personal injury. I further recognize that there are natural and manmade hazards, environmental conditions, diseases, and other risks, which in combination with my actions can cause injury to me. I hereby agree to assume all risks which may be associated with or may result from my participation in the programme, including, but not limited to, transportation to and from sites, providing emergency medical care, (e.g. controlling bleeding, treating shock, treating sprains and fractures, opening airways, transporting patients, etc.) and other similar activities. \n\n" +
                                                    "I recognize that these programme activities will involve physical activity and may cause physical and emotional discomfort. \n\n" +
                                                    "I recognize that my involvement in the EFAR programme is as a volunteer and not as an employee of either EFAR or Western Cape Health Emergency Medical Services and I have no expectations to any form of remuneration or permanent appointment for the assistance rendered to the patient or EFAR Programme. Any and all participation is voluntary. Notification of an emergency situation by EMS dispatch does not obligate me to respond but I will communicate my inability well in advance. \n\n" +
                                                    "I recognize that if I am accepted for the programme, I will be covered by the provisions of the \"Reasonable Mans Test” and other applicable laws during the time that I am performing approved volunteer activities. I specifically recognize that in accordance with this Test, workers compensation and medical benefits from my personal provider (job etc.) shall be the exclusive remedy for any injury that I sustain in the course and scope of my approved participation in the program. In addition: \n\n" +
                                                    "I agree to release EFAR and Western Cape Health Emergency Services, its departments, officers, employees, agents, and all sponsors and/or officials and staff from any said entity or person, their representatives, agents, affiliates, directors, servants, volunteers, and employees from the cost of any medical care that I receive while participating in this programme or as a result of it.\n\n" +
                                                    "Furthermore I agree that according to Circular NO H148/2002, as a volunteer I am not eligible for benefits outlined in the Compensation for Occupational Injuries and Diseases Act. 130 Of 1993. However, I understand that should I have an injury during EFAR activities, the Department will allow me to visit the nearest Provincial Hospital for free medical assistance.  I also note that should I decide to seek medical assistance at a private medical practitioner, this consultation and treatment shall not be paid from state funds. \n\n" +
                                                    "I further agree to release EFAR and Western Cape Health Emergency Services, its departments, officers, employees, agents, (entity and persons as appropriate) and all sponsors and/or officials and staff of any said entity or person, their representatives, agents, affiliates, directors, servants, volunteers and employees from any and all liability, claims, demands, actions, and causes of actions whatsoever for any loss claim, damage, injury, illness, attorney's fees or harm of any kind or nature to me arising out of any and all activities associated with the aforementioned activities.\n" +
                                                    "\u2028I further agree to hold harmless, and hereby release the above mentioned entities and persons from all liability, negligence, or breach of warranty associated with injuries or damages from any claim by me, my family, estate, heirs, or assigns from or in any way connected with the aforementioned activities. \n\n" +
                                                    "\n" +
                                                    "I further agree not to communicate or disclose to any person, or to publish either during the currency of this Agreement or after the termination thereof, any private, confidential or privileged information and/or documentation obtained by him/her in the course of rendering assistance, without the prior written consent of the Department to such communication, disclosure or publication.\n\n"
                                                    + "BY HITTING ACCEPT I AFFIRM THAT I HAVE CAREFULLY READ AND UNDERSTAND THE CONTENTS OF THE FOREGOING LANGUAGE AND I SPECIFICALLY INTEND IT TO COVER ANY PARTICIPATION IN THE EMERGENCY FIRST AID RESPONDER (“EFAR”) TRAINING.\n\n")
                                            .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    // store password and username for auto login
                                                    SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                                    editor.putString("id", id);
                                                    editor.putString("name", name);
                                                    editor.putString("old_id", id);
                                                    editor.putString("old_name", name);
                                                    editor.putBoolean("logged_in", true);
                                                    editor.commit();

                                                    // update users info
                                                    String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                    DatabaseReference userRef = database.getReference("users");
                                                    GPSTracker gps = new GPSTracker(loginScreen.this);
                                                    double my_lat = gps.getLatitude(); // latitude
                                                    double my_long = gps.getLongitude(); // longitude
                                                    userRef.child(id + "/name").setValue(name);
                                                    userRef.child(id + "/token").setValue(refreshedToken);
                                                    userRef.child(id + "/latitude").setValue(my_lat);
                                                    userRef.child(id + "/longitude").setValue(my_long);
                                                    userRef.child(id + "/logged_in").setValue(true);

                                                    mAuth = FirebaseAuth.getInstance();
                                                    FirebaseUser currentUser = mAuth.getCurrentUser();
                                                    if(currentUser != null){
                                                        mAuth.getCurrentUser().delete();
                                                    }

                                                    //if all matches then go onto the efar screen
                                                    mAuth.signInAnonymously()
                                                            .addOnCompleteListener(loginScreen.this, new OnCompleteListener<AuthResult>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                                    if (task.isSuccessful()) {
                                                                        // Sign in success, update UI with the signed-in user's information
                                                                        Log.d("LOGIN", "signInAnonymously:success");
                                                                        FirebaseUser user = mAuth.getCurrentUser();
                                                                        errorText.setText("");
                                                                        finish();
                                                                        launchEfarScreen();
                                                                    } else {
                                                                        // If sign in fails, display a message to the user.
                                                                        Log.w("LOGIN", "signInAnonymously:failure", task.getException());
                                                                        Toast.makeText(loginScreen.this, "Authentication failed.",
                                                                                Toast.LENGTH_SHORT).show();
                                                                        errorText.setText("ERROR: Authentication failed.");
                                                                    }
                                                                }
                                                            });
                                                }

                                            })
                                            .setNegativeButton("Cancel", null)
                                            .show();

                                }else{
                                    new AlertDialog.Builder(loginScreen.this)
                                            .setTitle("Incorrect Verification Code!")
                                            .setMessage("Please try again...")
                                            .setPositiveButton("Okay", null)
                                            .show();
                                }

                            }
                        });

                        alert.setNeutralButton("Resend", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        });

                        alert.setNegativeButton("Cancel", null);
                        alert.show();

                    } else {
                        errorText.setText("ERROR: username or id is incorrect...");
                    }
                } else {
                    Log.wtf("Login", "FAILURE!");
                    errorText.setText("ERROR: username or id is incorrect...");
                }
            }

            @Override
            public void onCancelled (DatabaseError firebaseError){
            }
            }

        );
    }

    //disables the werid transition beteen activities
    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

}
