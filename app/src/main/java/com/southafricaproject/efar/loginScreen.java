package com.southafricaproject.efar;

import android.app.AlertDialog;
import android.app.IntentService;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
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
