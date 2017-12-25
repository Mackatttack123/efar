package com.southafricaproject.efar;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
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

public class PatientMainActivity extends AppCompatActivity {

    boolean calling_efar = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_main);

        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  }, 1 );
        }


        // to auto login if possible
        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        String id = sharedPreferences.getString("id", "");
        String name = sharedPreferences.getString("name", "");
        if(id != ""){
            checkUser(name, id);
        }

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

                SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                String userEmergencyKey = sharedPreferences.getString("emergency_key", "");

                String e_key = dataSnapshot.getKey();
                if(e_key.equals(userEmergencyKey)){
                    TextView userUpdate = (TextView) findViewById(R.id.user_update );
                    String e_state = dataSnapshot.child("state").getValue().toString();
                    if(e_state.equals("1")){
                        userUpdate.setText("An EFAR has been contacted and is responding...");
                        userUpdate.setTextColor(Color.BLUE);
                    }else if(e_state.equals("2")){
                        userUpdate.setTextColor(Color.GREEN);
                        helpMeButton.setText("CALL FOR EFAR");
                        userUpdate.setText("An EFAR has ended your emergency.");
                        // fade out text
                        userUpdate.animate().alpha(0.0f).setDuration(10000);
                        calling_efar = false;
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
                                            // take away cancel button
                                            calling_efar = false;
                                            helpMeButton.setText("CALL FOR EFAR");
                                        }

                                    })
                                    .setNegativeButton("No", null)
                                    .show();
                        }
                    }
                }
        );

        Button toLoginButton = (Button)findViewById(R.id.to_login_button);

        toLoginButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        // go to login screen
                        launchLoginScreen();
                    }
                }
        );

    }

    // Starts up login screen
    private void launchLoginScreen() {

        Intent toLogin = new Intent(this, loginScreen.class);
        startActivity(toLogin);
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

}
