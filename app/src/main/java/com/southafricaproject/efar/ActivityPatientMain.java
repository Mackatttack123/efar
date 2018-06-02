package com.southafricaproject.efar;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Random;

public class ActivityPatientMain extends AppCompatActivity {

    boolean calling_efar = false;
    String responding_efar_id = null;
    String phone_token = "";

    FirebaseAuth mAuth;

    Button toLoginButton;
    Button toEmergencyListButton;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_main);

        //check location permissions for user and ask for permission if not granted
        if (ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {  Manifest.permission.ACCESS_COARSE_LOCATION  }, 1 );
        }
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        // check if GPS is available
        GPSTracker gps = new GPSTracker(this);
        if(!gps.canGetLocation()){
            gps.showSettingsAlert();
        }else{
            Double lat =  gps.getLatitude();
            Log.wtf("Patient Main", lat.toString());
        }

        //check network connection
        //check if a forced app update is needed
        //check if an logged in on another phone
        CheckFunctions.runAllAppChecks(ActivityPatientMain.this, this);

        //sign in anonymously if they are not an efar or bypass to efar home if they are
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        final SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        boolean efar_logged_in = sharedPreferences.getBoolean("logged_in", false);
        boolean screen_bypass = sharedPreferences.getBoolean("screen_bypass", true);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        if(efar_logged_in && screen_bypass && currentUser != null){
            editor.putBoolean("screen_bypass", false);
            editor.apply();
            launchEfarScreen();
        }else{
            editor.putBoolean("screen_bypass", true);
            editor.apply();
        }

        final Button helpMeButton = (Button)findViewById(R.id.help_me_button);

        //TODO: show the patient approximatly how far away the efar is via the progress bar
        final ProgressBar distance_progress = (ProgressBar) findViewById(R.id.patient_progress_bar);
        distance_progress.setVisibility(View.INVISIBLE);
        distance_progress.setProgress(0);

        FirebaseDatabase.getInstance().getReference().child("emergencies").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                if(dataSnapshot.child("state").exists()){

                    final SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                    final SharedPreferences.Editor editor = sharedPreferences.edit();
                    String userEmergencyKey = sharedPreferences.getString("emergency_key", "");
                    editor.putString("user_emergency_state", dataSnapshot.child("state").getValue().toString());
                    editor.apply();

                    String e_key = dataSnapshot.getKey();
                    if(e_key.equals(userEmergencyKey)){
                        final TextView userUpdate = (TextView) findViewById(R.id.user_update );
                        String e_state = dataSnapshot.child("state").getValue().toString();
                        if(e_state.equals("1")){
                            helpMeButton.setText("CANCEL EFAR");
                            helpMeButton.setBackgroundColor(0x55000000);
                            calling_efar = true;
                            blinkText();
                            userUpdate.setText("An EFAR has been contacted and is responding...");
                            userUpdate.setTextColor(Color.BLUE);
                            distance_progress.setVisibility(View.VISIBLE);
                            ObjectAnimator.ofInt(distance_progress, "progress", 60).start();
                        }else if(e_state.equals("0")){
                            userUpdate.animate().alpha(1.0f).setDuration(1);
                            userUpdate.setText("EFARs in your area are being contacted...");
                            userUpdate.setTextColor(Color.RED);
                            helpMeButton.setText("CANCEL EFAR");
                            helpMeButton.setBackgroundColor(0x55000000);
                            calling_efar = true;
                            blinkText();
                            distance_progress.setVisibility(View.VISIBLE);
                            ObjectAnimator.ofInt(distance_progress, "progress", 30).start();
                        }else if(e_state.equals("-2") || e_state.equals("-3")) {
                            userUpdate.setTextColor(Color.RED);
                            helpMeButton.setText("Call for Help");
                            helpMeButton.setBackgroundColor(Color.RED);
                            userUpdate.setText("There are no EFARs in your area!");
                            //clear the emergency key and state
                            editor.remove("emergency_key");
                            editor.putString("creation_date", "");
                            editor.putString("user_emergency_state", "100");
                            editor.apply();
                            responding_efar_id = null;
                            //change state to -4 and then clean up with backend
                            FirebaseDatabase.getInstance().getReference().child("emergencies/" + dataSnapshot.getKey() + "/state").setValue(-4);
                            // fade out text
                            userUpdate.animate().alpha(0.0f).setDuration(10000);
                            calling_efar = false;
                            distance_progress.setVisibility(View.INVISIBLE);
                            distance_progress.setProgress(0);
                        }
                    }

                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference().child("completed").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                String userEmergencyKey = sharedPreferences.getString("emergency_key", "");
                String creation_date = sharedPreferences.getString("creation_date", "");
                if(dataSnapshot.exists() && dataSnapshot.child("creation_date").exists()){
                    if(dataSnapshot.getKey().toString().equals(userEmergencyKey) || dataSnapshot.child("creation_date").getValue().toString().equals(creation_date)){
                        TextView userUpdate = (TextView) findViewById(R.id.user_update );
                        userUpdate.setTextColor(Color.GREEN);
                        helpMeButton.setText("Call for Help");
                        helpMeButton.setBackgroundColor(Color.RED);
                        userUpdate.setText("Emergency has been ended.");
                        // fade out text
                        userUpdate.animate().alpha(0.0f).setDuration(10000);
                        editor.putString("emergency_key", "");
                        editor.putString("creation_date", "");
                        editor.apply();
                        calling_efar = false;
                        distance_progress.setVisibility(View.INVISIBLE);
                        distance_progress.setProgress(0);
                    }
                }

                TextView userUpdate = (TextView) findViewById(R.id.user_update);
                if(dataSnapshot.child("emergency_made_by_efar_token").exists() && !userUpdate.getText().toString().equals("")){
                    if(dataSnapshot.child("emergency_made_by_efar_token").getValue().toString().equals(FirebaseInstanceId.getInstance().getToken())){
                        userUpdate.setTextColor(Color.GREEN);
                        helpMeButton.setText("Call for Help");
                        helpMeButton.setBackgroundColor(Color.RED);
                        userUpdate.setText("Emergency has been ended.");
                        // fade out text
                        userUpdate.animate().alpha(0.0f).setDuration(10000);
                        editor.putString("emergency_key", "");
                        editor.putString("creation_date", "");
                        editor.apply();
                        calling_efar = false;
                        distance_progress.setVisibility(View.INVISIBLE);
                        distance_progress.setProgress(0);
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference().child("canceled").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //check if an efar sent the message and if they are the only one in their area
                if(dataSnapshot.child("emergency_made_by_efar_token").exists()){
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if(dataSnapshot.child("emergency_made_by_efar_token").getValue().toString().equals(phone_token)){
                        phone_token = "";
                        TextView userUpdate = (TextView) findViewById(R.id.user_update );
                        userUpdate.setTextColor(Color.RED);
                        helpMeButton.setText("Call for Help");
                        helpMeButton.setBackgroundColor(Color.RED);
                        userUpdate.setText("No other EFARs available.");
                        // fade out text
                        userUpdate.animate().alpha(0.0f).setDuration(10000);
                        editor.putString("emergency_key", "");
                        editor.putString("creation_date", "");
                        editor.apply();
                        calling_efar = false;
                        distance_progress.setVisibility(View.INVISIBLE);
                        distance_progress.setProgress(0);
                    }
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //I NEED HELP button logic
        helpMeButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {

                        if(!(calling_efar)){
                            //send and alert asking if they are sure they want to call
                            new AlertDialog.Builder(ActivityPatientMain.this)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle("Call EFAR")
                                    .setMessage("Are you sure you want to call an EFAR?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            TextView userUpdate = (TextView) findViewById(R.id.user_update);
                                            userUpdate.animate().alpha(1.0f).setDuration(1);
                                            userUpdate.setText("EFARs in your area are being contacted...");
                                            userUpdate.setTextColor(Color.RED);
                                            helpMeButton.setText("CANCEL EFAR");
                                            helpMeButton.setBackgroundColor(0x55000000);
                                            calling_efar = true;
                                            blinkText();
                                            distance_progress.setVisibility(View.VISIBLE);
                                            ObjectAnimator.ofInt(distance_progress, "progress", 30).start();
                                            phone_token = FirebaseInstanceId.getInstance().getToken();
                                            launchPatientInfoScreen();
                                        }

                                    })
                                    .setNegativeButton("No", null)
                                    .show();
                        }else{
                            //send and alert asking if they are sure they want to cancel the efar
                            new AlertDialog.Builder(ActivityPatientMain.this)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle("Cancel EFAR")
                                    .setMessage("Are you sure you want to cancel your EFAR?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            final TextView userUpdate = (TextView) findViewById(R.id.user_update);
                                            helpMeButton.setEnabled(false);
                                            userUpdate.setTextColor(Color.argb(255, 0, 0, 0));
                                            userUpdate.setText("Canceling . . .");
                                            if(mAuth.getCurrentUser() != null){
                                                // Sign in success, update UI with the signed-in user's information
                                                Log.d("LOGIN", "signInAnonymously:success");
                                                // cancel EFAR here
                                                userUpdate.setText("EFAR Cancelled!");
                                                // fade out text
                                                userUpdate.animate().alpha(0.0f).setDuration(3000);
                                                // when canceled, delete the emergancy and move to canceled
                                                SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                                                final String emergency_key_to_delete = sharedPreferences.getString("emergency_key", "");
                                                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                DatabaseReference emergency_ref = database.getReference("emergencies/" + emergency_key_to_delete );
                                                DatabaseReference emergency_state_ref = database.getReference("emergencies/" + emergency_key_to_delete + "/state");
                                                emergency_state_ref.setValue("-1");
                                                moveFirebaseRecord(emergency_ref, database.getReference("canceled/" + emergency_key_to_delete));
                                                emergency_ref.removeValue();
                                                //clear the emergency key and state
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.remove("emergency_key");
                                                editor.putString("user_emergency_state", "100");
                                                editor.apply();
                                                responding_efar_id = null;
                                                // take away cancel button
                                                calling_efar = false;
                                                helpMeButton.setText("Call for Help");
                                                helpMeButton.setBackgroundColor(Color.RED);
                                                distance_progress.setVisibility(View.INVISIBLE);
                                                distance_progress.setProgress(0);
                                            }else{
                                                mAuth.signInAnonymously()
                                                        .addOnCompleteListener(ActivityPatientMain.this, new OnCompleteListener<AuthResult>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                                if (task.isSuccessful()) {
                                                                    // Sign in success, update UI with the signed-in user's information
                                                                    Log.d("LOGIN", "signInAnonymously:success");
                                                                    // cancel EFAR here
                                                                    userUpdate.setText("EFAR Cancelled!");
                                                                    // fade out text
                                                                    userUpdate.animate().alpha(0.0f).setDuration(3000);
                                                                    // when canceled, delete the emergancy and move to canceled
                                                                    SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                                                                    final String emergency_key_to_delete = sharedPreferences.getString("emergency_key", "");
                                                                    final FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                                    DatabaseReference emergency_ref = database.getReference("emergencies/" + emergency_key_to_delete );
                                                                    DatabaseReference emergency_state_ref = database.getReference("emergencies/" + emergency_key_to_delete + "/state");
                                                                    emergency_state_ref.setValue("-1");
                                                                    moveFirebaseRecord(emergency_ref, database.getReference("canceled/" + emergency_key_to_delete));
                                                                    emergency_ref.removeValue();
                                                                    //clear the emergency key and state
                                                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                                                    editor.remove("emergency_key");
                                                                    editor.putString("user_emergency_state", "100");
                                                                    editor.apply();
                                                                    responding_efar_id = null;
                                                                    // take away cancel button
                                                                    calling_efar = false;
                                                                    helpMeButton.setText("Call for Help");
                                                                    helpMeButton.setBackgroundColor(Color.RED);
                                                                    distance_progress.setVisibility(View.INVISIBLE);
                                                                    distance_progress.setProgress(0);

                                                                    if (mAuth.getCurrentUser() != null) {
                                                                        mAuth.getCurrentUser().delete();
                                                                    }
                                                                } else {
                                                                    // If sign in fails, display a message to the user.
                                                                    helpMeButton.setEnabled(true);
                                                                    userUpdate.setTextColor(Color.argb(255, 200, 0, 0));
                                                                    userUpdate.setText("Failed to cancel! ");
                                                                    Log.w("LOGIN", "signInAnonymously:failure", task.getException());
                                                                }
                                                            }
                                                        });
                                            }
                                        }

                                    })
                                    .setNegativeButton("No", null)
                                    .show();
                        }
                    }
                }
        );

        toLoginButton = (Button)findViewById(R.id.to_login_button);
        toEmergencyListButton = (Button)findViewById(R.id.to_emergencies_button);

        Boolean logged_in = sharedPreferences.getBoolean("logged_in", false);

        if(logged_in){
            // start tracking efar
            startService(new Intent(this, GPSTrackingService.class));

            //change to logout button
            toLoginButton.setText("logout");
            toLoginButton.setOnClickListener(
                    new Button.OnClickListener() {
                        public void onClick(View v) {
                            LogoutProcedure.logout(ActivityPatientMain.this, ActivityPatientMain.this);
                        }
                    }
            );

            //add in button to take EFAR back to emergency screen
            toEmergencyListButton.setVisibility(View.VISIBLE);
            toEmergencyListButton.setOnClickListener(
                    new Button.OnClickListener() {
                        public void onClick(View v) {
                            // go to login screen
                            launchEFARScreen();
                        }
                    }
            );
        }else{
            toLoginButton.setOnClickListener(
                    new Button.OnClickListener() {
                        public void onClick(View v) {
                            // go to login screen
                            launchLoginScreen();
                        }
                    }
            );
            toEmergencyListButton.setVisibility(View.GONE);
        }

        // check for a pre-existing emergency on this phone by pinging the database and activating the listener above
        final Handler handler = new Handler();
        final int delay = 1000; //milliseconds

        handler.postDelayed(new Runnable(){
            public void run(){
                final String userEmergencyKey = sharedPreferences.getString("emergency_key", "");
                if(!userEmergencyKey.equals("")){
                    String userEmergencyState = sharedPreferences.getString("user_emergency_state", "");
                    Log.wtf("KEY ------------->", userEmergencyKey + " STATE -------->: " + userEmergencyState);
                    final FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference emergencies_ref = database.getReference("emergencies/");
                    emergencies_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.hasChild(userEmergencyKey)) {
                                DatabaseReference emergency_ping_ref = database.getReference("emergencies/" + userEmergencyKey + "/ping");
                                final int min = 0;
                                final int max = 10000;
                                Random r = new Random();
                                int random = r.nextInt((max - min) + 1) + min;
                                emergency_ping_ref.setValue(random);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }else{
                    handler.postDelayed(this, delay);
                }

            }
        }, delay);

    }

    // Starts up login screen
    private void launchLoginScreen() {
        Intent toLogin = new Intent(this, ActivityLoginScreen.class);
        startActivity(toLogin);
        finish();
    }

    // Starts up login screen
    private void launchEFARScreen() {
        Intent toEFARScreen = new Intent(this, ActivityEFARMainTabbed.class);
        startActivity(toEFARScreen);
        finish();
    }

    // Goes to patient info tab to send more to EFARs
    private void launchPatientInfoScreen() {
        Intent toPatientInfoScreen = new Intent(this, ActivityPatientInfo.class);
        startActivity(toPatientInfoScreen);
    }

    // Starts up launchEfarScreen screen
    private void launchEfarScreen() {
        runOnUiThread(new Runnable(){
            public void run() {
                // This runs on the UI thread
                Log.wtf("TEST", "asdfghjklsdfghjkzxcvbnm");
                Log.wtf("TEST", "asdfghjklsdfghjkzxcvbnm");
                Log.wtf("TEST", "asdfghjklsdfghjkzxcvbnm");
                Log.wtf("TEST", "asdfghjklsdfghjkzxcvbnm");
                Log.wtf("TEST", "asdfghjklsdfghjkzxcvbnm");
                Log.wtf("TEST", "asdfghjklsdfghjkzxcvbnm");
                Log.wtf("TEST", "asdfghjklsdfghjkzxcvbnm");
                Intent toEfarScreen = new Intent(ActivityPatientMain.this, ActivityEFARMainTabbed.class);
                Log.wtf("Intent", "asdfghjklsdfghjkzxcvbnm");
                Log.wtf("Intent", "asdfghjklsdfghjkzxcvbnm");
                Log.wtf("Intent", "asdfghjklsdfghjkzxcvbnm");
                Log.wtf("Intent", "asdfghjklsdfghjkzxcvbnm");
                Log.wtf("Intent", "asdfghjklsdfghjkzxcvbnm");
                Log.wtf("Intent", "asdfghjklsdfghjkzxcvbnm");
                Log.wtf("Intent", "asdfghjklsdfghjkzxcvbnm");
                toEfarScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(toEfarScreen);
                Log.wtf("startActivity", "asdfghjklsdfghjkzxcvbnm");
                Log.wtf("startActivity", "asdfghjklsdfghjkzxcvbnm");
                Log.wtf("startActivity", "asdfghjklsdfghjkzxcvbnm");
                Log.wtf("startActivity", "asdfghjklsdfghjkzxcvbnm");
                Log.wtf("startActivity", "asdfghjklsdfghjkzxcvbnm");
                Log.wtf("startActivity", "asdfghjklsdfghjkzxcvbnm");
                Log.wtf("startActivity", "asdfghjklsdfghjkzxcvbnm");
                killActivity();
                Log.wtf("killActivity", "asdfghjklsdfghjkzxcvbnm");
                Log.wtf("killActivity", "asdfghjklsdfghjkzxcvbnm");
                Log.wtf("killActivity", "asdfghjklsdfghjkzxcvbnm");
                Log.wtf("killActivity", "asdfghjklsdfghjkzxcvbnm");
                Log.wtf("killActivity", "asdfghjklsdfghjkzxcvbnm");
                Log.wtf("killActivity", "asdfghjklsdfghjkzxcvbnm");
                Log.wtf("killActivity", "asdfghjklsdfghjkzxcvbnm");
            }
        });

    }

    public void moveFirebaseRecord(DatabaseReference fromPath, final DatabaseReference toPath) {
        fromPath.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                toPath.setValue(dataSnapshot.getValue(), new DatabaseReference.CompletionListener()
                {
                    @Override
                    public void onComplete(DatabaseError firebaseError, DatabaseReference firebase)
                    {
                        if (firebaseError != null)
                        {
                            System.out.println("Copy failed");
                        }
                        else
                        {
                            System.out.println("Success");
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError firebaseError)
            {
                System.out.println("Copy failed");
            }
        });
    }

    // blinking text animation
    public void blinkText(){
        TextView userUpdate = (TextView) findViewById(R.id.user_update );
        Animation anim = new AlphaAnimation(1.0f, 0.3f);
        anim.setDuration(800); //manage the time of the blink with this parameter
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        userUpdate.startAnimation(anim);
    }

    //disables the werid transition beteen activities
    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    private void killActivity() {
        finish();
    }

}
