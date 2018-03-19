package com.southafricaproject.efar;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Random;

public class PatientMainActivity extends AppCompatActivity {

    boolean calling_efar = false;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_main);

        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  }, 1 );
        }

        //check database connection
        /*DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (!connected) {
                    new AlertDialog.Builder(PatientMainActivity.this)
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


        // to auto login if possible
        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        String id = sharedPreferences.getString("id", "");
        String name = sharedPreferences.getString("name", "");
        String last_screen = sharedPreferences.getString("last_screen", "");

        final Button helpMeButton = (Button)findViewById(R.id.help_me_button);

        GPSTracker gps = new GPSTracker(this);
        // check if GPS is avalible
        if(!gps.canGetLocation()){
            gps.showSettingsAlert();
        }else{
            Double lat =  gps.getLatitude();
            Log.wtf("Patient Main", lat.toString());
        }

        FirebaseDatabase.getInstance().getReference().child("emergencies").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                if(dataSnapshot.child("state").exists()){

                    SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    String userEmergencyKey = sharedPreferences.getString("emergency_key", "");
                    editor.putString("user_emergency_state", dataSnapshot.child("state").getValue().toString());
                    editor.apply();

                    String e_key = dataSnapshot.getKey();
                    if(e_key.equals(userEmergencyKey)){
                        TextView userUpdate = (TextView) findViewById(R.id.user_update );
                        String e_state = dataSnapshot.child("state").getValue().toString();
                        if(e_state.equals("1")){
                            helpMeButton.setText("CANCEL EFAR");
                            helpMeButton.setBackgroundColor(0x55000000);
                            calling_efar = true;
                            blinkText();
                            userUpdate.setText("An EFAR has been contacted and is responding...");
                            userUpdate.setTextColor(Color.BLUE);
                        }else if(e_state.equals("2")){
                            userUpdate.setTextColor(Color.GREEN);
                            helpMeButton.setText("CALL FOR EFAR");
                            helpMeButton.setBackgroundColor(Color.RED);
                            userUpdate.setText("An EFAR has ended your emergency.");
                            // fade out text
                            userUpdate.animate().alpha(0.0f).setDuration(10000);
                            editor.putString("emergency_key", "");
                            editor.apply();
                            calling_efar = false;
                        }else if(e_state.equals("0")){
                            userUpdate.animate().alpha(1.0f).setDuration(1);
                            userUpdate.setText("EFARs in your area are being contacted...");
                            userUpdate.setTextColor(Color.RED);
                            helpMeButton.setText("CANCEL EFAR");
                            helpMeButton.setBackgroundColor(0x55000000);
                            calling_efar = true;
                            blinkText();
                        }
                    }
                }else{
                    SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove("emergency_key");
                    editor.putString("user_emergency_state", "100");
                    editor.apply();
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference emergency_ref = database.getReference("emergencies/" + dataSnapshot.getKey());
                    emergency_ref.removeValue();
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

        //I NEED HELP button logic
        helpMeButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {

                        if(!(calling_efar)){
                            //send and alert asking if they are sure they want to call
                            new AlertDialog.Builder(PatientMainActivity.this)
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
                                            launchPatientInfoScreen();
                                        }

                                    })
                                    .setNegativeButton("No", null)
                                    .show();
                        }else{
                            //send and alert asking if they are sure they want to cancel the efar
                            new AlertDialog.Builder(PatientMainActivity.this)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle("Cancel EFAR")
                                    .setMessage("Are you sure you want to cancel your EFAR?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // cancel EFAR here
                                            TextView userUpdate = (TextView) findViewById(R.id.user_update);
                                            userUpdate.setText("EFAR Cancelled!");
                                            // fade out text
                                            userUpdate.animate().alpha(0.0f).setDuration(3000);
                                            // when canceled, delete the emergancy and move to canceled
                                            SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                                            String emergency_key_to_delete = sharedPreferences.getString("emergency_key", "");
                                            FirebaseDatabase database = FirebaseDatabase.getInstance();
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
                                            // take away cancel button
                                            calling_efar = false;
                                            helpMeButton.setText("CALL FOR EFAR");
                                            helpMeButton.setBackgroundColor(Color.RED);
                                        }

                                    })
                                    .setNegativeButton("No", null)
                                    .show();
                        }
                    }
                }
        );

        Button toLoginButton = (Button)findViewById(R.id.to_login_button);
        Button toEmergencyListButton = (Button)findViewById(R.id.to_emergencies_button);

        Boolean logged_in = sharedPreferences.getBoolean("logged_in", false);

        if(logged_in){
            // start tracking efar
            startService(new Intent(this, MyService.class));

            //change to logout button
            toLoginButton.setText("logout");
            toLoginButton.setOnClickListener(
                    new Button.OnClickListener() {
                        public void onClick(View v) {
                            //to get rid of stored password and username
                            SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            // say that user has logged off
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference userRef = database.getReference("users");
                            userRef.child(sharedPreferences.getString("id", "") + "/logged_in").setValue(false);
                            userRef.child(sharedPreferences.getString("id", "") + "/token").setValue("null");
                            editor.putString("id", "");
                            editor.putString("name", "");
                            editor.putBoolean("logged_in", false);
                            stopService(new Intent(PatientMainActivity.this, MyService.class));
                            editor.commit();

                            //clear the phones token for the database
                            String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                            DatabaseReference token_ref = database.getReference("tokens/" + refreshedToken);
                            token_ref.removeValue();

                            finish();
                            startActivity(getIntent());
                        }
                    }
            );

            //add in button to take EFAR back to emergenecy screen
            toEmergencyListButton.setVisibility(View.VISIBLE);
            toEmergencyListButton.setOnClickListener(
                    new Button.OnClickListener() {
                        public void onClick(View v) {
                            // go to login screen
                            launchEfarScreen();
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
        String userEmergencyKey = sharedPreferences.getString("emergency_key", "");
        if(!userEmergencyKey.equals("")){
            String userEmergencyState = sharedPreferences.getString("user_emergency_state", "");
            Log.wtf("KEY ------------->", userEmergencyKey + " STATE -------->: " + userEmergencyState);
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference emergency_ping_ref = database.getReference("emergencies/" + userEmergencyKey + "/ping");
            final int min = 0;
            final int max = 10000;
            Random r = new Random();
            int random = r.nextInt((max - min) + 1) + min;
            emergency_ping_ref.setValue(random);
        }


    }

    // Starts up login screen
    private void launchLoginScreen() {
        Intent toLogin = new Intent(this, loginScreen.class);
        startActivity(toLogin);
        finish();
    }

    // Goes to patient info tab to send more to EFARs
    private void launchPatientInfoScreen() {
        Intent toPatientInfoScreen = new Intent(this, PatientInfoActivity.class);
        startActivity(toPatientInfoScreen);
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

    // Starts up launchEfarScreen screen
    private void launchEfarScreen() {
        Intent toEfarScreen = new Intent(this, EFARMainActivity.class);
        startActivity(toEfarScreen);
        finish();
    }

    // checks to see if  a user exists in the database for auto login
    private void checkUser(String user_name, String user_id) {

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
                           //if all matches then go onto the efar screen
                           finish();
                           launchEfarScreen();
                       } else {
                           Log.wtf("AutoLogin", "FAILURE!");
                       }
                   } else {
                       Log.wtf("AutoLogin", "FAILURE!");
                   }
               }

               @Override
               public void onCancelled (DatabaseError firebaseError){
               }
           }

        );
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

    //disables the werid transition beteen activities
    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

}
