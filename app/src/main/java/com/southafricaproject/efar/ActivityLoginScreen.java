package com.southafricaproject.efar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.content.Intent;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
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

public class ActivityLoginScreen extends AppCompatActivity {

    FirebaseAuth mAuth;
    EditText user_name;
    EditText user_password;
    EditText user_id;
    CheckBox showPasswordCheckBox;
    Button submitButton;

    boolean continue_on = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        //check network connection
        //check if a forced app update is needed
        //check if an logged in on another phone
        CheckFunctions.runAllChecks(ActivityLoginScreen.this, this);

        //button to get back to patient screen
        Button backButton = (Button) findViewById(R.id.login_back_button);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchPatientScreen();
                finish();
            }
        });

        submitButton = (Button) findViewById(R.id.login_submit_button);

        user_name = (EditText) findViewById(R.id.login_name_field);
        user_id = (EditText) findViewById(R.id.login_id_field);
        user_password = (EditText) findViewById(R.id.login_password_field);
        showPasswordCheckBox = (CheckBox) findViewById(R.id.checkBoxShowPassword);

        user_password.setVisibility(View.INVISIBLE);
        showPasswordCheckBox.setVisibility(View.INVISIBLE);
        submitButton.setText("Continue");

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

                        closeKeyboard();
                        errorText.setText("Loading...");
                        errorText.setTextColor(Color.BLACK);
                        user_name.clearFocus();
                        user_id.clearFocus();
                        user_password.clearFocus();
                        if(continue_on){
                            final String name = user_name.getText().toString().toLowerCase();
                            final String id = user_id.getText().toString().toLowerCase();
                            // make sure there is some data so app doesn't crash
                            if (name.equals("") || id.equals("")) {
                                Log.wtf("Login", "No data input. Cannot attempt login");
                                errorText.setText("Missing Name or ID");
                                submitButton.setEnabled(true);
                            } else {
                                // check and validate the user
                                submitButton.setEnabled(false);
                                checkUser(name, id);
                            }

                        }else{
                            final String name = user_name.getText().toString().toLowerCase();
                            final String id = user_id.getText().toString().toLowerCase();
                            final String password = user_password.getText().toString().toLowerCase();
                            submitButton.setEnabled(false);
                            checkpassword(password, id, name);
                        }
                    }
                }
        );

        showPasswordCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                    if(isChecked){
                        user_password.setTransformationMethod(null);
                    }else{
                        user_password.setTransformationMethod(new PasswordTransformationMethod());
                    }
                }
            }
        );

    }

    // Starts up launchEfarScreen screen
    private void launchEfarScreen() {

        Intent toEfarScreen = new Intent(this, ActivityEFARMainTabbed.class);
        finish();
        startActivity(toEfarScreen);
    }

    // Starts up launchEfarScreen screen
    private void launchPatientScreen() {
        Intent toPatientScreen = new Intent(this, ActivityPatientMain.class);
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

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange (DataSnapshot snapshot){
               if (snapshot.hasChild(id)) {
                   // check if name matches id in database
                   String check_name = snapshot.child(id + "/name").getValue().toString().toLowerCase();

                   if (check_name.equals(name)) {
                        if(snapshot.hasChild(id + "/password")){
                            errorText.setText("Now please enter your password.");
                            errorText.setTextColor(Color.BLACK);
                            user_password.setVisibility(View.VISIBLE);
                            showPasswordCheckBox.setVisibility(View.VISIBLE);
                            continue_on = false;
                            submitButton.setText("LOGIN");
                            submitButton.setEnabled(true);
                        }else{
                            setPassword(id);
                        }
                   } else {
                   errorText.setText("Name or ID is incorrect");
                       errorText.setTextColor(Color.RED);
                       submitButton.setEnabled(true);
                   }
               } else {
                    Log.wtf("Login", "FAILURE!");
                    errorText.setText("Name or ID is incorrect");
                   errorText.setTextColor(Color.RED);
                   submitButton.setEnabled(true);
                }

            }

            @Override
            public void onCancelled (DatabaseError firebaseError){

            }
        });
    }

    private void checkpassword(final String password, final String id, final String name){
        final TextView errorText = (TextView) findViewById(R.id.errorLoginText);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("users");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange (DataSnapshot snapshot){
                String check_password = snapshot.child(id + "/password").getValue().toString();

                if (check_password.equals(password)) {
                    errorText.setText("");
                    new AlertDialog.Builder(ActivityLoginScreen.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Non-Disclosure Agreement")
                            .setMessage("I, " + name +  ", understand that the Provincial Government of the Western Cape: Emergency Medical Services provides services to patients that are private and confidential and that I am a crucial step in respecting the privacy rights of Emergency Medical Services’ patients. I understand that it is necessary, in the rendering of Emergency Care, that patients provide personal information and that such information may exist in a variety of forms such as electronic, oral, written or photographic and that all such information is strictly confidential and protected from improper use and disclosure by national law and internal policies.\n" +
                                    "\n" +
                                    "I agree that I will comply with all confidentiality and security policies and procedures put in place by the Emergency Medical Services during my experience as an Emergency First Aid Responder/guest/student/trainee. If at any time I knowingly or inadvertently breach the patient confidentiality or security policies and procedures, I agree to notify the Directorate of Emergency Medical Services immediately.\n" +
                                    "\n" +
                                    "I also understand that I may be exposed to other confidential or proprietary information on the Emergency Medical Services and I agree not to reveal this information to anyone and any time.\n" +
                                    "\n" +
                                    "In addition, I understand that a breach of patient confidentiality may result in immediate suspension or termination of the privilege to gain clinical experience or observe the activities of the Emergency Medical Services. Upon termination of this privilege for any reason, or at any time upon request, I agree to return any and all patient confidential information in my possession.\n" +
                                    "\n" +
                                    "I agree to abide by all policies or my privilege to participate in clinical activities or to otherwise observe Emergency Medical Services activities will be terminated.\n\n"
                                    + "BY HITTING ACCEPT I AFFIRM THAT I HAVE CAREFULLY READ AND UNDERSTAND THE CONTENTS OF THE FOREGOING LANGUAGE AND I SPECIFICALLY INTEND IT TO COVER ANY PARTICIPATION IN THE EMERGENCY FIRST AID RESPONDER (“EFAR”) TRAINING.\n\n")
                            .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new AlertDialog.Builder(ActivityLoginScreen.this)
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .setTitle("Patient Information Protection Agreement")
                                            .setMessage("I, " + name +  ", by tapping accept, pledge to adhere to the before accepted confidentiality agreement, noting specifically that while using the communication features on this app I will not send any unique patient identification information such as name, name of relations or any person's national identification number. Furthermore any information I pass through this app will be the minimum amount necessary to provide emergency care, only discussing locations and health information that is strictly necessary for adequate care. \n" +
                                                    "\n" +
                                                    "BY TAPPING ACCEPT I CERTIFY I HAVE READ THE ABOVE IN FULL AND AGREE TO THE AFOREMENTIONED STIPULATIONS.")
                                            .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    new AlertDialog.Builder(ActivityLoginScreen.this)
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

                                                                    mAuth = FirebaseAuth.getInstance();
                                                                    FirebaseUser currentUser = mAuth.getCurrentUser();
                                                                    if(currentUser != null){
                                                                        mAuth.getCurrentUser().delete();
                                                                    }

                                                                    //if all matches then go onto the efar screen
                                                                    mAuth.signInAnonymously()
                                                                            .addOnCompleteListener(ActivityLoginScreen.this, new OnCompleteListener<AuthResult>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        // Sign in success, update UI with the signed-in user's information
                                                                                        Log.d("LOGIN", "signInAnonymously:success");
                                                                                        FirebaseUser user = mAuth.getCurrentUser();
                                                                                        errorText.setText("");
                                                                                        // update users info
                                                                                        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                                                                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                                                        DatabaseReference userRef = database.getReference("users");
                                                                                        GPSTracker gps = new GPSTracker(ActivityLoginScreen.this);
                                                                                        double my_lat = gps.getLatitude(); // latitude
                                                                                        double my_long = gps.getLongitude(); // longitude
                                                                                        userRef.child(id + "/name").setValue(name);
                                                                                        userRef.child(id + "/token").setValue(refreshedToken);
                                                                                        userRef.child(id + "/latitude").setValue(my_lat);
                                                                                        userRef.child(id + "/longitude").setValue(my_long);
                                                                                        userRef.child(id + "/logged_in").setValue(true);
                                                                                        finish();
                                                                                        launchEfarScreen();
                                                                                    } else {
                                                                                        // If sign in fails, display a message to the user.
                                                                                        Log.w("LOGIN", "signInAnonymously:failure", task.getException());
                                                                                        Toast.makeText(ActivityLoginScreen.this, "Authentication failed.",
                                                                                                Toast.LENGTH_SHORT).show();
                                                                                        errorText.setText("Authentication failed.");
                                                                                    }
                                                                                }
                                                                            });
                                                                }

                                                            })
                                                            .setNegativeButton("Cancel", null)
                                                            .show();
                                                }
                                            })
                                            .setNegativeButton("Cancel", null)
                                            .show();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                            submitButton.setEnabled(true);
                } else {
                    submitButton.setEnabled(true);
                    errorText.setText("Password is incorrect");
                    errorText.setTextColor(Color.RED);
                }

            }

            @Override
            public void onCancelled (DatabaseError firebaseError){

            }
        });
    }

    private void setPassword(final String id){
        final TextView errorText = (TextView) findViewById(R.id.errorLoginText);

        LinearLayout layout = new LinearLayout(ActivityLoginScreen.this);
        layout.setOrientation(LinearLayout.VERTICAL);

        AlertDialog.Builder alert = new AlertDialog.Builder(ActivityLoginScreen.this);
        final EditText edittext = new EditText(ActivityLoginScreen.this);
        final EditText edittext2 = new EditText(ActivityLoginScreen.this);
        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        final String efar_id = sharedPreferences.getString("id", "");
        edittext.setHint("Password");
        edittext2.setHint("Re-enter Password");
        alert.setMessage("This is the first time you had logged on. Please choose a secure password of 6 or more characters.");
        alert.setTitle("Set Password");
        alert.setCancelable(false);
        layout.addView(edittext);
        layout.addView(edittext2);
        alert.setView(layout);

        alert.setPositiveButton("Set Password", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String set_password = edittext.getText().toString();
                String re_enter_set_password = edittext2.getText().toString();
                if(set_password.equals(re_enter_set_password) && set_password.length() >= 6){
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference userRef = database.getReference("users");
                    userRef.child(id + "/password").setValue(set_password);
                    errorText.setText("Password set. Please enter it and log in.");
                    errorText.setTextColor(Color.BLACK);
                    user_password.setVisibility(View.VISIBLE);
                    showPasswordCheckBox.setVisibility(View.VISIBLE);
                    submitButton.setEnabled(true);
                    continue_on = false;
                    submitButton.setText("LOGIN");
                }else if(set_password.length() < 6){
                    submitButton.setEnabled(true);
                    errorText.setText("Password must be at least 6 characters long.");
                    errorText.setTextColor(Color.RED);
                }else{
                    submitButton.setEnabled(true);
                    errorText.setText("Passwords didn't match. Please try again.");
                    errorText.setTextColor(Color.RED);
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        alert.show();
    }

    //disables the werid transition beteen activities
    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    private void closeKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }

}
